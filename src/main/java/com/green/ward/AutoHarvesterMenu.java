package com.green.ward;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class AutoHarvesterMenu extends AbstractMachineMenu {

    public AutoHarvesterMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(1 + GreenwardConfig.MAX_STORAGE_SLOTS),
                new SimpleContainerData(AbstractMachineBlockEntity.BASE_DATA_COUNT), () -> { }, () -> { }, stack -> { });
    }

    public AutoHarvesterMenu(int containerId, Inventory inventory, AutoHarvesterBlockEntity machine, ContainerData data) {
        this(containerId, inventory, machine, data, machine::increaseStorageTier, machine::increaseSpeedTier,
                stack -> machine.setCompressionTier(stack.is(ModItems.DEEP_PRESS) ? 2 : 1));
    }

    private AutoHarvesterMenu(int containerId, Inventory inventory, Container container, ContainerData data,
                               Runnable onStorageUpgrade, Runnable onSpeedUpgrade,
                               java.util.function.Consumer<net.minecraft.world.item.ItemStack> onCompressionUpgrade) {
        super(ModMenus.AUTO_HARVESTER, containerId, inventory, container, data);
        addUpgradeSlot(UPGRADE_STORAGE_X, SPECIAL_ROW_Y, ModItems.STORAGE_UPGRADE, onStorageUpgrade);
        addUpgradeSlot(UPGRADE_SPEED_X, SPECIAL_ROW_Y, ModItems.SPEED_UPGRADE, onSpeedUpgrade);
        addUpgradeSlot(UPGRADE_COMPRESSION_X, SPECIAL_ROW_Y,
                stack -> stack.is(ModItems.PRESS) || stack.is(ModItems.DEEP_PRESS), onCompressionUpgrade);
        addStorageAndPlayerSlots(inventory);
    }
}
