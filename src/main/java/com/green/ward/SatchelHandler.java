package com.green.ward;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The passive half of a Satchel (Material Economy Update 3 § 3.4) — the active half
 * (sneak-right-click to dump) lives on {@link SatchelItem} itself. Every {@link
 * #CHECK_INTERVAL} ticks, for each online player and each Satchel type that has one
 * somewhere in their main inventory (slots 0-35, i.e. {@link Inventory#INVENTORY_SIZE} —
 * armor/offhand deliberately excluded, there's nothing to vacuum there), pulls matching
 * tagged materials out of the rest of the inventory and into that Satchel's {@link
 * SatchelContents}, up to its live capacity.
 *
 * <p>Capacity is refreshed every pass from the player's actual Field Guide progress
 * (highest completed tier among that pillar's collections) rather than stored once at
 * craft time, so a Satchel a player already owns gets roomier automatically as they climb
 * — no separate upgrade item to craft or lose, matching the design's explicit call to
 * compute this on the fly.
 */
final class SatchelHandler {
    private static final int CHECK_INTERVAL = 20;

    private SatchelHandler() {}

    private static int tickCounter;

    static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(SatchelHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (SatchelType type : SatchelType.values()) {
                vacuumInto(player, type);
            }
        }
    }

    private static void vacuumInto(ServerPlayer player, SatchelType type) {
        Item satchelItem = type.item();
        if (satchelItem == null) {
            return;
        }

        Inventory inventory = player.getInventory();
        int satchelSlot = -1;
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            if (inventory.getItem(i).getItem() == satchelItem) {
                satchelSlot = i;
                break;
            }
        }
        if (satchelSlot < 0) {
            return;
        }

        ItemStack satchelStack = inventory.getItem(satchelSlot);
        SatchelContents contents = satchelStack.getOrDefault(GreenwardComponents.SATCHEL_CONTENTS, SatchelContents.EMPTY);
        int capacity = liveCapacity(player, type);
        if (capacity != contents.capacity()) {
            contents = contents.withCapacity(capacity);
        }

        int remaining = capacity - contents.totalCount();
        if (remaining > 0) {
            for (int i = 0; i < Inventory.INVENTORY_SIZE && remaining > 0; i++) {
                if (i == satchelSlot) {
                    continue;
                }
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty() || !BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).is(type.materialsTag())) {
                    continue;
                }
                int pulled = Math.min(stack.getCount(), remaining);
                contents = contents.withAdded(stack.getItem(), pulled);
                stack.shrink(pulled);
                remaining -= pulled;
            }
        }

        satchelStack.set(GreenwardComponents.SATCHEL_CONTENTS, contents);
    }

    private static int liveCapacity(ServerPlayer player, SatchelType type) {
        int bestTier = 0;
        for (GreenwardCollection collection : GreenwardCollection.values()) {
            if (collection.skill() != type.skill()) {
                continue;
            }
            bestTier = Math.max(bestTier, PlayerProgress.getCompletedTier(player, collection));
        }
        if (bestTier >= 10) {
            return 4096;
        }
        if (bestTier >= 8) {
            return 1024;
        }
        return SatchelContents.DEFAULT_CAPACITY;
    }
}
