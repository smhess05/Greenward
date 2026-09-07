package com.green.ward;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Map;

/**
 * Custom item data components — plain item data, not world state (§ 1.1: "custom
 * items — silently dropped from inventories — acceptable"), so these carry zero
 * permanence risk regardless of what they hold.
 */
public final class GreenwardComponents {
    private GreenwardComponents() {}

    private static final Codec<Map<GreenwardStat, Double>> STATS_CODEC =
            Codec.unboundedMap(StringRepresentableCodecs.GREENWARD_STAT, Codec.DOUBLE);

    /** The flat stat grants an item makes while equipped/held — see {@link EquipmentStatContributor}. */
    public static DataComponentType<Map<GreenwardStat, Double>> STATS;

    public static DataComponentType<GreenwardRarity> RARITY;

    /** A Satchel's held materials — see {@link SatchelContents}. */
    public static DataComponentType<SatchelContents> SATCHEL_CONTENTS;

    /** Gem sockets on Tier II/III gear — see {@link SocketData}. */
    public static DataComponentType<SocketData> SOCKETS;

    public static void initialize() {
        STATS = register("stats", DataComponentType.<Map<GreenwardStat, Double>>builder()
                .persistent(STATS_CODEC)
                .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(STATS_CODEC))
                .build());

        RARITY = register("rarity", DataComponentType.<GreenwardRarity>builder()
                .persistent(StringRepresentableCodecs.GREENWARD_RARITY)
                .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(StringRepresentableCodecs.GREENWARD_RARITY))
                .build());

        SATCHEL_CONTENTS = register("satchel_contents", DataComponentType.<SatchelContents>builder()
                .persistent(SatchelContents.CODEC)
                .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SatchelContents.CODEC))
                .build());

        SOCKETS = register("sockets", DataComponentType.<SocketData>builder()
                .persistent(SocketData.CODEC)
                .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SocketData.CODEC))
                .build());
    }

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        ResourceKey<DataComponentType<?>> key =
                ResourceKey.create(Registries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, name));
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, type);
    }
}
