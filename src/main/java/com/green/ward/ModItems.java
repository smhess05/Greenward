package com.green.ward;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.function.Function;

public class ModItems {

    // Mod ID Declaration
    public static final String MOD_ID = "greenward";

    // Item Declarations — non-final, assigned conditionally in initialize() so a
    // disabled feature flag simply leaves the field null instead of registering.
    public static Item WHEAT_SHEAF;
    public static Item GILDED_CARROT;
    public static Item RADIANT_CARROT;

    public static Item FERTILIZER;

    public static Item STORAGE_UPGRADE;
    public static Item SPEED_UPGRADE;
    public static Item REGEN_UPGRADE;
    public static Item PRESS;
    public static Item DEEP_PRESS;
    public static Item FIELD_GUIDE;

    public static Item GOLDEN_HARVEST;
    public static Item DEEPSTONE_CORE;
    public static Item ABYSSAL_PEARL;
    public static Item GODLY_CATALYST;

    public static Item POTATO_SACK;
    public static Item POTATO_CRATE;
    public static Item HARVEST_CORE;
    public static Item ASCENSION_TEMPLATE;

    // Four Pillars Progression — Mining leg materials
    public static Item COBBLE_CLUSTER;
    public static Item COBBLE_MONOLITH;
    public static Item DEEPSLATE_CLUSTER;
    public static Item DEEPSLATE_MONOLITH;
    public static Item BEDROCK_CORE;

    // Four Pillars Progression — Fishing leg materials
    public static Item COD_SCHOOL;
    public static Item COD_SHOAL;
    public static Item KELP_REEF;
    public static Item LEVIATHANS_HEART;

    // Four Pillars Progression — Combat leg materials
    public static Item BONE_BUNDLE;
    public static Item BONE_RELIQUARY;
    public static Item POWDER_SATCHEL;
    public static Item POWDER_CACHE;
    public static Item REAPERS_CORE;

    // Material Economy Update 3 — compression ladder rungs 3/4 (rungs 1/2 are above,
    // interleaved with the Four Pillars fields since those shipped first). Diamond has no
    // rung 3/4 — see CompressionLadder's own doc comment.
    public static Item WHEAT_RICK;
    public static Item WHEAT_MOW;
    public static Item POTATO_PALLET;
    public static Item POTATO_GRANARY;
    public static Item SUNBURST_CARROT;
    public static Item SOLAR_CARROT;
    public static Item COBBLE_MASSIF;
    public static Item COBBLE_BATHOLITH;
    public static Item DEEPSLATE_MASSIF;
    public static Item DEEPSLATE_BATHOLITH;
    public static Item COD_HAUL;
    public static Item COD_TROVE;
    public static Item BONE_CHARNEL;
    public static Item BONE_OSSUARY;
    public static Item POWDER_MAGAZINE;
    public static Item POWDER_ARSENAL;

    // Design Program Update 10 — Endgame (§ 10.1/10.3).
    public static Item BOSS_ESSENCE;
    public static Item CINDER_HEART;
    public static Item PEARL_CLUSTER;
    public static Item PEARL_NEXUS;
    public static Item COIN_PURSE;
    public static Item SLAYER_HORN_ZOMBIE;
    public static Item SLAYER_HORN_SKELETON;
    public static Item SLAYER_HORN_SPIDER;
    public static Item SLAYER_HORN_CREEPER;
    public static Item SLAYER_HORN_ENDERMAN;

    // Design Program Update 8 — Villagers & Seals (§ 8.3/8.4).
    public static Item SEAL;
    public static Item KEEN_STONE;
    public static Item STURDY_STONE;
    public static Item BOUNTIFUL_STONE;
    public static Item DEEP_STONE;
    public static Item BRINY_STONE;
    public static Item SWIFT_STONE;
    public static Item GRIM_STONE;

    // Design Program Update 5 — Mining Depth (§ 5.1/5.2).
    public static Item GLIMMER;
    public static Item TITANSHARD;
    public static Item ROUGH_AMBER;
    public static Item FINE_AMBER;
    public static Item FLAWLESS_AMBER;
    public static Item PERFECT_AMBER;
    public static Item ROUGH_JADE;
    public static Item FINE_JADE;
    public static Item FLAWLESS_JADE;
    public static Item PERFECT_JADE;
    public static Item ROUGH_SAPPHIRE;
    public static Item FINE_SAPPHIRE;
    public static Item FLAWLESS_SAPPHIRE;
    public static Item PERFECT_SAPPHIRE;
    public static Item ROUGH_RUBY;
    public static Item FINE_RUBY;
    public static Item FLAWLESS_RUBY;
    public static Item PERFECT_RUBY;

    // Design Program Update 6 — Talismans (§ 6.3). Only the 6 non-villager-sourced ones
    // ship now; Miller's Favour and Quarryman's Nail need Update 8's villager Prosperity
    // system first (Appendix C anticipates exactly this — 8 of 20 were ever fully
    // specified at Update 6 time). Each has one upgrade step (Talisman -> Sigil).
    public static Item SHEAF_TOKEN;
    public static Item SHEAF_TOKEN_SIGIL;
    public static Item CUTTERS_CHARM;
    public static Item CUTTERS_CHARM_SIGIL;
    public static Item ANGLERS_KNOT;
    public static Item ANGLERS_KNOT_SIGIL;
    public static Item KNUCKLEBONE;
    public static Item KNUCKLEBONE_SIGIL;
    public static Item WRAITHS_EYE;
    public static Item WRAITHS_EYE_SIGIL;
    public static Item DEEPGLASS_LENS;
    public static Item DEEPGLASS_LENS_SIGIL;

    // Material Economy Update 3 — the four pillar Satchels (§ 3.4).
    public static Item AGRONOMY_SATCHEL;
    public static Item LITHIC_SATCHEL;
    public static Item TIDAL_SATCHEL;
    public static Item OSSUARY_SATCHEL;

    // Automation Doctrine Update 4 — Auto-Miner-only precious-ore regrowth gate (§ 4.1②).
    public static Item DEEP_REGROWTH_MODULE;

    // Automation Doctrine Update 4 — permanent, daylight-only fuel (§ 4.2).
    public static Item SUNWHEEL;

    // The registration helper
    public static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ResourceKey<Item> theKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));

        // Create the item using the factory function and the provided settings
        Item item = factory.apply(settings.setId(theKey));

        // Register the item
        Registry.register(BuiltInRegistries.ITEM, theKey, item);

        return item;
    }

    /** A talisman/sigil — Design Program Update 6 § 6.3. Deliberately carries NO {@link
     *  GreenwardComponents#STATS} component: talismans only grant their effect while
     *  socketed in the Heartwood ("apply to the player anywhere in the world" — i.e. not
     *  tied to being worn/held), via {@link TalismanType} + {@code HeartwoodStatContributor}
     *  reading {@code HeartwoodData}'s socketed list — never {@link EquipmentStatContributor}. */
    private static Item talisman(String name) {
        return register(name, properties -> new Item(properties), new Item.Properties().stacksTo(1));
    }

    // Called from your main class to force this class to load.
    public static void initialize() {
        if (GreenwardConfig.ENABLE_WHEAT_COMPRESSION) {
            WHEAT_SHEAF = register("wheat_sheaf", properties -> new Item(properties), new Item.Properties());
            WHEAT_RICK = register("wheat_rick", properties -> new Item(properties), new Item.Properties());
            WHEAT_MOW = register("wheat_mow", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_CONDENSED_CARROTS) {
            GILDED_CARROT = register("gilded_carrot", properties -> new Item(properties),
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build()));
            RADIANT_CARROT = register("radiant_carrot", properties -> new Item(properties),
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.8f).build()));
            SUNBURST_CARROT = register("sunburst_carrot", properties -> new Item(properties),
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(9).saturationModifier(0.9f).build()));
            SOLAR_CARROT = register("solar_carrot", properties -> new Item(properties),
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(11).saturationModifier(1.0f).build()));
        }

        if (GreenwardConfig.ENABLE_FERTILIZER) {
            FERTILIZER = register("fertilizer", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_AUTOMATION) {
            STORAGE_UPGRADE = register("storage_upgrade", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            SPEED_UPGRADE = register("speed_upgrade", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            REGEN_UPGRADE = register("regen_upgrade", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            FIELD_GUIDE = register("field_guide", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            SUNWHEEL = register("sunwheel", properties -> new Item(properties), new Item.Properties());
        }

        // Press/Deep Press gate on Cobblestone Tier V/X specifically (Update 3 § 3.3), not
        // just ENABLE_AUTOMATION, but registration itself only needs automation machines to
        // exist at all — the tier-gate lives in the recipe advancement, same pattern as
        // every other Field-Guide-gated item.
        if (GreenwardConfig.ENABLE_AUTOMATION && GreenwardConfig.ENABLE_MINING_PROGRESSION) {
            PRESS = register("press", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            DEEP_PRESS = register("deep_press", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            // Crafting the item is still gated on Diamond Tier VI (a resource-availability
            // gate, kept as-is from Update 4). The Design Program's own "Heartwood Mining
            // branch 4" gate now exists for real (Update 6's Stone-branch "Regrowth
            // Mastery" node) and is checked separately, at the point the module actually
            // does anything — see AutoMinerBlockEntity.doOperation — so installing this
            // module before unlocking that node has no effect yet.
            DEEP_REGROWTH_MODULE = register("deep_regrowth_module", properties -> new Item(properties), new Item.Properties().stacksTo(1));
        }

        if (GreenwardConfig.ENABLE_GOD_POTION) {
            GOLDEN_HARVEST = register("golden_harvest", properties -> new Item(properties), new Item.Properties());
            DEEPSTONE_CORE = register("deepstone_core", properties -> new Item(properties), new Item.Properties());
            ABYSSAL_PEARL = register("abyssal_pearl", properties -> new Item(properties), new Item.Properties());
            GODLY_CATALYST = register("godly_catalyst", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION) {
            POTATO_SACK = register("potato_sack", properties -> new Item(properties), new Item.Properties());
            POTATO_CRATE = register("potato_crate", properties -> new Item(properties), new Item.Properties());
            POTATO_PALLET = register("potato_pallet", properties -> new Item(properties), new Item.Properties());
            POTATO_GRANARY = register("potato_granary", properties -> new Item(properties), new Item.Properties());
            HARVEST_CORE = register("harvest_core", properties -> new Item(properties), new Item.Properties());
            // Shared across all four pillars' future Tier II->III smithing upgrades — a plain
            // Item rather than SmithingTemplateItem (which needs a batch of tooltip
            // Components/icon Identifiers per template); functionally identical for recipe
            // matching, just without vanilla's fancy "applies to" tooltip decoration.
            ASCENSION_TEMPLATE = register("ascension_template", properties -> new Item(properties), new Item.Properties());
            AGRONOMY_SATCHEL = register("agronomy_satchel", SatchelItem::new,
                    new Item.Properties().stacksTo(1).component(GreenwardComponents.SATCHEL_CONTENTS, SatchelContents.EMPTY));
        }

        if (GreenwardConfig.ENABLE_MINING_PROGRESSION) {
            COBBLE_CLUSTER = register("cobble_cluster", properties -> new Item(properties), new Item.Properties());
            COBBLE_MONOLITH = register("cobble_monolith", properties -> new Item(properties), new Item.Properties());
            COBBLE_MASSIF = register("cobble_massif", properties -> new Item(properties), new Item.Properties());
            COBBLE_BATHOLITH = register("cobble_batholith", properties -> new Item(properties), new Item.Properties());
            DEEPSLATE_CLUSTER = register("deepslate_cluster", properties -> new Item(properties), new Item.Properties());
            DEEPSLATE_MONOLITH = register("deepslate_monolith", properties -> new Item(properties), new Item.Properties());
            DEEPSLATE_MASSIF = register("deepslate_massif", properties -> new Item(properties), new Item.Properties());
            DEEPSLATE_BATHOLITH = register("deepslate_batholith", properties -> new Item(properties), new Item.Properties());
            BEDROCK_CORE = register("bedrock_core", properties -> new Item(properties), new Item.Properties());
            LITHIC_SATCHEL = register("lithic_satchel", SatchelItem::new,
                    new Item.Properties().stacksTo(1).component(GreenwardComponents.SATCHEL_CONTENTS, SatchelContents.EMPTY));
        }

        if (GreenwardConfig.ENABLE_HEARTWOOD) {
            SHEAF_TOKEN = talisman("sheaf_token");
            SHEAF_TOKEN_SIGIL = talisman("sheaf_token_sigil");
            CUTTERS_CHARM = talisman("cutters_charm");
            CUTTERS_CHARM_SIGIL = talisman("cutters_charm_sigil");
            ANGLERS_KNOT = talisman("anglers_knot");
            ANGLERS_KNOT_SIGIL = talisman("anglers_knot_sigil");
            KNUCKLEBONE = talisman("knucklebone");
            KNUCKLEBONE_SIGIL = talisman("knucklebone_sigil");
            WRAITHS_EYE = talisman("wraiths_eye");
            WRAITHS_EYE_SIGIL = talisman("wraiths_eye_sigil");
            DEEPGLASS_LENS = talisman("deepglass_lens");
            DEEPGLASS_LENS_SIGIL = talisman("deepglass_lens_sigil");
        }

        if (GreenwardConfig.ENABLE_ENDGAME) {
            // Never registered with a crafting recipe — only drops from the Ender Dragon
            // or Wither at Threat 75+ (§ 10.1/10.3), so Reaper's Core's God Potion
            // ingredient stays genuinely non-automatable.
            BOSS_ESSENCE = register("boss_essence", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_LAVA_FISHING) {
            // Never registered with a crafting recipe — only drops from the Magma Wyrm
            // (LavaFishingHandler), the lava-fishing capstone catch, mirroring how Heart
            // of the Sea (the Abyssal Warden's own capstone drop) is likewise uncraftable.
            CINDER_HEART = register("cinder_heart", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_VOIDSTEP) {
            // A 2-step Ender Pearl compression chain, purely to let the Voidstep Blade's
            // own recipe read as a normal 2-material + 1-stick sword shape instead of a
            // 9-slot flat pearl pile. Ratio is 2:1 per step (not the pillars' own 9:1 —
            // Ender Pearl isn't one of the 9 tracked collections, and 2:1 twice keeps the
            // blade's total pearl cost at 4 per Nexus × 2 Nexus = 8, matching the flat
            // 8-pearl cost this replaced rather than inflating it further.
            PEARL_CLUSTER = register("pearl_cluster", properties -> new Item(properties), new Item.Properties());
            PEARL_NEXUS = register("pearl_nexus", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_VILLAGERS_SEALS) {
            // Never registered with a crafting recipe — Seals only ever come from
            // Commissions (§ 8.3: "Never earned from: machines, Effigies, or selling
            // items"), so there is deliberately no way to craft one.
            SEAL = register("seal", properties -> new Item(properties), new Item.Properties());
            KEEN_STONE = register("keen_stone", properties -> new Item(properties), new Item.Properties());
            STURDY_STONE = register("sturdy_stone", properties -> new Item(properties), new Item.Properties());
            BOUNTIFUL_STONE = register("bountiful_stone", properties -> new Item(properties), new Item.Properties());
            DEEP_STONE = register("deep_stone", properties -> new Item(properties), new Item.Properties());
            BRINY_STONE = register("briny_stone", properties -> new Item(properties), new Item.Properties());
            SWIFT_STONE = register("swift_stone", properties -> new Item(properties), new Item.Properties());
            GRIM_STONE = register("grim_stone", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_MINING_DEPTH) {
            GLIMMER = register("glimmer", properties -> new Item(properties), new Item.Properties());
            TITANSHARD = register("titanshard", properties -> new Item(properties), new Item.Properties());
            ROUGH_AMBER = register("rough_amber", properties -> new Item(properties), new Item.Properties());
            FINE_AMBER = register("fine_amber", properties -> new Item(properties), new Item.Properties());
            FLAWLESS_AMBER = register("flawless_amber", properties -> new Item(properties), new Item.Properties());
            PERFECT_AMBER = register("perfect_amber", properties -> new Item(properties), new Item.Properties());
            ROUGH_JADE = register("rough_jade", properties -> new Item(properties), new Item.Properties());
            FINE_JADE = register("fine_jade", properties -> new Item(properties), new Item.Properties());
            FLAWLESS_JADE = register("flawless_jade", properties -> new Item(properties), new Item.Properties());
            PERFECT_JADE = register("perfect_jade", properties -> new Item(properties), new Item.Properties());
            ROUGH_SAPPHIRE = register("rough_sapphire", properties -> new Item(properties), new Item.Properties());
            FINE_SAPPHIRE = register("fine_sapphire", properties -> new Item(properties), new Item.Properties());
            FLAWLESS_SAPPHIRE = register("flawless_sapphire", properties -> new Item(properties), new Item.Properties());
            PERFECT_SAPPHIRE = register("perfect_sapphire", properties -> new Item(properties), new Item.Properties());
            ROUGH_RUBY = register("rough_ruby", properties -> new Item(properties), new Item.Properties());
            FINE_RUBY = register("fine_ruby", properties -> new Item(properties), new Item.Properties());
            FLAWLESS_RUBY = register("flawless_ruby", properties -> new Item(properties), new Item.Properties());
            PERFECT_RUBY = register("perfect_ruby", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_FISHING_PROGRESSION) {
            COD_SCHOOL = register("cod_school", properties -> new Item(properties), new Item.Properties());
            COD_SHOAL = register("cod_shoal", properties -> new Item(properties), new Item.Properties());
            COD_HAUL = register("cod_haul", properties -> new Item(properties), new Item.Properties());
            COD_TROVE = register("cod_trove", properties -> new Item(properties), new Item.Properties());
            KELP_REEF = register("kelp_reef", properties -> new Item(properties), new Item.Properties());
            LEVIATHANS_HEART = register("leviathans_heart", properties -> new Item(properties), new Item.Properties());
            TIDAL_SATCHEL = register("tidal_satchel", SatchelItem::new,
                    new Item.Properties().stacksTo(1).component(GreenwardComponents.SATCHEL_CONTENTS, SatchelContents.EMPTY));
        }

        if (GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            BONE_BUNDLE = register("bone_bundle", properties -> new Item(properties), new Item.Properties());
            BONE_RELIQUARY = register("bone_reliquary", properties -> new Item(properties), new Item.Properties());
            BONE_CHARNEL = register("bone_charnel", properties -> new Item(properties), new Item.Properties());
            BONE_OSSUARY = register("bone_ossuary", properties -> new Item(properties), new Item.Properties());
            POWDER_SATCHEL = register("powder_satchel", properties -> new Item(properties), new Item.Properties());
            POWDER_CACHE = register("powder_cache", properties -> new Item(properties), new Item.Properties());
            POWDER_MAGAZINE = register("powder_magazine", properties -> new Item(properties), new Item.Properties());
            POWDER_ARSENAL = register("powder_arsenal", properties -> new Item(properties), new Item.Properties());
            REAPERS_CORE = register("reapers_core", properties -> new Item(properties), new Item.Properties());
            OSSUARY_SATCHEL = register("ossuary_satchel", SatchelItem::new,
                    new Item.Properties().stacksTo(1).component(GreenwardComponents.SATCHEL_CONTENTS, SatchelContents.EMPTY));
        }

        if (GreenwardConfig.ENABLE_ECONOMY) {
            // Worn in the offhand to switch a sneak-right-click on a villager from
            // Commissions (unchanged) into the shop — see VillagerShopHandler. Not consumed,
            // not damageable; a permanent "mode" item like a compass or a totem.
            COIN_PURSE = register("coin_purse", properties -> new Item(properties), new Item.Properties().stacksTo(1));
        }

        if (GreenwardConfig.ENABLE_SLAYERS) {
            SLAYER_HORN_ZOMBIE = register("slayer_horn_zombie", properties -> new Item(properties), new Item.Properties());
            SLAYER_HORN_SKELETON = register("slayer_horn_skeleton", properties -> new Item(properties), new Item.Properties());
            SLAYER_HORN_SPIDER = register("slayer_horn_spider", properties -> new Item(properties), new Item.Properties());
            SLAYER_HORN_CREEPER = register("slayer_horn_creeper", properties -> new Item(properties), new Item.Properties());
            SLAYER_HORN_ENDERMAN = register("slayer_horn_enderman", properties -> new Item(properties), new Item.Properties());
        }
    }
}
