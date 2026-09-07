package com.green.ward;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * The nine tracked collections (Design Program Update 2 § 2.1/2.4). Each is the raw
 * vanilla item a genuine player action produces — the same item that already feeds that
 * pillar's compression chain, so collection progress and material progress are the same
 * lifetime count viewed two ways. Diamond is its own collection, separate from
 * Cobblestone/Deepslate, per the retrofit table's explicit "Ascension Template at
 * Diamond VI."
 */
public enum GreenwardCollection implements StringRepresentable {
    WHEAT("wheat", "Wheat", Items.WHEAT, GreenwardSkill.FARMING),
    POTATO("potato", "Potato", Items.POTATO, GreenwardSkill.FARMING),
    CARROT("carrot", "Carrot", Items.CARROT, GreenwardSkill.FARMING),
    COBBLESTONE("cobblestone", "Cobblestone", Items.COBBLESTONE, GreenwardSkill.MINING),
    DEEPSLATE("deepslate", "Deepslate", Items.COBBLED_DEEPSLATE, GreenwardSkill.MINING),
    DIAMOND("diamond", "Diamond", Items.DIAMOND, GreenwardSkill.MINING),
    COD("cod", "Cod", Items.COD, GreenwardSkill.FISHING),
    BONE("bone", "Bone", Items.BONE, GreenwardSkill.COMBAT),
    GUNPOWDER("gunpowder", "Gunpowder", Items.GUNPOWDER, GreenwardSkill.COMBAT);

    /** Tiers I–X threshold, index 0 = Tier I. */
    public static final long[] THRESHOLDS = {50, 250, 1000, 2500, 5000, 10000, 20000, 40000, 75000, 150000};
    public static final int MAX_TIER = THRESHOLDS.length;
    /** From this tier (1-based) upward, a Proof is required alongside quantity — § 2.1. */
    public static final int PROOF_STARTS_AT_TIER = 4;

    private final String id;
    private final String displayName;
    private final Item item;
    private final GreenwardSkill skill;

    GreenwardCollection(String id, String displayName, Item item, GreenwardSkill skill) {
        this.id = id;
        this.displayName = displayName;
        this.item = item;
        this.skill = skill;
    }

    public String displayName() {
        return displayName;
    }

    /** The vanilla item whose lifetime player-obtained count this collection tracks. */
    public Item item() {
        return item;
    }

    /** The skill that gains XP alongside this collection's quantity. */
    public GreenwardSkill skill() {
        return skill;
    }

    /** @return the tier (1-based) this quantity currently satisfies on its own, 0 if below Tier I. */
    public static int tierForQuantity(long quantity) {
        int tier = 0;
        for (long threshold : THRESHOLDS) {
            if (quantity >= threshold) {
                tier++;
            } else {
                break;
            }
        }
        return tier;
    }

    public static long thresholdForTier(int tier) {
        return THRESHOLDS[tier - 1];
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
