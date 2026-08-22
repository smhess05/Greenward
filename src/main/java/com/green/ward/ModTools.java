package com.green.ward;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;

/**
 * Custom tools, one modest upgrade per pillar over diamond (never netherite-tier), plus
 * three fishing rod tiers per the Sea Creatures addendum — each rod both boosts the
 * sea-creature chance (see SeaCreatureHandler) AND gatekeeps which creatures it can land.
 *
 * A rod's Lure/Luck of the Sea come baked in as a genuine default ENCHANTMENTS component
 * (Item.Properties.delayedComponent(...)), not a virtual/hooked bonus — this reuses
 * vanilla's real enchantment-reading code (FishingHook already reads enchantment levels
 * off the held rod) with zero mixins. Enchantments are a *reloadable* registry, unavailable
 * at mod-init time, hence "delayed": the lambda resolves the real Holder&lt;Enchantment&gt;
 * once registries are actually loaded.
 */
public final class ModTools {
    private ModTools() {}

    private static final TagKey<Item> REPAIRS_HARVESTERS_SCYTHE =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_harvesters_scythe"));
    private static final TagKey<Item> REPAIRS_CULTIVATORS_SCYTHE =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_cultivators_scythe"));
    private static final TagKey<Item> REPAIRS_HARVEST_WARDEN =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_harvest_warden"));

    // Diamond-tier mining level (same incorrect-blocks tag as diamond) with tuned speed/durability.
    private static final ToolMaterial SCYTHE_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1200, 8.0F, 3.0F, 10, REPAIRS_HARVESTERS_SCYTHE);
    private static final ToolMaterial DRILL_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1400, 9.2F, 3.0F, 10, ItemTags.DIAMOND_TOOL_MATERIALS);
    private static final ToolMaterial CULTIVATORS_SCYTHE_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1800, 8.0F, 3.0F, 10, REPAIRS_CULTIVATORS_SCYTHE);
    /** Farming Tier III: mining speed matches netherite's own (9.0) — the same stat vanilla
     *  itself uses to say "beyond diamond," reused literally rather than inventing a new one. */
    private static final ToolMaterial HARVEST_WARDEN_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2400, 9.0F, 3.0F, 15, REPAIRS_HARVEST_WARDEN);

    // --- Four Pillars Progression: Mining Tier II / Tier III pickaxes ---
    private static final TagKey<Item> REPAIRS_EXCAVATORS_PICK =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_excavators_pick"));
    private static final TagKey<Item> REPAIRS_BEDROCK_REAVER =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_bedrock_reaver"));
    /** Mining Tier II: mines faster than Prospector's Drill (9.6 vs. 9.2). */
    private static final ToolMaterial EXCAVATORS_PICK_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1800, 9.6F, 3.0F, 10, REPAIRS_EXCAVATORS_PICK);
    /** Mining Tier III — the true speed cap, tuned for a smoother curve off Excavator's
     *  Pick (8.0 diamond -> 9.2 Drill -> 9.6 Pick -> 10.5 Reaver) rather than the original
     *  12.0, which jumped too far past the Tier II step. Substituted for the spec's
     *  "toggleable 3x3 vein-mining + auto-smelt" flavor with a pure stat capstone; see
     *  UPDATING.md. */
    private static final ToolMaterial BEDROCK_REAVER_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2400, 10.5F, 4.0F, 15, REPAIRS_BEDROCK_REAVER);

    // --- Four Pillars Progression: Combat Tier I / II / III swords ---
    private static final TagKey<Item> REPAIRS_MARROWGUARD_BLADE =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_marrowguard_blade"));
    private static final TagKey<Item> REPAIRS_ASHWROUGHT_EDGE =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_ashwrought_edge"));
    private static final TagKey<Item> REPAIRS_REAPERS_EDGE =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_reapers_edge"));
    /** Combat Tier I: diamond-equivalent attack damage bonus (total Atk 7) — this pillar has
     *  no prior weapon to build on, so it starts at the same power level as the other Tier Is. */
    private static final ToolMaterial MARROWGUARD_BLADE_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1200, 8.0F, 3.0F, 10, REPAIRS_MARROWGUARD_BLADE);
    /** Combat Tier II: netherite-equivalent attack damage bonus (total Atk 8). */
    private static final ToolMaterial ASHWROUGHT_EDGE_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1800, 8.0F, 4.0F, 10, REPAIRS_ASHWROUGHT_EDGE);
    /** Combat Tier III — Reaper's Edge, named directly in the spec: exceeds netherite
     *  (total Atk 9, the spec's exact target), matching every other pillar's Tier III ceiling. */
    private static final ToolMaterial REAPERS_EDGE_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2400, 9.0F, 5.0F, 15, REPAIRS_REAPERS_EDGE);

    public static Item HARVESTERS_SCYTHE;
    public static Item PROSPECTORS_DRILL;
    public static Item ANGLERS_LINE;
    public static Item DEEP_SEA_ROD;
    public static Item LEVIATHAN_ROD;

    // Four Pillars Progression — Farming Tier II / Tier III
    public static Item CULTIVATORS_SCYTHE;
    public static Item HARVEST_WARDEN;

    // Four Pillars Progression — Mining Tier II / Tier III
    public static Item EXCAVATORS_PICK;
    public static Item BEDROCK_REAVER;

    // Four Pillars Progression — Combat Tier I / II / III
    public static Item MARROWGUARD_BLADE;
    public static Item ASHWROUGHT_EDGE;
    public static Item REAPERS_EDGE;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_CUSTOM_TOOLS) {
            return;
        }

        HARVESTERS_SCYTHE = ModItems.register("harvesters_scythe",
                properties -> new HoeItem(SCYTHE_MATERIAL, -3.0F, 0.0F, properties),
                new Item.Properties());

        PROSPECTORS_DRILL = ModItems.register("prospectors_drill",
                properties -> new Item(properties),
                new Item.Properties().pickaxe(DRILL_MATERIAL, 1.0F, -2.8F));

        ANGLERS_LINE = ModItems.register("anglers_line",
                properties -> new FishingRodItem(properties),
                new Item.Properties().durability(256).enchantable(1)
                        .delayedComponent(DataComponents.ENCHANTMENTS, bakedEnchantments(Map.of(Enchantments.LURE, 1))));

        DEEP_SEA_ROD = ModItems.register("deep_sea_rod",
                properties -> new FishingRodItem(properties),
                new Item.Properties().durability(512).enchantable(1)
                        .delayedComponent(DataComponents.ENCHANTMENTS,
                                bakedEnchantments(Map.of(Enchantments.LURE, 2, Enchantments.LUCK_OF_THE_SEA, 1))));

        LEVIATHAN_ROD = ModItems.register("leviathan_rod",
                properties -> new FishingRodItem(properties),
                new Item.Properties().durability(1024).enchantable(1)
                        .delayedComponent(DataComponents.ENCHANTMENTS,
                                bakedEnchantments(Map.of(Enchantments.LURE, 3, Enchantments.LUCK_OF_THE_SEA, 2))));

        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION) {
            CULTIVATORS_SCYTHE = ModItems.register("cultivators_scythe",
                    properties -> new HoeItem(CULTIVATORS_SCYTHE_MATERIAL, -3.0F, 0.0F, properties),
                    new Item.Properties());

            // Not registered through a crafting recipe — obtained via smithing_transform,
            // see data/greenward/recipe/harvest_warden.json.
            HARVEST_WARDEN = ModItems.register("harvest_warden",
                    properties -> new HoeItem(HARVEST_WARDEN_MATERIAL, -3.0F, 0.0F, properties),
                    new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_MINING_PROGRESSION) {
            EXCAVATORS_PICK = ModItems.register("excavators_pick",
                    properties -> new Item(properties),
                    new Item.Properties().pickaxe(EXCAVATORS_PICK_MATERIAL, 1.0F, -2.8F));

            // Not registered through a crafting recipe — obtained via smithing_transform,
            // see data/greenward/recipe/bedrock_reaver.json.
            BEDROCK_REAVER = ModItems.register("bedrock_reaver",
                    properties -> new Item(properties),
                    new Item.Properties().pickaxe(BEDROCK_REAVER_MATERIAL, 1.0F, -2.8F));
        }

        if (GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            MARROWGUARD_BLADE = ModItems.register("marrowguard_blade",
                    properties -> new Item(properties),
                    new Item.Properties().sword(MARROWGUARD_BLADE_MATERIAL, 3.0F, -2.4F));

            ASHWROUGHT_EDGE = ModItems.register("ashwrought_edge",
                    properties -> new Item(properties),
                    new Item.Properties().sword(ASHWROUGHT_EDGE_MATERIAL, 3.0F, -2.4F));

            // Not registered through a crafting recipe — obtained via smithing_transform,
            // see data/greenward/recipe/reapers_edge.json.
            REAPERS_EDGE = ModItems.register("reapers_edge",
                    properties -> new Item(properties),
                    new Item.Properties().sword(REAPERS_EDGE_MATERIAL, 3.0F, -2.4F));
        }
    }

    private static DataComponentInitializers.SingleComponentInitializer<ItemEnchantments> bakedEnchantments(
            Map<ResourceKey<Enchantment>, Integer> levels) {
        return (HolderLookup.Provider context) -> {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            HolderLookup.RegistryLookup<Enchantment> lookup = context.lookupOrThrow(Registries.ENCHANTMENT);
            for (Map.Entry<ResourceKey<Enchantment>, Integer> entry : levels.entrySet()) {
                Holder<Enchantment> holder = lookup.getOrThrow(entry.getKey());
                mutable.set(holder, entry.getValue());
            }
            return mutable.toImmutable();
        };
    }
}
