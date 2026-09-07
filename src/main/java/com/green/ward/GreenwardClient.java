package com.green.ward;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.InteractionResult;

public class GreenwardClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GreenwardTooltipRenderer.register();

        if (GreenwardConfig.ENABLE_MINING_DEPTH) {
            EntityRendererRegistry.register(ModEntities.VEIN_BLAST, context -> new ThrownItemRenderer<>(context, 1.0F, false));
        }

        if (GreenwardConfig.ENABLE_HEARTWOOD) {
            MenuScreens.register(ModMenus.HEARTWOOD, HeartwoodScreen::new);
        }

        if (GreenwardConfig.ENABLE_ECONOMY) {
            MenuScreens.register(ModMenus.VILLAGER_SHOP, VillagerShopScreen::new);
        }

        if (!GreenwardConfig.ENABLE_AUTOMATION) {
            return;
        }

        MenuScreens.register(ModMenus.AUTO_HARVESTER, AutoHarvesterScreen::new);
        MenuScreens.register(ModMenus.AUTO_MINER, AutoMinerScreen::new);
        MenuScreens.register(ModMenus.AUTO_FISHER, AutoFisherScreen::new);
        if (GreenwardConfig.ENABLE_EFFIGIES) {
            MenuScreens.register(ModMenus.EFFIGY, EffigyScreen::new);
        }

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide() || player.getItemInHand(hand).getItem() != ModItems.FIELD_GUIDE) {
                return InteractionResult.PASS;
            }
            Minecraft.getInstance().setScreenAndShow(new BookViewScreen(new BookViewScreen.BookAccess(FieldGuideContent.pages())));
            return InteractionResult.SUCCESS;
        });
    }
}
