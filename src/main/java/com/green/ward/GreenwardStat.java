package com.green.ward;

import net.minecraft.util.StringRepresentable;

/**
 * The complete Greenward stat list (Design Program Revision 3, Update 1 § 1.2) —
 * deliberately not open to extension elsewhere in the codebase. Cut from Revision 2:
 * Magic Find (Threat's job, Update 7), Ferocity, Pristine, Breaking Power, Magical
 * Power, Intelligence/Mana.
 *
 * LAVA_CREATURE_CHANCE is a post-Design-Program addition (the user's own lava-fishing
 * request) — kept as its own stat rather than folded into SEA_CREATURE_CHANCE since the
 * two rod lines (Scorched Leviathan Rod vs. Angler's Line/Deep-Sea/Leviathan) are meant
 * to run in parallel, not share a single power budget.
 */
public enum GreenwardStat implements StringRepresentable {
    HEALTH("health", "Health", "❤", 100.0, false),
    DEFENSE("defense", "Defense", "❈", 0.0, false),
    STRENGTH("strength", "Strength", "❁", 0.0, false),
    CRIT_CHANCE("crit_chance", "Crit Chance", "☣", 5.0, true),
    CRIT_DAMAGE("crit_damage", "Crit Damage", "☠", 50.0, true),
    SPEED("speed", "Speed", "✦", 100.0, false),
    MINING_SPEED("mining_speed", "Mining Speed", "⫕", 0.0, false),
    MINING_FORTUNE("mining_fortune", "Mining Fortune", "☘", 0.0, false),
    FARMING_FORTUNE("farming_fortune", "Farming Fortune", "☘", 0.0, false),
    SEA_CREATURE_CHANCE("sea_creature_chance", "Sea Creature Chance", "α", 5.0, true),
    FISHING_SPEED("fishing_speed", "Fishing Speed", "☂", 0.0, false),
    LAVA_CREATURE_CHANCE("lava_creature_chance", "Lava Creature Chance", "α", 5.0, true);

    private final String id;
    private final String displayName;
    private final String symbol;
    private final double baseValue;
    private final boolean percentage;

    GreenwardStat(String id, String displayName, String symbol, double baseValue, boolean percentage) {
        this.id = id;
        this.displayName = displayName;
        this.symbol = symbol;
        this.baseValue = baseValue;
        this.percentage = percentage;
    }

    public String displayName() {
        return displayName;
    }

    public String symbol() {
        return symbol;
    }

    public double baseValue() {
        return baseValue;
    }

    /** Whether this stat's value is conventionally read/displayed as a percentage. */
    public boolean isPercentage() {
        return percentage;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
