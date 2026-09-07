package com.green.ward;

import java.util.EnumMap;
import java.util.Map;

/**
 * A flat additive bundle of stat contributions. {@link PlayerStatManager} sums the base
 * value plus every registered {@link StatContributor}'s profile into a player's live
 * total; individual contributors (equipped items today, Skills/Heartwood/Tempering in
 * later Updates) each return one of these for their own slice.
 */
public final class StatProfile {
    private final Map<GreenwardStat, Double> values = new EnumMap<>(GreenwardStat.class);

    public StatProfile add(GreenwardStat stat, double amount) {
        if (amount != 0.0) {
            values.merge(stat, amount, Double::sum);
        }
        return this;
    }

    public StatProfile addAll(StatProfile other) {
        other.values.forEach(this::add);
        return this;
    }

    public double get(GreenwardStat stat) {
        return values.getOrDefault(stat, 0.0);
    }

    public Map<GreenwardStat, Double> asMap() {
        return Map.copyOf(values);
    }

    public static StatProfile empty() {
        return new StatProfile();
    }
}
