package com.green.ward;

import net.minecraft.server.level.ServerPlayer;

/**
 * A pluggable source of stat contributions. {@link PlayerStatManager} sums every
 * registered contributor's {@link StatProfile} into the player's live total — this is
 * the seam later Updates attach through without touching the stat core: Skills (Update
 * 2), Heartwood branches (Update 6), and Tempering (Update 8) each register their own
 * contributor rather than the manager growing a special case per system.
 */
public interface StatContributor {
    StatProfile contribute(ServerPlayer player);

    /** Shown as the source label in {@code /greenward stats}'s breakdown. */
    default String label() {
        return getClass().getSimpleName();
    }
}
