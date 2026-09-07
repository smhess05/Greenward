package com.green.ward;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

/**
 * Bone/Gunpowder collections, Combat XP, and the four combat Proofs (Design Program
 * Update 2). Kill detection reuses the exact same structural gate The Marked already
 * established ({@code DamageSource.isDirect()} + {@code getEntity() instanceof
 * ServerPlayer}) rather than a config check — a trap/fall/lava kill or a projectile kill
 * can't satisfy either condition, so there's no automatable path into this collection,
 * matching § 2.2's general rule.
 *
 * <p>Collection counts (Bone from skeleton-family kills, Gunpowder from creeper kills)
 * are a flat "+1 per kill" approximation rather than an exact loot-table roll — vanilla's
 * own bone/gunpowder drop counts are a small random range per kill, and re-deriving the
 * exact roll would need intercepting the mob's own death loot table separately; a flat
 * per-kill count is close enough for a lifetime tally, same tradeoff already accepted for
 * Farming's "passes" counting.
 */
public final class GreenwardCombatHandler {
    private GreenwardCombatHandler() {}

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register(GreenwardCombatHandler::onAfterDeath);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(GreenwardCombatHandler::onAfterDamage);
    }

    private static void onAfterDeath(LivingEntity entity, DamageSource source) {
        if (!GreenwardConfig.ENABLE_COLLECTIONS_PROOFS || !(entity instanceof Monster)) {
            return;
        }
        if (!source.isDirect() || !(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        // No flat "+1 Combat XP for any hostile kill" here — that would double-grant on
        // top of addCollection's own automatic XP grant below for skeleton/creeper kills
        // specifically, and every other skill already only grants XP from its own
        // collection-relevant actions (Mining XP never fires for andesite, only
        // cobblestone/deepslate/diamond) — Combat stays consistent with that rather than
        // being the one skill with a blanket fallback.

        // Bare-Knuckle: any hostile kill with an empty chestplate slot.
        if (player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            PlayerProgress.incrementLifetimeCounter(player, GreenwardProof.BARE_KNUCKLE, 1, 50);
        }

        // Graveyard Shift: melee kill between midnight and dawn (ticks 18000-23999).
        long timeOfDay = level.getOverworldClockTime() % 24000L;
        if (timeOfDay >= 18000L) {
            PlayerProgress.incrementLifetimeCounter(player, GreenwardProof.GRAVEYARD_SHIFT, 1, 100);
        }

        // Marked Hunter: the kill target was one of The Marked.
        if (MarkedMobHandler.isMarked(entity)) {
            int dayNumber = (int) (level.getOverworldClockTime() / 24000L);
            PlayerProgress.incrementDayCounter(player, GreenwardProof.MARKED_HUNTER, dayNumber, 1, 10);
        }

        if (entity instanceof AbstractSkeleton) {
            PlayerProgress.addCollection(player, GreenwardCollection.BONE, 1, true);
        } else if (entity instanceof Creeper) {
            PlayerProgress.addCollection(player, GreenwardCollection.GUNPOWDER, 1, true);
            PlayerProgress.incrementStreak(player, GreenwardProof.HELD_NERVE, 1, 25);
            int dayNumber = (int) (level.getOverworldClockTime() / 24000L);
            PlayerProgress.incrementDayCounter(player, GreenwardProof.POWDER_TRAIL, dayNumber, 1, 200);
        }
    }

    /** Held Nerve's streak breaks the moment a creeper actually damages the player —
     *  checked independently of the kill hook above, since the damage that breaks the
     *  streak and the kill that would complete it are two different events entirely. */
    private static void onAfterDamage(LivingEntity entity, DamageSource source, float baseDamage, float actualDamage, boolean blocked) {
        if (!GreenwardConfig.ENABLE_COLLECTIONS_PROOFS || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (source.getEntity() instanceof Creeper) {
            PlayerProgress.resetStreak(player, GreenwardProof.HELD_NERVE);
        }
    }
}
