package com.green.ward;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;

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
 *
 * <p>Post-Design-Program addition (user-requested — "every tool, including vanilla,
 * needs damage/mining speed/fortune numbers"): a second, item-agnostic block reads the
 * tool's own real {@code Tool}/{@code ItemAttributeModifiers} components directly —
 * these exist on every pickaxe/axe/shovel/hoe/sword in the game, vanilla or modded, so
 * a plain Diamond Pickaxe now shows its Mining Speed exactly the same way a Prospector's
 * Drill does, letting the two be compared apples-to-apples. Rendered in gray (a
 * descriptive base-stat reading) rather than the green/red Greenward stat lines below it
 * (an applied bonus), so the two read as visually distinct categories. Fortune-type
 * stats stay Greenward-STATS-only below, since vanilla has no equivalent to read.
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
        Float miningSpeed = toolMiningSpeed(stack);
        Float damage = attackDamageBonus(stack);

        if (stats == null && rarity == null && satchel == null && miningSpeed == null && damage == null) {
            return;
        }

        if (damage != null) {
            removeVanillaAttackDamageLine(lines);
        }

        if (satchel != null) {
            lines.add(Component.literal("Holding: " + satchel.totalCount() + " / " + satchel.capacity())
                    .withStyle(ChatFormatting.GRAY));
        }

        if (damage != null) {
            lines.add(Component.literal("❁ Damage: +" + trim(damage)).withStyle(ChatFormatting.GRAY));
        }
        if (miningSpeed != null) {
            lines.add(Component.literal("⫕ Mining Speed: " + trim(miningSpeed)).withStyle(ChatFormatting.GRAY));
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

    /** The tool's own best "correct material" speed — the same headline number a
     *  material's own {@code ToolMaterial.speed()} represents (e.g. vanilla Diamond
     *  Pickaxe reads 8.0), read straight off the real {@link Tool} component rather than
     *  hand-maintained per-material constants, so it's automatically accurate for any
     *  tool the game or any mod ever adds. */
    private static Float toolMiningSpeed(ItemStack stack) {
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool == null) {
            return null;
        }
        float best = tool.defaultMiningSpeed();
        for (Tool.Rule rule : tool.rules()) {
            if (rule.speed().isPresent()) {
                best = Math.max(best, rule.speed().get());
            }
        }
        return best > 0.0F ? best : null;
    }

    /** The item's own raw Attack Damage attribute bonus — the weapon's real contribution,
     *  not folded together with the player's own bare-fist baseline the way vanilla's own
     *  tooltip line does (see {@code ItemAttributeModifiers.Display.Default.apply}'s
     *  {@code displayWithBase} branch, which adds the attribute's default value of 1.0
     *  when the modifier carries {@link Item#BASE_ATTACK_DAMAGE_ID}) — that convention is
     *  vanilla's own and not what this line is for. */
    private static Float attackDamageBonus(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return null;
        }
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.slot() == EquipmentSlotGroup.MAINHAND) {
                return (float) entry.modifier().amount();
            }
        }
        return null;
    }

    /** Vanilla's own default tooltip (already built by the time this callback runs) prints
     *  its own "+X Attack Damage" line for any weapon/tool carrying a base-attack-damage
     *  modifier — the same stat the line above now shows. Rather than print it twice,
     *  strip vanilla's own copy and let the line above be the only one — found by content
     *  rather than position. Vanilla renders this two different ways depending on whether
     *  a live player was available to fold the base value in ({@code displayWithBase}):
     *  a plain top-level translatable ("+X Attack Damage") when there's no player, or a
     *  space-then-append-wrapped one ("X Attack Damage") when there is — the translatable
     *  actually naming the attribute can be the line's own contents OR buried a level down
     *  in a sibling, so this walks both. */
    private static void removeVanillaAttackDamageLine(List<Component> lines) {
        String attackDamageId = Attributes.ATTACK_DAMAGE.value().getDescriptionId();
        lines.removeIf(line -> isAttributeModifierLineFor(line, attackDamageId));
    }

    private static boolean isAttributeModifierLineFor(Component component, String attributeDescriptionId) {
        if (component.getContents() instanceof TranslatableContents translatable
                && translatable.getKey().startsWith("attribute.modifier.")
                && hasAttributeArg(translatable, attributeDescriptionId)) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (isAttributeModifierLineFor(sibling, attributeDescriptionId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAttributeArg(TranslatableContents translatable, String attributeDescriptionId) {
        for (Object arg : translatable.getArgs()) {
            if (arg instanceof Component argComponent && componentNames(argComponent, attributeDescriptionId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean componentNames(Component component, String translationKey) {
        if (component.getContents() instanceof TranslatableContents translatable
                && translatable.getKey().equals(translationKey)) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (componentNames(sibling, translationKey)) {
                return true;
            }
        }
        return false;
    }

    private static String trim(float value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.1f", value);
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
