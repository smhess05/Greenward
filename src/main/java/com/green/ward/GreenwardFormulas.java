package com.green.ward;

import net.minecraft.util.RandomSource;

/** The three formulas Update 1 § 1.3 specifies, verbatim. */
public final class GreenwardFormulas {
    private GreenwardFormulas() {}

    /**
     * Every 100 points of fortune guarantees one additional multiple of the base
     * amount; the remainder becomes the percentage chance of one more on top of that.
     * 100 fortune -> always 2x. 130 fortune -> always 2x, 30% chance of 3x. 150 fortune
     * -> always 2x, 50% chance of 3x.
     */
    public static int applyFortune(int baseAmount, double fortune, RandomSource random) {
        if (fortune <= 0) {
            return baseAmount;
        }
        int guaranteedMultiplier = 1 + (int) Math.floor(fortune / 100.0);
        double bonusChance = (fortune % 100.0) / 100.0;
        int result = baseAmount * guaranteedMultiplier;
        if (bonusChance > 0.0 && random.nextDouble() < bonusChance) {
            result += baseAmount;
        }
        return result;
    }

    /** {@code (baseWeaponDamage + 5) * (1 + Strength/100) * (crit ? 1 + CritDamage/100 : 1)}. */
    public static double playerDamage(double baseWeaponDamage, double strength, double critDamage, boolean crit) {
        double damage = (baseWeaponDamage + 5.0) * (1.0 + strength / 100.0);
        if (crit) {
            damage *= 1.0 + critDamage / 100.0;
        }
        return damage;
    }

    /** {@code damageTaken = incoming * (1 - Defense/(Defense + 100))}. */
    public static float applyDefense(float incoming, double defense) {
        if (defense <= 0) {
            return incoming;
        }
        double reduction = defense / (defense + 100.0);
        return (float) (incoming * (1.0 - reduction));
    }
}
