package com.green.ward;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The Marked — melee-kill-only elite variants of the five classic hostiles. Structurally
 * safe against auto-farm exploitation the same way SeaCreatureHandler is safe against the
 * Auto-Fisher: the trigger checks {@code DamageSource.isDirect()} (true only when the
 * entity that caused the damage IS the direct entity — i.e. a real melee swing, never an
 * arrow/trident/fall/lava/explosion) and {@code DamageSource.getEntity() instanceof
 * ServerPlayer}. A typical mob-farm kill method (fall damage, water pushing, lava, campfire)
 * can never satisfy both checks at once, so there is no automatable path to Marked loot.
 */
public final class MarkedMobHandler {
    private MarkedMobHandler() {}

    private static final AttachmentType<String> MARKER = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "marked"), Codec.STRING);

    /** Used by {@link GreenwardCombatHandler} for the Marked Hunter proof. */
    public static boolean isMarked(net.minecraft.world.entity.LivingEntity entity) {
        return entity.getAttached(MARKER) != null;
    }

    private enum Marked {
        REVENANT("Revenant", EntityTypes.ZOMBIE, 1.5),
        DEADEYE("Deadeye", EntityTypes.SKELETON, 1.5),
        WIDOWFANG("Widowfang", EntityTypes.SPIDER, 1.5),
        ASHBORN("Ashborn", EntityTypes.CREEPER, 1.5),
        WRAITH("Wraith", EntityTypes.ENDERMAN, 1.5);

        final String title;
        final EntityType<? extends Mob> type;
        final double healthMultiplier;

        Marked(String title, EntityType<? extends Mob> type, double healthMultiplier) {
            this.title = title;
            this.type = type;
            this.healthMultiplier = healthMultiplier;
        }
    }

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            return;
        }

        ServerLivingEntityEvents.AFTER_DEATH.register(MarkedMobHandler::onDeath);
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        String marker = entity.getAttached(MARKER);
        if (marker != null) {
            dropMarkedLoot(level, entity, marker);
            return;
        }

        Marked target = baseTypeFor(entity);
        if (target == null) {
            return;
        }
        if (!source.isDirect() || !(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        float chance = chanceFor(player);
        if (level.getRandom().nextFloat() >= chance) {
            return;
        }

        Marked marked = Marked.values()[level.getRandom().nextInt(Marked.values().length)];
        spawnMarked(level, entity, marked, player);
    }

    private static Marked baseTypeFor(LivingEntity entity) {
        for (Marked marked : Marked.values()) {
            if (entity.getType() == marked.type) {
                return marked;
            }
        }
        return null;
    }

    /** Base 3%, +Marrowguard 1%, +Ashwrought 2% (cumulative with Marrowguard's, since a
     *  player wearing Ashwrought no longer wears Marrowguard, but keeping the tiers additive
     *  in code reads clearly and never actually double-applies since only one full set can be
     *  worn at once). Reaper's Aegis jumps straight to the spec's literal 10% cap. */
    private static float chanceFor(ServerPlayer player) {
        if (!GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            return 0.0F;
        }
        if (ModArmor.hasFullReapersAegisSet(player)) {
            return 0.10F;
        }
        float chance = 0.03F;
        if (ModArmor.hasFullAshwroughtSet(player)) {
            chance += 0.02F;
        } else if (ModArmor.hasFullMarrowguardSet(player)) {
            chance += 0.01F;
        }
        return Math.min(chance, 0.10F);
    }

    private static void spawnMarked(ServerLevel level, LivingEntity deadMob, Marked marked, ServerPlayer player) {
        Mob mob = marked.type.create(level, EntitySpawnReason.TRIGGERED);
        if (mob == null) {
            return;
        }
        mob.setPos(deadMob.getX(), deadMob.getY(), deadMob.getZ());
        mob.setYRot(level.getRandom().nextFloat() * 360.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(deadMob.blockPosition()), EntitySpawnReason.TRIGGERED, null);

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * marked.healthMultiplier);
        }
        mob.setHealth(mob.getMaxHealth());

        mob.setCustomName(Component.literal(marked.title));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.setAttached(MARKER, marked.name());
        level.addFreshEntity(mob);

        player.sendSystemMessage(Component.literal("A " + marked.title + " rises to answer you!"));
    }

    // --- Death drops: every Marked drops Bone (Primary chain currency); Ashborn and Wraith
    // also drop their vanilla-identity bonus, feeding the Secondary chain and Rare ingredient
    // directly — the fast, manual route to Reaper's Core, mirroring Tidecaller/Abyssal
    // Warden's role for the Abyssal Pearl. ---

    private static void dropMarkedLoot(ServerLevel level, LivingEntity entity, String marker) {
        Marked marked;
        try {
            marked = Marked.valueOf(marker);
        } catch (IllegalArgumentException e) {
            return;
        }

        RandomSource random = level.getRandom();
        drop(level, entity, Items.BONE, 2 + random.nextInt(3));

        switch (marked) {
            case ASHBORN -> drop(level, entity, Items.GUNPOWDER, 2 + random.nextInt(3));
            case WRAITH -> {
                drop(level, entity, Items.ENDER_PEARL, 1);
                // Wraith's Eye talisman — Design Program Update 6 § 6.3, "Wraith drop, 4%".
                if (GreenwardConfig.ENABLE_HEARTWOOD && random.nextFloat() < 0.04F) {
                    drop(level, entity, ModItems.WRAITHS_EYE, 1);
                }
            }
            default -> { }
        }
    }

    private static void drop(ServerLevel level, LivingEntity entity, Item item, int count) {
        entity.spawnAtLocation(level, new ItemStack(item, count));
    }
}
