package com.green.ward;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Update 1 § 1.5 tooltip layout: stats block, blank line, rarity footer in the
 * rarity's colour. (The spec's "ability/set block" has no data source yet in Update 1
 * — nothing populates it — so it's simply absent rather than an empty placeholder
 * section; later Updates that add unique-ability text insert it between the two.)
 * Positive stat values render green, negative red. Client-only rendering — registered
 * from {@link GreenwardClient}, never referenced from common code.
 */
final class GreenwardTooltipRenderer {
    private GreenwardTooltipRenderer() {}

    static void register() {
        ItemTooltipCallback.EVENT.register(GreenwardTooltipRenderer::addTooltip);
    }

    private static void addTooltip(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
                                    net.minecraft.world.item.TooltipFlag flags, List<Component> lines) {
        Map<GreenwardStat, Double> stats = stack.get(GreenwardComponents.STATS);
        GreenwardRarity rarity = stack.get(GreenwardComponents.RARITY);
        SatchelContents satchel = stack.get(GreenwardComponents.SATCHEL_CONTENTS);
        if (stats == null && rarity == null && satchel == null) {
            return;
        }

        if (satchel != null) {
            lines.add(Component.literal("Holding: " + satchel.totalCount() + " / " + satchel.capacity())
                    .withStyle(ChatFormatting.GRAY));
        }

        if (stats != null && !stats.isEmpty()) {
            for (GreenwardStat stat : GreenwardStat.values()) {
                Double value = stats.get(stat);
                if (value == null || value == 0.0) {
                    continue;
                }
                lines.add(statLine(stat, value));
            }
        }

        if (rarity != null) {
            lines.add(Component.literal(""));
            lines.add(Component.literal(capitalize(rarity.name())).withStyle(rarity.color(), ChatFormatting.BOLD));
        }
    }

    private static Component statLine(GreenwardStat stat, double value) {
        ChatFormatting color = value > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String sign = value > 0 ? "+" : "";
        String formatted = value == Math.floor(value)
                ? String.valueOf((long) value)
                : String.format(Locale.ROOT, "%.1f", value);
        String suffix = stat.isPercentage() ? "%" : "";
        return Component.literal(stat.symbol() + " " + stat.displayName() + ": " + sign + formatted + suffix)
                .withStyle(color);
    }

    private static String capitalize(String enumName) {
        return enumName.charAt(0) + enumName.substring(1).toLowerCase(Locale.ROOT);
    }
}
