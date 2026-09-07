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
 * Blight (Design Program Update 9 § 9.1) — a side-data condition on over-fertilized
 * farmland, never a block (§ 1.2 rule 4, same reasoning as {@link FertilizedFarmlandData}
 * itself): the tile underneath stays real {@code minecraft:farmland} forever, so removing
 * the mod just leaves ordinary farmland behind, Blight and all, with nothing to clean up.
 */
public final class BlightData extends SavedData {
    private static final Codec<BlightData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("positions").forGetter(data -> new ArrayList<>(data.positions))
    ).apply(instance, BlightData::new));

    public static final SavedDataType<BlightData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "blight"),
            BlightData::new, CODEC, DataFixTypes.LEVEL);

    private final Set<BlockPos> positions;

    private BlightData() {
        this(List.of());
    }

    private BlightData(List<BlockPos> positions) {
        this.positions = new HashSet<>(positions);
    }

    public static BlightData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isBlighted(BlockPos pos) {
        return positions.contains(pos.immutable());
    }

    public void setBlighted(BlockPos pos) {
        if (positions.add(pos.immutable())) {
            setDirty();
        }
    }

    public void clearBlighted(BlockPos pos) {
        if (positions.remove(pos)) {
            setDirty();
        }
    }
}
