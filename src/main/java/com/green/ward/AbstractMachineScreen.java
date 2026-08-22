package com.green.ward;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Shared background/fuel-bar/progress-bar/slot-label rendering for all three machine
 * screens. The fuel flame and progress-arrow sprites are the real vanilla furnace
 * sprites, reused as-is; the panel itself is one of Greenward's own placeholder
 * textures — subclasses pass the variant that matches how many special slots
 * (fuel + upgrades [+ seed]) they actually add, so the background never shows a
 * slot-shaped box that isn't backed by a real, clickable Slot. Every special slot also
 * gets a short name printed under it (and, where it has one, a "tier n/max" readout)
 * so it's never ambiguous which slot wants fuel vs. which upgrade.
 */
@Environment(EnvType.CLIENT)
public abstract class AbstractMachineScreen<T extends AbstractMachineMenu> extends AbstractContainerScreen<T> {

    /** One labeled special slot. Pass {@code tierDataIndex = -1} (and any maxTier) for a slot with no tier (fuel/seed). */
    protected record SlotLabel(int x, String name, int tierDataIndex, int maxTier) {
    }

    private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier BURN_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int LOCKED_OVERLAY_COLOR = 0xA0202020;
    private static final int LABEL_COLOR = 0x404040;

    private final Identifier texture;
    private final List<SlotLabel> slotLabels;

    protected AbstractMachineScreen(T menu, Inventory inventory, Component title, Identifier texture, List<SlotLabel> slotLabels) {
        super(menu, inventory, title, 176, 256);
        this.texture = texture;
        this.slotLabels = slotLabels;
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int xo = this.leftPos;
        int yo = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        int operationsRemaining = this.menu.data.get(0);
        int burnMax = this.menu.data.get(1);
        if (operationsRemaining > 0 && burnMax > 0) {
            int litHeight = Mth.ceil((float) operationsRemaining / burnMax * 13.0F) + 1;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - litHeight,
                    xo + 8, yo + AbstractMachineMenu.STATUS_ROW_Y + 14 - litHeight, 14, litHeight);
        }
        int progress = this.menu.data.get(2);
        int progressMax = this.menu.data.get(3);
        int barWidth = progressMax > 0 ? Mth.ceil((float) progress / progressMax * 24.0F) : 0;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_SPRITE, 24, 16, 0, 0,
                xo + 90, yo + AbstractMachineMenu.STATUS_ROW_Y, barWidth, 16);
        graphics.text(this.font, "Working", xo + 118, yo + AbstractMachineMenu.STATUS_ROW_Y + 3, LABEL_COLOR);

        for (SlotLabel label : this.slotLabels) {
            int centerX = xo + label.x() + 9;
            int nameY = yo + AbstractMachineMenu.SPECIAL_ROW_Y + 20;
            graphics.text(this.font, label.name(), centerX - this.font.width(label.name()) / 2, nameY, LABEL_COLOR);
            if (label.tierDataIndex() >= 0) {
                int tier = this.menu.data.get(label.tierDataIndex());
                String tierText = (tier + 1) + "/" + (label.maxTier() + 1);
                graphics.text(this.font, tierText, centerX - this.font.width(tierText) / 2, nameY + 9, LABEL_COLOR);
            }
        }

        // Darken storage slots beyond the current storage tier so "locked" actually looks locked.
        int storageTier = this.menu.data.get(AbstractMachineMenu.DATA_STORAGE_TIER);
        int unlockedSlots = GreenwardConfig.STORAGE_SLOTS_PER_TIER * (storageTier + 1);
        for (int row = 0; row < AbstractMachineMenu.STORAGE_ROWS; row++) {
            for (int col = 0; col < AbstractMachineMenu.STORAGE_COLS; col++) {
                int index = row * AbstractMachineMenu.STORAGE_COLS + col;
                if (index >= unlockedSlots) {
                    int sx = xo + AbstractMachineMenu.STORAGE_GRID_X + col * 18;
                    int sy = yo + AbstractMachineMenu.STORAGE_GRID_Y + row * 18;
                    graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, LOCKED_OVERLAY_COLOR);
                }
            }
        }
    }
}
