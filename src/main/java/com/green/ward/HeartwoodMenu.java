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

import java.util.Map;

/**
 * The Heartwood's screen (Design Program Update 6): a real {@code Slot} grid for
 * socketing talismans (up to {@link HeartwoodData#totalSockets()}, backed by a
 * {@link SimpleContainer} subclass that mirrors into {@link HeartwoodData} on every
 * change), plus 8 branch-action buttons routed through vanilla's own generic
 * {@code clickMenuButton} mechanism (the same one the enchanting table/loom/stonecutter
 * use) — unlocking a node and respeccing a branch are state-changing actions that need
 * server authority, unlike which branch tab is showing, which is pure client UI state
 * the {@link HeartwoodScreen} tracks locally.
 */
public class HeartwoodMenu extends AbstractContainerMenu {

    public static final int MAX_SOCKETS = 12;
    static final int TALISMAN_SLOT_X = 8;
    static final int TALISMAN_SLOT_Y = 18;

    private static final int DATA_ROOT_UNLOCKED = 0;
    private static final int DATA_STONE_UNLOCKED = 1;
    private static final int DATA_TIDE_UNLOCKED = 2;
    private static final int DATA_ASH_UNLOCKED = 3;
    private static final int DATA_TOTAL_SOCKETS = 4;
    private static final int DATA_THREAT_ENABLED = 5;
    private static final int DATA_COUNT = 6;

    /** One past the last branch button id (4 branches x 2 buttons each) — the Threat
     *  toggle (user-requested, post-Design-Program) piggybacks on the same generic
     *  clickMenuButton mechanism rather than adding a whole new interaction path. */
    static final int TOGGLE_THREAT_BUTTON_ID = HeartwoodBranch.values().length * 2;

    final ContainerData data;
    private final int playerSlotStart;

    public HeartwoodMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new TalismanContainer(null), new SimpleContainerData(DATA_COUNT));
    }

    public HeartwoodMenu(int containerId, Inventory inventory, ServerPlayer opener) {
        this(containerId, inventory, new TalismanContainer(HeartwoodData.get(((net.minecraft.server.level.ServerLevel) opener.level()).getServer())), liveData(opener));
    }

    private static ContainerData liveData(ServerPlayer opener) {
        HeartwoodData data = HeartwoodData.get(((net.minecraft.server.level.ServerLevel) opener.level()).getServer());
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_ROOT_UNLOCKED -> data.unlockedCount(HeartwoodBranch.ROOT);
                    case DATA_STONE_UNLOCKED -> data.unlockedCount(HeartwoodBranch.STONE);
                    case DATA_TIDE_UNLOCKED -> data.unlockedCount(HeartwoodBranch.TIDE);
                    case DATA_ASH_UNLOCKED -> data.unlockedCount(HeartwoodBranch.ASH);
                    case DATA_TOTAL_SOCKETS -> data.totalSockets();
                    case DATA_THREAT_ENABLED -> data.isThreatEnabled() ? 1 : 0;
                    default -> 0;
                };
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

    private HeartwoodMenu(int containerId, Inventory inventory, Container talismanContainer, ContainerData data) {
        super(ModMenus.HEARTWOOD, containerId);
        this.data = data;
        addDataSlots(data);

        for (int i = 0; i < MAX_SOCKETS; i++) {
            int col = i % 6;
            int row = i / 6;
            addSlot(new Slot(talismanContainer, i, TALISMAN_SLOT_X + col * 18, TALISMAN_SLOT_Y + row * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return getContainerSlot() < HeartwoodMenu.this.data.get(DATA_TOTAL_SOCKETS)
                            && TalismanType.isTalismanOrSigil(stack.getItem());
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        playerSlotStart = slots.size();
        addStandardInventorySlots(inventory, 8, 86);
    }

    public int unlockedCount(HeartwoodBranch branch) {
        return switch (branch) {
            case ROOT -> data.get(DATA_ROOT_UNLOCKED);
            case STONE -> data.get(DATA_STONE_UNLOCKED);
            case TIDE -> data.get(DATA_TIDE_UNLOCKED);
            case ASH -> data.get(DATA_ASH_UNLOCKED);
        };
    }

    public int totalSockets() {
        return data.get(DATA_TOTAL_SOCKETS);
    }

    public boolean threatEnabled() {
        return data.get(DATA_THREAT_ENABLED) != 0;
    }

    /** Button ids for {@link #clickMenuButton} — 2 per branch (unlock, respec), in
     *  {@link HeartwoodBranch} enum order. */
    private static int unlockButtonId(HeartwoodBranch branch) {
        return branch.ordinal() * 2;
    }

    private static int respecButtonId(HeartwoodBranch branch) {
        return branch.ordinal() * 2 + 1;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide()) {
            return false;
        }
        if (id == TOGGLE_THREAT_BUTTON_ID) {
            return toggleThreat(serverPlayer);
        }
        for (HeartwoodBranch branch : HeartwoodBranch.values()) {
            if (id == unlockButtonId(branch)) {
                return tryUnlock(serverPlayer, branch);
            }
            if (id == respecButtonId(branch)) {
                return tryRespec(serverPlayer, branch);
            }
        }
        return false;
    }

    private boolean toggleThreat(ServerPlayer player) {
        HeartwoodData heartwood = HeartwoodData.get(((net.minecraft.server.level.ServerLevel) player.level()).getServer());
        boolean next = !heartwood.isThreatEnabled();
        heartwood.setThreatEnabled(next);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "World Threat " + (next ? "enabled — mobs will now scale with your progress." : "disabled — mobs are back to vanilla difficulty.")), true);
        return true;
    }

    private boolean tryUnlock(ServerPlayer player, HeartwoodBranch branch) {
        HeartwoodData heartwood = HeartwoodData.get(((net.minecraft.server.level.ServerLevel) player.level()).getServer());
        HeartwoodNode next = heartwood.nextNode(branch);
        if (next == null) {
            return false;
        }
        for (Map.Entry<Item, Integer> entry : next.cost().entrySet()) {
            if (!hasEnough(player, entry.getKey(), entry.getValue())) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Not enough " + new ItemStack(entry.getKey()).getHoverName().getString() + "."), true);
                return false;
            }
        }
        next.cost().forEach((item, count) -> removeFromInventory(player, item, count));
        heartwood.unlockNext(branch);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Unlocked: " + next.displayName()), true);
        return true;
    }

    private boolean tryRespec(ServerPlayer player, HeartwoodBranch branch) {
        HeartwoodData heartwood = HeartwoodData.get(((net.minecraft.server.level.ServerLevel) player.level()).getServer());
        if (heartwood.unlockedCount(branch) == 0) {
            return false;
        }
        Map<Item, Integer> refund = heartwood.respec(branch);
        refund.forEach((item, count) -> {
            ItemStack stack = new ItemStack(item, count);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        });
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                branch.displayName() + " branch respecced — 75% refunded."), true);
        return true;
    }

    private static boolean hasEnough(Player player, Item item, int count) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total >= count;
    }

    private static void removeFromInventory(Player player, Item item, int count) {
        int remaining = count;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (remaining <= 0) {
                break;
            }
            if (stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
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
            if (!TalismanType.isTalismanOrSigil(original.getItem()) || !moveItemStackTo(original, 0, MAX_SOCKETS, false)) {
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

    /** Mirrors its contents into {@link HeartwoodData} on every change — the container is
     *  the interaction surface, the data is the source of truth that survives menu close. */
    private static class TalismanContainer extends SimpleContainer {
        private final HeartwoodData heartwood;

        TalismanContainer(HeartwoodData heartwood) {
            super(MAX_SOCKETS);
            this.heartwood = heartwood;
            if (heartwood != null) {
                java.util.List<Item> existing = heartwood.socketedTalismans();
                for (int i = 0; i < existing.size() && i < MAX_SOCKETS; i++) {
                    setItem(i, new ItemStack(existing.get(i), 1));
                }
            }
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (heartwood == null) {
                return;
            }
            java.util.List<Item> current = new java.util.ArrayList<>();
            for (ItemStack stack : getItems()) {
                if (!stack.isEmpty()) {
                    current.add(stack.getItem());
                }
            }
            heartwood.setSocketedTalismans(current);
        }
    }
}
