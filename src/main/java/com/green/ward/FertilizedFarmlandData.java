package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permanence Charter (§ 1.2 rule 4): "fertilized" is a property of a {@link BlockPos},
 * never a distinct block. The farmland underneath stays real {@code minecraft:farmland}
 * forever — this class is the entire fertilization record, stored in this level's own
 * save-data file (outside any chunk/block data vanilla or another mod would ever read).
 * Remove the mod and this file is simply never opened again; the farmland was never
 * anything but vanilla farmland.
 *
 * One instance per {@link ServerLevel} (via {@code getDataStorage()}), so dimension
 * scoping falls out for free — no need to key by dimension manually.
 */
public final class FertilizedFarmlandData extends SavedData {
    private static final Codec<FertilizedFarmlandData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("positions").forGetter(data -> new ArrayList<>(data.positions))
    ).apply(instance, FertilizedFarmlandData::new));

    public static final SavedDataType<FertilizedFarmlandData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "fertilized_farmland"),
            FertilizedFarmlandData::new, CODEC, DataFixTypes.LEVEL);

    private final Set<BlockPos> positions;

    private FertilizedFarmlandData() {
        this(List.of());
    }

    private FertilizedFarmlandData(List<BlockPos> positions) {
        this.positions = new HashSet<>(positions);
    }

    public static FertilizedFarmlandData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isFertilized(BlockPos pos) {
        return positions.contains(pos.immutable());
    }

    public void setFertilized(BlockPos pos) {
        if (positions.add(pos.immutable())) {
            setDirty();
        }
    }

    public void clearFertilized(BlockPos pos) {
        if (positions.remove(pos)) {
            setDirty();
        }
    }

    /** Used by {@code /greenward decommission} — every recorded position, for a bulk sweep. */
    public Set<BlockPos> allPositions() {
        return Set.copyOf(positions);
    }
}
