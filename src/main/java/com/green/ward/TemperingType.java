package com.green.ward;

import net.minecraft.world.item.Item;

import java.util.Map;

/**
 * The seven Temperings (Design Program Update 8 § 8.4) — each a Tempering Stone
 * consumed with Seals via {@link TemperingRecipe} to merge a stat bonus into gear.
 * Magnitudes are invented (the source table names only which stats, not how much) —
 * picked to sit below a full Heartwood branch's total but above a single gem, since
 * Tempering is a Seals-gated economy sink, not a free progression reward.
 *
 * <p>Scope simplification, documented: the source text has Tempering require a
 * physical Tempering Station placed adjacent to a matching-profession villager.
 * {@link TemperingRecipe} skips both — a plain crafting-table interaction — to reuse
 * the same working {@link GemSocketRecipe}-style mechanism rather than building a new
 * block, adjacency check, and profession-matching UI under this session's time budget.
 * The "applies to weapons/armor/tools only" restriction is similarly not enforced —
 * any item carrying {@link GreenwardComponents#STATS} can be tempered.
 */
enum TemperingType {
    KEEN(Map.of(GreenwardStat.STRENGTH, 6.0, GreenwardStat.CRIT_DAMAGE, 15.0)),
    STURDY(Map.of(GreenwardStat.HEALTH, 20.0, GreenwardStat.DEFENSE, 15.0)),
    BOUNTIFUL(Map.of(GreenwardStat.FARMING_FORTUNE, 10.0)),
    DEEP(Map.of(GreenwardStat.MINING_FORTUNE, 8.0, GreenwardStat.MINING_SPEED, 15.0)),
    BRINY(Map.of(GreenwardStat.SEA_CREATURE_CHANCE, 3.0, GreenwardStat.FISHING_SPEED, 10.0)),
    SWIFT(Map.of(GreenwardStat.SPEED, 10.0, GreenwardStat.CRIT_CHANCE, 3.0)),
    GRIM(Map.of(GreenwardStat.STRENGTH, 12.0, GreenwardStat.HEALTH, -10.0));

    private final Map<GreenwardStat, Double> grants;

    TemperingType(Map<GreenwardStat, Double> grants) {
        this.grants = grants;
    }

    public Map<GreenwardStat, Double> grants() {
        return grants;
    }

    private Item stoneItem() {
        return switch (this) {
            case KEEN -> ModItems.KEEN_STONE;
            case STURDY -> ModItems.STURDY_STONE;
            case BOUNTIFUL -> ModItems.BOUNTIFUL_STONE;
            case DEEP -> ModItems.DEEP_STONE;
            case BRINY -> ModItems.BRINY_STONE;
            case SWIFT -> ModItems.SWIFT_STONE;
            case GRIM -> ModItems.GRIM_STONE;
        };
    }

    static TemperingType forStone(Item item) {
        for (TemperingType type : values()) {
            if (type.stoneItem() == item) {
                return type;
            }
        }
        return null;
    }
}
