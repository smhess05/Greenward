package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every player's Coin balance — a virtual number, never a physical item (the user's own
 * explicit call, matching how SkyBlock's own Purse works: a HUD number, not a stack that
 * clutters an inventory or hits the 64-per-stack cap). Anchored to the overworld's data
 * storage, same convention as every other per-UUID map this project keeps (see
 * {@link VillagerCommissionData}).
 */
public final class PlayerPurseData extends SavedData {
    private static final Codec<Map<UUID, Long>> BALANCES_CODEC =
            Codec.unboundedMap(net.minecraft.core.UUIDUtil.STRING_CODEC, Codec.LONG);

    private static final Codec<PlayerPurseData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BALANCES_CODEC.fieldOf("balances").forGetter(data -> new HashMap<>(data.balances))
    ).apply(instance, PlayerPurseData::new));

    public static final SavedDataType<PlayerPurseData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "player_purses"),
            PlayerPurseData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<UUID, Long> balances;

    private PlayerPurseData() {
        this(Map.of());
    }

    private PlayerPurseData(Map<UUID, Long> balances) {
        this.balances = new HashMap<>(balances);
    }

    public static PlayerPurseData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public long get(UUID player) {
        return balances.getOrDefault(player, 0L);
    }

    /** @param amount may be negative (a spend already validated by the caller). */
    public void add(UUID player, long amount) {
        balances.merge(player, amount, Long::sum);
        setDirty();
    }
}
