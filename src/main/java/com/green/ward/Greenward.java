package com.green.ward;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Greenward implements ModInitializer {

    @Override
    public void onInitialize() {
        ModItems.initialize();
        ModBlocks.initialize();

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);

            if (!(held.getItem() instanceof HoeItem) || level.isClientSide()) {
                return InteractionResult.PASS;
            }

            BlockPos center = hitResult.getBlockPos();
            BlockState state = level.getBlockState(center);

            if (!isMatureCrop(state)) {
                return InteractionResult.PASS;
            }

            int radius = radiusForHoe(held.getItem());
            harvestArea((ServerLevel) level, center, radius, player, hand);

            return InteractionResult.SUCCESS;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);

            if (held.getItem() != ModItems.FERTILIZER || level.isClientSide()) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState clicked = level.getBlockState(pos);

            if (clicked.getBlock() != Blocks.FARMLAND) {
                return InteractionResult.PASS;
            }

            level.setBlockAndUpdate(pos, ModBlocks.FERTILIZED_FARMLAND.defaultBlockState());

            if (!player.isCreative()) {
                held.shrink(1);
            }

            return InteractionResult.SUCCESS;
        });
    }

    private static boolean isMatureCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
    }

    private static int radiusForHoe(Item item) {
        if (item == Items.NETHERITE_HOE) return 2;
        if (item == Items.DIAMOND_HOE)   return 2;
        if (item == Items.IRON_HOE)      return 1;
        return 0;
    }

    private static void harvestArea(ServerLevel level, BlockPos center, int radius,
                                    Player player, InteractionHand hand) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (isMatureCrop(state)) {
                        harvestOne(level, pos, state, player, hand);
                        break;
                    }
                }
            }
        }
    }

    private static void harvestOne(ServerLevel level, BlockPos pos, BlockState state,
                                    Player player, InteractionHand hand) {
        CropBlock crop = (CropBlock) state.getBlock();

        BlockState soil = level.getBlockState(pos.below());

        // Drop the normal yield.
        Block.dropResources(state, level, pos, null, player, player.getItemInHand(hand));
        if (soil.getBlock() == ModBlocks.FERTILIZED_FARMLAND) {
            Block.dropResources(state, level, pos, null, player, player.getItemInHand(hand));
        }

        level.setBlockAndUpdate(pos, crop.getStateForAge(0));
        player.getItemInHand(hand).hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}