package com.green.ward;

/**
 * Compile-time feature flags. Flip a flag to false to disable a subsystem
 * WITHOUT deleting its code or assets. Disabled content is not registered,
 * so its recipes/items simply never appear in game.
 *
 * NOTE on ENABLE_UNGATED_TREASURE_FISHING: this flag only controls the Auto-Fisher's
 * server-side loot roll. The vanilla loot-table override that removes the open-water
 * gate for PLAYER fishing lives at src/main/resources/data/minecraft/loot_table/gameplay/fishing.json
 * — a plain data file, which cannot read this Java flag. Disabling this flag does NOT
 * remove that override; delete/rename the file yourself if you want vanilla's open-water
 * gate restored for players (see GREENWARD_README.md).
 */
public final class GreenwardConfig {
    private GreenwardConfig() {}

    /**
     * Condensed carrot tiers (gilded/radiant). Re-enabled by the Four Pillars update —
     * Radiant Carrot is the farming catalyst's Rare ingredient.
     *
     * The 4 condensed-carrot recipe files (gilded_carrot[.json/_uncraft.json],
     * radiant_carrot[.json/_uncraft.json]) had a "fabric:load_conditions": [{"condition":
     * "fabric:false"}] block while this was off (data packs can't read this Java flag, so
     * that block was the only way to stop them erroring on an unregistered item). That
     * block has been removed from all 4 files now that this is true — if you ever flip
     * this back to false, put it back or those recipes will log parse errors.
     */
    public static final boolean ENABLE_CONDENSED_CARROTS = true;

    /** Wheat Sheaf + Wheat Bale. KEEP TRUE — Wheat Bale is a fuel tier. */
    public static final boolean ENABLE_WHEAT_COMPRESSION = true;

    /** Fertilizer item + fertilized farmland + yield multiplier. */
    public static final boolean ENABLE_FERTILIZER = true;

    /** Auto-Harvester / Auto-Miner / Auto-Fisher automation blocks. */
    public static final boolean ENABLE_AUTOMATION = true;

    /** Removes vanilla's open-water requirement for fishing treasure loot. */
    public static final boolean ENABLE_UNGATED_TREASURE_FISHING = true;

    // --- Machine upgrade balance ---

    /** Storage upgrade tiers: 0 (base) .. MAX_STORAGE_TIER. Each tier unlocks one more row of slots. */
    public static final int MAX_STORAGE_TIER = 4;
    public static final int STORAGE_SLOTS_PER_TIER = 9;
    public static final int MAX_STORAGE_SLOTS = STORAGE_SLOTS_PER_TIER * (MAX_STORAGE_TIER + 1);

    /** Speed upgrade tiers: 0 (base) .. MAX_SPEED_TIER. Reduces the operation interval. */
    public static final int MAX_SPEED_TIER = 4;

    /** Auto-Miner-only: ore regrowth-delay upgrade tiers: 0 (base) .. MAX_REGEN_TIER. */
    public static final int MAX_REGEN_TIER = 4;

    /** Design Program Update 4 § 4.3 — the five Effigies (combat automation). */
    public static final boolean ENABLE_EFFIGIES = true;

    /** Design Program Update 5 — Mining Depth: rare drops from vanilla ore, gem cutting
     *  and sockets, the retired mining-speed ladder. */
    public static final boolean ENABLE_MINING_DEPTH = true;

    /** Design Program Update 6 — The Heartwood: one placed block holding four perk
     *  branches and talisman sockets. */
    public static final boolean ENABLE_HEARTWOOD = true;

    /** Design Program Update 7 — Threat: world difficulty scaling with vanilla mobs. */
    public static final boolean ENABLE_THREAT = true;

    /** § 7.2 — damage scales more slowly than health/speed by design, but can be
     *  switched off entirely (health/speed scaling stay on) if it proves too punishing
     *  for a player mid-progression, per the spec's own explicit config knob. */
    public static final boolean THREAT_DAMAGE_SCALING = true;

    /** Design Program Update 8 — Villagers & Seals: commissions, Prosperity, Tempering. */
    public static final boolean ENABLE_VILLAGERS_SEALS = true;

    /** Design Program Update 9 — Farming & Fishing Depth: Harvest Fairs, Blight, Twin
     *  Bite, the Catch Log. */
    public static final boolean ENABLE_FARMING_FISHING_DEPTH = true;

    /** Design Program Update 10 — Endgame: rescaled vanilla bosses, the God Potion
     *  rework's non-automatable ingredients. */
    public static final boolean ENABLE_ENDGAME = true;

    /** Design Program Update 4 § 4.1③ — per-world placement cap on the automation
     *  blocks, enforced by {@link MachinePlacementGuard}, counting every automation
     *  block across every dimension in this save. The spec's per-chunk cap was removed
     *  at the user's explicit request ("I want the peak of automation to be very
     *  powerful") — dense single-chunk automation farms are intentionally allowed. */
    public static final int MAX_MACHINES_PER_WORLD = 24;

    /** Harvester / Prospector / Angler armor sets and their set bonuses. */
    public static final boolean ENABLE_GEAR_SETS = true;

    /** Custom tools: Harvester's Scythe, Prospector's Drill, the three Angler rod tiers. */
    public static final boolean ENABLE_CUSTOM_TOOLS = true;

    /** God Potion, its three pillar catalysts, the Godly Catalyst, and the brewing chain. */
    public static final boolean ENABLE_GOD_POTION = true;

    /** Sea creatures spawning from manual fishing (never from the Auto-Fisher). */
    public static final boolean ENABLE_SEA_CREATURES = true;

    /**
     * Four Pillars Progression, farming leg: Potato Sack/Crate, Harvest Core, and the
     * Cultivator's/Warden's gear tiers built on top of the existing Harvester's Garb/Scythe
     * (which is Tier I of this ladder).
     */
    public static final boolean ENABLE_FARMING_PROGRESSION = true;

    /**
     * Four Pillars Progression, mining leg: Cobble/Deepslate compression chains, Bedrock
     * Core, and the Excavator's/Bedrock gear tiers built on top of the existing Prospector's
     * Plate/Drill (Tier I of this ladder).
     */
    public static final boolean ENABLE_MINING_PROGRESSION = true;

    /**
     * Four Pillars Progression, fishing leg: Cod/Kelp compression chains, Leviathan's Heart,
     * and the Tidal/Leviathan's armor tiers built on top of the existing Angler's Wear
     * (Tier I). Deliberately no new rod tier — the existing Angler's Line -> Deep-Sea Rod ->
     * Leviathan Rod ladder from the Gear & God Potion update already reaches this pillar's
     * tool cap on its own.
     */
    public static final boolean ENABLE_FISHING_PROGRESSION = true;

    /**
     * Four Pillars Progression, combat leg: the only pillar with no pre-existing Tier I —
     * Marrowguard/Ashwrought/Reaper's Aegis armor and Marrowguard Blade/Ashwrought Edge/
     * Reaper's Edge weapons, all three tiers new, plus The Marked (melee-kill-triggered
     * elite variants of the five classic hostiles).
     */
    public static final boolean ENABLE_COMBAT_PROGRESSION = true;

    /**
     * Design Program Update 2: Collections (9 tracked materials, Tiers I–X), Skills
     * (Farming/Mining/Combat/Fishing, levels 0–50), and Proofs (the manual gate from
     * Tier IV up — quantity alone is never enough). Also gates the § 2.4 recipe retrofit
     * that re-keys existing recipe advancements to collection-tier completion instead of
     * simple item pickup.
     */
    public static final boolean ENABLE_COLLECTIONS_PROOFS = true;

    /** Post-Design-Program addition: fuses a vanilla Elytra into any chestplate at the
     *  smithing table via {@link ElytraFusionRecipe}, so armor and glide stop competing
     *  for the chest slot. User-requested, not part of the original 10-update program. */
    public static final boolean ENABLE_ELYTRA_FUSION = true;

    /** Post-Design-Program addition: the Voidstep Blade, a short-range combat teleport
     *  gated behind a large Ender Pearl cost (Hypixel SkyBlock's "Aspect of the End"). */
    public static final boolean ENABLE_VOIDSTEP = true;

    /** Post-Design-Program addition: lava fishing, a parallel late-game fishing track
     *  whose top tier is calibrated to slightly exceed the Leviathan Rod (the best
     *  water-fishing tier from Update 9). */
    public static final boolean ENABLE_LAVA_FISHING = true;

    /** Post-Design-Program addition: the Coin economy — a virtual per-player balance,
     *  a universal sell mechanic (Coin Purse), and three sinks (Waystone fast travel,
     *  villager instant-repair, villager Wares). User-requested, explicitly scoped to
     *  never touch vanilla villager trades — see VillagerShopHandler/VillagerShopMenu. */
    public static final boolean ENABLE_ECONOMY = true;

    /** Post-Design-Program addition: Slayers — summon-a-boss quests reusing The Marked's
     *  own five vanilla mob types, the deliberate "break from the grind" moment SkyBlock
     *  has and Greenward didn't (The Marked itself is a passive random bonus, not a goal
     *  a player chooses to chase). */
    public static final boolean ENABLE_SLAYERS = true;
}
