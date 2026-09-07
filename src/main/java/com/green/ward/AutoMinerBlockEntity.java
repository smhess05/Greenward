package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;

import java.util.List;

public class AutoMinerBlockEntity extends AbstractMachineBlockEntity {

    /** Seed-slot acceptance only — a tag-of-tags union of common+precious. Ancient Debris
     *  is deliberately in neither (Design Program Update 4 § 4.1① — Netherite stays
     *  manual; an infinite tap on a tier-4 regen + tier-4 speed module would trivialize
     *  the Deepstone Core and the God Potion behind it). */
    public static final TagKey<Block> REGENERABLE_ORES =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "regenerable_ores"));
    public static final TagKey<Block> REGENERABLE_COMMON =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "regenerable_common"));
    public static final TagKey<Block> REGENERABLE_PRECIOUS =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "regenerable_precious"));

    private static final int[] TICK_INTERVALS_BY_TIER = {60, 48, 36, 24, 15};
    private static final int[] REGEN_TICKS_BY_TIER = {200, 150, 100, 60, 30};
    /** Precious-ore regrowth is 5× the common delay at every tier (§ 4.1②), and only
     *  runs at all with a Deep Regrowth Module installed — see {@link #hasDeepRegrowthModule}. */
    private static final int PRECIOUS_REGEN_MULTIPLIER = 5;

    private static final int DATA_REGEN_TICKS = BASE_DATA_COUNT;
    private static final int DATA_REGEN_MAX = BASE_DATA_COUNT + 1;
    static final int DATA_REGEN_TIER = BASE_DATA_COUNT + 2;
    static final int DATA_DEEP_REGROWTH = BASE_DATA_COUNT + 3;
    static final int DATA_COUNT = BASE_DATA_COUNT + 4;

    private static final String TAG_REGEN_BLOCK = "regen_block";
    private static final String TAG_REGEN_TICKS = "regen_ticks_remaining";
    private static final String TAG_REGEN_TIER = "regen_tier";
    private static final String TAG_DEEP_REGROWTH = "deep_regrowth_module";
    private static final String TAG_SEED = "seed";

    private final NonNullList<ItemStack> seed = NonNullList.withSize(1, ItemStack.EMPTY);
    private final Container seedContainer = new Container() {
        @Override public int getContainerSize() { return 1; }
        @Override public boolean isEmpty() { return seed.get(0).isEmpty(); }
        @Override public ItemStack getItem(int slot) { return seed.get(0); }
        @Override public ItemStack removeItem(int slot, int count) {
            ItemStack result = ContainerHelper.removeItem(seed, 0, count);
            if (!result.isEmpty()) setChanged();
            return result;
        }
        @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(seed, 0); }
        @Override public void setItem(int slot, ItemStack stack) { seed.set(0, stack); setChanged(); }
        @Override public void setChanged() { AutoMinerBlockEntity.this.setChanged(); }
        @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(AutoMinerBlockEntity.this, player); }
        @Override public void clearContent() { seed.set(0, ItemStack.EMPTY); }
    };

    private Block regenBlock;
    private int regenTicksRemaining;
    private int regenTier;
    private boolean hasDeepRegrowthModule;

    public AutoMinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTO_MINER, pos, state);
    }

    public Container seedContainer() {
        return seedContainer;
    }

    public boolean increaseRegenTier() {
        if (regenTier >= GreenwardConfig.MAX_REGEN_TIER) {
            return false;
        }
        regenTier++;
        setChanged();
        return true;
    }

    public int regenTier() {
        return regenTier;
    }

    /** One-shot, like Compression's install — no tiers, just installed or not. */
    public boolean installDeepRegrowthModule() {
        if (hasDeepRegrowthModule) {
            return false;
        }
        hasDeepRegrowthModule = true;
        setChanged();
        return true;
    }

    public boolean hasDeepRegrowthModule() {
        return hasDeepRegrowthModule;
    }

    @Override
    protected int[] tickIntervalsByTier() {
        return TICK_INTERVALS_BY_TIER;
    }

    @Override
    protected void tickEveryTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (regenTicksRemaining <= 0 || regenBlock == null) {
            return;
        }
        regenTicksRemaining--;
        if (regenTicksRemaining == 0) {
            BlockPos targetPos = pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
            if (level.getBlockState(targetPos).isAir()) {
                level.setBlockAndUpdate(targetPos, regenBlock.defaultBlockState());
            }
            regenBlock = null;
        }
    }

    @Override
    protected boolean doOperation(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockState targetState = level.getBlockState(targetPos);

        // Bootstrap: if the spot is empty and a seed ore is loaded, plant a copy of it.
        // It gets mined (and regen scheduled) on a following operation once it's there.
        if (targetState.isAir() && !seed.get(0).isEmpty() && regenBlock == null
                && seed.get(0).getItem() instanceof BlockItem seedBlockItem) {
            level.setBlockAndUpdate(targetPos, seedBlockItem.getBlock().defaultBlockState());
            return true;
        }

        if (!isMinable(level, targetPos, targetState)) {
            return false;
        }

        boolean isCommonOre = targetState.is(REGENERABLE_COMMON);
        boolean isPreciousOre = targetState.is(REGENERABLE_PRECIOUS);
        List<ItemStack> drops = Block.getDrops(targetState, level, targetPos, null);
        depositOrDrop(level, pos, drops);

        // Common ores always regrow on the base machine. Precious ores (gold/diamond/
        // emerald) only regrow with a Deep Regrowth Module installed AND the Heartwood's
        // Stone-branch "Regrowth Mastery" node unlocked (Update 6 § 6.2 — the module
        // alone was the interim gate before the Heartwood existed; both are required
        // now), and then at 5× the common delay (§ 4.1②) — without either, mining a
        // precious ore is permanent, same as a player mining it by hand.
        if (isCommonOre) {
            regenBlock = targetState.getBlock();
            regenTicksRemaining = REGEN_TICKS_BY_TIER[Math.min(regenTier, REGEN_TICKS_BY_TIER.length - 1)];
        } else if (isPreciousOre && hasDeepRegrowthModule
                && HeartwoodData.get(level.getServer()).isNodeUnlocked(HeartwoodBranch.STONE, HeartwoodBranch.STONE_REGROWTH_MASTERY)) {
            regenBlock = targetState.getBlock();
            regenTicksRemaining = REGEN_TICKS_BY_TIER[Math.min(regenTier, REGEN_TICKS_BY_TIER.length - 1)] * PRECIOUS_REGEN_MULTIPLIER;
        }

        level.removeBlock(targetPos, false);
        return true;
    }

    // Blacklist: bedrock/unbreakable (negative destroy speed), anything with a block
    // entity (chests etc.), fluids, and air.
    private static boolean isMinable(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.hasBlockEntity()) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        return state.getDestroySpeed(level, pos) >= 0;
    }

    @Override
    protected int dataCount() {
        return DATA_COUNT;
    }

    @Override
    protected int getData(int index) {
        if (index == DATA_REGEN_TICKS) return regenTicksRemaining;
        if (index == DATA_REGEN_MAX) {
            int base = REGEN_TICKS_BY_TIER[Math.min(regenTier, REGEN_TICKS_BY_TIER.length - 1)];
            return regenBlock != null && regenBlock.defaultBlockState().is(REGENERABLE_PRECIOUS)
                    ? base * PRECIOUS_REGEN_MULTIPLIER : base;
        }
        if (index == DATA_REGEN_TIER) return regenTier;
        if (index == DATA_DEEP_REGROWTH) return hasDeepRegrowthModule ? 1 : 0;
        return super.getData(index);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input.childOrEmpty(TAG_SEED), seed);
        regenTicksRemaining = input.getIntOr(TAG_REGEN_TICKS, 0);
        regenTier = Math.min(input.getIntOr(TAG_REGEN_TIER, 0), GreenwardConfig.MAX_REGEN_TIER);
        hasDeepRegrowthModule = input.getBooleanOr(TAG_DEEP_REGROWTH, false);
        String blockId = input.getStringOr(TAG_REGEN_BLOCK, "");
        regenBlock = blockId.isEmpty() ? null : resolveBlock(blockId);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.child(TAG_SEED), seed);
        output.putInt(TAG_REGEN_TICKS, regenTicksRemaining);
        output.putInt(TAG_REGEN_TIER, regenTier);
        output.putBoolean(TAG_DEEP_REGROWTH, hasDeepRegrowthModule);
        if (regenBlock != null) {
            output.putString(TAG_REGEN_BLOCK, BuiltInRegistries.BLOCK.getKey(regenBlock).toString());
        }
    }

    private static Block resolveBlock(String id) {
        Identifier parsed = Identifier.tryParse(id);
        if (parsed == null) {
            return null;
        }
        return BuiltInRegistries.BLOCK.get(parsed).map(Holder.Reference::value).orElse(null);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.greenward.auto_miner");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AutoMinerMenu(containerId, inventory, this, containerData());
    }
}
