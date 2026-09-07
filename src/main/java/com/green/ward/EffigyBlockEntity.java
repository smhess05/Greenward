package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * One shared class for all five Effigies — behavior is identical across them (roll this
 * tick's drops, deposit into storage), only {@link EffigyType} differs, so five
 * near-identical classes would just be copy-paste. Reuses the full shared machine
 * architecture (fuel-as-boost, Storage/Speed/Compression upgrades, GUI sync) from {@link
 * AbstractMachineBlockEntity} exactly as the Design Program's own § 4.3 text asks ("the
 * same frame and module system as the other machines").
 */
public class EffigyBlockEntity extends AbstractMachineBlockEntity {

    private final EffigyType effigyType;

    public EffigyBlockEntity(EffigyType effigyType, BlockPos pos, BlockState state) {
        super(resolveType(effigyType), pos, state);
        this.effigyType = effigyType;
    }

    /** Same enum-init-order-safe lazy lookup as {@link CompressionLadder}/{@link
     *  SatchelType} — resolved at construction time (a real placement or chunk load),
     *  long after {@code ModBlockEntities.initialize()} has run, never at class-load
     *  time. Avoids needing a self-referencing supplier just to hand the BlockEntityType
     *  back to its own constructor. */
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
    protected int[] tickIntervalsByTier() {
        int base = effigyType.baseInterval();
        // Same proportional speed-tier curve as the other machines (roughly 0.75/0.6/0.4/0.25
        // of base by max tier), scaled off this Effigy's own base interval.
        return new int[] {
                base,
                Math.round(base * 0.8F),
                Math.round(base * 0.6F),
                Math.round(base * 0.4F),
                Math.round(base * 0.25F)
        };
    }

    @Override
    protected boolean doOperation(ServerLevel level, BlockPos pos, BlockState state) {
        List<ItemStack> drops = effigyType.rollDrops(level.getRandom());
        if (drops.isEmpty()) {
            return true;
        }
        depositOrDrop(level, pos, drops);
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.greenward." + effigyType.id());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new EffigyMenu(containerId, inventory, this, containerData());
    }
}
