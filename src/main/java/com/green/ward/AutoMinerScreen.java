package com.green.ward;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class AutoMinerScreen extends AbstractMachineScreen<AutoMinerMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("greenward", "textures/gui/container/machine_5.png");
    private static final List<SlotLabel> LABELS = List.of(
            new SlotLabel(AbstractMachineMenu.FUEL_X, "Fuel", -1, -1),
            new SlotLabel(AbstractMachineMenu.UPGRADE_STORAGE_X, "Storage", AbstractMachineMenu.DATA_STORAGE_TIER, GreenwardConfig.MAX_STORAGE_TIER),
            new SlotLabel(AbstractMachineMenu.UPGRADE_SPEED_X, "Speed", AbstractMachineMenu.DATA_SPEED_TIER, GreenwardConfig.MAX_SPEED_TIER),
            new SlotLabel(AbstractMachineMenu.UPGRADE_REGEN_X, "Regen", AutoMinerBlockEntity.DATA_REGEN_TIER, GreenwardConfig.MAX_REGEN_TIER),
            new SlotLabel(AbstractMachineMenu.SEED_X, "Seed", -1, -1)
    );

    public AutoMinerScreen(AutoMinerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, LABELS);
    }
}
