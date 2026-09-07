package com.green.ward;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class AutoMinerMenu extends AbstractMachineMenu {

    public AutoMinerMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(1 + GreenwardConfig.MAX_STORAGE_SLOTS), new SimpleContainer(1),
                new SimpleContainerData(AutoMinerBlockEntity.DATA_COUNT), () -> { }, () -> { }, () -> { }, stack -> { }, () -> { });
    }

    public AutoMinerMenu(int containerId, Inventory inventory, AutoMinerBlockEntity machine, ContainerData data) {
        this(containerId, inventory, machine, machine.seedContainer(), data,
                machine::increaseStorageTier, machine::increaseSpeedTier, machine::increaseRegenTier,
                stack -> machine.setCompressionTier(stack.is(ModItems.DEEP_PRESS) ? 2 : 1),
                machine::installDeepRegrowthModule);
    }

    private AutoMinerMenu(int containerId, Inventory inventory, Container container, Container seedContainer, ContainerData data,
                           Runnable onStorageUpgrade, Runnable onSpeedUpgrade, Runnable onRegenUpgrade,
                           java.util.function.Consumer<ItemStack> onCompressionUpgrade, Runnable onDeepRegrowthUpgrade) {
        super(ModMenus.AUTO_MINER, containerId, inventory, container, data);
        addUpgradeSlot(UPGRADE_STORAGE_X, SPECIAL_ROW_Y, ModItems.STORAGE_UPGRADE, onStorageUpgrade);
        addUpgradeSlot(UPGRADE_SPEED_X, SPECIAL_ROW_Y, ModItems.SPEED_UPGRADE, onSpeedUpgrade);
        addUpgradeSlot(UPGRADE_REGEN_X, SPECIAL_ROW_Y, ModItems.REGEN_UPGRADE, onRegenUpgrade);
        addUpgradeSlot(UPGRADE_COMPRESSION_MINER_X, SPECIAL_ROW_Y,
                stack -> stack.is(ModItems.PRESS) || stack.is(ModItems.DEEP_PRESS), onCompressionUpgrade);
        addUpgradeSlot(UPGRADE_DEEP_REGROWTH_MINER_X, SPECIAL_ROW_Y, ModItems.DEEP_REGROWTH_MODULE, onDeepRegrowthUpgrade);
        addSlot(new Slot(seedContainer, 0, SEED_X, SPECIAL_ROW_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BlockItem blockItem
                        && blockItem.getBlock().defaultBlockState().is(AutoMinerBlockEntity.REGENERABLE_ORES);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addStorageAndPlayerSlots(inventory);
    }
}
