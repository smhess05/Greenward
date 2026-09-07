package com.green.ward;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Opens {@link VillagerShopMenu} — a real GUI (Sell slot, Repair slot, a small Buy
 * catalog) the user asked for after trying the original hidden "hold an item and
 * sneak-click" gesture and wanting something more discoverable, matching how a shop
 * should actually feel. Same trigger as before (sneak-right-click a villager while
 * wearing a Coin Purse in the offhand) — only what happens on that click changed.
 * {@link CommissionHandler} already steps aside whenever the Purse is worn, so this
 * never collides with Commissions, and vanilla trading (a plain, un-sneaked click) is
 * still never touched at all.
 */
public final class VillagerShopHandler {
    private VillagerShopHandler() {}

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_ECONOMY) {
            return;
        }
        UseEntityCallback.EVENT.register(VillagerShopHandler::onUseEntity);
    }

    private static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity target, EntityHitResult hit) {
        if (level.isClientSide() || !player.isShiftKeyDown() || !(player instanceof ServerPlayer serverPlayer)
                || !(target instanceof Villager) || !player.getOffhandItem().is(ModItems.COIN_PURSE)) {
            return InteractionResult.PASS;
        }

        serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, inventory, p) -> new VillagerShopMenu(containerId, inventory, (ServerPlayer) p),
                Component.literal("Villager Shop")));
        return InteractionResult.SUCCESS;
    }
}
