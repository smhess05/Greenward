package com.green.ward;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

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

    // The registration helper
    public static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ResourceKey<Item> theKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));

        // Create the item using the factory function and the provided settings
        Item item = factory.apply(settings.setId(theKey));

        // Register the item
        Registry.register(BuiltInRegistries.ITEM, theKey, item);

        return item;
    }

    // Called from your main class to force this class to load.
    public static void initialize() {
        if (GreenwardConfig.ENABLE_WHEAT_COMPRESSION) {
            WHEAT_SHEAF = register("wheat_sheaf", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_CONDENSED_CARROTS) {
            GILDED_CARROT = register("gilded_carrot", properties -> new Item(properties),
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build()));
            RADIANT_CARROT = register("radiant_carrot", properties -> new Item(properties),
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.8f).build()));
        }

        if (GreenwardConfig.ENABLE_FERTILIZER) {
            FERTILIZER = register("fertilizer", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_AUTOMATION) {
            STORAGE_UPGRADE = register("storage_upgrade", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            SPEED_UPGRADE = register("speed_upgrade", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            REGEN_UPGRADE = register("regen_upgrade", properties -> new Item(properties), new Item.Properties().stacksTo(1));
            FIELD_GUIDE = register("field_guide", properties -> new Item(properties), new Item.Properties().stacksTo(1));
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
            HARVEST_CORE = register("harvest_core", properties -> new Item(properties), new Item.Properties());
            // Shared across all four pillars' future Tier II->III smithing upgrades — a plain
            // Item rather than SmithingTemplateItem (which needs a batch of tooltip
            // Components/icon Identifiers per template); functionally identical for recipe
            // matching, just without vanilla's fancy "applies to" tooltip decoration.
            ASCENSION_TEMPLATE = register("ascension_template", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_MINING_PROGRESSION) {
            COBBLE_CLUSTER = register("cobble_cluster", properties -> new Item(properties), new Item.Properties());
            COBBLE_MONOLITH = register("cobble_monolith", properties -> new Item(properties), new Item.Properties());
            DEEPSLATE_CLUSTER = register("deepslate_cluster", properties -> new Item(properties), new Item.Properties());
            DEEPSLATE_MONOLITH = register("deepslate_monolith", properties -> new Item(properties), new Item.Properties());
            BEDROCK_CORE = register("bedrock_core", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_FISHING_PROGRESSION) {
            COD_SCHOOL = register("cod_school", properties -> new Item(properties), new Item.Properties());
            COD_SHOAL = register("cod_shoal", properties -> new Item(properties), new Item.Properties());
            KELP_REEF = register("kelp_reef", properties -> new Item(properties), new Item.Properties());
            LEVIATHANS_HEART = register("leviathans_heart", properties -> new Item(properties), new Item.Properties());
        }

        if (GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            BONE_BUNDLE = register("bone_bundle", properties -> new Item(properties), new Item.Properties());
            BONE_RELIQUARY = register("bone_reliquary", properties -> new Item(properties), new Item.Properties());
            POWDER_SATCHEL = register("powder_satchel", properties -> new Item(properties), new Item.Properties());
            POWDER_CACHE = register("powder_cache", properties -> new Item(properties), new Item.Properties());
            REAPERS_CORE = register("reapers_core", properties -> new Item(properties), new Item.Properties());
        }
    }
}
