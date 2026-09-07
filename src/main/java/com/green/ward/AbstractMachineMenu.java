package com.green.ward;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Shared slot layout for all three machines: a fuel slot, one instant-consume slot per
 * upgrade axis, a fixed 45-slot output storage grid (slots beyond the current storage
 * tier are locked, checked against the synced storage-tier data slot rather than the
 * block entity directly, so the client-side "blank" menu — built from a plain Container
 * before the real contents sync in, exactly like vanilla's furnace menu — locks slots
 * correctly too), and the standard player inventory. Subclasses add any machine-specific
 * slots (the miner's ore seed slot) before calling {@link #addStorageAndPlayerSlots}.
 */
public abstract class AbstractMachineMenu extends AbstractContainerMenu {

    protected static final int STORAGE_COLS = GreenwardConfig.STORAGE_SLOTS_PER_TIER;
    protected static final int STORAGE_ROWS = GreenwardConfig.MAX_STORAGE_TIER + 1;
    protected static final int STORAGE_GRID_X = 8;
    protected static final int STORAGE_GRID_Y = 78;
    protected static final int PLAYER_INV_Y = 172;

    // Special-slot row: fuel + one slot per upgrade axis (+ the miner's seed slot),
    // spaced 36px apart so a short label fits under each one without colliding with
    // its neighbor. Shared between the menu (slot placement) and the screen (label/tier
    // text placement) so the two can never drift apart. The fuel flame + progress bar
    // get their own row below this one (STATUS_ROW_Y) rather than fighting for space in
    // this row — the miner's 5 special slots already use the full panel width.
    protected static final int SPECIAL_ROW_Y = 18;
    protected static final int STATUS_ROW_Y = 58;
    protected static final int FUEL_X = 8;
    protected static final int UPGRADE_STORAGE_X = 44;
    protected static final int UPGRADE_SPEED_X = 80;
    protected static final int UPGRADE_REGEN_X = 116;
    protected static final int SEED_X = 152;
    /** The Auto-Harvester/Auto-Fisher never had a Regen slot, so Compression reuses that
     *  column for them. The Auto-Miner already fills all of fuel/storage/speed/regen/seed,
     *  so it gets Compression as a new 6th position instead — see {@link #UPGRADE_COMPRESSION_MINER_X}. */
    protected static final int UPGRADE_COMPRESSION_X = 116;
    protected static final int UPGRADE_COMPRESSION_MINER_X = 188;
    /** Auto-Miner only — Deep Regrowth Module (Update 4 § 4.1②), an 8th special-slot
     *  position past Compression's 188. */
    protected static final int UPGRADE_DEEP_REGROWTH_MINER_X = 224;

    static final int DATA_STORAGE_TIER = 4;
    static final int DATA_SPEED_TIER = 5;
    static final int DATA_COMPRESSION_TIER = 6;

    protected final Container container;
    protected final ContainerData data;
    private int storageSlotStart = -1;
    private int storageSlotEnd = -1;
    private int playerSlotStart = -1;

    protected AbstractMachineMenu(MenuType<?> type, int containerId, Inventory inventory,
                                   Container container, ContainerData data) {
        super(type, containerId);
        this.container = container;
        this.data = data;
        this.addSlot(new Slot(container, AbstractMachineBlockEntity.FUEL_SLOT, FUEL_X, SPECIAL_ROW_Y));
        addDataSlots(data);
    }

    protected void addUpgradeSlot(int x, int y, Item requiredItem, Runnable onUpgrade) {
        addUpgradeSlot(x, y, stack -> stack.is(requiredItem), stack -> onUpgrade.run());
    }

    /** For an upgrade axis with more than one accepted item at different target tiers
     *  (Press vs. Deep Press) — {@code onUpgrade} receives the actual item inserted so the
     *  caller can decide which target tier it maps to. */
    protected void addUpgradeSlot(int x, int y, java.util.function.Predicate<ItemStack> accepts,
                                   java.util.function.Consumer<ItemStack> onUpgrade) {
        Container instant = new InstantConsumeContainer(accepts, onUpgrade);
        addSlot(new Slot(instant, 0, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return accepts.test(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    protected void addStorageAndPlayerSlots(Inventory inventory) {
        storageSlotStart = slots.size();
        for (int row = 0; row < STORAGE_ROWS; row++) {
            for (int col = 0; col < STORAGE_COLS; col++) {
                int index = AbstractMachineBlockEntity.FIRST_STORAGE_SLOT + row * STORAGE_COLS + col;
                addSlot(new StorageSlot(container, data, index, STORAGE_GRID_X + col * 18, STORAGE_GRID_Y + row * 18));
            }
        }
        storageSlotEnd = slots.size();

        playerSlotStart = slots.size();
        addStandardInventorySlots(inventory, 8, PLAYER_INV_Y);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
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
            // From player inventory: prefer fuel, then any unlocked storage slot.
            if (AutomationFuel.isFuel(original.getItem()) && moveItemStackTo(original, 0, 1, false)) {
                // moved into fuel
            } else if (!moveItemStackTo(original, storageSlotStart, storageSlotEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From fuel/storage/upgrade/seed slots: move to player inventory.
            if (!moveItemStackTo(original, playerSlotStart, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
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

    /** A storage-grid slot that refuses interaction once its index is beyond the current storage tier. */
    private static class StorageSlot extends Slot {
        private final ContainerData data;
        private final int storageIndex;

        StorageSlot(Container container, ContainerData data, int containerSlot, int x, int y) {
            super(container, containerSlot, x, y);
            this.data = data;
            this.storageIndex = containerSlot - AbstractMachineBlockEntity.FIRST_STORAGE_SLOT;
        }

        private boolean unlocked() {
            int unlockedSlots = GreenwardConfig.STORAGE_SLOTS_PER_TIER * (data.get(DATA_STORAGE_TIER) + 1);
            return storageIndex < unlockedSlots;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return unlocked();
        }

        @Override
        public boolean mayPickup(Player player) {
            return unlocked();
        }
    }

    /** Backing "container" for an upgrade slot: never actually holds anything — inserting
     *  an accepted item instantly applies the upgrade and the slot reports empty again. */
    private static class InstantConsumeContainer implements Container {
        private final java.util.function.Predicate<ItemStack> accepts;
        private final java.util.function.Consumer<ItemStack> onUpgrade;

        InstantConsumeContainer(java.util.function.Predicate<ItemStack> accepts,
                                 java.util.function.Consumer<ItemStack> onUpgrade) {
            this.accepts = accepts;
            this.onUpgrade = onUpgrade;
        }

        @Override public int getContainerSize() { return 1; }
        @Override public boolean isEmpty() { return true; }
        @Override public ItemStack getItem(int slot) { return ItemStack.EMPTY; }
        @Override public ItemStack removeItem(int slot, int count) { return ItemStack.EMPTY; }
        @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (!stack.isEmpty() && accepts.test(stack)) {
                onUpgrade.accept(stack);
            }
        }

        @Override public void setChanged() { }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public void clearContent() { }
    }
}
