package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The one Heartwood's shared, world-wide progress (Design Program Update 6) — branch
 * unlocks and socketed talismans benefit every player, matching the spec's own framing
 * of a single placed block ("One per world") rather than a per-player structure. Anchored
 * to the overworld's data storage, same convention as {@link MachineCountData}.
 *
 * <p>Custom player-adjacent data, dropped harmlessly on uninstall (§ 1.1) — none of this
 * is world state; a missing Heartwood block just means this file is never read again.
 */
public final class HeartwoodData extends SavedData {
    private static final Codec<Map<HeartwoodBranch, Integer>> UNLOCKED_CODEC =
            Codec.unboundedMap(StringRepresentable.fromEnum(HeartwoodBranch::values), Codec.INT);

    private static final Codec<HeartwoodData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.optionalFieldOf("position").forGetter(data -> java.util.Optional.ofNullable(data.position)),
            UNLOCKED_CODEC.fieldOf("unlocked").forGetter(data -> new HashMap<>(data.unlockedCount)),
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("talismans").forGetter(data -> new ArrayList<>(data.socketedTalismans)),
            Codec.BOOL.optionalFieldOf("threat_enabled", false).forGetter(data -> data.threatEnabled)
    ).apply(instance, HeartwoodData::new));

    public static final SavedDataType<HeartwoodData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "heartwood"),
            HeartwoodData::new, CODEC, DataFixTypes.LEVEL);

    private static final int BASE_SOCKETS = 4;
    private static final double RESPEC_REFUND = 0.75;

    private BlockPos position;
    private final Map<HeartwoodBranch, Integer> unlockedCount;
    private final List<Item> socketedTalismans;
    private boolean threatEnabled;

    private HeartwoodData() {
        this(java.util.Optional.empty(), Map.of(), List.of(), false);
    }

    private HeartwoodData(java.util.Optional<BlockPos> position, Map<HeartwoodBranch, Integer> unlockedCount,
                           List<Item> socketedTalismans, boolean threatEnabled) {
        this.position = position.orElse(null);
        this.unlockedCount = new EnumMap<>(HeartwoodBranch.class);
        for (HeartwoodBranch branch : HeartwoodBranch.values()) {
            this.unlockedCount.put(branch, unlockedCount.getOrDefault(branch, 0));
        }
        this.socketedTalismans = new ArrayList<>(socketedTalismans);
        this.threatEnabled = threatEnabled;
    }

    public static HeartwoodData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean hasPosition() {
        return position != null;
    }

    /** Threat (Update 7) is OFF by default even once the Heartwood is placed — the user's
     *  explicit request during playtesting was to gate world difficulty scaling behind a
     *  deliberate opt-in at the Heartwood rather than an always-on world property, so
     *  players get a vanilla-difficulty grace period until they choose otherwise. See
     *  {@link ThreatManager#computeThreat}, which returns 0 whenever this is false or no
     *  Heartwood has ever been placed. */
    public boolean isThreatEnabled() {
        return threatEnabled;
    }

    public void setThreatEnabled(boolean enabled) {
        this.threatEnabled = enabled;
        setDirty();
    }

    public BlockPos position() {
        return position;
    }

    public void setPosition(BlockPos pos) {
        this.position = pos.immutable();
        setDirty();
    }

    public void clearPosition() {
        this.position = null;
        setDirty();
    }

    public int unlockedCount(HeartwoodBranch branch) {
        return unlockedCount.get(branch);
    }

    public boolean isNodeUnlocked(HeartwoodBranch branch, String nodeId) {
        List<HeartwoodNode> nodes = branch.nodes();
        int unlocked = unlockedCount(branch);
        for (int i = 0; i < unlocked && i < nodes.size(); i++) {
            if (nodes.get(i).id().equals(nodeId)) {
                return true;
            }
        }
        return false;
    }

    /** @return the next locked node in this branch, or null if the branch is complete. */
    public HeartwoodNode nextNode(HeartwoodBranch branch) {
        List<HeartwoodNode> nodes = branch.nodes();
        int unlocked = unlockedCount(branch);
        return unlocked < nodes.size() ? nodes.get(unlocked) : null;
    }

    public void unlockNext(HeartwoodBranch branch) {
        unlockedCount.merge(branch, 1, Integer::sum);
        setDirty();
    }

    /** Resets a branch to 0 nodes, returning 75% of each unlocked node's cost for the
     *  caller to hand back to the player. */
    public Map<Item, Integer> respec(HeartwoodBranch branch) {
        Map<Item, Integer> refund = new HashMap<>();
        List<HeartwoodNode> nodes = branch.nodes();
        int unlocked = unlockedCount(branch);
        for (int i = 0; i < unlocked && i < nodes.size(); i++) {
            nodes.get(i).cost().forEach((item, count) ->
                    refund.merge(item, (int) Math.floor(count * RESPEC_REFUND), Integer::sum));
        }
        unlockedCount.put(branch, 0);
        setDirty();
        return refund;
    }

    public int totalSockets() {
        int bonus = 0;
        for (HeartwoodBranch branch : HeartwoodBranch.values()) {
            List<HeartwoodNode> nodes = branch.nodes();
            int unlocked = unlockedCount(branch);
            for (int i = 0; i < unlocked && i < nodes.size(); i++) {
                if (nodes.get(i).grantsSocket()) {
                    bonus++;
                }
            }
        }
        return BASE_SOCKETS + bonus;
    }

    public List<Item> socketedTalismans() {
        return List.copyOf(socketedTalismans);
    }

    /** Replaces the whole socketed list wholesale — {@link HeartwoodMenu}'s talisman
     *  slots are the interaction surface, this is just kept in sync with their current
     *  contents rather than mutated one socket at a time. */
    public void setSocketedTalismans(List<Item> talismans) {
        socketedTalismans.clear();
        socketedTalismans.addAll(talismans);
        setDirty();
    }
}
