package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class AutoHarvesterBlockEntity extends AbstractMachineBlockEntity {

    private static final int RADIUS = 4;
    private static final String TAG_SCAN_INDEX = "scan_index";
    private static final int[] TICK_INTERVALS_BY_TIER = {40, 32, 24, 18, 10};

    // Deterministic scan order over the 9x9 footprint at the machine's Y and Y+1,
    // built once and walked steadily via a persisted index rather than rescanned each op.
    private static final BlockPos[] SCAN_OFFSETS = buildScanOffsets();

    private int scanIndex;

    public AutoHarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTO_HARVESTER, pos, state);
    }

    private static BlockPos[] buildScanOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    offsets.add(new BlockPos(dx, dy, dz));
                }
            }
        }
        return offsets.toArray(new BlockPos[0]);
    }

    @Override
    protected int[] tickIntervalsByTier() {
        return TICK_INTERVALS_BY_TIER;
    }

    @Override
    protected boolean doOperation(ServerLevel level, BlockPos pos, BlockState state) {
        for (int i = 0; i < SCAN_OFFSETS.length; i++) {
            int index = (scanIndex + i) % SCAN_OFFSETS.length;
            BlockPos target = pos.offset(SCAN_OFFSETS[index]);
            BlockState targetState = level.getBlockState(target);
            if (HarvestLogic.isMatureCrop(targetState)) {
                scanIndex = (index + 1) % SCAN_OFFSETS.length;
                return HarvestLogic.harvestIntoStorage(level, target, targetState, this);
            }
        }
        scanIndex = (scanIndex + 1) % SCAN_OFFSETS.length;
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        scanIndex = input.getIntOr(TAG_SCAN_INDEX, 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(TAG_SCAN_INDEX, scanIndex);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.greenward.auto_harvester");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AutoHarvesterMenu(containerId, inventory, this, containerData());
    }
}
