package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/**
 * Shared ticking/fuel/storage machinery for the automation blocks. Subclasses implement
 * only the work interval and what one operation actually does; fuel accounting, hopper
 * insertion, output storage, upgrade tiers, NBT persistence, the "lit" blockstate, and
 * GUI data sync are handled here.
 *
 * Container layout: slot 0 is fuel, slots 1..MAX_STORAGE_SLOTS are output storage. Storage
 * is always allocated at full size; the current storage tier only controls how many of
 * those slots are "unlocked" (see {@link #unlockedStorageSlots()}) — this keeps the slot
 * COUNT identical between client and server at all times, so upgrading storage never
 * needs to renegotiate the menu's slot layout over the network.
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int FUEL_SLOT = 0;
    public static final int FIRST_STORAGE_SLOT = 1;

    /** Repurposed for the duration-boost model (Update 4 § 4.2): the flame indicator now
     *  shows how much of the CURRENT boost's duration remains, not an operation budget —
     *  same visual meaning ("how much charge is left"), different underlying quantity. */
    private static final int DATA_OPERATIONS_REMAINING = 0;
    private static final int DATA_BURN_MAX = 1;
    private static final int DATA_PROGRESS = 2;
    private static final int DATA_PROGRESS_MAX = 3;
    private static final int DATA_STORAGE_TIER = 4;
    private static final int DATA_SPEED_TIER = 5;
    private static final int DATA_COMPRESSION_TIER = 6;
    protected static final int BASE_DATA_COUNT = 7;

    private static final String TAG_TICKS_UNTIL_NEXT_OP = "ticks_until_next_op";
    private static final String TAG_STORAGE_TIER = "storage_tier";
    private static final String TAG_SPEED_TIER = "speed_tier";
    private static final String TAG_COMPRESSION_TIER = "compression_tier";
    private static final String TAG_BOOST_PERCENT = "boost_percent";
    private static final String TAG_BOOST_TICKS_REMAINING = "boost_ticks_remaining";
    private static final String TAG_BOOST_DURATION_MAX = "boost_duration_max";
    private static final String TAG_SUNWHEEL_INSTALLED = "sunwheel_installed";

    /** 0 = raw output, 1 = Press installed (auto-compress to rung 1), 2 = Deep Press
     *  installed (auto-compress to rung 2). Design Program Update 3 § 3.3. */
    public static final int MAX_COMPRESSION_TIER = 2;

    private final NonNullList<ItemStack> fuel = NonNullList.withSize(1, ItemStack.EMPTY);
    private final NonNullList<ItemStack> storage = NonNullList.withSize(GreenwardConfig.MAX_STORAGE_SLOTS, ItemStack.EMPTY);
    private int ticksUntilNextOp;
    private int storageTier;
    private int speedTier;
    private int compressionTier;

    // --- Update 4 § 4.2 fuel-as-boost fields ---
    private int boostPercent;
    private int boostTicksRemaining;
    private int boostDurationMax;
    private boolean sunwheelInstalled;
    /** Recomputed once per tick in {@link #tick} (needs the {@code ServerLevel} for the
     *  Sunwheel's daylight check) and read by {@link #tickInterval()}. */
    private int cachedEffectivePercent;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return AbstractMachineBlockEntity.this.getData(index);
        }

        @Override
        public void set(int index, int value) {
            AbstractMachineBlockEntity.this.setData(index, value);
        }

        @Override
        public int getCount() {
            return AbstractMachineBlockEntity.this.dataCount();
        }
    };

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Centralizes the removal side of {@link MachinePlacementGuard}'s placement caps
     *  (Update 4 § 4.1③) for all three automation block types in one place, rather than
     *  needing a matching override in each Block subclass — this fires exactly once
     *  whenever a placed automation block leaves the world, by break, explosion, or
     *  {@code /greenward decommission}. */
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            MachinePlacementGuard.onRemoved(serverLevel, worldPosition);
        }
    }

    /** Base operation interval (in ticks) for each speed tier, index 0..MAX_SPEED_TIER. */
    protected abstract int[] tickIntervalsByTier();

    /** @return true if work actually happened. Called every operation interval
     *  regardless of fuel — Update 4 § 4.2 removed the old "must have fuel to run at
     *  all" gate. Fuel is now purely a speed boost on top of the base rate. */
    protected abstract boolean doOperation(ServerLevel level, BlockPos pos, BlockState state);

    /** Called every single game tick (not gated by the operation interval); default no-op. */
    protected void tickEveryTick(ServerLevel level, BlockPos pos, BlockState state) {
    }

    private int tickInterval() {
        int[] intervals = tickIntervalsByTier();
        int base = intervals[Math.min(speedTier, intervals.length - 1)];
        if (cachedEffectivePercent <= 0) {
            return base;
        }
        return Math.max(1, Math.round(base / (1.0F + cachedEffectivePercent / 100.0F)));
    }

    private static boolean isDaytime(ServerLevel level) {
        long timeOfDay = level.getOverworldClockTime() % 24000L;
        return timeOfDay < 12000L;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractMachineBlockEntity be) {
        ServerLevel serverLevel = (ServerLevel) level;
        boolean wasLit = state.getValue(BlockStateProperties.LIT);

        be.tickEveryTick(serverLevel, pos, state);

        if (be.boostTicksRemaining > 0) {
            be.boostTicksRemaining--;
            if (be.boostTicksRemaining == 0) {
                be.boostPercent = 0;
                be.boostDurationMax = 0;
            }
        }
        int sunwheelBonus = be.sunwheelInstalled && isDaytime(serverLevel) ? AutomationFuel.boostPercent(ModItems.SUNWHEEL) : 0;
        be.cachedEffectivePercent = Math.max(be.boostPercent, sunwheelBonus);

        if (be.ticksUntilNextOp > 0) {
            be.ticksUntilNextOp--;
        } else {
            be.ticksUntilNextOp = be.tickInterval();

            be.tryConsumeFuel();

            if (!be.isStorageFull() && be.doOperation(serverLevel, pos, state)) {
                be.setChanged();
            }
        }

        boolean isLit = be.cachedEffectivePercent > 0;
        if (isLit != wasLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }
    }

    /** Consumes exactly one fuel item when doing so would actually help: the Sunwheel
     *  installs once (idempotent, permanent); any other fuel tops up only when the
     *  current boost has expired or the new item beats the active percentage — otherwise
     *  it's left untouched in the slot rather than being wastefully eaten every interval. */
    private void tryConsumeFuel() {
        ItemStack stack = fuel.get(0);
        if (stack.isEmpty()) {
            return;
        }
        if (AutomationFuel.isSunwheel(stack.getItem())) {
            if (!sunwheelInstalled) {
                sunwheelInstalled = true;
                stack.shrink(1);
                setChanged();
            }
            return;
        }

        int newPercent = AutomationFuel.boostPercent(stack.getItem());
        if (newPercent <= 0) {
            return;
        }
        if (boostTicksRemaining > 0 && newPercent <= boostPercent) {
            return;
        }

        int newDuration = AutomationFuel.boostDuration(stack.getItem());
        if (boostTicksRemaining <= 0) {
            boostPercent = newPercent;
            boostTicksRemaining = newDuration;
            boostDurationMax = newDuration;
        } else {
            boostPercent = Math.max(boostPercent, newPercent);
            boostTicksRemaining += newDuration;
            boostDurationMax += newDuration;
        }
        stack.shrink(1);
        setChanged();
    }

    // --- Storage output ---

    public int unlockedStorageSlots() {
        return GreenwardConfig.STORAGE_SLOTS_PER_TIER * (storageTier + 1);
    }

    protected boolean isStorageFull() {
        int unlocked = unlockedStorageSlots();
        for (int i = 0; i < unlocked; i++) {
            ItemStack stack = storage.get(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    /** Deposits each stack into the first available unlocked storage slot, merging where possible.
     *  Anything that doesn't fit overflows as a dropped item above {@code pos} — items are never lost. */
    protected void depositOrDrop(ServerLevel level, BlockPos pos, List<ItemStack> drops) {
        if (compressionTier > 0) {
            drops = applyCompression(drops);
        }

        int unlocked = unlockedStorageSlots();
        for (ItemStack drop : drops) {
            ItemStack remaining = drop.copy();
            for (int i = 0; i < unlocked && !remaining.isEmpty(); i++) {
                ItemStack slotStack = storage.get(i);
                if (slotStack.isEmpty()) {
                    int move = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                    storage.set(i, remaining.copyWithCount(move));
                    remaining.shrink(move);
                } else if (ItemStack.isSameItemSameComponents(slotStack, remaining)) {
                    int room = slotStack.getMaxStackSize() - slotStack.getCount();
                    int move = Math.min(room, remaining.getCount());
                    if (move > 0) {
                        slotStack.grow(move);
                        remaining.shrink(move);
                    }
                }
            }
            if (!remaining.isEmpty()) {
                Block.popResource(level, pos.above(), remaining);
            }
        }
        setChanged();
    }

    /**
     * Press/Deep Press (Design Program Update 3 § 3.3). For each drop that's a raw
     * collection item with a known chain, folds it together with whatever raw (and, at
     * tier 2, rung-1) stock is already sitting in this machine's storage, and re-expresses
     * the combined total as the fewest possible items at the machine's compression tier —
     * e.g. 47 raw wheat already in storage plus 9 more freshly harvested becomes 6 Wheat
     * Sheaf + 2 raw wheat, not 56 raw wheat sitting there uncompressed. Anything without a
     * known chain (most ores, fish, mob drops) passes through completely untouched.
     */
    private List<ItemStack> applyCompression(List<ItemStack> drops) {
        List<ItemStack> result = new java.util.ArrayList<>();
        for (ItemStack drop : drops) {
            GreenwardCollection collection = CompressionLadder.collectionForRawItem(drop.getItem());
            net.minecraft.world.item.Item rung1 = collection == null ? null : CompressionLadder.rung1For(collection);
            if (collection == null || rung1 == null) {
                result.add(drop);
                continue;
            }

            long rawTotal = removeAllFromStorage(drop.getItem()) + drop.getCount();
            long rung1Count = rawTotal / 9;
            long rawRemainder = rawTotal % 9;

            net.minecraft.world.item.Item rung2 = compressionTier >= 2 ? CompressionLadder.rung2For(collection) : null;
            if (rung2 != null) {
                long rung1Total = removeAllFromStorage(rung1) + rung1Count;
                long rung2Count = rung1Total / 9;
                long rung1Remainder = rung1Total % 9;
                addAsStacks(result, rung2, rung2Count);
                addAsStacks(result, rung1, rung1Remainder);
            } else {
                addAsStacks(result, rung1, rung1Count);
            }
            addAsStacks(result, drop.getItem(), rawRemainder);
        }
        return result;
    }

    /** Removes every stack of {@code item} from storage (so it can be re-expressed as a
     *  compressed total) and returns how many were removed. */
    private long removeAllFromStorage(net.minecraft.world.item.Item item) {
        long total = 0;
        for (int i = 0; i < storage.size(); i++) {
            ItemStack stack = storage.get(i);
            if (!stack.isEmpty() && stack.is(item)) {
                total += stack.getCount();
                storage.set(i, ItemStack.EMPTY);
            }
        }
        return total;
    }

    private static void addAsStacks(List<ItemStack> result, net.minecraft.world.item.Item item, long count) {
        if (item == null || count <= 0) {
            return;
        }
        int maxStack = new ItemStack(item).getMaxStackSize();
        while (count > 0) {
            int chunk = (int) Math.min(count, maxStack);
            result.add(new ItemStack(item, chunk));
            count -= chunk;
        }
    }

    // --- Upgrades ---

    public int storageTier() {
        return storageTier;
    }

    public int speedTier() {
        return speedTier;
    }

    public boolean increaseStorageTier() {
        if (storageTier >= GreenwardConfig.MAX_STORAGE_TIER) {
            return false;
        }
        storageTier++;
        setChanged();
        return true;
    }

    public boolean increaseSpeedTier() {
        if (speedTier >= GreenwardConfig.MAX_SPEED_TIER) {
            return false;
        }
        speedTier++;
        setChanged();
        return true;
    }

    public int compressionTier() {
        return compressionTier;
    }

    /** Press sets target 1, Deep Press sets target 2 — dropping either only ever raises
     *  the tier, and Deep Press can be applied directly without Press first. */
    public boolean setCompressionTier(int target) {
        if (target <= compressionTier || target > MAX_COMPRESSION_TIER) {
            return false;
        }
        compressionTier = target;
        setChanged();
        return true;
    }

    // --- GUI data sync (see subclasses for any additional indices beyond BASE_DATA_COUNT) ---

    protected int dataCount() {
        return BASE_DATA_COUNT;
    }

    protected int getData(int index) {
        switch (index) {
            case DATA_OPERATIONS_REMAINING: return boostTicksRemaining;
            case DATA_BURN_MAX: return boostDurationMax;
            case DATA_PROGRESS: return tickInterval() - ticksUntilNextOp;
            case DATA_PROGRESS_MAX: return tickInterval();
            case DATA_STORAGE_TIER: return storageTier;
            case DATA_SPEED_TIER: return speedTier;
            case DATA_COMPRESSION_TIER: return compressionTier;
            default: return 0;
        }
    }

    protected void setData(int index, int value) {
        // The server-side block entity is authoritative; this exists only to satisfy
        // ContainerData's interface (the client's mirrored copy is what actually gets set).
    }

    public ContainerData containerData() {
        return containerData;
    }

    // --- MenuProvider ---

    @Override
    public boolean shouldCloseCurrentScreen() {
        return true;
    }

    // --- NBT persistence ---

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        NonNullList<ItemStack> loaded = NonNullList.withSize(1 + GreenwardConfig.MAX_STORAGE_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, loaded);
        fuel.set(0, loaded.get(0));
        for (int i = 0; i < storage.size(); i++) {
            storage.set(i, loaded.get(1 + i));
        }
        ticksUntilNextOp = input.getIntOr(TAG_TICKS_UNTIL_NEXT_OP, 0);
        storageTier = Math.min(input.getIntOr(TAG_STORAGE_TIER, 0), GreenwardConfig.MAX_STORAGE_TIER);
        speedTier = Math.min(input.getIntOr(TAG_SPEED_TIER, 0), GreenwardConfig.MAX_SPEED_TIER);
        compressionTier = Math.min(input.getIntOr(TAG_COMPRESSION_TIER, 0), MAX_COMPRESSION_TIER);
        boostPercent = input.getIntOr(TAG_BOOST_PERCENT, 0);
        boostTicksRemaining = input.getIntOr(TAG_BOOST_TICKS_REMAINING, 0);
        boostDurationMax = input.getIntOr(TAG_BOOST_DURATION_MAX, 0);
        sunwheelInstalled = input.getBooleanOr(TAG_SUNWHEEL_INSTALLED, false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        NonNullList<ItemStack> combined = NonNullList.withSize(1 + GreenwardConfig.MAX_STORAGE_SLOTS, ItemStack.EMPTY);
        combined.set(0, fuel.get(0));
        for (int i = 0; i < storage.size(); i++) {
            combined.set(1 + i, storage.get(i));
        }
        ContainerHelper.saveAllItems(output, combined);
        output.putInt(TAG_TICKS_UNTIL_NEXT_OP, ticksUntilNextOp);
        output.putInt(TAG_STORAGE_TIER, storageTier);
        output.putInt(TAG_SPEED_TIER, speedTier);
        output.putInt(TAG_COMPRESSION_TIER, compressionTier);
        output.putInt(TAG_BOOST_PERCENT, boostPercent);
        output.putInt(TAG_BOOST_TICKS_REMAINING, boostTicksRemaining);
        output.putInt(TAG_BOOST_DURATION_MAX, boostDurationMax);
        output.putBoolean(TAG_SUNWHEEL_INSTALLED, sunwheelInstalled);
    }

    // --- WorldlyContainer: hoppers may push fuel into slot 0 and pull finished goods
    //     out of storage slots, but never pull fuel back out. ---

    @Override
    public int[] getSlotsForFace(Direction direction) {
        int[] slots = new int[getContainerSize()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == FUEL_SLOT && AutomationFuel.isFuel(stack.getItem());
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot != FUEL_SLOT;
    }

    @Override
    public int getContainerSize() {
        return 1 + GreenwardConfig.MAX_STORAGE_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        if (!fuel.get(0).isEmpty()) {
            return false;
        }
        for (ItemStack stack : storage) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == FUEL_SLOT ? fuel.get(0) : storage.get(slot - FIRST_STORAGE_SLOT);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        NonNullList<ItemStack> list = slot == FUEL_SLOT ? fuel : storage;
        int index = slot == FUEL_SLOT ? 0 : slot - FIRST_STORAGE_SLOT;
        ItemStack result = ContainerHelper.removeItem(list, index, count);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> list = slot == FUEL_SLOT ? fuel : storage;
        int index = slot == FUEL_SLOT ? 0 : slot - FIRST_STORAGE_SLOT;
        return ContainerHelper.takeItem(list, index);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == FUEL_SLOT) {
            fuel.set(0, stack);
        } else {
            storage.set(slot - FIRST_STORAGE_SLOT, stack);
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        fuel.set(0, ItemStack.EMPTY);
        for (int i = 0; i < storage.size(); i++) {
            storage.set(i, ItemStack.EMPTY);
        }
    }
}
