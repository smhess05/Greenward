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
        if (!GreenwardConfig.ENABLE_SEA_CREATURES) {
            return;
        }

        LootTableEvents.MODIFY_DROPS.register(SeaCreatureHandler::onLootDrops);
        ServerLivingEntityEvents.AFTER_DEATH.register(SeaCreatureHandler::onDeath);
        ServerTickEvents.END_SERVER_TICK.register(SeaCreatureHandler::suppressAbyssalWardenFatigue);
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
        if (!(player instanceof net.minecraft.server.level.ServerPlayer) || !(hook.level() instanceof ServerLevel level)) {
            return;
        }

        int rodTier = rodTier(player);
        float chance = computeChance(player, rodTier);
        if (level.getRandom().nextFloat() >= chance) {
            return;
        }

        Creature creature = rollCreature(level.getRandom(), rodTier);
        drops.clear();
        spawnCreature(level, hook.position(), creature, player);
    }

    private static int rodTier(Player player) {
        Item held = player.getMainHandItem().getItem();
        if (held == ModTools.LEVIATHAN_ROD) return 3;
        if (held == ModTools.DEEP_SEA_ROD) return 2;
        if (held == ModTools.ANGLERS_LINE) return 1;
        return 0;
    }

    private static float computeChance(Player player, int rodTier) {
        float chance = 0.05F;
        chance += switch (rodTier) {
            case 1 -> 0.05F;
            case 2 -> 0.15F;
            case 3 -> 0.30F;
            default -> 0.0F;
        };
        boolean fullSet = GreenwardConfig.ENABLE_GEAR_SETS && ModArmor.hasFullAnglerSet(player);
        if (fullSet) {
            chance += 0.15F;
        }
        if (rodTier == 3 && fullSet) {
            chance += 0.35F; // late-game synergy: top rod + full set is meant to feel nearly guaranteed
        }
        return Math.min(chance, 1.0F);
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

    // --- Abyssal Warden: suppress its vanilla mining-fatigue curse on nearby players ---

    private static void suppressAbyssalWardenFatigue(MinecraftServer server) {
        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
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
