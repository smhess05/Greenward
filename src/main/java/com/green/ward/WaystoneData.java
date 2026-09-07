package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Every player's own list of bound Waystones (user-requested Coin sink: fast travel).
 * Dimension is stored as a plain identifier string rather than a real
 * {@code ResourceKey<Level>} — simpler codec, and resolving it back to a live level only
 * ever needs {@code server.getLevel(ResourceKey.create(Registries.DIMENSION, id))} at
 * teleport time anyway.
 */
public final class WaystoneData extends SavedData {
    public record Waypoint(String name, String dimension, BlockPos pos) {}

    private static final Codec<Waypoint> WAYPOINT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Waypoint::name),
            Codec.STRING.fieldOf("dimension").forGetter(Waypoint::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(Waypoint::pos)
    ).apply(instance, Waypoint::new));

    private static final Codec<Map<UUID, List<Waypoint>>> MAP_CODEC =
            Codec.unboundedMap(net.minecraft.core.UUIDUtil.STRING_CODEC, WAYPOINT_CODEC.listOf());

    private static final Codec<WaystoneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MAP_CODEC.fieldOf("waypoints").forGetter(data -> new HashMap<>(data.waypoints))
    ).apply(instance, WaystoneData::new));

    public static final SavedDataType<WaystoneData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "waystones"),
            WaystoneData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<UUID, List<Waypoint>> waypoints;

    private WaystoneData() {
        this(Map.of());
    }

    private WaystoneData(Map<UUID, List<Waypoint>> waypoints) {
        this.waypoints = new HashMap<>();
        waypoints.forEach((id, list) -> this.waypoints.put(id, new ArrayList<>(list)));
    }

    public static WaystoneData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public List<Waypoint> get(UUID player) {
        return waypoints.getOrDefault(player, List.of());
    }

    public Waypoint findAt(UUID player, String dimension, BlockPos pos) {
        for (Waypoint wp : get(player)) {
            if (wp.dimension().equals(dimension) && wp.pos().equals(pos)) {
                return wp;
            }
        }
        return null;
    }

    public Waypoint findByName(UUID player, String name) {
        for (Waypoint wp : get(player)) {
            if (wp.name().equalsIgnoreCase(name)) {
                return wp;
            }
        }
        return null;
    }

    public void add(UUID player, Waypoint waypoint) {
        waypoints.computeIfAbsent(player, k -> new ArrayList<>()).add(waypoint);
        setDirty();
    }
}
