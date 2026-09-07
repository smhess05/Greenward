package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Satchel's held materials — item counts only, no per-stack components, matching
 * every material this holds (raw drops and their compression-ladder items) being plain
 * data-free items. Held on the Satchel item itself via {@link GreenwardComponents#SATCHEL_CONTENTS}
 * — "custom items — silently dropped from inventories — acceptable" per the Permanence
 * Charter, so this carries zero world-state risk.
 */
public record SatchelContents(Map<Item, Integer> counts, int capacity) {

    /** Tier VI is the recipe's own gate (see the four Satchel advancements), so a freshly
     *  crafted Satchel is never below this — the vacuum tick refreshes it to the player's
     *  actual live tier on its very next pass regardless. */
    public static final int DEFAULT_CAPACITY = 256;

    public static final SatchelContents EMPTY = new SatchelContents(Map.of(), DEFAULT_CAPACITY);

    private static final Codec<Map<Item, Integer>> COUNTS_CODEC =
            Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Codec.INT);

    public static final Codec<SatchelContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COUNTS_CODEC.fieldOf("counts").forGetter(SatchelContents::counts),
            Codec.INT.fieldOf("capacity").forGetter(SatchelContents::capacity)
    ).apply(instance, SatchelContents::new));

    public int totalCount() {
        int total = 0;
        for (int count : counts.values()) {
            total += count;
        }
        return total;
    }

    public boolean isEmpty() {
        return counts.isEmpty();
    }

    public SatchelContents withAdded(Item item, int amount) {
        Map<Item, Integer> next = new LinkedHashMap<>(counts);
        next.merge(item, amount, Integer::sum);
        return new SatchelContents(next, capacity);
    }

    public SatchelContents withCapacity(int newCapacity) {
        return capacity == newCapacity ? this : new SatchelContents(counts, newCapacity);
    }
}
