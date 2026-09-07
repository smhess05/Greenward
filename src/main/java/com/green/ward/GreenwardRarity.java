package com.green.ward;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

/**
 * Vanilla's own {@code net.minecraft.world.item.Rarity} only has 4 tiers (COMMON/
 * UNCOMMON/RARE/EPIC) and, being a plain enum, can't be extended with a 5th — hence a
 * parallel Greenward-owned enum rather than reusing vanilla's. This is a plain item
 * data component (§ 1.1: "custom items — silently dropped from inventories —
 * acceptable"), not world state, so it carries zero permanence risk.
 */
public enum GreenwardRarity implements StringRepresentable {
    COMMON("common", ChatFormatting.WHITE),
    UNCOMMON("uncommon", ChatFormatting.GREEN),
    RARE("rare", ChatFormatting.BLUE),
    EPIC("epic", ChatFormatting.DARK_PURPLE),
    LEGENDARY("legendary", ChatFormatting.GOLD);

    private final String id;
    private final ChatFormatting color;

    GreenwardRarity(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public ChatFormatting color() {
        return color;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
