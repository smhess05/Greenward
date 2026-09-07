package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks every placed automation block (Auto-Harvester/Miner/Fisher/Effigy) across the
 * whole save, for {@link MachinePlacementGuard}'s per-world (24) cap — Design Program
 * Update 4 § 4.1③ (the per-chunk cap it also specified was removed at the user's
 * request). Custom player data, dropped harmlessly on uninstall (§ 1.1) — this is
 * bookkeeping, not world state; the blocks it counts are the real source of truth and
 * this file just goes stale and unread with the mod removed.
 *
 * <p>Anchored to the overworld's own data storage regardless of which dimension a given
 * machine actually sits in ({@link #get} always resolves via {@code server.overworld()})
 * since a per-world (not per-dimension) count needs one shared instance, and {@code
 * SavedData} itself has no storage location that isn't tied to a specific {@code
 * ServerLevel}.
 */
public final class MachineCountData extends SavedData {
    private static final Codec<MachineCountData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.listOf().fieldOf("positions").forGetter(data -> new ArrayList<>(data.positions))
    ).apply(instance, MachineCountData::new));

    public static final SavedDataType<MachineCountData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "machine_counts"),
            MachineCountData::new, CODEC, DataFixTypes.LEVEL);

    private final Set<GlobalPos> positions;

    private MachineCountData() {
        this(List.of());
    }

    private MachineCountData(List<GlobalPos> positions) {
        this.positions = new HashSet<>(positions);
    }

    public static MachineCountData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public int worldTotal() {
        return positions.size();
    }

    public void register(ResourceKey<Level> dimension, BlockPos pos) {
        if (positions.add(GlobalPos.of(dimension, pos.immutable()))) {
            setDirty();
        }
    }

    public void unregister(ResourceKey<Level> dimension, BlockPos pos) {
        if (positions.remove(GlobalPos.of(dimension, pos.immutable()))) {
            setDirty();
        }
    }
}
