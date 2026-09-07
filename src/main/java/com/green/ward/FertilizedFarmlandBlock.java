package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * MIGRATION SHIM ONLY — kept registered purely so a world containing this block from
 * before the Permanence Retrofit doesn't have it turn to air on load (Permanence
 * Charter § 1.1: a missing block registration becomes air on the next chunk load).
 * Nothing ever places this block anymore; {@code Greenward}'s fertilizer-use handler
 * now writes straight to {@link FertilizedFarmlandData} instead. Any surviving instance
 * converts itself to real {@code minecraft:farmland} (moisture preserved) plus a
 * {@link FertilizedFarmlandData} record the moment it's random-ticked — the same cadence
 * vanilla farmland itself already ticks on, so this self-heals within normal play without
 * a dedicated chunk-scan pass. {@code /greenward decommission} sweeps any stragglers
 * immediately. Safe to delete this class entirely in a future update once enough time
 * has passed that no unconverted world is expected to remain.
 */
public class FertilizedFarmlandBlock extends FarmlandBlock {

    public FertilizedFarmlandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        convertToVanilla(level, pos, state);
    }

    public static void convertToVanilla(ServerLevel level, BlockPos pos, BlockState state) {
        int moisture = state.getValue(MOISTURE);
        level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, moisture));
        FertilizedFarmlandData.get(level).setFertilized(pos);
    }
}
