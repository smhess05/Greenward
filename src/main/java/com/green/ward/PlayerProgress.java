package com.green.ward;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central facade for the Collections + Skills + Proofs system (Design Program Update 2).
 * Mirrors {@link PlayerStatManager}'s role for the stat layer: everything else calls
 * through here rather than touching the attachment or the ephemeral tracking maps
 * directly.
 *
 * <p>Four Proof-tracking patterns, deliberately kept to four rather than one bespoke
 * mechanic per proof:
 * <ul>
 *   <li><b>Instant</b> — completes the moment a qualifying action happens (Full Yield,
 *       Storm Catch). No counter at all.</li>
 *   <li><b>Lifetime cumulative</b> — a gated counter that only ever goes up, persisted
 *       (Rainfed, Down Deep, Bedrock Bound, Cold Iron, Bare-Knuckle, Graveyard Shift).</li>
 *   <li><b>Day-scoped</b> — resets whenever the in-game day number changes, kept
 *       in-memory only (a rare restart losing same-day progress is an accepted,
 *       self-healing inconvenience — the same call already made for Grim Resolve's
 *       cooldown).</li>
 *   <li><b>Streak</b> — resets on a specific "broke it" condition rather than a day
 *       boundary, also in-memory only (Unbroken Harvest, Single Breath, Deep Vein,
 *       Patient, Held Nerve).</li>
 * </ul>
 * Stockpile-check proofs (Full Silo, Full Cellar, Fallow No More) need no counter of
 * either kind — they're a live threshold check against current inventory/world state.
 */
public final class PlayerProgress {
    private PlayerProgress() {}

    static final AttachmentType<PlayerProgressData> ATTACHMENT = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "progress"),
            (AttachmentRegistry.Builder<PlayerProgressData> builder) -> builder
                    .persistent(PlayerProgressData.CODEC)
                    .copyOnDeath());

    // --- Ephemeral (in-memory only) tracking, per player ---
    private static final Map<UUID, Map<GreenwardProof, Long>> DAY_COUNTERS = new HashMap<>();
    private static final Map<UUID, Map<GreenwardProof, Integer>> DAY_ANCHORS = new HashMap<>();
    private static final Map<UUID, Map<GreenwardProof, Long>> STREAK_COUNTERS = new HashMap<>();

    private static PlayerProgressData data(ServerPlayer player) {
        return player.getAttachedOrCreate(ATTACHMENT, PlayerProgressData::new);
    }

    private static void markDirty(ServerPlayer player, PlayerProgressData data) {
        player.setAttached(ATTACHMENT, data);
    }

    // --- Collections ---

    /** @param playerAction true for a genuine player action (also grants skill XP and
     *  checks for newly-completed tiers); false is reserved for future machine-visible
     *  bookkeeping and never should grant XP or complete a Proof. */
    public static void addCollection(ServerPlayer player, GreenwardCollection collection, long amount, boolean playerAction) {
        if (amount <= 0) {
            return;
        }
        PlayerProgressData data = data(player);
        long newQuantity = data.collectionQuantity.merge(collection, amount, Long::sum);
        markDirty(player, data);
        if (playerAction) {
            addSkillXp(player, collection.skill(), amount);
            checkTierUnlocks(player, collection, newQuantity);
        }
    }

    public static long getCollectionQuantity(ServerPlayer player, GreenwardCollection collection) {
        return data(player).collectionQuantity.getOrDefault(collection, 0L);
    }

    public static int getCollectionTierByQuantity(ServerPlayer player, GreenwardCollection collection) {
        return GreenwardCollection.tierForQuantity(getCollectionQuantity(player, collection));
    }

    /** A tier is complete only once its quantity threshold is met AND (if it has one, from
     *  Tier IV up) its Proof is done — § 2.1's whole point: volume alone isn't enough. */
    public static boolean isTierComplete(ServerPlayer player, GreenwardCollection collection, int tier) {
        if (tier < 1 || tier > GreenwardCollection.MAX_TIER) {
            return false;
        }
        if (getCollectionQuantity(player, collection) < GreenwardCollection.thresholdForTier(tier)) {
            return false;
        }
        GreenwardProof proof = GreenwardProof.forTier(collection, tier);
        return proof == null || hasProof(player, proof);
    }

    /** Highest tier where every tier up to and including it is fully complete (quantity + proof). */
    public static int getCompletedTier(ServerPlayer player, GreenwardCollection collection) {
        int tier = 0;
        while (tier < GreenwardCollection.MAX_TIER && isTierComplete(player, collection, tier + 1)) {
            tier++;
        }
        return tier;
    }

    private static void checkTierUnlocks(ServerPlayer player, GreenwardCollection collection, long quantity) {
        int reachableByQuantity = GreenwardCollection.tierForQuantity(quantity);
        for (int tier = 1; tier <= reachableByQuantity; tier++) {
            if (isTierComplete(player, collection, tier)) {
                GreenwardTierCriterion.trigger(player, collection, tier);
            }
        }
    }

    // --- Skills ---

    public static void addSkillXp(ServerPlayer player, GreenwardSkill skill, long amount) {
        if (amount <= 0) {
            return;
        }
        PlayerProgressData data = data(player);
        int oldLevel = SkillXpCurve.levelForXp(data.skillXp.getOrDefault(skill, 0L));
        long newXp = data.skillXp.merge(skill, amount, Long::sum);
        markDirty(player, data);

        player.sendSystemMessage(Component.literal("+" + amount + " " + skill.displayName() + " XP"), true);

        int newLevel = SkillXpCurve.levelForXp(newXp);
        if (newLevel > oldLevel) {
            player.sendSystemMessage(Component.literal(skill.displayName() + " skill is now level " + newLevel + "!"));
        }
    }

    public static long getSkillXp(ServerPlayer player, GreenwardSkill skill) {
        return data(player).skillXp.getOrDefault(skill, 0L);
    }

    public static int getSkillLevel(ServerPlayer player, GreenwardSkill skill) {
        return SkillXpCurve.levelForXp(getSkillXp(player, skill));
    }

    // --- Proofs ---

    public static boolean hasProof(ServerPlayer player, GreenwardProof proof) {
        return data(player).completedProofs.contains(proof);
    }

    /** The only way a Proof is ever marked done — every call site is a player-verified
     *  action; there is deliberately no path here reachable from machine code. */
    public static void completeProof(ServerPlayer player, GreenwardProof proof) {
        if (hasProof(player, proof)) {
            return;
        }
        PlayerProgressData data = data(player);
        data.completedProofs.add(proof);
        markDirty(player, data);
        player.sendSystemMessage(Component.literal("Proof complete: " + proof.title() + "!"));
        checkTierUnlocks(player, proof.collection(), getCollectionQuantity(player, proof.collection()));
        grantTalismanIfEarned(player, proof);
    }

    /** Four of the six shipped talismans are sourced from a specific Tier IV Proof
     *  (Design Program Update 6 § 6.3's own table) — granted the moment that Proof
     *  completes, once each. */
    private static void grantTalismanIfEarned(ServerPlayer player, GreenwardProof proof) {
        if (!GreenwardConfig.ENABLE_HEARTWOOD) {
            return;
        }
        net.minecraft.world.item.Item talisman = switch (proof) {
            case FULL_YIELD -> ModItems.SHEAF_TOKEN;
            case DOWN_DEEP -> ModItems.CUTTERS_CHARM;
            case PATIENT -> ModItems.ANGLERS_KNOT;
            case BARE_KNUCKLE -> ModItems.KNUCKLEBONE;
            default -> null;
        };
        if (talisman != null) {
            player.getInventory().placeItemBackInInventory(new net.minecraft.world.item.ItemStack(talisman, 1));
        }
    }

    // --- Lifetime cumulative counters ---

    public static void incrementLifetimeCounter(ServerPlayer player, GreenwardProof proof, long amount, long target) {
        PlayerProgressData data = data(player);
        long updated = data.lifetimeProofCounters.merge(proof, amount, Long::sum);
        markDirty(player, data);
        if (updated >= target) {
            completeProof(player, proof);
        }
    }

    public static long getLifetimeCounter(ServerPlayer player, GreenwardProof proof) {
        return data(player).lifetimeProofCounters.getOrDefault(proof, 0L);
    }

    // --- Day-scoped counters (in-memory) ---

    /** @param dayNumber the world's current day number ({@code level.getDayTime() / 24000}). */
    public static void incrementDayCounter(ServerPlayer player, GreenwardProof proof, int dayNumber, long amount, long target) {
        Map<GreenwardProof, Integer> anchors = DAY_ANCHORS.computeIfAbsent(player.getUUID(), id -> new EnumMap<>(GreenwardProof.class));
        Map<GreenwardProof, Long> counters = DAY_COUNTERS.computeIfAbsent(player.getUUID(), id -> new EnumMap<>(GreenwardProof.class));

        Integer anchor = anchors.get(proof);
        if (anchor == null || anchor != dayNumber) {
            anchors.put(proof, dayNumber);
            counters.put(proof, 0L);
        }
        long updated = counters.merge(proof, amount, Long::sum);
        if (updated >= target) {
            completeProof(player, proof);
        }
    }

    // --- Streak counters (in-memory) ---

    public static void incrementStreak(ServerPlayer player, GreenwardProof proof, long amount, long target) {
        Map<GreenwardProof, Long> streaks = STREAK_COUNTERS.computeIfAbsent(player.getUUID(), id -> new EnumMap<>(GreenwardProof.class));
        long updated = streaks.merge(proof, amount, Long::sum);
        if (updated >= target) {
            completeProof(player, proof);
        }
    }

    public static void resetStreak(ServerPlayer player, GreenwardProof proof) {
        Map<GreenwardProof, Long> streaks = STREAK_COUNTERS.get(player.getUUID());
        if (streaks != null) {
            streaks.put(proof, 0L);
        }
    }

    public static long getStreak(ServerPlayer player, GreenwardProof proof) {
        Map<GreenwardProof, Long> streaks = STREAK_COUNTERS.get(player.getUUID());
        return streaks == null ? 0L : streaks.getOrDefault(proof, 0L);
    }

    // --- Stockpile-check proofs (no counter needed — a live threshold check) ---

    /** Full Silo, Full Cellar, Fallow No More — checked every 20 ticks alongside the stat
     *  recompute, same cadence as everything else in that loop. */
    public static void checkStockpileProofs(ServerPlayer player) {
        if (!hasProof(player, GreenwardProof.FULL_SILO) && countInInventory(player, ModBlocks.WHEAT_BALE_BLOCK == null ? null : ModBlocks.WHEAT_BALE_BLOCK.asItem()) >= 512) {
            completeProof(player, GreenwardProof.FULL_SILO);
        }
        if (!hasProof(player, GreenwardProof.FULL_CELLAR) && countInInventory(player, ModItems.POTATO_CRATE) >= 512) {
            completeProof(player, GreenwardProof.FULL_CELLAR);
        }
        if (!hasProof(player, GreenwardProof.FALLOW_NO_MORE) && GreenwardConfig.ENABLE_FERTILIZER
                && FertilizedFarmlandData.get(player.level()).allPositions().size() >= 256) {
            completeProof(player, GreenwardProof.FALLOW_NO_MORE);
        }
    }

    private static long countInInventory(ServerPlayer player, net.minecraft.world.item.Item item) {
        if (item == null) {
            return 0;
        }
        long count = 0;
        for (net.minecraft.world.item.ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static void clearEphemeral(ServerPlayer player) {
        DAY_COUNTERS.remove(player.getUUID());
        DAY_ANCHORS.remove(player.getUUID());
        STREAK_COUNTERS.remove(player.getUUID());
    }
}
