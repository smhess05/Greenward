package com.green.ward;

import net.minecraft.util.StringRepresentable;

/**
 * Every Proof (Design Program Update 2 § 2.2), one per (collection, tier ≥ IV) that
 * either the spec names explicitly or the § 2.4 recipe retrofit needs. 11 of these are
 * verbatim from the spec's own table; the other 13 are invented to fill tiers the
 * retrofit table references but the spec's example table doesn't cover (it only gives
 * 11 worked examples, not all ~63 possible collection/tier slots) — built from a small
 * set of reusable, skill-based patterns (a single-day volume push, a stockpile check, an
 * unbroken streak, a depth threshold, a time-of-day condition) rather than one bespoke
 * mechanic per proof, and flagged as invented in their own doc comment rather than
 * silently presented as spec content.
 *
 * <p>One deliberate adjustment against the literal spec text, documented here since it's
 * the one genuine contradiction found in the source material: {@link #FULL_YIELD} as
 * literally written ("harvest a complete mature 9×9 in a single hoe swing") requires
 * radius 4, which only Harvest Warden + full Warden's Garb reaches — but Harvest Warden
 * itself is gated behind Wheat IX in the very same retrofit table, which can't be
 * reached without first completing Wheat IV. Implemented radius-agnostic instead:
 * "every tile your currently-equipped hoe can reach was mature and got harvested," which
 * preserves the actual point of the proof (a clean, fully-planned harvest, not a lucky
 * partial one) without the circular tool dependency.
 */
public enum GreenwardProof implements StringRepresentable {
    // --- Wheat ---
    FULL_YIELD(GreenwardCollection.WHEAT, 4, "Full Yield",
            "Harvest a complete mature field in a single hoe swing (every tile your hoe's radius covers)."),
    BUMPER_CROP(GreenwardCollection.WHEAT, 5, "Bumper Crop",
            "Harvest 300 wheat in a single Minecraft day."), // invented — fills Wheat V for the Auto-Harvester retrofit slot
    RAINFED(GreenwardCollection.WHEAT, 6, "Rainfed",
            "Harvest 500 wheat during rainfall."),
    FULL_SILO(GreenwardCollection.WHEAT, 7, "Full Silo",
            "Hold 512 Wheat Bale in your inventory at once."), // invented — fills Wheat VII for Cultivator's
    FALLOW_NO_MORE(GreenwardCollection.WHEAT, 8, "Fallow No More",
            "Have 256 fertilized farmland blocks active at once."),
    UNBROKEN_HARVEST(GreenwardCollection.WHEAT, 9, "Unbroken Harvest",
            "Complete 20 full-field hoe swings in a row with nothing wasted in between."), // invented — fills Wheat IX for Harvest Core

    // --- Potato ---
    DUG_IN(GreenwardCollection.POTATO, 5, "Dug In",
            "Harvest 300 potatoes in a single Minecraft day."), // invented — fills Potato V for Cultivator's
    FULL_CELLAR(GreenwardCollection.POTATO, 8, "Full Cellar",
            "Hold 512 Potato Crate in your inventory at once."), // invented — fills Potato VIII for Harvest Core

    // --- Carrot ---
    GOLDEN_HOUR(GreenwardCollection.CARROT, 7, "Golden Hour",
            "Harvest 300 carrots in a single Minecraft day."), // invented — fills Carrot VII for Harvest Core

    // --- Diamond ---
    VEIN_RUNNER(GreenwardCollection.DIAMOND, 6, "Vein Runner",
            "Mine 20 diamond ore in a single Minecraft day."), // invented — fills Diamond VI for the Ascension Template

    // --- Cobblestone ---
    DOWN_DEEP(GreenwardCollection.COBBLESTONE, 4, "Down Deep",
            "Mine 1,000 blocks below Y -50."),
    QUARRY_SHIFT(GreenwardCollection.COBBLESTONE, 5, "Quarry Shift",
            "Mine 2,000 cobblestone in a single Minecraft day."), // invented — fills Cobblestone V for the Auto-Miner
    SINGLE_BREATH(GreenwardCollection.COBBLESTONE, 6, "Single Breath",
            "Mine a 64-block vein without surfacing above Y 0."),
    BEDROCK_BOUND(GreenwardCollection.COBBLESTONE, 7, "Bedrock Bound",
            "Mine 100 blocks at Y -60 or below."), // invented — fills Cobblestone VII for Excavator's

    // --- Deepslate ---
    DEEP_VEIN(GreenwardCollection.DEEPSLATE, 5, "Deep Vein",
            "Mine 64 deepslate in a single unbroken vein-mining streak."), // invented — fills Deepslate V for Excavator's
    COLD_IRON(GreenwardCollection.DEEPSLATE, 7, "Cold Iron",
            "Mine 500 deepslate with no torch placed within 16 blocks."),

    // --- Cod ---
    PATIENT(GreenwardCollection.COD, 4, "Patient",
            "Land 3 sea creatures without leaving the water."),
    FULL_NET(GreenwardCollection.COD, 5, "Full Net",
            "Land 10 cod in a single Minecraft day."), // invented — fills Cod V for the Auto-Fisher
    STORM_CATCH(GreenwardCollection.COD, 7, "Storm Catch",
            "Land a sea creature during a thunderstorm."),

    // --- Bone ---
    BARE_KNUCKLE(GreenwardCollection.BONE, 4, "Bare-Knuckle",
            "Kill 50 hostiles wearing no chestplate."),
    MARKED_HUNTER(GreenwardCollection.BONE, 6, "Marked Hunter",
            "Kill 10 Marked in a single Minecraft day."),
    GRAVEYARD_SHIFT(GreenwardCollection.BONE, 7, "Graveyard Shift",
            "Kill 100 hostiles in melee between midnight and dawn."), // invented — fills Bone VII for Ashwrought

    // --- Gunpowder ---
    POWDER_TRAIL(GreenwardCollection.GUNPOWDER, 5, "Powder Trail",
            "Obtain 200 gunpowder in a single Minecraft day."), // invented — fills Gunpowder V for Ashwrought
    HELD_NERVE(GreenwardCollection.GUNPOWDER, 7, "Held Nerve",
            "Kill 25 creepers in melee without taking creeper damage.");

    private final GreenwardCollection collection;
    private final int tier;
    private final String title;
    private final String requirement;

    GreenwardProof(GreenwardCollection collection, int tier, String title, String requirement) {
        this.collection = collection;
        this.tier = tier;
        this.title = title;
        this.requirement = requirement;
    }

    public GreenwardCollection collection() {
        return collection;
    }

    public int tier() {
        return tier;
    }

    public String title() {
        return title;
    }

    public String requirement() {
        return requirement;
    }

    /** The Proof, if any, gating this (collection, tier) pair. */
    public static GreenwardProof forTier(GreenwardCollection collection, int tier) {
        for (GreenwardProof proof : values()) {
            if (proof.collection == collection && proof.tier == tier) {
                return proof;
            }
        }
        return null;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
