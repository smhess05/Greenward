package com.green.ward;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/** One shared Block class for all five Effigies, parametrized by {@link EffigyType} —
 *  see {@link EffigyBlockEntity}'s own doc comment for why a single shared
 *  implementation is used instead of five near-identical ones. Subject to the same
 *  placement cap as the three automation machines ({@link MachinePlacementGuard}) —
 *  Effigies are just as much "automation blocks" as those three, so leaving them
 *  uncapped would be an obvious loophole around Update 4 § 4.1③'s whole point. */
public class EffigyBlock extends BaseEntityBlock {

    private final EffigyType effigyType;
    /** One shared Java class backs five distinct registered blocks (one per {@link
     *  EffigyType}), so there's no single {@code Properties -> EffigyBlock} factory that
     *  could reconstruct the right instance generically — {@code MapCodec.unit} just
     *  hands back this exact, already-fully-constructed instance instead. */
    private final MapCodec<EffigyBlock> codec = MapCodec.unit(() -> this);

    public EffigyBlock(EffigyType effigyType, BlockBehaviour.Properties properties) {
        super(properties);
        this.effigyType = effigyType;
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, false));
    }

    /** Resolved lazily (construction happens in {@code ModBlocks.initialize()}, which
     *  runs BEFORE {@code ModBlockEntities.initialize()} creates these fields) — same
     *  pattern as {@link EffigyBlockEntity#resolveType}. */
    private static BlockEntityType<EffigyBlockEntity> resolveType(EffigyType effigyType) {
        return switch (effigyType) {
            case ROTTING -> ModBlockEntities.EFFIGY_ROTTING;
            case BONEPILE -> ModBlockEntities.EFFIGY_BONEPILE;
            case WEBBED -> ModBlockEntities.EFFIGY_WEBBED;
            case VOLATILE -> ModBlockEntities.EFFIGY_VOLATILE;
            case VOID -> ModBlockEntities.EFFIGY_VOID;
        };
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EffigyBlockEntity(effigyType, pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            player.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, resolveType(effigyType), AbstractMachineBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            MachinePlacementGuard.enforceOnPlace(serverLevel, pos, placer, stack, this);
        }
    }
}
