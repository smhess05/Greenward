package com.green.ward;

import net.minecraft.world.item.Item;

import java.util.Map;

/**
 * One Heartwood branch node (Design Program Update 6 § 6.2). {@code statGrants} is
 * empty for a "special unlock" node (Vein Blast/Stoneflow, the Deep Regrowth Module
 * recipe, Twin Bite) — those are checked directly via {@code HeartwoodData.isUnlocked}
 * at their own call sites rather than through a stat.
 */
public record HeartwoodNode(String id, String displayName, Map<Item, Integer> cost,
                             Map<GreenwardStat, Double> statGrants, boolean grantsSocket) {

    public boolean isSpecialUnlock() {
        return statGrants.isEmpty();
    }
}
