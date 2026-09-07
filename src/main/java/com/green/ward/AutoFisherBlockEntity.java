package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AutoFisherBlockEntity extends AbstractMachineBlockEntity {

    private static final int[] TICK_INTERVALS_BY_TIER = {200, 160, 120, 80, 50};

    public AutoFisherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTO_FISHER, pos, state);
    }

    @Override
    protected int[] tickIntervalsByTier() {
        return TICK_INTERVALS_BY_TIER;
    }

    /** Independent nautilus chance for the Auto-Fisher, replacing the treasure pool it no
     *  longer rolls — Design Program Update 4 § 4.1④. */
    private static final float NAUTILUS_CHANCE = 0.02F;

    /** Vanilla's top-level fishing table weights junk 10 / treasure 5 / fish 85. The
     *  Auto-Fisher never rolls treasure at all (§ 4.1④ — that pool is ungated for real
     *  player casts only, via the data-file override in {@code
     *  data/minecraft/loot_table/gameplay/fishing.json}, and the machine must not reach
     *  it), so it picks between just junk and fish at their original relative weights. */
    private static final int JUNK_WEIGHT = 10;
    private static final int FISH_WEIGHT = 85;

    @Override
    protected boolean doOperation(ServerLevel level, BlockPos pos, BlockState state) {
        if (!hasAdjacentWaterSource(level, pos)) {
            return false;
        }

        // Roll fish/junk directly instead of simulating a bobber/bite. The tool is a
        // plain, unenchanted fishing rod, so Luck of the Sea/Lure never apply to machine
        // catches. Treasure is deliberately excluded — see the fields above.
        var tableKey = level.getRandom().nextInt(JUNK_WEIGHT + FISH_WEIGHT) < JUNK_WEIGHT
                ? BuiltInLootTables.FISHING_JUNK : BuiltInLootTables.FISHING_FISH;
        LootTable table = level.getServer().reloadableRegistries().getLootTable(tableKey);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .create(LootContextParamSets.FISHING);

        List<ItemStack> drops = new ArrayList<>();
        table.getRandomItems(params, drops::add);
        if (level.getRandom().nextFloat() < NAUTILUS_CHANCE) {
            drops.add(new ItemStack(Items.NAUTILUS_SHELL));
        }
        depositOrDrop(level, pos, drops);
        return true;
    }

    private static boolean hasAdjacentWaterSource(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (isWaterSource(level, pos.relative(direction))) {
                return true;
            }
        }
        return isWaterSource(level, pos.below());
    }

    private static boolean isWaterSource(ServerLevel level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        return fluidState.isSourceOfType(Fluids.WATER);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.greenward.auto_fisher");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AutoFisherMenu(containerId, inventory, this, containerData());
    }
}
