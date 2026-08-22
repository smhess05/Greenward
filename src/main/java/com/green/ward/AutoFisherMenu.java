package com.green.ward;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class AutoFisherMenu extends AbstractMachineMenu {

    public AutoFisherMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(1 + GreenwardConfig.MAX_STORAGE_SLOTS),
                new SimpleContainerData(AbstractMachineBlockEntity.BASE_DATA_COUNT), () -> { }, () -> { });
    }

    public AutoFisherMenu(int containerId, Inventory inventory, AutoFisherBlockEntity machine, ContainerData data) {
        this(containerId, inventory, machine, data, machine::increaseStorageTier, machine::increaseSpeedTier);
    }

    private AutoFisherMenu(int containerId, Inventory inventory, Container container, ContainerData data,
                            Runnable onStorageUpgrade, Runnable onSpeedUpgrade) {
        super(ModMenus.AUTO_FISHER, containerId, inventory, container, data);
        addUpgradeSlot(UPGRADE_STORAGE_X, SPECIAL_ROW_Y, ModItems.STORAGE_UPGRADE, onStorageUpgrade);
        addUpgradeSlot(UPGRADE_SPEED_X, SPECIAL_ROW_Y, ModItems.SPEED_UPGRADE, onSpeedUpgrade);
        addStorageAndPlayerSlots(inventory);
    }
}
