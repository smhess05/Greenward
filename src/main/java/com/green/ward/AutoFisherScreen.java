package com.green.ward;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class AutoFisherScreen extends AbstractMachineScreen<AutoFisherMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("greenward", "textures/gui/container/machine_3.png");
    private static final List<SlotLabel> LABELS = List.of(
            new SlotLabel(AbstractMachineMenu.FUEL_X, "Fuel", -1, -1),
            new SlotLabel(AbstractMachineMenu.UPGRADE_STORAGE_X, "Storage", AbstractMachineMenu.DATA_STORAGE_TIER, GreenwardConfig.MAX_STORAGE_TIER),
            new SlotLabel(AbstractMachineMenu.UPGRADE_SPEED_X, "Speed", AbstractMachineMenu.DATA_SPEED_TIER, GreenwardConfig.MAX_SPEED_TIER)
    );

    public AutoFisherScreen(AutoFisherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, LABELS);
    }
}
