package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every villager's current Commission (Design Program Update 8 § 8.1) — one at a time
 * per villager, keyed by UUID, refreshing once per Minecraft day as an approximation of
 * "the vanilla restock cycle" (the source text's own phrase; a real restock-tick hook
 * would need reading each villager's internal offer-refresh timer, which isn't exposed
 * by a stable API — a day-based refresh is close enough for the same practical effect:
 * commissions don't refresh on demand).
 */
public final class VillagerCommissionData extends SavedData {
    public record Commission(Item item, int count, int sealReward, int dayAssigned) {}

    private static final Codec<Commission> COMMISSION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Commission::item),
            Codec.INT.fieldOf("count").forGetter(Commission::count),
            Codec.INT.fieldOf("seal_reward").forGetter(Commission::sealReward),
            Codec.INT.fieldOf("day_assigned").forGetter(Commission::dayAssigned)
    ).apply(instance, Commission::new));

    private static final Codec<Map<UUID, Commission>> MAP_CODEC =
            Codec.unboundedMap(net.minecraft.core.UUIDUtil.STRING_CODEC, COMMISSION_CODEC);

    private static final Codec<VillagerCommissionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MAP_CODEC.fieldOf("commissions").forGetter(data -> new HashMap<>(data.commissions))
    ).apply(instance, VillagerCommissionData::new));

    public static final SavedDataType<VillagerCommissionData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "villager_commissions"),
            VillagerCommissionData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<UUID, Commission> commissions;

    private VillagerCommissionData() {
        this(Map.of());
    }

    private VillagerCommissionData(Map<UUID, Commission> commissions) {
        this.commissions = new HashMap<>(commissions);
    }

    public static VillagerCommissionData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Commission get(UUID villagerId) {
        return commissions.get(villagerId);
    }

    public void set(UUID villagerId, Commission commission) {
        commissions.put(villagerId, commission);
        setDirty();
    }

    public void clear(UUID villagerId) {
        commissions.remove(villagerId);
        setDirty();
    }
}
