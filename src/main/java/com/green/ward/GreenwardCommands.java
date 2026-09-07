package com.green.ward;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * {@code /greenward stats} and {@code /greenward decommission} — Update 1 § 1.2 rule 8
 * and § 1.6. Decommission is the clean-exit path the Permanence Charter requires: sweep
 * loaded chunks, convert every Greenward block to a sensible vanilla equivalent, and
 * drop machine inventories as items, so a player can walk away from the mod cleanly
 * without hand-mining every machine first.
 */
public final class GreenwardCommands {
    private GreenwardCommands() {}

    /** Chunk radius for {@code decommission world} — an approximation of "a full pass"
     *  bounded to something that finishes in a reasonable time; see class javadoc on
     *  why a literal disk-level region scan isn't attempted. Measured live: radius 24
     *  (~2,400 chunks, each force-loaded/generated synchronously) blocked the main
     *  thread long enough to time out an RCON connection, so this stays modest — the
     *  bottleneck is chunk *loading*, not the per-block scan below (which is already
     *  section-fast-pathed via {@code LevelChunkSection.maybeHas}). */
    private static final int WORLD_SWEEP_CHUNK_RADIUS = 6;

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(GreenwardCommands::register);
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                  net.minecraft.commands.CommandBuildContext context,
                                  net.minecraft.commands.Commands.CommandSelection selection) {
        dispatcher.register(literal("greenward")
                .then(literal("stats").executes(GreenwardCommands::runStats))
                .then(literal("progress").executes(GreenwardCommands::runProgress))
                .then(literal("threat").executes(GreenwardCommands::runThreat))
                .then(literal("balance").executes(GreenwardCommands::runBalance))
                .then(literal("waystone")
                        .then(literal("tp")
                                .then(argument("name", StringArgumentType.greedyString())
                                        .executes(GreenwardCommands::runWaystoneTeleport))))
                .then(literal("decommission")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> runDecommission(ctx.getSource(), false))
                        .then(literal("world").executes(ctx -> runDecommission(ctx.getSource(), true)))));
    }

    // --- /greenward progress ---

    /**
     * The primary "check my Collections/Skills/Proofs status" interface for Update 2.
     * Deliberately a command, not a Field Guide book page: the book (
     * {@code FieldGuideContent}) is rendered entirely client-side via
     * {@code BookViewScreen}, and {@link PlayerProgressData} lives server-side with no
     * sync channel wired up to the client yet — building one properly (a custom network
     * payload plus client-side cache) is real, separate infrastructure this pass
     * deliberately doesn't rush. Flagged here rather than silently deferred; a live
     * in-book progress view is the natural next step once that channel exists.
     */
    private static int runProgress(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Only a player can run this."));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("=== Greenward Progress: " + player.getGameProfile().name() + " ==="), false);

        ctx.getSource().sendSuccess(() -> Component.literal("-- Skills --"), false);
        for (GreenwardSkill skill : GreenwardSkill.values()) {
            int level = PlayerProgress.getSkillLevel(player, skill);
            long xp = PlayerProgress.getSkillXp(player, skill);
            long nextLevelXp = SkillXpCurve.xpForLevel(Math.min(level + 1, GreenwardSkill.MAX_LEVEL));
            String line = String.format(Locale.ROOT, "%s: Level %d (%,d / %,d XP)", skill.displayName(), level, xp, nextLevelXp);
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal("-- Collections --"), false);
        for (GreenwardCollection collection : GreenwardCollection.values()) {
            long quantity = PlayerProgress.getCollectionQuantity(player, collection);
            int completedTier = PlayerProgress.getCompletedTier(player, collection);
            int quantityTier = PlayerProgress.getCollectionTierByQuantity(player, collection);
            StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "%s: %,d (Tier %d/%d complete",
                    collection.displayName(), quantity, completedTier, GreenwardCollection.MAX_TIER));
            if (quantityTier > completedTier) {
                GreenwardProof pending = GreenwardProof.forTier(collection, completedTier + 1);
                if (pending != null && !PlayerProgress.hasProof(player, pending)) {
                    line.append(", pending Proof: ").append(pending.title());
                }
            }
            line.append(")");
            String rendered = line.toString();
            ctx.getSource().sendSuccess(() -> Component.literal(rendered), false);
        }

        return 1;
    }

    // --- /greenward threat ---

    private static int runThreat(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        boolean enabled = GreenwardConfig.ENABLE_HEARTWOOD
                && HeartwoodData.get(ctx.getSource().getServer()).isThreatEnabled();
        if (!enabled) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "World Threat is disabled. Toggle it on at the Heartwood to turn on mob scaling."), false);
            return 0;
        }
        int threat = ThreatManager.computeThreat(ctx.getSource().getServer());
        String band = ThreatManager.bandName(threat);
        ctx.getSource().sendSuccess(() -> Component.literal(
                String.format(Locale.ROOT, "World Threat: %d/100 (%s)", threat, band)), false);
        return threat;
    }

    // --- /greenward balance ---

    private static int runBalance(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Only a player can run this."));
            return 0;
        }
        long balance = GreenwardCurrency.get(player);
        ctx.getSource().sendSuccess(() -> Component.literal(
                String.format(Locale.ROOT, "Purse: %,d Coins", balance)), false);
        return (int) Math.min(Integer.MAX_VALUE, balance);
    }

    // --- /greenward waystone tp ---

    /** Backs the clickable travel-list entries {@link WaystoneBlock} sends to chat —
     *  never meant to be typed by hand, though nothing stops it (see that class's own
     *  javadoc on why that's an accepted, non-exploitable gap: it still costs Coins). */
    private static int runWaystoneTeleport(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Only a player can run this."));
            return 0;
        }
        String name = StringArgumentType.getString(ctx, "name");
        WaystoneData data = WaystoneData.get(ctx.getSource().getServer());
        WaystoneData.Waypoint target = data.findByName(player.getUUID(), name);
        if (target == null) {
            player.sendSystemMessage(Component.literal("Unknown Waystone: " + name), true);
            return 0;
        }

        long cost = WaystoneBlock.travelCost(player, target);
        if (!GreenwardCurrency.spend(player, cost)) {
            player.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
                    "Traveling to %s costs %,d Coins — you don't have enough.", target.name(), cost)), true);
            return 0;
        }

        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimensionKey = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.Identifier.parse(target.dimension()));
        ServerLevel destinationLevel = ctx.getSource().getServer().getLevel(dimensionKey);
        if (destinationLevel == null) {
            player.sendSystemMessage(Component.literal("That Waystone's dimension no longer exists."), true);
            GreenwardCurrency.add(player, cost);
            return 0;
        }

        BlockPos pos = target.pos();
        player.teleportTo(destinationLevel, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                Set.of(), player.getYRot(), player.getXRot(), false);
        WaystoneBlock.playTeleportEffects(destinationLevel, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        player.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
                "Traveled to %s for %,d Coins.", target.name(), cost)), true);
        return 1;
    }

    // --- /greenward stats ---

    /** Lines per book page — vanilla's book-reading screen comfortably fits about this
     *  many short lines before text starts running off the bottom. */
    private static final int STAT_LINES_PER_PAGE = 8;

    /**
     * Opens a vanilla written-book screen populated with the player's full stat
     * breakdown — the "place to view all your stats" asked for during playtesting,
     * built entirely on vanilla's own book-reading GUI ({@code Player#openItemGui}, the
     * same mechanism a lectern or a real written book uses) rather than a custom Screen
     * + networking packet. The book itself is never added to the player's inventory —
     * it only ever exists as the transient {@link ItemStack} passed to openItemGui.
     */
    private static int runStats(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Only a player can run this."));
            return 0;
        }

        Map<String, StatProfile> breakdown = PlayerStatManager.breakdown(player);
        List<Component> lines = new ArrayList<>();
        for (GreenwardStat stat : GreenwardStat.values()) {
            double total = PlayerStatManager.get(player, stat);
            StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "%s %s: %s%s",
                    stat.symbol(), stat.displayName(), formatValue(total, stat), stat.isPercentage() ? "%" : ""));
            breakdown.forEach((source, profile) -> {
                double contribution = profile.get(stat);
                if (contribution != 0.0 && !source.equals("Base")) {
                    line.append(String.format(Locale.ROOT, "\n  +%s %s", formatValue(contribution, stat), source));
                }
            });
            lines.add(Component.literal(line.toString()));
        }

        List<Filterable<Component>> pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += STAT_LINES_PER_PAGE) {
            MutableComponent page = Component.empty();
            List<Component> pageLines = lines.subList(i, Math.min(i + STAT_LINES_PER_PAGE, lines.size()));
            for (int j = 0; j < pageLines.size(); j++) {
                if (j > 0) {
                    page.append("\n\n");
                }
                page.append(pageLines.get(j));
            }
            pages.add(Filterable.passThrough(page));
        }

        WrittenBookContent content = new WrittenBookContent(
                Filterable.passThrough("Greenward Stats"), player.getGameProfile().name(), 0, pages, true);
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        player.openItemGui(book, InteractionHand.MAIN_HAND);
        return 1;
    }

    private static String formatValue(double value, GreenwardStat stat) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.1f", value);
    }

    // --- /greenward decommission ---

    private static int runDecommission(CommandSourceStack source, boolean worldPass) {
        int chunksSwept = 0;
        int blocksConverted = 0;
        int inventoriesEmptied = 0;

        for (ServerLevel level : source.getServer().getAllLevels()) {
            Set<Long> visited = new HashSet<>();
            Set<ChunkPos> centers = new HashSet<>();

            // A "world" pass always includes spawn, independent of whether anyone is
            // online — a headless/no-player server should still be sweepable.
            if (worldPass) {
                centers.add(ChunkPos.containing(level.getRespawnData().pos()));
            }
            for (ServerPlayer player : level.players()) {
                centers.add(player.chunkPosition());
            }

            for (ChunkPos center : centers) {
                int radius = worldPass ? WORLD_SWEEP_CHUNK_RADIUS
                        : source.getServer().getPlayerList().getViewDistance();
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        ChunkPos pos = new ChunkPos(center.x() + dx, center.z() + dz);
                        if (!visited.add(pos.pack())) {
                            continue;
                        }
                        LevelChunk chunk = worldPass
                                ? level.getChunk(pos.x(), pos.z())
                                : level.getChunkSource().getChunkNow(pos.x(), pos.z());
                        if (chunk == null) {
                            continue;
                        }
                        chunksSwept++;
                        SweepResult result = sweepChunk(level, chunk);
                        blocksConverted += result.blocksConverted;
                        inventoriesEmptied += result.inventoriesEmptied;
                    }
                }
            }
        }

        int finalChunks = chunksSwept;
        int finalBlocks = blocksConverted;
        int finalInventories = inventoriesEmptied;
        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Greenward decommission complete: %d chunk(s) swept, %d block(s) converted, %d inventory(ies) emptied.",
                finalChunks, finalBlocks, finalInventories)), true);
        if (!worldPass) {
            source.sendSuccess(() -> Component.literal(
                    "Only loaded chunks were swept — run '/greenward decommission world' for a wider pass."), false);
        }
        return 1;
    }

    private record SweepResult(int blocksConverted, int inventoriesEmptied) {}

    private static boolean isGreenwardBlock(BlockState state) {
        Block block = state.getBlock();
        return block instanceof FertilizedFarmlandBlock || vanillaEquivalentFor(block) != null;
    }

    private static SweepResult sweepChunk(ServerLevel level, LevelChunk chunk) {
        int converted = 0;
        int emptied = 0;
        ChunkPos cp = chunk.getPos();

        // Threat's transient attribute modifiers (Update 7 § 7.4) — belt-and-suspenders
        // alongside ENTITY_UNLOAD/SERVER_STOPPING, since decommission is the explicit
        // "clean exit" path § 1.2 rule 8 requires.
        if (GreenwardConfig.ENABLE_THREAT) {
            net.minecraft.world.phys.AABB chunkBounds = new net.minecraft.world.phys.AABB(
                    cp.getMinBlockX(), level.getMinY(), cp.getMinBlockZ(),
                    cp.getMaxBlockX() + 1, level.getMaxY(), cp.getMaxBlockZ() + 1);
            for (net.minecraft.world.entity.Entity entity : level.getEntities((net.minecraft.world.entity.Entity) null, chunkBounds, e -> true)) {
                ThreatMobHandler.stripModifiers(entity);
            }
        }

        net.minecraft.world.level.chunk.LevelChunkSection[] sections = chunk.getSections();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            net.minecraft.world.level.chunk.LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir() || !section.maybeHas(GreenwardCommands::isGreenwardBlock)) {
                continue;
            }

            int minY = level.getSectionYFromSectionIndex(sectionIndex) * 16;
            for (int y = minY; y < minY + 16; y++) {
                for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
                    for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = chunk.getBlockState(pos);
                        Block block = state.getBlock();

                        if (block instanceof FertilizedFarmlandBlock) {
                            FertilizedFarmlandBlock.convertToVanilla(level, pos, state);
                            converted++;
                            continue;
                        }

                        Block equivalent = vanillaEquivalentFor(block);
                        if (equivalent == null) {
                            continue;
                        }

                        BlockEntity entity = level.getBlockEntity(pos);
                        if (entity instanceof Container container) {
                            Containers.dropContents(level, pos, container);
                            emptied++;
                        }

                        level.setBlockAndUpdate(pos, equivalent.defaultBlockState());
                        converted++;
                    }
                }
            }
        }
        return new SweepResult(converted, emptied);
    }

    /** @return the vanilla block a Greenward block should become, or {@code null} if it
     *  isn't one of ours (fast-pathed as a plain reference-equality check per block). */
    private static Block vanillaEquivalentFor(Block block) {
        if (block == ModBlocks.WHEAT_BALE_BLOCK) {
            return Blocks.HAY_BLOCK;
        }
        if (block == ModBlocks.AUTO_HARVESTER || block == ModBlocks.AUTO_MINER || block == ModBlocks.AUTO_FISHER) {
            return Blocks.IRON_BLOCK;
        }
        return null;
    }
}
