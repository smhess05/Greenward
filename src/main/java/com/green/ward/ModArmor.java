package com.green.ward;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.List;
import java.util.Map;

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

    /** Every Farming/Mining armor piece's own share of that tier's Fortune total — split
     *  per-piece (helm/chest/legs/boots) exactly like Defense already is, so the number
     *  shows up on each individual item's tooltip via {@link EquipmentStatContributor}
     *  instead of only applying (invisibly) when a full set is worn. Totals per tier:
     *  12 (Tier I) / 25 (Tier II) / 60 (Tier III) — Tier I/III split flat 4 ways, Tier II
     *  gives the chestplate the odd point out, matching Defense's own biggest-share-to-
     *  chest convention. */
    private static final int[] FORTUNE_TIER_I = {3, 3, 3, 3};   // helm, chest, legs, boots
    private static final int[] FORTUNE_TIER_II = {6, 7, 6, 6};
    private static final int[] FORTUNE_TIER_III = {15, 15, 15, 15};

    public static void initialize() {
        if (GreenwardConfig.ENABLE_GEAR_SETS) {
            HARVESTERS_HAT = piece("harvesters_hat", ModArmorMaterials.HARVESTERS_GARB, ArmorType.HELMET, 15, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_I[0]));
            HARVESTERS_TUNIC = piece("harvesters_tunic", ModArmorMaterials.HARVESTERS_GARB, ArmorType.CHESTPLATE, 40, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_I[1]));
            HARVESTERS_LEGGINGS = piece("harvesters_leggings", ModArmorMaterials.HARVESTERS_GARB, ArmorType.LEGGINGS, 30, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_I[2]));
            HARVESTERS_BOOTS = piece("harvesters_boots", ModArmorMaterials.HARVESTERS_GARB, ArmorType.BOOTS, 15, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_I[3]));

            PROSPECTORS_HELM = piece("prospectors_helm", ModArmorMaterials.PROSPECTORS_PLATE, ArmorType.HELMET, 25, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_I[0]));
            PROSPECTORS_PLATE = piece("prospectors_plate", ModArmorMaterials.PROSPECTORS_PLATE, ArmorType.CHESTPLATE, 50, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_I[1]));
            PROSPECTORS_GREAVES = piece("prospectors_greaves", ModArmorMaterials.PROSPECTORS_PLATE, ArmorType.LEGGINGS, 40, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_I[2]));
            PROSPECTORS_TREADS = piece("prospectors_treads", ModArmorMaterials.PROSPECTORS_PLATE, ArmorType.BOOTS, 25, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_I[3]));

            ANGLERS_CAP = piece("anglers_cap", ModArmorMaterials.ANGLERS_WEAR_HELM, ArmorType.HELMET, 15, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_I[0]));
            ANGLERS_COAT = piece("anglers_coat", ModArmorMaterials.ANGLERS_WEAR, ArmorType.CHESTPLATE, 40, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_I[1]));
            ANGLERS_WADERS = piece("anglers_waders", ModArmorMaterials.ANGLERS_WEAR, ArmorType.LEGGINGS, 30, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_I[2]));
            ANGLERS_FINS = piece("anglers_fins", ModArmorMaterials.ANGLERS_WEAR, ArmorType.BOOTS, 15, GreenwardRarity.UNCOMMON, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_I[3]));
        }

        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION) {
            CULTIVATORS_HAT = piece("cultivators_hat", ModArmorMaterials.CULTIVATORS_GARB, ArmorType.HELMET, 35, GreenwardRarity.RARE, 2, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_II[0]));
            CULTIVATORS_TUNIC = piece("cultivators_tunic", ModArmorMaterials.CULTIVATORS_GARB, ArmorType.CHESTPLATE, 90, GreenwardRarity.RARE, 2, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_II[1]));
            CULTIVATORS_LEGGINGS = piece("cultivators_leggings", ModArmorMaterials.CULTIVATORS_GARB, ArmorType.LEGGINGS, 65, GreenwardRarity.RARE, 2, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_II[2]));
            CULTIVATORS_BOOTS = piece("cultivators_boots", ModArmorMaterials.CULTIVATORS_GARB, ArmorType.BOOTS, 35, GreenwardRarity.RARE, 2, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_II[3]));

            // Not registered through a crafting recipe — obtained via smithing_transform
            // (Cultivator's piece + Ascension Template + Harvest Core), see ModRecipes-
            // equivalent JSON files under data/greenward/recipe/. Still needs normal item
            // registration like any other piece.
            WARDENS_HAT = piece("wardens_hat", ModArmorMaterials.WARDENS_GARB, ArmorType.HELMET, 60, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_III[0]));
            WARDENS_TUNIC = piece("wardens_tunic", ModArmorMaterials.WARDENS_GARB, ArmorType.CHESTPLATE, 130, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_III[1]));
            WARDENS_LEGGINGS = piece("wardens_leggings", ModArmorMaterials.WARDENS_GARB, ArmorType.LEGGINGS, 100, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_III[2]));
            WARDENS_BOOTS = piece("wardens_boots", ModArmorMaterials.WARDENS_GARB, ArmorType.BOOTS, 60, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.FARMING_FORTUNE, FORTUNE_TIER_III[3]));
        }

        if (GreenwardConfig.ENABLE_MINING_PROGRESSION) {
            EXCAVATORS_HELM = piece("excavators_helm", ModArmorMaterials.EXCAVATORS_PLATE, ArmorType.HELMET, 35, GreenwardRarity.RARE, 2, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_II[0]));
            EXCAVATORS_PLATE = piece("excavators_plate", ModArmorMaterials.EXCAVATORS_PLATE, ArmorType.CHESTPLATE, 90, GreenwardRarity.RARE, 2, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_II[1]));
            EXCAVATORS_GREAVES = piece("excavators_greaves", ModArmorMaterials.EXCAVATORS_PLATE, ArmorType.LEGGINGS, 65, GreenwardRarity.RARE, 2, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_II[2]));
            EXCAVATORS_TREADS = piece("excavators_treads", ModArmorMaterials.EXCAVATORS_PLATE, ArmorType.BOOTS, 35, GreenwardRarity.RARE, 2, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_II[3]));

            // Obtained via smithing_transform (Excavator's piece + Ascension Template + Bedrock Core).
            BEDROCK_HELM = piece("bedrock_helm", ModArmorMaterials.BEDROCK_PLATE, ArmorType.HELMET, 60, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_III[0]));
            BEDROCK_PLATE = piece("bedrock_plate", ModArmorMaterials.BEDROCK_PLATE, ArmorType.CHESTPLATE, 130, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_III[1]));
            BEDROCK_GREAVES = piece("bedrock_greaves", ModArmorMaterials.BEDROCK_PLATE, ArmorType.LEGGINGS, 100, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_III[2]));
            BEDROCK_TREADS = piece("bedrock_treads", ModArmorMaterials.BEDROCK_PLATE, ArmorType.BOOTS, 60, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.MINING_FORTUNE, FORTUNE_TIER_III[3]));
        }

        if (GreenwardConfig.ENABLE_FISHING_PROGRESSION) {
            TIDAL_CAP = piece("tidal_cap", ModArmorMaterials.TIDAL_WEAR_HELM, ArmorType.HELMET, 35, GreenwardRarity.RARE, 2, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_II[0]));
            TIDAL_COAT = piece("tidal_coat", ModArmorMaterials.TIDAL_WEAR, ArmorType.CHESTPLATE, 90, GreenwardRarity.RARE, 2, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_II[1]));
            TIDAL_WADERS = piece("tidal_waders", ModArmorMaterials.TIDAL_WEAR, ArmorType.LEGGINGS, 65, GreenwardRarity.RARE, 2, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_II[2]));
            TIDAL_FINS = piece("tidal_fins", ModArmorMaterials.TIDAL_WEAR, ArmorType.BOOTS, 35, GreenwardRarity.RARE, 2, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_II[3]));

            // Obtained via smithing_transform (Tidal piece + Ascension Template + Leviathan's Heart).
            LEVIATHANS_CAP = piece("leviathans_cap", ModArmorMaterials.LEVIATHANS_WEAR_HELM, ArmorType.HELMET, 60, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_III[0]));
            LEVIATHANS_COAT = piece("leviathans_coat", ModArmorMaterials.LEVIATHANS_WEAR, ArmorType.CHESTPLATE, 130, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_III[1]));
            LEVIATHANS_WADERS = piece("leviathans_waders", ModArmorMaterials.LEVIATHANS_WEAR, ArmorType.LEGGINGS, 100, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_III[2]));
            LEVIATHANS_FINS = piece("leviathans_fins", ModArmorMaterials.LEVIATHANS_WEAR, ArmorType.BOOTS, 60, GreenwardRarity.EPIC, 3, fortune(GreenwardStat.SEA_CREATURE_CHANCE, FORTUNE_TIER_III[3]));
        }

        if (GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            MARROWGUARD_HELMET = piece("marrowguard_helmet", ModArmorMaterials.MARROWGUARD, ArmorType.HELMET, 15, GreenwardRarity.UNCOMMON);
            MARROWGUARD_CHESTPLATE = piece("marrowguard_chestplate", ModArmorMaterials.MARROWGUARD, ArmorType.CHESTPLATE, 50, GreenwardRarity.UNCOMMON);
            MARROWGUARD_LEGGINGS = piece("marrowguard_leggings", ModArmorMaterials.MARROWGUARD, ArmorType.LEGGINGS, 40, GreenwardRarity.UNCOMMON);
            MARROWGUARD_BOOTS = piece("marrowguard_boots", ModArmorMaterials.MARROWGUARD, ArmorType.BOOTS, 15, GreenwardRarity.UNCOMMON);

            ASHWROUGHT_HELMET = piece("ashwrought_helmet", ModArmorMaterials.ASHWROUGHT, ArmorType.HELMET, 35, GreenwardRarity.RARE, 2);
            ASHWROUGHT_CHESTPLATE = piece("ashwrought_chestplate", ModArmorMaterials.ASHWROUGHT, ArmorType.CHESTPLATE, 90, GreenwardRarity.RARE, 2);
            ASHWROUGHT_LEGGINGS = piece("ashwrought_leggings", ModArmorMaterials.ASHWROUGHT, ArmorType.LEGGINGS, 65, GreenwardRarity.RARE, 2);
            ASHWROUGHT_BOOTS = piece("ashwrought_boots", ModArmorMaterials.ASHWROUGHT, ArmorType.BOOTS, 35, GreenwardRarity.RARE, 2);

            // Obtained via smithing_transform (Ashwrought piece + Ascension Template + Reaper's Core).
            REAPERS_AEGIS_HELMET = piece("reapers_aegis_helmet", ModArmorMaterials.REAPERS_AEGIS, ArmorType.HELMET, 60, GreenwardRarity.EPIC, 3);
            REAPERS_AEGIS_CHESTPLATE = piece("reapers_aegis_chestplate", ModArmorMaterials.REAPERS_AEGIS, ArmorType.CHESTPLATE, 130, GreenwardRarity.EPIC, 3);
            REAPERS_AEGIS_LEGGINGS = piece("reapers_aegis_leggings", ModArmorMaterials.REAPERS_AEGIS, ArmorType.LEGGINGS, 100, GreenwardRarity.EPIC, 3);
            REAPERS_AEGIS_BOOTS = piece("reapers_aegis_boots", ModArmorMaterials.REAPERS_AEGIS, ArmorType.BOOTS, 60, GreenwardRarity.EPIC, 3);
        }
    }

    private static Map<GreenwardStat, Double> fortune(GreenwardStat stat, int amount) {
        return Map.of(stat, (double) amount);
    }

    /**
     * @param defense the piece's share of its set's total Defense stat (Update 1 § 1.4) —
     *                see {@link ModArmorMaterials} for how each set's total was split.
     *                Every value across every set is a multiple of 5, per the user's
     *                explicit request during playtesting.
     */
    private static Item piece(String name, ArmorMaterial material, ArmorType type, int defense, GreenwardRarity rarity) {
        return piece(name, material, type, defense, rarity, 0, Map.of());
    }

    private static Item piece(String name, ArmorMaterial material, ArmorType type, int defense, GreenwardRarity rarity, Map<GreenwardStat, Double> extraStats) {
        return piece(name, material, type, defense, rarity, 0, extraStats);
    }

    /** @param maxSockets Design Program Update 5 § 5.2 — Tier II gear gets 2, Tier III
     *                    gets 3 (a flat rule off the spec's "1-3" range); Tier I gets 0
     *                    via the 5-arg overload above. */
    private static Item piece(String name, ArmorMaterial material, ArmorType type, int defense, GreenwardRarity rarity, int maxSockets) {
        return piece(name, material, type, defense, rarity, maxSockets, Map.of());
    }

    /** @param extraStats Farming/Mining Fortune's own per-piece share (see
     *                    {@link #FORTUNE_TIER_I}/II/III) — empty for pillars with no
     *                    armor-side Fortune grant (Fishing/Combat). Merged with Defense
     *                    into one {@link GreenwardComponents#STATS} map so both show on
     *                    the item's own tooltip via {@link EquipmentStatContributor}. */
    private static Item piece(String name, ArmorMaterial material, ArmorType type, int defense, GreenwardRarity rarity, int maxSockets, Map<GreenwardStat, Double> extraStats) {
        Map<GreenwardStat, Double> stats = new java.util.HashMap<>(extraStats);
        stats.put(GreenwardStat.DEFENSE, (double) defense);
        Item.Properties properties = new Item.Properties()
                .humanoidArmor(material, type)
                .component(GreenwardComponents.STATS, stats)
                .component(GreenwardComponents.RARITY, rarity);
        if (maxSockets > 0) {
            properties = properties.component(GreenwardComponents.SOCKETS, new SocketData(maxSockets, List.of()));
        }
        return ModItems.register(name, props -> new Item(props), properties);
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
