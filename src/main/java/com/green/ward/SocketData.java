package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Gem sockets on Tier II/III gear (Design Program Update 5 § 5.2) — Tier II gets 2,
 * Tier III gets 3 (a flat rule off the spec's "1-3" range, since no per-item allocation
 * was specified). {@code socketedGems} holds the cut gem items inserted so far, in
 * insertion order; its size can never exceed {@code maxSockets}.
 */
public record SocketData(int maxSockets, List<Item> socketedGems) {

    public static final Codec<SocketData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("max_sockets").forGetter(SocketData::maxSockets),
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("socketed_gems").forGetter(SocketData::socketedGems)
    ).apply(instance, SocketData::new));

    public boolean isFull() {
        return socketedGems.size() >= maxSockets;
    }

    public SocketData withGemAdded(Item gem) {
        List<Item> next = new ArrayList<>(socketedGems);
        next.add(gem);
        return new SocketData(maxSockets, next);
    }
}
