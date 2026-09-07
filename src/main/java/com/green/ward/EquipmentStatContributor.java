package com.green.ward;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Reads {@link GreenwardComponents#STATS} off every equipped armor piece plus whatever
 * is held in the main hand, and sums them flat. This is the only {@link StatContributor}
 * Update 1 ships — Skills/Heartwood/Tempering register their own in later Updates.
 */
public final class EquipmentStatContributor implements StatContributor {

    @Override
    public String label() {
        return "Equipment";
    }

    @Override
    public StatProfile contribute(ServerPlayer player) {
        StatProfile profile = StatProfile.empty();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR && slot != EquipmentSlot.MAINHAND) {
                continue;
            }
            addFrom(profile, player.getItemBySlot(slot));
        }
        return profile;
    }

    private void addFrom(StatProfile profile, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        Map<GreenwardStat, Double> stats = stack.get(GreenwardComponents.STATS);
        if (stats == null) {
            return;
        }
        stats.forEach(profile::add);
    }
}
