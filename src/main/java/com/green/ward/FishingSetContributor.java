package com.green.ward;

import net.minecraft.server.level.ServerPlayer;

/**
 * Design Program Update 9 § 9.2 — the full-Angler's-set (+15%) and rod-tier-3-plus-full-set
 * synergy (+35%) bonuses that used to live inline in {@code SeaCreatureHandler}'s hardcoded
 * switch, now expressed as a plain {@link StatContributor} alongside the rods' own flat
 * grants (see {@code ModTools}).
 */
public final class FishingSetContributor implements StatContributor {

    @Override
    public String label() {
        return "Fishing Set Bonus";
    }

    @Override
    public StatProfile contribute(ServerPlayer player) {
        StatProfile profile = StatProfile.empty();
        if (!GreenwardConfig.ENABLE_GEAR_SETS || !ModArmor.hasFullAnglerSet(player)) {
            return profile;
        }
        profile.add(GreenwardStat.SEA_CREATURE_CHANCE, 15.0);
        if (player.getMainHandItem().getItem() == ModTools.LEVIATHAN_ROD) {
            profile.add(GreenwardStat.SEA_CREATURE_CHANCE, 35.0);
        }
        return profile;
    }
}
