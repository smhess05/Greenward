package com.green.ward;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The five Effigies (Design Program Update 4 § 4.3) — combat automation that generates
 * base mob drops on a timer, spawning and killing nothing. Each type's {@link
 * #rollDrops} is a flat approximation of one mob's typical drop range rather than an
 * exact loot-table replica, the same documented tradeoff already accepted for Bone/
 * Gunpowder collection counting (see {@code GreenwardCombatHandler}'s own doc comment).
 *
 * <p>The recipe center ingredient ("the mob's rung-2 compressed drop" per spec) only
 * exists for Bone (Bonepile → Bone Reliquary) and Gunpowder (Volatile → Powder Cache) —
 * Rotten Flesh, String, and Ender Pearl were never tracked collections, so those three
 * substitute their own raw item as the center ingredient instead (see each block's own
 * recipe JSON). Documented here since it's a deviation from the spec's literal wording,
 * not an oversight.
 */
public enum EffigyType {
    ROTTING("rotting_effigy", 100),
    BONEPILE("bonepile_effigy", 100),
    WEBBED("webbed_effigy", 120),
    VOLATILE("volatile_effigy", 140),
    VOID("void_effigy", 400);

    private final String id;
    private final int baseInterval;

    EffigyType(String id, int baseInterval) {
        this.id = id;
        this.baseInterval = baseInterval;
    }

    public String id() {
        return id;
    }

    public int baseInterval() {
        return baseInterval;
    }

    /** Flat per-operation drop roll, approximating one mob kill's typical yield. */
    public List<ItemStack> rollDrops(RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        switch (this) {
            case ROTTING -> addRange(drops, random, Items.ROTTEN_FLESH, 0, 2);
            case BONEPILE -> {
                addRange(drops, random, Items.BONE, 0, 2);
                addRange(drops, random, Items.ARROW, 0, 2);
            }
            case WEBBED -> {
                addRange(drops, random, Items.STRING, 0, 2);
                if (random.nextFloat() < 0.34F) {
                    drops.add(new ItemStack(Items.SPIDER_EYE, 1));
                }
            }
            case VOLATILE -> addRange(drops, random, Items.GUNPOWDER, 0, 2);
            case VOID -> {
                if (random.nextFloat() < 0.5F) {
                    drops.add(new ItemStack(Items.ENDER_PEARL, 1));
                }
            }
        }
        return drops;
    }

    private static void addRange(List<ItemStack> drops, RandomSource random, net.minecraft.world.item.Item item, int min, int max) {
        int count = min + random.nextInt(max - min + 1);
        if (count > 0) {
            drops.add(new ItemStack(item, count));
        }
    }
}
