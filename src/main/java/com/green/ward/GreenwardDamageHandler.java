package com.green.ward;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Applies Update 1 § 1.3's damage formulas without a mixin. There is no Fabric event
 * that lets a listener change a damage amount directly ({@code ALLOW_DAMAGE} is a
 * boolean gate only) — so this cancels the original {@code hurtServer} call and
 * re-invokes it once with the adjusted amount, exactly the mechanism vanilla's own
 * Totem-of-Undying-style saves use internally. A {@link ThreadLocal} guards against the
 * re-invocation re-triggering itself (server damage handling is single-threaded per
 * tick, so this is safe).
 *
 * Both adjustments are layered onto vanilla's own computed {@code amount} (which
 * already includes enchantment/weapon bonuses) rather than replacing the calculation
 * outright — deliberately conservative per § 1.8 ("do not remove vanilla crit... only
 * when the held item carries a Greenward stat component"). Order: attacker's
 * Strength/Crit boost first, then defender's Defense reduction, matching how the two
 * would compose in a real hit (buff the blow, then mitigate it).
 *
 * Known approximation, documented rather than silently accepted: Greenward Crit is an
 * independent roll against Crit Chance, layered on top of whatever vanilla already
 * decided (including vanilla's own fall-based crit, which is already baked into
 * {@code amount} by the time this fires and can't be cleanly detected or stripped
 * without a mixin into {@code Player.attack}). The two can in principle compound on a
 * falling player wielding a Greenward weapon. Biases toward more damage, not less, and
 * only in a narrow, uncommon setup — accepted for Update 1; revisit with a mixin if
 * precise crit replacement is ever required.
 */
public final class GreenwardDamageHandler {
    private GreenwardDamageHandler() {}

    private static final ThreadLocal<DamageSource> REPROCESSING = new ThreadLocal<>();

    public static void initialize() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(GreenwardDamageHandler::onAllowDamage);
    }

    private static boolean onAllowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (source == REPROCESSING.get()) {
            return true;
        }

        float working = amount;

        if (source.getEntity() instanceof ServerPlayer attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.has(GreenwardComponents.STATS)) {
                StatProfile stats = PlayerStatManager.getProfile(attacker);
                double strength = stats.get(GreenwardStat.STRENGTH);
                double critChance = stats.get(GreenwardStat.CRIT_CHANCE) / 100.0;
                double critDamage = stats.get(GreenwardStat.CRIT_DAMAGE);
                boolean crit = entity.level().getRandom().nextDouble() < critChance;
                working = (float) (working * (1.0 + strength / 100.0) * (crit ? 1.0 + critDamage / 100.0 : 1.0));
            }
        }

        if (entity instanceof ServerPlayer defender) {
            double defense = PlayerStatManager.get(defender, GreenwardStat.DEFENSE);
            working = GreenwardFormulas.applyDefense(working, defense);
        }

        if (working == amount || !(entity.level() instanceof ServerLevel level)) {
            return true;
        }

        REPROCESSING.set(source);
        try {
            entity.hurtServer(level, source, working);
        } finally {
            REPROCESSING.remove();
        }
        return false;
    }
}
