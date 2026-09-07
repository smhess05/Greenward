package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared crop-harvest logic used by both the hoe right-click harvest and the
 * Auto-Harvester machine, so the mature-check / drop / fertilized-bonus / replant
 * sequence lives in exactly one place.
 */
public final class HarvestLogic {
    private HarvestLogic() {}

    /** Only ever rolls on already-fertilized farmland ("over-fertilized" per § 9.1) — a
     *  small per-harvest chance, invented (the source text doesn't give a rate), that
     *  purely-manual farming can eventually trigger but reliably avoiding fertilizer
     *  entirely cannot. */
    private static final float BLIGHT_CHANCE = 0.02F;

    private static void rollForBlight(ServerLevel level, BlockPos pos) {
        if (!GreenwardConfig.ENABLE_FARMING_FISHING_DEPTH || !GreenwardConfig.ENABLE_FERTILIZER) {
            return;
        }
        BlockPos soil = pos.below();
        if (FertilizedFarmlandData.get(level).isFertilized(soil) && level.getRandom().nextFloat() < BLIGHT_CHANCE) {
            BlightData.get(level).setBlighted(soil);
        }
    }

    public static boolean isMatureCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
    }

    /**
     * Drops the crop's loot, applies the fertilized-farmland yield bonus, and replants
     * at age 0. Pass {@code harvester = null} and {@code tool = ItemStack.EMPTY} for a
     * machine-driven harvest (no Fortune, since there is no enchanted tool in context).
     *
     * @return true if a crop was actually harvested (state was a mature crop).
     */
    public static boolean harvestAndReplant(ServerLevel level, BlockPos pos, BlockState state,
                                             Entity harvester, ItemStack tool) {
        return harvestAndReplant(level, pos, state, harvester, tool, 0.0F);
    }

    /**
     * Same as {@link #harvestAndReplant(ServerLevel, BlockPos, BlockState, Entity, ItemStack)},
     * plus a chance (e.g. the Harvester's Garb set bonus) to drop loot one additional time.
     * This is additive with, not multiplicative on, the fertilized-soil bonus: the
     * fertilized 2x is computed first, then {@code extraDropChance} is rolled once for a
     * single extra pass — never compounded.
     */
    public static boolean harvestAndReplant(ServerLevel level, BlockPos pos, BlockState state,
                                             Entity harvester, ItemStack tool, float extraDropChance) {
        if (!isMatureCrop(state)) {
            return false;
        }

        CropBlock crop = (CropBlock) state.getBlock();
        int passes = 1;

        Block.dropResources(state, level, pos, null, harvester, tool);
        if (GreenwardConfig.ENABLE_FERTILIZER && FertilizedFarmlandData.get(level).isFertilized(pos.below())) {
            Block.dropResources(state, level, pos, null, harvester, tool);
            passes++;
        }
        if (extraDropChance > 0.0F && level.getRandom().nextFloat() < extraDropChance) {
            Block.dropResources(state, level, pos, null, harvester, tool);
            passes++;
        }
        // Farming Fortune (Update 1 § 1.2/1.3), retired-set-bonus-unified in Update 9 § 9.1
        // (see Greenward.extraDropChanceFor's own doc comment). Suppressed entirely on
        // Blighted tiles (§ 9.1) until cleared — checked here, not in a separate hook,
        // since this is the one place both "was this fertilized" and "is this a genuine
        // player harvest" are already established; machine harvests never reach this
        // branch at all (harvester instanceof ServerPlayer), so Blight can never suppress
        // an Auto-Harvester's yield — there's nothing there to suppress.
        boolean blighted = GreenwardConfig.ENABLE_FARMING_FISHING_DEPTH && BlightData.get(level).isBlighted(pos.below());
        if (harvester instanceof ServerPlayer player) {
            if (!blighted) {
                rollForBlight(level, pos);
            }
            double fortune = blighted ? 0.0 : PlayerStatManager.get(player, GreenwardStat.FARMING_FORTUNE);
            if (fortune > 0.0) {
                int guaranteedExtraRolls = (int) Math.floor(fortune / 100.0);
                double bonusChance = (fortune % 100.0) / 100.0;
                for (int i = 0; i < guaranteedExtraRolls; i++) {
                    Block.dropResources(state, level, pos, null, harvester, tool);
                    passes++;
                }
                if (bonusChance > 0.0 && level.getRandom().nextDouble() < bonusChance) {
                    Block.dropResources(state, level, pos, null, harvester, tool);
                    passes++;
                }
            }
        }

        level.setBlockAndUpdate(pos, crop.getStateForAge(0));

        // Collections + Skills (Design Program Update 2) — "passes" stands in for actual
        // item count (each dropResources() call is one loot-table roll's worth of yield);
        // exact enough for a lifetime tally without needing to intercept the spawned stacks.
        if (GreenwardConfig.ENABLE_COLLECTIONS_PROOFS && harvester instanceof ServerPlayer player) {
            trackCropCollection(level, player, crop, passes);
        }

        return true;
    }

    private static void trackCropCollection(ServerLevel level, ServerPlayer player, CropBlock crop, int passes) {
        GreenwardCollection collection;
        GreenwardProof dayProof;
        if (crop == Blocks.WHEAT) {
            collection = GreenwardCollection.WHEAT;
            dayProof = GreenwardProof.BUMPER_CROP;
        } else if (crop == Blocks.POTATOES) {
            collection = GreenwardCollection.POTATO;
            dayProof = GreenwardProof.DUG_IN;
        } else if (crop == Blocks.CARROTS) {
            collection = GreenwardCollection.CARROT;
            dayProof = GreenwardProof.GOLDEN_HOUR;
        } else {
            return;
        }

        PlayerProgress.addCollection(player, collection, passes, true);
        int dayNumber = (int) (level.getOverworldClockTime() / 24000L);
        PlayerProgress.incrementDayCounter(player, dayProof, dayNumber, passes, 300);

        if (collection == GreenwardCollection.WHEAT && level.isRaining()) {
            PlayerProgress.incrementLifetimeCounter(player, GreenwardProof.RAINFED, passes, 500);
        }
    }

    /**
     * Machine variant: same mature-check / fertilized-bonus / replant sequence, but the
     * loot is captured (no Fortune, no player) and deposited into the machine's own
     * storage instead of dropped in the world.
     */
    public static boolean harvestIntoStorage(ServerLevel level, BlockPos pos, BlockState state,
                                              AbstractMachineBlockEntity machine) {
        if (!isMatureCrop(state)) {
            return false;
        }

        CropBlock crop = (CropBlock) state.getBlock();

        List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, pos, null));
        if (GreenwardConfig.ENABLE_FERTILIZER && FertilizedFarmlandData.get(level).isFertilized(pos.below())) {
            drops.addAll(Block.getDrops(state, level, pos, null));
        }

        level.setBlockAndUpdate(pos, crop.getStateForAge(0));
        machine.depositOrDrop(level, machine.getBlockPos(), drops);
        return true;
    }
}
