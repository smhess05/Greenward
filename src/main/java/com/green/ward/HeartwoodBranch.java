package com.green.ward;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Map;

/**
 * The four Heartwood branches (Design Program Update 6 § 6.2), each fed directly by its
 * own pillar's compression materials — no intermediate token/currency, per the spec's
 * explicit instruction. 8 nodes per branch, unlocked strictly in order (a documented
 * simplification of the spec's "8-10 nodes across 5 depths" — a single linear chain
 * rather than a branching 5-depth tree, since the spec never specifies which nodes sit
 * at which depth or how they'd fork). Node effects and costs are invented — the source
 * document gives only "sample nodes" per branch, not an exhaustive list.
 *
 * <p>Two Stone-branch nodes are load-bearing outside the Heartwood itself: {@code
 * REAVERS_WRATH} is the real gate {@link MiningAbilityHandler} now checks for Vein
 * Blast/Stoneflow, and {@code REGROWTH_MASTERY} is the real gate for the Deep Regrowth
 * Module recipe — both previously used "wield Bedrock Reaver" as an interim substitute
 * (Update 4/5) for exactly this Heartwood gate, which now exists.
 */
public enum HeartwoodBranch implements StringRepresentable {
    ROOT("root", "Root"),
    STONE("stone", "Stone"),
    TIDE("tide", "Tide"),
    ASH("ash", "Ash");

    private final String id;
    private final String displayName;

    HeartwoodBranch(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    /** Node id constants referenced from outside this class for special-unlock checks. */
    static final String STONE_REAVERS_WRATH = "reavers_wrath";
    static final String STONE_REGROWTH_MASTERY = "regrowth_mastery";
    static final String TIDE_TWIN_CURRENT = "twin_current";
    static final String ASH_MARKED_SENSE = "marked_sense";
    static final String ASH_UNDYING_RESOLVE = "undying_resolve";
    static final String ROOT_FALLOW_WARD = "fallow_ward";
    static final String ROOT_FALLOW_WARD_II = "fallow_ward_ii";
    static final String ROOT_VERDANT_AURA = "verdant_aura";
    static final String TIDE_TIDAL_SIGHT = "tidal_sight";

    /** Lazy, like {@link CompressionLadder} — the item fields these costs reference
     *  aren't assigned yet when enum constants would otherwise initialize. */
    List<HeartwoodNode> nodes() {
        return switch (this) {
            case ROOT -> List.of(
                    new HeartwoodNode("sprouting_vigor", "Sprouting Vigor", cost(ModItems.WHEAT_RICK, 16),
                            Map.of(GreenwardStat.FARMING_FORTUNE, 5.0), false),
                    new HeartwoodNode("deep_roots", "Deep Roots", cost(ModItems.WHEAT_RICK, 24),
                            Map.of(GreenwardStat.FARMING_FORTUNE, 5.0), true),
                    new HeartwoodNode(ROOT_FALLOW_WARD, "Fallow Ward", cost(ModItems.WHEAT_MOW, 8), Map.of(), false),
                    new HeartwoodNode("bountiful_harvest", "Bountiful Harvest", cost(ModItems.WHEAT_MOW, 16),
                            Map.of(GreenwardStat.FARMING_FORTUNE, 10.0), false),
                    new HeartwoodNode(ROOT_VERDANT_AURA, "Verdant Aura", cost(ModItems.WHEAT_MOW, 24), Map.of(), false),
                    new HeartwoodNode("sunlit_yield", "Sunlit Yield", cost(ModItems.SUNBURST_CARROT, 8),
                            Map.of(GreenwardStat.FARMING_FORTUNE, 10.0), true),
                    new HeartwoodNode(ROOT_FALLOW_WARD_II, "Fallow Ward II", cost(ModItems.SUNBURST_CARROT, 16), Map.of(), false),
                    new HeartwoodNode("solar_bounty", "Solar Bounty", cost(ModItems.SOLAR_CARROT, 16),
                            Map.of(GreenwardStat.FARMING_FORTUNE, 10.0), false));
            case STONE -> List.of(
                    new HeartwoodNode("deep_strike", "Deep Strike", cost(ModItems.COBBLE_MASSIF, 16),
                            Map.of(GreenwardStat.MINING_SPEED, 20.0), false),
                    new HeartwoodNode("ore_sense", "Ore Sense", cost(ModItems.COBBLE_MASSIF, 24),
                            Map.of(GreenwardStat.MINING_FORTUNE, 10.0), true),
                    new HeartwoodNode(STONE_REAVERS_WRATH, "Reaver's Wrath", cost(ModItems.DEEPSLATE_MASSIF, 16), Map.of(), false),
                    new HeartwoodNode("bedrock_instinct", "Bedrock Instinct", cost(ModItems.DEEPSLATE_MASSIF, 24),
                            Map.of(GreenwardStat.MINING_SPEED, 30.0), false),
                    new HeartwoodNode(STONE_REGROWTH_MASTERY, "Regrowth Mastery", cost(ModItems.GLIMMER, 8), Map.of(), false),
                    new HeartwoodNode("vein_sight", "Vein Sight", cost(ModItems.GLIMMER, 16),
                            Map.of(GreenwardStat.MINING_FORTUNE, 15.0), true),
                    new HeartwoodNode("titans_grip", "Titan's Grip", cost(ModItems.TITANSHARD, 8),
                            Map.of(GreenwardStat.MINING_SPEED, 30.0), false),
                    new HeartwoodNode("core_tap", "Core Tap", cost(ModItems.TITANSHARD, 16),
                            Map.of(GreenwardStat.MINING_FORTUNE, 15.0), false));
            case TIDE -> List.of(
                    new HeartwoodNode("current_reading", "Current Reading", cost(ModItems.COD_HAUL, 16),
                            Map.of(GreenwardStat.SEA_CREATURE_CHANCE, 2.0), false),
                    new HeartwoodNode("swift_cast", "Swift Cast", cost(ModItems.COD_HAUL, 24),
                            Map.of(GreenwardStat.FISHING_SPEED, 10.0), true),
                    new HeartwoodNode(TIDE_TWIN_CURRENT, "Twin Current", cost(ModItems.COD_TROVE, 16), Map.of(), false),
                    new HeartwoodNode("abyssal_pull", "Abyssal Pull", cost(ModItems.COD_TROVE, 24),
                            Map.of(GreenwardStat.SEA_CREATURE_CHANCE, 3.0), false),
                    new HeartwoodNode(TIDE_TIDAL_SIGHT, "Tidal Sight", cost(net.minecraft.world.item.Items.NAUTILUS_SHELL, 4), Map.of(), false),
                    new HeartwoodNode("deep_cast", "Deep Cast", cost(net.minecraft.world.item.Items.NAUTILUS_SHELL, 8),
                            Map.of(GreenwardStat.FISHING_SPEED, 15.0), true),
                    new HeartwoodNode("leviathans_favor", "Leviathan's Favor", cost(net.minecraft.world.item.Items.NAUTILUS_SHELL, 12),
                            Map.of(GreenwardStat.SEA_CREATURE_CHANCE, 5.0), false),
                    new HeartwoodNode("master_angler", "Master Angler", cost(net.minecraft.world.item.Items.NAUTILUS_SHELL, 16),
                            Map.of(GreenwardStat.FISHING_SPEED, 15.0), false));
            case ASH -> List.of(
                    new HeartwoodNode("grim_focus", "Grim Focus", cost(ModItems.BONE_CHARNEL, 16),
                            Map.of(GreenwardStat.STRENGTH, 5.0), false),
                    new HeartwoodNode("blood_fervor", "Blood Fervor", cost(ModItems.BONE_CHARNEL, 24),
                            Map.of(GreenwardStat.CRIT_CHANCE, 3.0), true),
                    new HeartwoodNode(ASH_MARKED_SENSE, "Marked Sense", cost(ModItems.BONE_OSSUARY, 16), Map.of(), false),
                    new HeartwoodNode("reapers_edge_node", "Reaper's Edge", cost(ModItems.BONE_OSSUARY, 24),
                            Map.of(GreenwardStat.STRENGTH, 8.0), false),
                    new HeartwoodNode("volatile_rage", "Volatile Rage", cost(ModItems.POWDER_MAGAZINE, 8),
                            Map.of(GreenwardStat.CRIT_CHANCE, 4.0), false),
                    new HeartwoodNode(ASH_UNDYING_RESOLVE, "Undying Resolve", cost(ModItems.POWDER_MAGAZINE, 16), Map.of(), true),
                    new HeartwoodNode("ashen_might", "Ashen Might", cost(ModItems.POWDER_ARSENAL, 8),
                            Map.of(GreenwardStat.STRENGTH, 8.0), false),
                    new HeartwoodNode("executioners_wrath", "Executioner's Wrath", cost(ModItems.POWDER_ARSENAL, 16),
                            Map.of(GreenwardStat.CRIT_CHANCE, 5.0), false));
        };
    }

    private static Map<Item, Integer> cost(Item item, int count) {
        return Map.of(item, count);
    }
}
