package com.green.ward;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * The four Satchels share this one Item class — behavior is identical across all of them,
 * only the {@link SatchelType} (resolved per-instance via {@link SatchelType#forItem}) and
 * the tag it vacuums differ, so one class avoids four near-identical copies. Filling
 * happens passively via {@link SatchelHandler}'s tick; emptying is the only direct
 * interaction this class provides — sneak-right-click dumps everything back into the
 * player's inventory (overflow drops on the ground rather than being destroyed).
 */
public class SatchelItem extends Item {
    private static final int BAR_WIDTH = 13;

    public SatchelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        SatchelContents contents = stack.getOrDefault(GreenwardComponents.SATCHEL_CONTENTS, SatchelContents.EMPTY);
        if (contents.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer) {
            for (Map.Entry<Item, Integer> entry : contents.counts().entrySet()) {
                int remaining = entry.getValue();
                while (remaining > 0) {
                    int chunk = Math.min(remaining, entry.getKey().getDefaultMaxStackSize());
                    ItemStack toGive = new ItemStack(entry.getKey(), chunk);
                    if (!player.getInventory().add(toGive)) {
                        player.drop(toGive, false);
                    }
                    remaining -= chunk;
                }
            }
            stack.set(GreenwardComponents.SATCHEL_CONTENTS, new SatchelContents(Map.of(), contents.capacity()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        SatchelContents contents = stack.get(GreenwardComponents.SATCHEL_CONTENTS);
        return contents != null && !contents.isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        SatchelContents contents = stack.get(GreenwardComponents.SATCHEL_CONTENTS);
        if (contents == null || contents.capacity() <= 0) {
            return 0;
        }
        int width = Math.round(BAR_WIDTH * (float) contents.totalCount() / contents.capacity());
        return Math.max(0, Math.min(BAR_WIDTH, width));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        SatchelType type = SatchelType.forItem(stack.getItem());
        if (type == null) {
            return 0xFFFFFF;
        }
        return switch (type) {
            case AGRONOMY -> 0x7CBB4A;
            case LITHIC -> 0x9C9CA6;
            case TIDAL -> 0x4A9CBB;
            case OSSUARY -> 0xB04A4A;
        };
    }
}
