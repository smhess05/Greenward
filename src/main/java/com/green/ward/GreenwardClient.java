package com.green.ward;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.InteractionResult;

public class GreenwardClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (!GreenwardConfig.ENABLE_AUTOMATION) {
            return;
        }

        MenuScreens.register(ModMenus.AUTO_HARVESTER, AutoHarvesterScreen::new);
        MenuScreens.register(ModMenus.AUTO_MINER, AutoMinerScreen::new);
        MenuScreens.register(ModMenus.AUTO_FISHER, AutoFisherScreen::new);

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide() || player.getItemInHand(hand).getItem() != ModItems.FIELD_GUIDE) {
                return InteractionResult.PASS;
            }
            Minecraft.getInstance().setScreenAndShow(new BookViewScreen(new BookViewScreen.BookAccess(FieldGuideContent.pages())));
            return InteractionResult.SUCCESS;
        });
    }
}
