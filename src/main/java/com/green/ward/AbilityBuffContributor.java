package com.green.ward;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Temporary stat buffs from active abilities (currently just Stoneflow — Design Program
 * Update 5 § 5.3). A {@link StatContributor} like any other, so it plugs into {@link
 * PlayerStatManager}'s existing recompute pipeline with no special-casing there;
 * expired buffs are pruned lazily on read rather than needing their own tick loop.
 */
public final class AbilityBuffContributor implements StatContributor {
    private record Buff(GreenwardStat stat, double magnitude, long expiryGameTime) {}

    private static final Map<UUID, List<Buff>> ACTIVE = new HashMap<>();

    public static void grant(ServerPlayer player, GreenwardStat stat, double magnitude, long durationTicks) {
        long expiry = player.level().getGameTime() + durationTicks;
        ACTIVE.computeIfAbsent(player.getUUID(), id -> new ArrayList<>()).add(new Buff(stat, magnitude, expiry));
    }

    @Override
    public String label() {
        return "Abilities";
    }

    @Override
    public StatProfile contribute(ServerPlayer player) {
        StatProfile profile = StatProfile.empty();
        List<Buff> buffs = ACTIVE.get(player.getUUID());
        if (buffs == null || buffs.isEmpty()) {
            return profile;
        }
        long now = player.level().getGameTime();
        buffs.removeIf(buff -> buff.expiryGameTime() <= now);
        for (Buff buff : buffs) {
            profile.add(buff.stat(), buff.magnitude());
        }
        return profile;
    }
}
