package com.green.ward;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * The Heartwood's screen (Design Program Update 6). A plain filled-panel background
 * (no dedicated texture yet — see this session's final art recommendation) with 4 tab
 * buttons switching the visible branch, a live "next node" readout, and Unlock/Respec
 * buttons that round-trip through {@link HeartwoodMenu#clickMenuButton} via vanilla's
 * own {@code handleInventoryButtonClick} — the same mechanism the enchanting table uses.
 */
@Environment(EnvType.CLIENT)
public class HeartwoodScreen extends AbstractContainerScreen<HeartwoodMenu> {
    private static final int PANEL_COLOR = 0xFF3B2A1A;
    private static final int SLOT_AREA_COLOR = 0xFF2A1D12;
    private static final int TEXT_COLOR = 0xFFE8D8B8;

    private HeartwoodBranch selectedBranch = HeartwoodBranch.ROOT;
    private Button unlockButton;
    private Button respecButton;
    private Button threatToggleButton;

    public HeartwoodScreen(HeartwoodMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 222);
        this.inventoryLabelY = 128;
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        int tabWidth = 40;
        for (int i = 0; i < HeartwoodBranch.values().length; i++) {
            HeartwoodBranch branch = HeartwoodBranch.values()[i];
            addRenderableWidget(Button.builder(Component.literal(branch.displayName()), button -> {
                        selectedBranch = branch;
                        refreshButtons();
                    })
                    .bounds(leftPos + 8 + i * tabWidth, topPos + 4, tabWidth - 2, 16)
                    .build());
        }

        unlockButton = addRenderableWidget(Button.builder(Component.literal("Unlock"), button -> {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                            menu.containerId, selectedBranch.ordinal() * 2);
                })
                .bounds(leftPos + 8, topPos + 60, 78, 18)
                .build());
        respecButton = addRenderableWidget(Button.builder(Component.literal("Respec (75%)"), button -> {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                            menu.containerId, selectedBranch.ordinal() * 2 + 1);
                })
                .bounds(leftPos + 90, topPos + 60, 78, 18)
                .build());
        threatToggleButton = addRenderableWidget(Button.builder(Component.literal(""), button -> {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                            menu.containerId, HeartwoodMenu.TOGGLE_THREAT_BUTTON_ID);
                })
                .bounds(leftPos + 8, topPos + 106, 160, 18)
                .build());
        refreshButtons();
    }

    private void refreshButtons() {
        int unlocked = menu.unlockedCount(selectedBranch);
        HeartwoodNode next = unlocked < selectedBranch.nodes().size() ? selectedBranch.nodes().get(unlocked) : null;
        if (next == null) {
            unlockButton.setMessage(Component.literal("Branch complete"));
            unlockButton.active = false;
        } else {
            unlockButton.setMessage(Component.literal("Unlock: " + next.displayName()));
            unlockButton.active = true;
        }
        respecButton.active = unlocked > 0;
        refreshThreatButton();
    }

    /** Called every tick (not just on tab switch, unlike the branch buttons above) so the
     *  label flips promptly once the server's toggled state syncs back down — a plain
     *  on-click label guess would show stale text for a tick or two otherwise. */
    private void refreshThreatButton() {
        threatToggleButton.setMessage(Component.literal(
                menu.threatEnabled() ? "World Threat: ON (click to disable)" : "World Threat: OFF (click to enable)"));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        refreshThreatButton();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int xo = leftPos;
        int yo = topPos;
        graphics.fill(xo, yo, xo + imageWidth, yo + imageHeight, PANEL_COLOR);
        graphics.fill(xo + 6, yo + 16, xo + 116, yo + 54, SLOT_AREA_COLOR);
        for (int i = 0; i < HeartwoodMenu.MAX_SOCKETS; i++) {
            int col = i % 6;
            int row = i / 6;
            int sx = xo + HeartwoodMenu.TALISMAN_SLOT_X + col * 18 - 1;
            int sy = yo + HeartwoodMenu.TALISMAN_SLOT_Y + row * 18 - 1;
            boolean unlocked = i < menu.totalSockets();
            graphics.fill(sx, sy, sx + 18, sy + 18, unlocked ? 0xFF5A4630 : 0x80202020);
        }
        graphics.fill(xo + 6, yo + 82, xo + 170, yo + 84, 0xFF5A4630);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        int unlocked = menu.unlockedCount(selectedBranch);
        int total = selectedBranch.nodes().size();
        graphics.text(this.font, selectedBranch.displayName() + " Branch: " + unlocked + "/" + total,
                8, 34, TEXT_COLOR);

        HeartwoodNode next = unlocked < total ? selectedBranch.nodes().get(unlocked) : null;
        if (next != null) {
            StringBuilder costText = new StringBuilder();
            for (Map.Entry<Item, Integer> entry : next.cost().entrySet()) {
                if (!costText.isEmpty()) {
                    costText.append(", ");
                }
                costText.append(entry.getValue()).append("x ").append(new ItemStack(entry.getKey()).getHoverName().getString());
            }
            graphics.text(this.font, costText.toString(), 8, 46, TEXT_COLOR);
        }
    }
}
