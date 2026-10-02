package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The real, visible block {@code Greenward}'s fertilizer-use handler swaps farmland to
 * (user-requested — fertilizing should "switch the blockstate to the fertilized farmland
 * and the texture," permanently, not just an invisible data record with a chat message).
 * A plain {@link FarmlandBlock} subclass with no behavior overrides of its own, so it
 * inherits vanilla farmland's hydration/crop-growth/trampling exactly — only its
 * blockstate (own dry/wet textures, see {@code assets/greenward/blockstates/
 * fertilized_farmland.json}) and its identity (read by {@link HarvestLogic} via {@link
 * FertilizedFarmlandData}, which the fertilizer handler still also writes to as a
 * belt-and-suspenders record) differ from real farmland.
 *
 * <p>An earlier version of this class auto-converted itself back to vanilla farmland on
 * every random tick, framed as a "Permanence Charter" migration shim — that undermined
 * the entire point of a persistent custom block, converting away before a player could
 * ever really see the texture. That auto-revert is gone; {@code /greenward decommission}
 * (which already knows how to convert this block, via {@link #convertToVanilla}) is the
 * project's real, existing, user-initiated answer to "make my world safe before removing
 * the mod" — the same mechanism every other Greenward block already relies on, not a
 * silent per-tick self-destruct unique to this one.
 */
public class FertilizedFarmlandBlock extends FarmlandBlock {

    public FertilizedFarmlandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** Used by {@code /greenward decommission} to convert this block to vanilla farmland
     *  (moisture preserved) before the mod is removed — see the class doc above. */
    public static void convertToVanilla(ServerLevel level, BlockPos pos, BlockState state) {
        int moisture = state.getValue(MOISTURE);
        level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, moisture));
        FertilizedFarmlandData.get(level).setFertilized(pos);
    }
}
