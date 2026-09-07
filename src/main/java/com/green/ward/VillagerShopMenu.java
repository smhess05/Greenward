package com.green.ward;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A real shop screen (user-requested, replacing the earlier hidden "hold an item and
 * sneak-click" gesture — "something more along the lines of another shop... where they
 * sell the waystones for example or you can give them the tool to repair"). Opened the
 * same way the old gesture was gated — sneak-right-click a villager while wearing a Coin
 * Purse in the offhand, see {@link VillagerShopHandler} — but now it's a visible GUI with
 * a Sell slot, a Repair slot, and a small Buy catalog, instead of an instant, undiscoverable
 * action. Vanilla trading and Commissions are still both completely untouched.
 */
public class VillagerShopMenu extends AbstractContainerMenu {
    static final int SELL_SLOT = 0;
    static final int REPAIR_SLOT = 1;
    private static final int CONTAINER_SIZE = 2;

    private static final int DATA_BALANCE = 0;
    private static final int DATA_COUNT = 1;

    static final int BUTTON_SELL = 0;
    static final int BUTTON_REPAIR = 1;
    static final int BUTTON_BUY_WAYSTONE = 2;
    static final int BUTTON_BUY_COIN_PURSE = 3;

    static final long WAYSTONE_PRICE = 300;
    static final long COIN_PURSE_PRICE = 50;

    private final Container container;
    private final ContainerData data;
    private final int playerSlotStart;

    public VillagerShopMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(CONTAINER_SIZE), new SimpleContainerData(DATA_COUNT));
    }

    public VillagerShopMenu(int containerId, Inventory inventory, ServerPlayer opener) {
        this(containerId, inventory, new SimpleContainer(CONTAINER_SIZE), liveData(opener));
    }

    private static ContainerData liveData(ServerPlayer opener) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return index == DATA_BALANCE ? (int) Math.min(Integer.MAX_VALUE, GreenwardCurrency.get(opener)) : 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    private VillagerShopMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.VILLAGER_SHOP, containerId);
        this.container = container;
        this.data = data;
        addDataSlots(data);

        addSlot(new Slot(container, SELL_SLOT, 30, 33));
        addSlot(new Slot(container, REPAIR_SLOT, 126, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.isDamageableItem();
            }
        });

        playerSlotStart = slots.size();
        addStandardInventorySlots(inventory, 8, 84);
    }

    public long balance() {
        return data.get(DATA_BALANCE);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide()) {
            return false;
        }
        return switch (id) {
            case BUTTON_SELL -> trySell(serverPlayer);
            case BUTTON_REPAIR -> tryRepair(serverPlayer);
            case BUTTON_BUY_WAYSTONE -> tryBuy(serverPlayer, Item.byBlock(ModBlocks.WAYSTONE), WAYSTONE_PRICE);
            case BUTTON_BUY_COIN_PURSE -> tryBuy(serverPlayer, ModItems.COIN_PURSE, COIN_PURSE_PRICE);
            default -> false;
        };
    }

    private boolean trySell(ServerPlayer player) {
        ItemStack stack = container.getItem(SELL_SLOT);
        long unitPrice = stack.isEmpty() ? 0 : SellPriceTable.unitPrice(stack.getItem());
        if (unitPrice <= 0) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "That doesn't sell for anything."), true);
            return false;
        }
        long total = unitPrice * stack.getCount();
        container.setItem(SELL_SLOT, ItemStack.EMPTY);
        GreenwardCurrency.add(player, total);
        return true;
    }

    private boolean tryRepair(ServerPlayer player) {
        ItemStack stack = container.getItem(REPAIR_SLOT);
        if (stack.isEmpty() || !stack.isDamageableItem() || stack.getDamageValue() <= 0) {
            return false;
        }
        long cost = stack.getDamageValue();
        if (!GreenwardCurrency.spend(player, cost)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Not enough Coins for that repair."), true);
            return false;
        }
        stack.setDamageValue(0);
        return true;
    }

    private boolean tryBuy(ServerPlayer player, Item item, long price) {
        if (!GreenwardCurrency.spend(player, price)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Not enough Coins for that."), true);
            return false;
        }
        ItemStack stack = new ItemStack(item, 1);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem();
        ItemStack clicked = original.copy();

        if (slotIndex >= playerSlotStart) {
            if (!moveItemStackTo(original, SELL_SLOT, REPAIR_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(original, playerSlotStart, slots.size(), true)) {
            return ItemStack.EMPTY;
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (original.getCount() == clicked.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, original);
        return clicked;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            // Anything left in the Sell/Repair slots on close goes back to the player,
            // same as vanilla's own crafting-grid-on-close convention.
            for (int i = 0; i < CONTAINER_SIZE; i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(stack);
                }
            }
        }
    }
}
