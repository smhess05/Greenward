package com.green.ward;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Design Program Update 5 § 5.1 — rare drops from vanilla ore, gated by tool tier and
 * Mining skill level, with **zero worldgen additions** (§ 5.6): every material here comes
 * from a loot roll on a genuine player break, never a new block. Structurally
 * machine-proof for the same reason {@link GreenwardFortuneHandler} is — {@code
 * PlayerBlockBreakEvents.AFTER} only ever fires with a real {@link Player}, and the
 * Auto-Miner removes blocks directly, never through this event.
 *
 * <p>"Rates are affected by Mining Fortune" (§ 5.1) is implemented as a straight
 * multiplier on the base chance ({@code effective = base * (1 + fortune/100)}) rather
 * than the guaranteed-extra-roll formula {@link GreenwardFortuneHandler} uses for
 * ordinary yield — that formula assumes a base yield of at least 1, which doesn't fit a
 * sub-1%-chance rare drop. Documented interpretation, not a literal spec formula.
 */
public final class GreenwardRareOreHandler {
    private GreenwardRareOreHandler() {}

    /** Fabric's own convention tag, unioning every vanilla ore block — reused rather
     *  than hand-listing every ore type again for Amber's "any ore" condition, and again
     *  by {@link VeinBlastProjectile} for its detonation radius. */
    static final TagKey<Block> VANILLA_ORES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

    private static final int AMBER_CLUSTER_MIN = 5;
    /** Caps the flood-fill search so a real vein's exact size never needs computing past
     *  the threshold that matters — cheap even on the largest natural veins. */
    private static final int AMBER_CLUSTER_SEARCH_CAP = 12;

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register(GreenwardRareOreHandler::onAfterBreak);
    }

    private static void onAfterBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!GreenwardConfig.ENABLE_MINING_DEPTH) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Block block = state.getBlock();
        int y = pos.getY();
        int toolTier = toolTier(serverPlayer.getMainHandItem());
        int miningLevel = PlayerProgress.getSkillLevel(serverPlayer, GreenwardSkill.MINING);
        double fortune = PlayerStatManager.get(serverPlayer, GreenwardStat.MINING_FORTUNE);

        if ((block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) && y <= -40
                && toolTier >= 1 && miningLevel >= 15) {
            roll(serverLevel, serverPlayer, pos, ModItems.GLIMMER, 0.015, fortune);
        }

        if (isOneOf(block, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE) && y <= -40 && toolTier >= 2 && miningLevel >= 25) {
            roll(serverLevel, serverPlayer, pos, ModItems.TITANSHARD, 0.01, fortune);
        }

        if (state.is(VANILLA_ORES) && toolTier >= 2 && miningLevel >= 30
                && clusterSize(serverLevel, pos, block) >= AMBER_CLUSTER_MIN) {
            roll(serverLevel, serverPlayer, pos, ModItems.ROUGH_AMBER, 0.006, fortune);
        }

        if (isOneOf(block, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE) && toolTier >= 2 && miningLevel >= 30) {
            roll(serverLevel, serverPlayer, pos, ModItems.ROUGH_JADE, 0.04, fortune);
        }

        if (isOneOf(block, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE) && y <= -40 && toolTier >= 2 && miningLevel >= 35) {
            roll(serverLevel, serverPlayer, pos, ModItems.ROUGH_SAPPHIRE, 0.008, fortune);
        }

        if (isOneOf(block, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE) && y <= -55 && toolTier >= 3 && miningLevel >= 40) {
            roll(serverLevel, serverPlayer, pos, ModItems.ROUGH_RUBY, 0.005, fortune);
        }
    }

    private static void roll(ServerLevel level, ServerPlayer player, BlockPos pos, Item item, double baseChance, double fortune) {
        double effectiveChance = baseChance * (1.0 + fortune / 100.0);
        if (level.getRandom().nextDouble() < effectiveChance) {
            Block.popResource(level, pos, new ItemStack(item, 1));
        }
    }

    /** 0 = no Greenward pickaxe, 1 = Prospector's Drill, 2 = Excavator's Pick,
     *  3 = Bedrock Reaver — an ordered tier so "Drill+"/"Pick+" checks are a single
     *  {@code >=} comparison. */
    private static int toolTier(ItemStack tool) {
        Item item = tool.getItem();
        if (item == ModTools.BEDROCK_REAVER) {
            return 3;
        }
        if (item == ModTools.EXCAVATORS_PICK) {
            return 2;
        }
        if (item == ModTools.PROSPECTORS_DRILL) {
            return 1;
        }
        return 0;
    }

    private static boolean isOneOf(Block block, Block... candidates) {
        for (Block candidate : candidates) {
            if (block == candidate) {
                return true;
            }
        }
        return false;
    }

    /** Bounded 6-directional flood fill counting same-block neighbors, capped at {@link
     *  #AMBER_CLUSTER_SEARCH_CAP} — only "is this at least {@link #AMBER_CLUSTER_MIN}"
     *  matters, so the search stops the moment that's answered either way. */
    private static int clusterSize(ServerLevel level, BlockPos origin, Block oreBlock) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < AMBER_CLUSTER_SEARCH_CAP) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (visited.contains(neighbor)) {
                    continue;
                }
                if (level.getBlockState(neighbor).is(oreBlock)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    if (visited.size() >= AMBER_CLUSTER_MIN) {
                        return visited.size();
                    }
                }
            }
        }
        return visited.size();
    }
}
