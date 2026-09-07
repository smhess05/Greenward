package com.green.ward;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

/**
 * Heartwood branch node bonuses + socketed talisman effects (Design Program Update 6) —
 * a plain {@link StatContributor}, applied to every player identically, since the
 * Heartwood is one shared, world-wide structure rather than a per-player one (§ 6.1's
 * own framing: "One per world"). Whoever pays a node's material cost unlocks it for
 * everyone; socketed talismans likewise benefit every player "anywhere in the world"
 * exactly as § 6.3 specifies.
 */
public final class HeartwoodStatContributor implements StatContributor {

    @Override
    public String label() {
        return "Heartwood";
    }

    @Override
    public StatProfile contribute(ServerPlayer player) {
        StatProfile profile = StatProfile.empty();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return profile;
        }
        HeartwoodData data = HeartwoodData.get(serverLevel.getServer());

        for (HeartwoodBranch branch : HeartwoodBranch.values()) {
            var nodes = branch.nodes();
            int unlocked = data.unlockedCount(branch);
            for (int i = 0; i < unlocked && i < nodes.size(); i++) {
                nodes.get(i).statGrants().forEach(profile::add);
            }
        }

        for (Item talisman : data.socketedTalismans()) {
            var stats = TalismanType.statsFor(talisman);
            if (stats != null) {
                stats.forEach(profile::add);
            }
        }

        return profile;
    }
}
