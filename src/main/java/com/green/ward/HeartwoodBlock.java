package com.green.ward;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** One per world (§ 6.1) — enforced the same way {@link MachinePlacementGuard} enforces
 *  the automation-block world cap, just with a limit of exactly 1 and its own dedicated
 *  {@link HeartwoodData} slot rather than a counted set. */
public class HeartwoodBlock extends BaseEntityBlock {
    private static final MapCodec<HeartwoodBlock> CODEC = simpleCodec(HeartwoodBlock::new);

    public HeartwoodBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeartwoodBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.HEARTWOOD, HeartwoodBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            player.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        HeartwoodData data = HeartwoodData.get(serverLevel.getServer());
        if (data.hasPosition()) {
            level.removeBlock(pos, false);
            if (placer instanceof Player player) {
                ItemStack returned = new ItemStack(this.asItem(), 1);
                if (!player.getInventory().add(returned)) {
                    Block.popResource(level, pos, returned);
                }
                player.sendSystemMessage(Component.literal("Refused: only one Heartwood may exist per world."));
            } else {
                Block.popResource(level, pos, new ItemStack(this.asItem()));
            }
            return;
        }
        data.setPosition(pos);
    }
}
