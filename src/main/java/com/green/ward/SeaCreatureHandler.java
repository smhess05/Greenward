package com.green.ward;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manual-fishing-only sea creatures. Hooks {@code LootTableEvents.MODIFY_DROPS} (Fabric's
 * sanctioned interception point for a loot roll's result, fabric-loot-api-v3) rather than a
 * mixin. The critical safety property — the Auto-Fisher must NEVER trigger this — falls
 * out of the data itself rather than which code path called it: a real player catch is the
 * only caller that puts {@code LootContextParams.THIS_ENTITY} (the FishingHook) in the loot
 * context (confirmed against FishingHook.retrieve()'s decompiled source); the Auto-Fisher
 * builds its own LootParams with only ORIGIN + TOOL, never THIS_ENTITY. Filtering on both
 * "is this the top-level fishing table" and "is THIS_ENTITY actually a FishingHook" means
 * this can never fire for the machine even if the event turns out to fire more broadly than
 * expected.
 */
public final class SeaCreatureHandler {
    private SeaCreatureHandler() {}

    private static final AttachmentType<String> MARKER = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "sea_creature"), Codec.STRING);

    private enum Creature {
        DRENCHED_HUSK("Drenched Husk", EntityTypes.DROWNED, 45, 1, 1.0, 0,
                drop(Items.PRISMARINE_SHARD, 1, 3)),
        REEF_STALKER("Reef Stalker", EntityTypes.GUARDIAN, 25, 1, 1.0, 1,
                drop(Items.PRISMARINE_CRYSTALS, 2, 4)),
        BLOATED_SWARM("Bloated Swarm", EntityTypes.PUFFERFISH, 15, 4, 1.0, 0,
                drop(Items.PUFFERFISH, 1, 2)),
        TIDECALLER("Tidecaller", EntityTypes.DROWNED, 10, 1, 1.5, 2,
                drop(Items.NAUTILUS_SHELL, 1, 1), drop(Items.PRISMARINE_CRYSTALS, 2, 5)),
        ABYSSAL_WARDEN("Abyssal Warden", EntityTypes.ELDER_GUARDIAN, 5, 1, 2.0, 3,
                drop(Items.HEART_OF_THE_SEA, 1, 1), drop(Items.NAUTILUS_SHELL, 1, 1), drop(Items.PRISMARINE_CRYSTALS, 4, 8));

        final String displayName;
        final EntityType<? extends Mob> type;
        final int weight;
        final int spawnCount;
        final double healthMultiplier;
        final int minRodTier;
        final DropRange[] drops;

        Creature(String displayName, EntityType<? extends Mob> type, int weight, int spawnCount,
                 double healthMultiplier, int minRodTier, DropRange... drops) {
            this.displayName = displayName;
            this.type = type;
            this.weight = weight;
            this.spawnCount = spawnCount;
            this.healthMultiplier = healthMultiplier;
            this.minRodTier = minRodTier;
            this.drops = drops;
        }
    }

    private record DropRange(Item item, int min, int max) {}

    private static DropRange drop(Item item, int min, int max) {
        return new DropRange(item, min, max);
    }

    public static void initialize() {
        // Registered unconditionally — this is also where Design Program Update 2's Cod
        // collection/Fishing XP tracking lives (same genuine-player-catch detection sea
        // creatures already need), so it can't be gated behind ENABLE_SEA_CREATURES alone
        // without also silently disabling fishing progression when that flag is off.
        LootTableEvents.MODIFY_DROPS.register(SeaCreatureHandler::onLootDrops);
        ServerLivingEntityEvents.AFTER_DEATH.register(SeaCreatureHandler::onDeath);
        ServerTickEvents.END_SERVER_TICK.register(SeaCreatureHandler::onServerTick);
    }

    // --- Spawn hook ---

    private static void onLootDrops(Holder<LootTable> table, LootContext context, List<ItemStack> drops) {
        if (!table.is(BuiltInLootTables.FISHING)) {
            return;
        }
        if (!context.hasParameter(LootContextParams.THIS_ENTITY)) {
            return;
        }
        if (!(context.getParameter(LootContextParams.THIS_ENTITY) instanceof FishingHook hook)) {
            return;
        }
        Player player = hook.getPlayerOwner();
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) || !(hook.level() instanceof ServerLevel level)) {
            return;
        }

        int rodTier = rodTier(player);

        if (GreenwardConfig.ENABLE_SEA_CREATURES) {
            float chance = computeChance(player, rodTier);
            if (level.getRandom().nextFloat() < chance) {
                Creature creature = rollCreature(level.getRandom(), rodTier);
                drops.clear();
                spawnCreature(level, hook.position(), creature, player);
                announceCatchLogEntry(player, creature);
                if (hasTwinBite(serverPlayer) && level.getRandom().nextFloat() < 0.25F) {
                    spawnCreature(level, hook.position(), rollCreature(level.getRandom(), rodTier), player);
                }
                if (GreenwardConfig.ENABLE_COLLECTIONS_PROOFS) {
                    trackSeaCreatureProofs(serverPlayer, level);
                }
                return;
            }
        }

        rollBonusFishingMaterials(level, rodTier, drops);

        if (GreenwardConfig.ENABLE_COLLECTIONS_PROOFS) {
            trackFishingCollection(serverPlayer, level, drops);
        }
    }

    /** Bootstraps the Fishing pillar's own material chain (user-requested — "base fishing
     *  rod needs to be able to fish up prismarine shards and pieces to craft the better
     *  rods"): even a plain vanilla rod (tier 0) can now fish up Prismarine Shard, so
     *  getting into Angler's Line at all never requires a Guardian kill first. Each rod
     *  tier then unlocks a chance at the NEXT tier's own ingredient once you're already
     *  holding it — Angler's Line (1) adds Prismarine Crystals, Deep-Sea Rod (2) adds
     *  Nautilus Shell — so climbing the rod ladder is self-sufficient end to end, ordered
     *  to match crafting progression. Purely additive, never replaces the normal catch. */
    private static void rollBonusFishingMaterials(ServerLevel level, int rodTier, List<ItemStack> drops) {
        RandomSource random = level.getRandom();

        float shardChance = switch (rodTier) {
            case 0 -> 0.10F;
            case 1 -> 0.20F;
            case 2 -> 0.15F;
            default -> 0.20F;
        };
        if (random.nextFloat() < shardChance) {
            drops.add(new ItemStack(Items.PRISMARINE_SHARD, rodTier >= 1 ? 1 + random.nextInt(2) : 1));
        }

        if (rodTier >= 1) {
            float crystalChance = rodTier == 1 ? 0.08F : rodTier == 2 ? 0.15F : 0.20F;
            if (random.nextFloat() < crystalChance) {
                drops.add(new ItemStack(Items.PRISMARINE_CRYSTALS, 1 + random.nextInt(2)));
            }
        }

        if (rodTier >= 2) {
            float shellChance = rodTier == 2 ? 0.06F : 0.10F;
            if (random.nextFloat() < shellChance) {
                drops.add(new ItemStack(Items.NAUTILUS_SHELL, 1));
            }
        }
    }

    /** Patient (streak, broken by leaving the water — see {@link #onServerTick}) and
     *  Storm Catch (instant, checked at the moment of the catch). */
    private static void trackSeaCreatureProofs(net.minecraft.server.level.ServerPlayer player, ServerLevel level) {
        PlayerProgress.incrementStreak(player, GreenwardProof.PATIENT, 1, 3);
        if (level.isThundering()) {
            PlayerProgress.completeProof(player, GreenwardProof.STORM_CATCH);
        }
    }

    /** Cod collection + Fishing XP + Full Net (day-scoped), only for a catch that actually
     *  contains cod — a junk/treasure/other-fish catch is still a genuine player action but
     *  doesn't feed the Cod collection specifically. */
    private static void trackFishingCollection(net.minecraft.server.level.ServerPlayer player, ServerLevel level, List<ItemStack> drops) {
        int codCount = 0;
        for (ItemStack drop : drops) {
            if (drop.is(Items.COD)) {
                codCount += drop.getCount();
            }
        }
        if (codCount <= 0) {
            return;
        }
        PlayerProgress.addCollection(player, GreenwardCollection.COD, codCount, true);
        int dayNumber = (int) (level.getOverworldClockTime() / 24000L);
        PlayerProgress.incrementDayCounter(player, GreenwardProof.FULL_NET, dayNumber, codCount, 10);
    }

    /** Twin Bite (Design Program Update 9 § 9.2) — granted by Tidal Wear, Leviathan's
     *  Wear, or the Heartwood Tide branch's "Twin Current" node; a flat 25% chance to
     *  land a second creature isn't specified by the source text as an exact number, so
     *  this is a documented, invented rate. */
    private static boolean hasTwinBite(net.minecraft.server.level.ServerPlayer player) {
        if (GreenwardConfig.ENABLE_GEAR_SETS && (ModArmor.hasFullTidalSet(player) || ModArmor.hasFullLeviathanSet(player))) {
            return true;
        }
        return GreenwardConfig.ENABLE_HEARTWOOD && HeartwoodData.get(((net.minecraft.server.level.ServerLevel) player.level()).getServer())
                .isNodeUnlocked(HeartwoodBranch.TIDE, HeartwoodBranch.TIDE_TWIN_CURRENT);
    }

    private static int rodTier(Player player) {
        Item held = player.getMainHandItem().getItem();
        if (held == ModTools.LEVIATHAN_ROD) return 3;
        if (held == ModTools.DEEP_SEA_ROD) return 2;
        if (held == ModTools.ANGLERS_LINE) return 1;
        return 0;
    }

    /** Design Program Update 9 § 9.2 — "Retire the hardcoded rod-tier lookup table; the
     *  existing chance values become stat grants on the rods, computed through the stat
     *  layer." The rods' own {@code GreenwardComponents.STATS} components now carry the
     *  same 5/15/30% this switch used to hardcode; {@link FishingSetContributor} carries
     *  the full-set and rod-tier-3-synergy bonuses. This method just reads the total. */
    private static float computeChance(Player player, int rodTier) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return 0.0F;
        }
        double chance = PlayerStatManager.get(serverPlayer, GreenwardStat.SEA_CREATURE_CHANCE) / 100.0;
        return (float) Math.min(chance, 1.0);
    }

    private static Creature rollCreature(RandomSource random, int rodTier) {
        List<Creature> eligible = new ArrayList<>();
        for (Creature creature : Creature.values()) {
            if (creature.minRodTier <= rodTier) {
                eligible.add(creature);
            }
        }
        int totalWeight = eligible.stream().mapToInt(c -> c.weight).sum();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Creature creature : eligible) {
            cumulative += creature.weight;
            if (roll < cumulative) {
                return creature;
            }
        }
        return eligible.get(eligible.size() - 1);
    }

    /** The Catch Log (Design Program Update 9 § 9.2), in reduced scope: grades and
     *  announces a notable catch live rather than maintaining the full persisted,
     *  queryable list + milestone rewards the source text describes — a genuinely
     *  separate piece of infrastructure (its own storage, a Field Guide page) that this
     *  session's time budget didn't reach. Grade is derived from the creature's own
     *  {@code minRodTier} (0-3), matching Modest/Fine/Superb/Legendary directly onto the
     *  existing rod-tier gating rather than inventing a second rarity axis. */
    private static final String[] CATCH_LOG_GRADES = {"Modest", "Fine", "Superb", "Legendary"};

    private static void announceCatchLogEntry(Player player, Creature creature) {
        if (!GreenwardConfig.ENABLE_FARMING_FISHING_DEPTH) {
            return;
        }
        String grade = CATCH_LOG_GRADES[Math.max(0, Math.min(3, creature.minRodTier))];
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Catch Log: " + grade + " catch — " + creature.name() + "!"));
    }

    private static void spawnCreature(ServerLevel level, Vec3 pos, Creature creature, Player player) {
        for (int i = 0; i < creature.spawnCount; i++) {
            Mob mob = creature.type.create(level, EntitySpawnReason.TRIGGERED);
            if (mob == null) {
                continue;
            }
            mob.setPos(pos.x, pos.y, pos.z);
            mob.setYRot(level.getRandom().nextFloat() * 360.0F);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(pos)), EntitySpawnReason.TRIGGERED, null);

            if (creature.healthMultiplier != 1.0) {
                AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
                if (maxHealth != null) {
                    maxHealth.setBaseValue(maxHealth.getBaseValue() * creature.healthMultiplier);
                }
                mob.setHealth(mob.getMaxHealth());
            }
            if (creature == Creature.TIDECALLER) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            }

            mob.setCustomName(Component.literal(creature.displayName));
            mob.setCustomNameVisible(true);
            mob.setPersistenceRequired();
            mob.setAttached(MARKER, creature.name());
            level.addFreshEntity(mob);
        }

        player.sendSystemMessage(Component.literal("A " + creature.displayName + " surfaces!"));
    }

    // --- Death drops ---

    private static void onDeath(LivingEntity entity, DamageSource source) {
        String marker = entity.getAttached(MARKER);
        if (marker == null) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        Creature creature;
        try {
            creature = Creature.valueOf(marker);
        } catch (IllegalArgumentException e) {
            return;
        }

        RandomSource random = level.getRandom();
        for (DropRange dropRange : creature.drops) {
            int count = dropRange.min() + random.nextInt(dropRange.max() - dropRange.min() + 1);
            entity.spawnAtLocation(level, new ItemStack(dropRange.item(), count));
        }
    }

    // --- Abyssal Warden fatigue suppression + Patient streak upkeep ---

    private static void onServerTick(MinecraftServer server) {
        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Patient (Update 2): leaving the water breaks the streak. Checked per tick
            // rather than at an exact "just left water" event — no such event exists —
            // which is fine here since water entry/exit is a continuous state, unlike a
            // one-shot action.
            if (GreenwardConfig.ENABLE_COLLECTIONS_PROOFS && !player.isInWater()) {
                PlayerProgress.resetStreak(player, GreenwardProof.PATIENT);
            }

            if (!player.hasEffect(MobEffects.MINING_FATIGUE)) {
                continue;
            }
            ServerLevel level = player.level();
            boolean nearWarden = !level.getEntitiesOfClass(net.minecraft.world.entity.monster.ElderGuardian.class,
                    player.getBoundingBox().inflate(32.0),
                    e -> creatureMarker(e) == Creature.ABYSSAL_WARDEN).isEmpty();
            if (nearWarden) {
                player.removeEffect(MobEffects.MINING_FATIGUE);
            }
        }
    }

    private static Creature creatureMarker(LivingEntity entity) {
        String marker = entity.getAttached(MARKER);
        if (marker == null) {
            return null;
        }
        try {
            return Creature.valueOf(marker);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
