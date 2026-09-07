package com.green.ward;

import net.minecraft.util.StringRepresentable;

import java.util.Map;

/** The four pillar skills (Design Program Update 2 § 2.3), levels 0–50, XP from player
 *  actions only. Per-level stat grants are exactly the spec's table. */
public enum GreenwardSkill implements StringRepresentable {
    FARMING("farming", "Farming", Map.of(GreenwardStat.FARMING_FORTUNE, 2.0, GreenwardStat.HEALTH, 2.0)),
    MINING("mining", "Mining", Map.of(GreenwardStat.MINING_SPEED, 1.0, GreenwardStat.MINING_FORTUNE, 1.0, GreenwardStat.DEFENSE, 1.0)),
    COMBAT("combat", "Combat", Map.of(GreenwardStat.STRENGTH, 0.5, GreenwardStat.CRIT_CHANCE, 0.25)),
    FISHING("fishing", "Fishing", Map.of(GreenwardStat.SEA_CREATURE_CHANCE, 0.3, GreenwardStat.HEALTH, 2.0));

    public static final int MAX_LEVEL = 50;

    private final String id;
    private final String displayName;
    private final Map<GreenwardStat, Double> perLevel;

    GreenwardSkill(String id, String displayName, Map<GreenwardStat, Double> perLevel) {
        this.id = id;
        this.displayName = displayName;
        this.perLevel = perLevel;
    }

    public String displayName() {
        return displayName;
    }

    public Map<GreenwardStat, Double> perLevel() {
        return perLevel;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
