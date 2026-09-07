package com.green.ward;

import net.minecraft.world.item.Item;

import java.util.Map;

/**
 * The four cuttable gems (Design Program Update 5 § 5.2) and what a socketed gem grants
 * at each of its four cut qualities (Rough/Fine/Flawless/Perfect, indices 0-3). The
 * per-cut magnitudes (5/15/35/75) aren't in the source document — it only specifies
 * "grants its stat scaled by cut" — so this is a documented, invented progression rather
 * than a literal spec number.
 */
enum GemMaterial {
    AMBER,
    JADE,
    SAPPHIRE,
    RUBY;

    private static final double[] MAGNITUDE_BY_CUT = {5.0, 15.0, 35.0, 75.0};

    /** @return the stat grant for {@code item} if it's a recognized cut gem, else {@code null}. */
    static Map<GreenwardStat, Double> statsFor(Item item) {
        for (GemMaterial gem : values()) {
            for (int cut = 0; cut < 4; cut++) {
                if (gem.itemForCut(cut) == item) {
                    return gem.statMap(cut);
                }
            }
        }
        return null;
    }

    static boolean isGem(Item item) {
        return statsFor(item) != null;
    }

    private Item itemForCut(int cut) {
        return switch (this) {
            case AMBER -> switch (cut) {
                case 0 -> ModItems.ROUGH_AMBER;
                case 1 -> ModItems.FINE_AMBER;
                case 2 -> ModItems.FLAWLESS_AMBER;
                default -> ModItems.PERFECT_AMBER;
            };
            case JADE -> switch (cut) {
                case 0 -> ModItems.ROUGH_JADE;
                case 1 -> ModItems.FINE_JADE;
                case 2 -> ModItems.FLAWLESS_JADE;
                default -> ModItems.PERFECT_JADE;
            };
            case SAPPHIRE -> switch (cut) {
                case 0 -> ModItems.ROUGH_SAPPHIRE;
                case 1 -> ModItems.FINE_SAPPHIRE;
                case 2 -> ModItems.FLAWLESS_SAPPHIRE;
                default -> ModItems.PERFECT_SAPPHIRE;
            };
            case RUBY -> switch (cut) {
                case 0 -> ModItems.ROUGH_RUBY;
                case 1 -> ModItems.FINE_RUBY;
                case 2 -> ModItems.FLAWLESS_RUBY;
                default -> ModItems.PERFECT_RUBY;
            };
        };
    }

    private Map<GreenwardStat, Double> statMap(int cut) {
        double magnitude = MAGNITUDE_BY_CUT[cut];
        return switch (this) {
            case AMBER -> Map.of(GreenwardStat.MINING_SPEED, magnitude);
            case JADE -> Map.of(GreenwardStat.MINING_FORTUNE, magnitude);
            case SAPPHIRE -> Map.of(
                    GreenwardStat.FISHING_SPEED, magnitude,
                    GreenwardStat.SEA_CREATURE_CHANCE, magnitude / 10.0);
            case RUBY -> Map.of(GreenwardStat.STRENGTH, magnitude);
        };
    }
}
