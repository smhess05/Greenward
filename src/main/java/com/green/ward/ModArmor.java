package com.green.ward;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public final class ModArmor {
    private ModArmor() {}

    public static Item HARVESTERS_HAT;
    public static Item HARVESTERS_TUNIC;
    public static Item HARVESTERS_LEGGINGS;
    public static Item HARVESTERS_BOOTS;

    public static Item PROSPECTORS_HELM;
    public static Item PROSPECTORS_PLATE;
    public static Item PROSPECTORS_GREAVES;
    public static Item PROSPECTORS_TREADS;

    public static Item ANGLERS_CAP;
    public static Item ANGLERS_COAT;
    public static Item ANGLERS_WADERS;
    public static Item ANGLERS_FINS;

    // Four Pillars Progression — Farming Tier II / Tier III
    public static Item CULTIVATORS_HAT;
    public static Item CULTIVATORS_TUNIC;
    public static Item CULTIVATORS_LEGGINGS;
    public static Item CULTIVATORS_BOOTS;

    public static Item WARDENS_HAT;
    public static Item WARDENS_TUNIC;
    public static Item WARDENS_LEGGINGS;
    public static Item WARDENS_BOOTS;

    // Four Pillars Progression — Mining Tier II / Tier III
    public static Item EXCAVATORS_HELM;
    public static Item EXCAVATORS_PLATE;
    public static Item EXCAVATORS_GREAVES;
    public static Item EXCAVATORS_TREADS;

    public static Item BEDROCK_HELM;
    public static Item BEDROCK_PLATE;
    public static Item BEDROCK_GREAVES;
    public static Item BEDROCK_TREADS;

    // Four Pillars Progression — Fishing Tier II / Tier III
    public static Item TIDAL_CAP;
    public static Item TIDAL_COAT;
    public static Item TIDAL_WADERS;
    public static Item TIDAL_FINS;

    public static Item LEVIATHANS_CAP;
    public static Item LEVIATHANS_COAT;
    public static Item LEVIATHANS_WADERS;
    public static Item LEVIATHANS_FINS;

    // Four Pillars Progression — Combat Tier I / II / III
    public static Item MARROWGUARD_HELMET;
    public static Item MARROWGUARD_CHESTPLATE;
    public static Item MARROWGUARD_LEGGINGS;
    public static Item MARROWGUARD_BOOTS;

    public static Item ASHWROUGHT_HELMET;
    public static Item ASHWROUGHT_CHESTPLATE;
    public static Item ASHWROUGHT_LEGGINGS;
    public static Item ASHWROUGHT_BOOTS;

    public static Item REAPERS_AEGIS_HELMET;
    public static Item REAPERS_AEGIS_CHESTPLATE;
    public static Item REAPERS_AEGIS_LEGGINGS;
    public static Item REAPERS_AEGIS_BOOTS;

    public static void initialize() {
        if (GreenwardConfig.ENABLE_GEAR_SETS) {
            HARVESTERS_HAT = piece("harvesters_hat", ModArmorMaterials.HARVESTERS_GARB, ArmorType.HELMET);
            HARVESTERS_TUNIC = piece("harvesters_tunic", ModArmorMaterials.HARVESTERS_GARB, ArmorType.CHESTPLATE);
            HARVESTERS_LEGGINGS = piece("harvesters_leggings", ModArmorMaterials.HARVESTERS_GARB, ArmorType.LEGGINGS);
            HARVESTERS_BOOTS = piece("harvesters_boots", ModArmorMaterials.HARVESTERS_GARB, ArmorType.BOOTS);

            PROSPECTORS_HELM = piece("prospectors_helm", ModArmorMaterials.PROSPECTORS_PLATE, ArmorType.HELMET);
            PROSPECTORS_PLATE = piece("prospectors_plate", ModArmorMaterials.PROSPECTORS_PLATE, ArmorType.CHESTPLATE);
            PROSPECTORS_GREAVES = piece("prospectors_greaves", ModArmorMaterials.PROSPECTORS_PLATE, ArmorType.LEGGINGS);
            PROSPECTORS_TREADS = piece("prospectors_treads", ModArmorMaterials.PROSPECTORS_PLATE, ArmorType.BOOTS);

            ANGLERS_CAP = piece("anglers_cap", ModArmorMaterials.ANGLERS_WEAR, ArmorType.HELMET);
            ANGLERS_COAT = piece("anglers_coat", ModArmorMaterials.ANGLERS_WEAR, ArmorType.CHESTPLATE);
            ANGLERS_WADERS = piece("anglers_waders", ModArmorMaterials.ANGLERS_WEAR, ArmorType.LEGGINGS);
            ANGLERS_FINS = piece("anglers_fins", ModArmorMaterials.ANGLERS_WEAR, ArmorType.BOOTS);
        }

        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION) {
            CULTIVATORS_HAT = piece("cultivators_hat", ModArmorMaterials.CULTIVATORS_GARB, ArmorType.HELMET);
            CULTIVATORS_TUNIC = piece("cultivators_tunic", ModArmorMaterials.CULTIVATORS_GARB, ArmorType.CHESTPLATE);
            CULTIVATORS_LEGGINGS = piece("cultivators_leggings", ModArmorMaterials.CULTIVATORS_GARB, ArmorType.LEGGINGS);
            CULTIVATORS_BOOTS = piece("cultivators_boots", ModArmorMaterials.CULTIVATORS_GARB, ArmorType.BOOTS);

            // Not registered through a crafting recipe — obtained via smithing_transform
            // (Cultivator's piece + Ascension Template + Harvest Core), see ModRecipes-
            // equivalent JSON files under data/greenward/recipe/. Still needs normal item
            // registration like any other piece.
            WARDENS_HAT = piece("wardens_hat", ModArmorMaterials.WARDENS_GARB, ArmorType.HELMET);
            WARDENS_TUNIC = piece("wardens_tunic", ModArmorMaterials.WARDENS_GARB, ArmorType.CHESTPLATE);
            WARDENS_LEGGINGS = piece("wardens_leggings", ModArmorMaterials.WARDENS_GARB, ArmorType.LEGGINGS);
            WARDENS_BOOTS = piece("wardens_boots", ModArmorMaterials.WARDENS_GARB, ArmorType.BOOTS);
        }

        if (GreenwardConfig.ENABLE_MINING_PROGRESSION) {
            EXCAVATORS_HELM = piece("excavators_helm", ModArmorMaterials.EXCAVATORS_PLATE, ArmorType.HELMET);
            EXCAVATORS_PLATE = piece("excavators_plate", ModArmorMaterials.EXCAVATORS_PLATE, ArmorType.CHESTPLATE);
            EXCAVATORS_GREAVES = piece("excavators_greaves", ModArmorMaterials.EXCAVATORS_PLATE, ArmorType.LEGGINGS);
            EXCAVATORS_TREADS = piece("excavators_treads", ModArmorMaterials.EXCAVATORS_PLATE, ArmorType.BOOTS);

            // Obtained via smithing_transform (Excavator's piece + Ascension Template + Bedrock Core).
            BEDROCK_HELM = piece("bedrock_helm", ModArmorMaterials.BEDROCK_PLATE, ArmorType.HELMET);
            BEDROCK_PLATE = piece("bedrock_plate", ModArmorMaterials.BEDROCK_PLATE, ArmorType.CHESTPLATE);
            BEDROCK_GREAVES = piece("bedrock_greaves", ModArmorMaterials.BEDROCK_PLATE, ArmorType.LEGGINGS);
            BEDROCK_TREADS = piece("bedrock_treads", ModArmorMaterials.BEDROCK_PLATE, ArmorType.BOOTS);
        }

        if (GreenwardConfig.ENABLE_FISHING_PROGRESSION) {
            TIDAL_CAP = piece("tidal_cap", ModArmorMaterials.TIDAL_WEAR, ArmorType.HELMET);
            TIDAL_COAT = piece("tidal_coat", ModArmorMaterials.TIDAL_WEAR, ArmorType.CHESTPLATE);
            TIDAL_WADERS = piece("tidal_waders", ModArmorMaterials.TIDAL_WEAR, ArmorType.LEGGINGS);
            TIDAL_FINS = piece("tidal_fins", ModArmorMaterials.TIDAL_WEAR, ArmorType.BOOTS);

            // Obtained via smithing_transform (Tidal piece + Ascension Template + Leviathan's Heart).
            LEVIATHANS_CAP = piece("leviathans_cap", ModArmorMaterials.LEVIATHANS_WEAR, ArmorType.HELMET);
            LEVIATHANS_COAT = piece("leviathans_coat", ModArmorMaterials.LEVIATHANS_WEAR, ArmorType.CHESTPLATE);
            LEVIATHANS_WADERS = piece("leviathans_waders", ModArmorMaterials.LEVIATHANS_WEAR, ArmorType.LEGGINGS);
            LEVIATHANS_FINS = piece("leviathans_fins", ModArmorMaterials.LEVIATHANS_WEAR, ArmorType.BOOTS);
        }

        if (GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            MARROWGUARD_HELMET = piece("marrowguard_helmet", ModArmorMaterials.MARROWGUARD, ArmorType.HELMET);
            MARROWGUARD_CHESTPLATE = piece("marrowguard_chestplate", ModArmorMaterials.MARROWGUARD, ArmorType.CHESTPLATE);
            MARROWGUARD_LEGGINGS = piece("marrowguard_leggings", ModArmorMaterials.MARROWGUARD, ArmorType.LEGGINGS);
            MARROWGUARD_BOOTS = piece("marrowguard_boots", ModArmorMaterials.MARROWGUARD, ArmorType.BOOTS);

            ASHWROUGHT_HELMET = piece("ashwrought_helmet", ModArmorMaterials.ASHWROUGHT, ArmorType.HELMET);
            ASHWROUGHT_CHESTPLATE = piece("ashwrought_chestplate", ModArmorMaterials.ASHWROUGHT, ArmorType.CHESTPLATE);
            ASHWROUGHT_LEGGINGS = piece("ashwrought_leggings", ModArmorMaterials.ASHWROUGHT, ArmorType.LEGGINGS);
            ASHWROUGHT_BOOTS = piece("ashwrought_boots", ModArmorMaterials.ASHWROUGHT, ArmorType.BOOTS);

            // Obtained via smithing_transform (Ashwrought piece + Ascension Template + Reaper's Core).
            REAPERS_AEGIS_HELMET = piece("reapers_aegis_helmet", ModArmorMaterials.REAPERS_AEGIS, ArmorType.HELMET);
            REAPERS_AEGIS_CHESTPLATE = piece("reapers_aegis_chestplate", ModArmorMaterials.REAPERS_AEGIS, ArmorType.CHESTPLATE);
            REAPERS_AEGIS_LEGGINGS = piece("reapers_aegis_leggings", ModArmorMaterials.REAPERS_AEGIS, ArmorType.LEGGINGS);
            REAPERS_AEGIS_BOOTS = piece("reapers_aegis_boots", ModArmorMaterials.REAPERS_AEGIS, ArmorType.BOOTS);
        }
    }

    private static Item piece(String name, ArmorMaterial material, ArmorType type) {
        return ModItems.register(name, properties -> new Item(properties),
                new Item.Properties().humanoidArmor(material, type));
    }

    private static boolean hasFullSet(LivingEntity entity, Item hat, Item tunic, Item leggings, Item boots) {
        return isWorn(entity, EquipmentSlot.HEAD, hat)
                && isWorn(entity, EquipmentSlot.CHEST, tunic)
                && isWorn(entity, EquipmentSlot.LEGS, leggings)
                && isWorn(entity, EquipmentSlot.FEET, boots);
    }

    private static boolean isWorn(LivingEntity entity, EquipmentSlot slot, Item expected) {
        return expected != null && entity.getItemBySlot(slot).is(expected);
    }

    public static boolean hasFullHarvesterSet(LivingEntity entity) {
        return hasFullSet(entity, HARVESTERS_HAT, HARVESTERS_TUNIC, HARVESTERS_LEGGINGS, HARVESTERS_BOOTS);
    }

    public static boolean hasFullProspectorSet(LivingEntity entity) {
        return hasFullSet(entity, PROSPECTORS_HELM, PROSPECTORS_PLATE, PROSPECTORS_GREAVES, PROSPECTORS_TREADS);
    }

    public static boolean hasFullAnglerSet(LivingEntity entity) {
        return hasFullSet(entity, ANGLERS_CAP, ANGLERS_COAT, ANGLERS_WADERS, ANGLERS_FINS);
    }

    public static boolean hasFullCultivatorSet(LivingEntity entity) {
        return hasFullSet(entity, CULTIVATORS_HAT, CULTIVATORS_TUNIC, CULTIVATORS_LEGGINGS, CULTIVATORS_BOOTS);
    }

    public static boolean hasFullWardenSet(LivingEntity entity) {
        return hasFullSet(entity, WARDENS_HAT, WARDENS_TUNIC, WARDENS_LEGGINGS, WARDENS_BOOTS);
    }

    public static boolean hasFullExcavatorSet(LivingEntity entity) {
        return hasFullSet(entity, EXCAVATORS_HELM, EXCAVATORS_PLATE, EXCAVATORS_GREAVES, EXCAVATORS_TREADS);
    }

    public static boolean hasFullBedrockSet(LivingEntity entity) {
        return hasFullSet(entity, BEDROCK_HELM, BEDROCK_PLATE, BEDROCK_GREAVES, BEDROCK_TREADS);
    }

    public static boolean hasFullTidalSet(LivingEntity entity) {
        return hasFullSet(entity, TIDAL_CAP, TIDAL_COAT, TIDAL_WADERS, TIDAL_FINS);
    }

    public static boolean hasFullLeviathanSet(LivingEntity entity) {
        return hasFullSet(entity, LEVIATHANS_CAP, LEVIATHANS_COAT, LEVIATHANS_WADERS, LEVIATHANS_FINS);
    }

    public static boolean hasFullMarrowguardSet(LivingEntity entity) {
        return hasFullSet(entity, MARROWGUARD_HELMET, MARROWGUARD_CHESTPLATE, MARROWGUARD_LEGGINGS, MARROWGUARD_BOOTS);
    }

    public static boolean hasFullAshwroughtSet(LivingEntity entity) {
        return hasFullSet(entity, ASHWROUGHT_HELMET, ASHWROUGHT_CHESTPLATE, ASHWROUGHT_LEGGINGS, ASHWROUGHT_BOOTS);
    }

    public static boolean hasFullReapersAegisSet(LivingEntity entity) {
        return hasFullSet(entity, REAPERS_AEGIS_HELMET, REAPERS_AEGIS_CHESTPLATE, REAPERS_AEGIS_LEGGINGS, REAPERS_AEGIS_BOOTS);
    }
}
