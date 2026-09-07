package com.green.ward;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Central stat aggregator. Recomputed on {@link SetBonusHandler}'s existing 20-tick
 * cadence (never per-tick, per Update 1 § 1.6) and cached per player in between.
 * {@link StatContributor}s register here; the manager itself never special-cases any
 * one source.
 */
public final class PlayerStatManager {
    private PlayerStatManager() {}

    private static final List<StatContributor> CONTRIBUTORS = new ArrayList<>();
    private static final Map<UUID, StatProfile> CACHE = new HashMap<>();

    static {
        register(new EquipmentStatContributor());
        register(new SkillStatContributor());
        register(new AbilityBuffContributor());
        register(new HeartwoodStatContributor());
        // Farming Fortune's full-set bonus was folded into each Harvester's/Cultivator's/
        // Warden's piece's own STATS component (see ModArmor.piece()'s extraStats param)
        // during the playtesting pass, so it shows on every individual item's tooltip
        // instead of only applying invisibly once all 4 pieces are on — EquipmentStat-
        // Contributor already sums those, so a dedicated contributor is no longer needed.
        register(new FishingSetContributor());
    }

    public static void register(StatContributor contributor) {
        CONTRIBUTORS.add(contributor);
    }

    /** Recomputes and caches this player's full profile (base values + every contributor). */
    public static StatProfile recompute(ServerPlayer player) {
        StatProfile profile = StatProfile.empty();
        for (GreenwardStat stat : GreenwardStat.values()) {
            profile.add(stat, stat.baseValue());
        }
        for (StatContributor contributor : CONTRIBUTORS) {
            profile.addAll(contributor.contribute(player));
        }
        CACHE.put(player.getUUID(), profile);
        return profile;
    }

    /** The cached profile, or an immediate recompute if this player has never been computed yet. */
    public static StatProfile getProfile(ServerPlayer player) {
        StatProfile cached = CACHE.get(player.getUUID());
        return cached != null ? cached : recompute(player);
    }

    public static double get(ServerPlayer player, GreenwardStat stat) {
        return getProfile(player).get(stat);
    }

    public static void clear(ServerPlayer player) {
        CACHE.remove(player.getUUID());
    }

    /**
     * Per-source breakdown for {@code /greenward stats} — "Base" plus each registered
     * contributor's own slice, in insertion order. Computed fresh on demand; this is a
     * low-frequency, player-triggered read, not part of the 20-tick recompute path.
     */
    public static Map<String, StatProfile> breakdown(ServerPlayer player) {
        Map<String, StatProfile> breakdown = new LinkedHashMap<>();
        StatProfile base = StatProfile.empty();
        for (GreenwardStat stat : GreenwardStat.values()) {
            base.add(stat, stat.baseValue());
        }
        breakdown.put("Base", base);
        for (StatContributor contributor : CONTRIBUTORS) {
            breakdown.put(contributor.label(), contributor.contribute(player));
        }
        return breakdown;
    }
}
