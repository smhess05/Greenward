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
            new SlotLabel(AbstractMachineMenu.SEED_X, "Seed", -1, -1),
            new SlotLabel(AbstractMachineMenu.UPGRADE_COMPRESSION_MINER_X, "Press", AbstractMachineMenu.DATA_COMPRESSION_TIER, AbstractMachineBlockEntity.MAX_COMPRESSION_TIER),
            // Boolean install (not a tier ladder), so no numeric readout — SlotLabel's
            // "tier/max" text assumes a 0..maxTier range, which doesn't read sensibly for
            // an installed/not-installed toggle.
            new SlotLabel(AbstractMachineMenu.UPGRADE_DEEP_REGROWTH_MINER_X, "Deep Regrowth", -1, -1)
    );

    /** The Miner's 6th and 7th special-slot columns (Compression at X=188, Deep Regrowth
     *  at X=224) run past the standard 176px panel width — see {@link
     *  AbstractMachineScreen}'s wide-canvas constructor. */
    private static final int IMAGE_WIDTH = 250;

    public AutoMinerScreen(AutoMinerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, LABELS, IMAGE_WIDTH);
    }
}
