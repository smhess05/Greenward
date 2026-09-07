package com.green.ward;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

/**
 * The Villager Shop's screen — see {@link VillagerShopMenu} for why this replaced the
 * earlier hidden gesture. A plain filled-panel background (no dedicated texture, same
 * placeholder convention {@link HeartwoodScreen} already uses).
 */
@Environment(EnvType.CLIENT)
public class VillagerShopScreen extends AbstractContainerScreen<VillagerShopMenu> {
    private static final int PANEL_COLOR = 0xFF2A2A3A;
    private static final int SLOT_AREA_COLOR = 0xFF1A1A26;
    private static final int TEXT_COLOR = 0xFFE8E0D0;

    public VillagerShopScreen(VillagerShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 196);
        this.inventoryLabelY = 102;
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableWidget(Button.builder(Component.literal("Sell"), button ->
                        Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, VillagerShopMenu.BUTTON_SELL))
                .bounds(leftPos + 8, topPos + 52, 60, 18)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Repair"), button ->
                        Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, VillagerShopMenu.BUTTON_REPAIR))
                .bounds(leftPos + 108, topPos + 52, 60, 18)
                .build());
        addRenderableWidget(Button.builder(Component.literal(String.format(Locale.ROOT, "Buy Waystone (%,d)", VillagerShopMenu.WAYSTONE_PRICE)), button ->
                        Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, VillagerShopMenu.BUTTON_BUY_WAYSTONE))
                .bounds(leftPos + 8, topPos + 74, 160, 18)
                .build());
        addRenderableWidget(Button.builder(Component.literal(String.format(Locale.ROOT, "Buy Coin Purse (%,d)", VillagerShopMenu.COIN_PURSE_PRICE)), button ->
                        Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, VillagerShopMenu.BUTTON_BUY_COIN_PURSE))
                .bounds(leftPos + 8, topPos + 94, 160, 18)
                .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int xo = leftPos;
        int yo = topPos;
        graphics.fill(xo, yo, xo + imageWidth, yo + imageHeight, PANEL_COLOR);
        graphics.fill(xo + 29, yo + 32, xo + 47, yo + 50, SLOT_AREA_COLOR);
        graphics.fill(xo + 125, yo + 32, xo + 143, yo + 50, SLOT_AREA_COLOR);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(this.font, "Sell", 24, 22, TEXT_COLOR);
        graphics.text(this.font, "Repair", 118, 22, TEXT_COLOR);
        graphics.text(this.font, String.format(Locale.ROOT, "Your Coins: %,d", menu.balance()), 8, 118, TEXT_COLOR);
    }
}
