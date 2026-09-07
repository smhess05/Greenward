package com.green.ward;

import net.minecraft.world.item.Item;

import java.util.Map;

/**
 * The six shipped talismans (Design Program Update 6 § 6.3) — only these 6 of the
 * spec's 20 are buildable yet; Miller's Favour and Quarryman's Nail need Update 8's
 * villager Prosperity system to even exist as a source, and the remaining 12 were never
 * specified by the source document at all (its own Appendix C names this as an open
 * item). Effect magnitudes are exactly the spec's own numbers; Sigils double them.
 *
 * <p>Deliberately not a stat on the item itself — see {@link ModItems#talisman}'s doc
 * comment. {@code HeartwoodStatContributor} reads this lookup for every socketed item.
 */
enum TalismanType {
    SHEAF_TOKEN(GreenwardStat.FARMING_FORTUNE, 8.0),
    CUTTERS_CHARM(GreenwardStat.MINING_SPEED, 12.0),
    ANGLERS_KNOT(GreenwardStat.SEA_CREATURE_CHANCE, 3.0),
    KNUCKLEBONE(GreenwardStat.STRENGTH, 8.0),
    WRAITHS_EYE(GreenwardStat.CRIT_CHANCE, 4.0),
    DEEPGLASS_LENS(GreenwardStat.FISHING_SPEED, 15.0);

    private final GreenwardStat stat;
    private final double baseMagnitude;

    TalismanType(GreenwardStat stat, double baseMagnitude) {
        this.stat = stat;
        this.baseMagnitude = baseMagnitude;
    }

    private Item baseItem() {
        return switch (this) {
            case SHEAF_TOKEN -> ModItems.SHEAF_TOKEN;
            case CUTTERS_CHARM -> ModItems.CUTTERS_CHARM;
            case ANGLERS_KNOT -> ModItems.ANGLERS_KNOT;
            case KNUCKLEBONE -> ModItems.KNUCKLEBONE;
            case WRAITHS_EYE -> ModItems.WRAITHS_EYE;
            case DEEPGLASS_LENS -> ModItems.DEEPGLASS_LENS;
        };
    }

    private Item sigilItem() {
        return switch (this) {
            case SHEAF_TOKEN -> ModItems.SHEAF_TOKEN_SIGIL;
            case CUTTERS_CHARM -> ModItems.CUTTERS_CHARM_SIGIL;
            case ANGLERS_KNOT -> ModItems.ANGLERS_KNOT_SIGIL;
            case KNUCKLEBONE -> ModItems.KNUCKLEBONE_SIGIL;
            case WRAITHS_EYE -> ModItems.WRAITHS_EYE_SIGIL;
            case DEEPGLASS_LENS -> ModItems.DEEPGLASS_LENS_SIGIL;
        };
    }

    /** @return the stat grant for {@code item} if it's a recognized talisman or sigil, else {@code null}. */
    static Map<GreenwardStat, Double> statsFor(Item item) {
        for (TalismanType type : values()) {
            if (type.baseItem() == item) {
                return Map.of(type.stat, type.baseMagnitude);
            }
            if (type.sigilItem() == item) {
                return Map.of(type.stat, type.baseMagnitude * 2.0);
            }
        }
        return null;
    }

    static boolean isTalismanOrSigil(Item item) {
        return statsFor(item) != null;
    }
}
