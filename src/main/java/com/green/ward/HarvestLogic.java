package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared crop-harvest logic used by both the hoe right-click harvest and the
 * Auto-Harvester machine, so the mature-check / drop / fertilized-bonus / replant
 * sequence lives in exactly one place.
 */
public final class HarvestLogic {
    private HarvestLogic() {}

    public static boolean isMatureCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
    }

    /**
     * Drops the crop's loot, applies the fertilized-farmland yield bonus, and replants
     * at age 0. Pass {@code harvester = null} and {@code tool = ItemStack.EMPTY} for a
     * machine-driven harvest (no Fortune, since there is no enchanted tool in context).
     *
     * @return true if a crop was actually harvested (state was a mature crop).
     */
    public static boolean harvestAndReplant(ServerLevel level, BlockPos pos, BlockState state,
                                             Entity harvester, ItemStack tool) {
        return harvestAndReplant(level, pos, state, harvester, tool, 0.0F);
    }

    /**
     * Same as {@link #harvestAndReplant(ServerLevel, BlockPos, BlockState, Entity, ItemStack)},
     * plus a chance (e.g. the Harvester's Garb set bonus) to drop loot one additional time.
     * This is additive with, not multiplicative on, the fertilized-soil bonus: the
     * fertilized 2x is computed first, then {@code extraDropChance} is rolled once for a
     * single extra pass — never compounded.
     */
    public static boolean harvestAndReplant(ServerLevel level, BlockPos pos, BlockState state,
                                             Entity harvester, ItemStack tool, float extraDropChance) {
        if (!isMatureCrop(state)) {
            return false;
        }

        CropBlock crop = (CropBlock) state.getBlock();
        BlockState soil = level.getBlockState(pos.below());

        Block.dropResources(state, level, pos, null, harvester, tool);
        if (GreenwardConfig.ENABLE_FERTILIZER && soil.getBlock() == ModBlocks.FERTILIZED_FARMLAND) {
            Block.dropResources(state, level, pos, null, harvester, tool);
        }
        if (extraDropChance > 0.0F && level.getRandom().nextFloat() < extraDropChance) {
            Block.dropResources(state, level, pos, null, harvester, tool);
        }

        level.setBlockAndUpdate(pos, crop.getStateForAge(0));
        return true;
    }

    /**
     * Machine variant: same mature-check / fertilized-bonus / replant sequence, but the
     * loot is captured (no Fortune, no player) and deposited into the machine's own
     * storage instead of dropped in the world.
     */
    public static boolean harvestIntoStorage(ServerLevel level, BlockPos pos, BlockState state,
                                              AbstractMachineBlockEntity machine) {
        if (!isMatureCrop(state)) {
            return false;
        }

        CropBlock crop = (CropBlock) state.getBlock();
        BlockState soil = level.getBlockState(pos.below());

        List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, pos, null));
        if (GreenwardConfig.ENABLE_FERTILIZER && soil.getBlock() == ModBlocks.FERTILIZED_FARMLAND) {
            drops.addAll(Block.getDrops(state, level, pos, null));
        }

        level.setBlockAndUpdate(pos, crop.getStateForAge(0));
        machine.depositOrDrop(level, machine.getBlockPos(), drops);
        return true;
    }
}
