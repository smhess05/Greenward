package com.green.ward;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;

/**
 * Mining Fortune (Update 1) plus Update 2's mining Collections/XP/Proofs — kept in one
 * handler since both key off the same {@code PlayerBlockBreakEvents.AFTER} firing and
 * the same "how many copies of this block's yield did the player actually get" count.
 * Structurally machine-proof, not just config-gated: this event only ever fires with a
 * real {@link Player} — the Auto-Miner removes blocks directly
 * ({@code Block.dropResources} + {@code level.removeBlock}), never through this event.
 *
 * <p>Fortune deliberately layers on top of vanilla's own break (including any vanilla
 * Fortune enchant already on the tool) rather than cancelling and replacing it — see the
 * class's original Update 1 doc comment history in git for the full reasoning.
 */
public final class GreenwardFortuneHandler {
    private GreenwardFortuneHandler() {}

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register(GreenwardFortuneHandler::onAfterBreak);
    }

    private static void onAfterBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int totalCopies = applyFortune(serverLevel, serverPlayer, pos, state, blockEntity);

        if (GreenwardConfig.ENABLE_COLLECTIONS_PROOFS) {
            trackDepthProofs(serverLevel, serverPlayer, pos);
            trackBlockCollection(serverLevel, serverPlayer, pos, state, totalCopies);
        }
    }

    /** @return how many total copies of the block's yield the player received (1 = base only). */
    private static int applyFortune(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        double fortune = PlayerStatManager.get(player, GreenwardStat.MINING_FORTUNE);
        if (fortune <= 0) {
            return 1;
        }

        int guaranteedExtraRolls = (int) Math.floor(fortune / 100.0);
        double bonusChance = (fortune % 100.0) / 100.0;
        ItemStack tool = player.getMainHandItem();
        int copies = 1;

        for (int i = 0; i < guaranteedExtraRolls; i++) {
            dropExtra(level, pos, state, blockEntity, player, tool);
            copies++;
        }
        if (bonusChance > 0.0 && level.getRandom().nextDouble() < bonusChance) {
            dropExtra(level, pos, state, blockEntity, player, tool);
            copies++;
        }
        return copies;
    }

    private static void dropExtra(ServerLevel level, BlockPos pos, BlockState state,
                                   BlockEntity blockEntity, ServerPlayer player, ItemStack tool) {
        for (ItemStack drop : Block.getDrops(state, level, pos, blockEntity, player, tool)) {
            Block.popResource(level, pos, drop);
        }
    }

    /** Down Deep / Bedrock Bound / Single Breath — depth-gated, apply to any block mined. */
    private static void trackDepthProofs(ServerLevel level, ServerPlayer player, BlockPos pos) {
        if (pos.getY() <= -50) {
            PlayerProgress.incrementLifetimeCounter(player, GreenwardProof.DOWN_DEEP, 1, 1000);
        }
        if (pos.getY() <= -60) {
            PlayerProgress.incrementLifetimeCounter(player, GreenwardProof.BEDROCK_BOUND, 1, 100);
        }

        // "Without surfacing above Y 0" — checked at mine-time (the player's own position,
        // not the block's), so a trip above Y0 without mining anything in between won't
        // register as broken until the next block is mined; a documented simplification,
        // not a continuous per-tick position watch.
        if (player.getY() > 0) {
            PlayerProgress.resetStreak(player, GreenwardProof.SINGLE_BREATH);
        } else {
            PlayerProgress.incrementStreak(player, GreenwardProof.SINGLE_BREATH, 1, 64);
        }
    }

    private static void trackBlockCollection(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, int copies) {
        Block block = state.getBlock();
        int dayNumber = (int) (level.getOverworldClockTime() / 24000L);

        if (block == Blocks.STONE || block == Blocks.COBBLESTONE) {
            PlayerProgress.addCollection(player, GreenwardCollection.COBBLESTONE, copies, true);
            PlayerProgress.incrementDayCounter(player, GreenwardProof.QUARRY_SHIFT, dayNumber, copies, 2000);
            return;
        }

        if (block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) {
            PlayerProgress.addCollection(player, GreenwardCollection.DEEPSLATE, copies, true);
            PlayerProgress.incrementStreak(player, GreenwardProof.DEEP_VEIN, 1, 64);
            // Cold Iron: a torch within 16 blocks would raise the local block-light level —
            // using the light level itself as the check is a cheap O(1) proxy for "no torch
            // nearby" instead of a brute-force 33x33x33 block scan, and arguably matches the
            // proof's real intent (mining in genuine darkness) more directly than literally
            // searching for torch blocks specifically.
            if (level.getBrightness(LightLayer.BLOCK, pos) == 0) {
                PlayerProgress.incrementLifetimeCounter(player, GreenwardProof.COLD_IRON, 1, 500);
            }
            return;
        }
        // Any non-deepslate block mined in between breaks the Deep Vein streak.
        PlayerProgress.resetStreak(player, GreenwardProof.DEEP_VEIN);

        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            PlayerProgress.addCollection(player, GreenwardCollection.DIAMOND, copies, true);
            // Vein Runner counts ore blocks mined, not Fortune-multiplied diamond yield.
            PlayerProgress.incrementDayCounter(player, GreenwardProof.VEIN_RUNNER, dayNumber, 1, 20);
        }
    }
}
