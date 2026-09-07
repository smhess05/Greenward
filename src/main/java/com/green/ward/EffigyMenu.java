package com.green.ward;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/** One shared menu for all five Effigy blocks — same Storage/Speed/Compression layout as
 *  the Auto-Harvester/Auto-Fisher (no Regen or seed slot; there's no ore concept here). */
public class EffigyMenu extends AbstractMachineMenu {

    public EffigyMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(1 + GreenwardConfig.MAX_STORAGE_SLOTS),
                new SimpleContainerData(AbstractMachineBlockEntity.BASE_DATA_COUNT), () -> { }, () -> { }, stack -> { });
    }

    public EffigyMenu(int containerId, Inventory inventory, EffigyBlockEntity machine, ContainerData data) {
        this(containerId, inventory, machine, data, machine::increaseStorageTier, machine::increaseSpeedTier,
                stack -> machine.setCompressionTier(stack.is(ModItems.DEEP_PRESS) ? 2 : 1));
    }

    private EffigyMenu(int containerId, Inventory inventory, Container container, ContainerData data,
                        Runnable onStorageUpgrade, Runnable onSpeedUpgrade, Consumer<ItemStack> onCompressionUpgrade) {
        super(ModMenus.EFFIGY, containerId, inventory, container, data);
        addUpgradeSlot(UPGRADE_STORAGE_X, SPECIAL_ROW_Y, ModItems.STORAGE_UPGRADE, onStorageUpgrade);
        addUpgradeSlot(UPGRADE_SPEED_X, SPECIAL_ROW_Y, ModItems.SPEED_UPGRADE, onSpeedUpgrade);
        addUpgradeSlot(UPGRADE_COMPRESSION_X, SPECIAL_ROW_Y,
                stack -> stack.is(ModItems.PRESS) || stack.is(ModItems.DEEP_PRESS), onCompressionUpgrade);
        addStorageAndPlayerSlots(inventory);
    }
}
