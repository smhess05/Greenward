package com.green.ward;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Three custom {@link ArmorMaterial}s, one per automation pillar. In 26.2 armor material
 * is a plain data record (durability, per-{@code ArmorType} defense map, enchantment
 * value, equip sound, toughness, knockback resistance, repair-ingredient tag, and a
 * worn-armor asset id) — there is no {@code ArmorItem} class or pre-1.20.5 enum/interface
 * to extend; a set's pieces are plain {@link net.minecraft.world.item.Item}s built with
 * {@code Item.Properties.humanoidArmor(material, armorType)} (see {@link ModArmor}).
 *
 * Repair ingredients must be a {@link TagKey}, not a bare Item, so each set gets its own
 * single-item tag under data/greenward/tags/item/.
 *
 * Worn-armor textures (the equipment asset) are placeholders reusing vanilla's own
 * assets (LEATHER / IRON / TURTLE_SCUTE) — see GREENWARD_README.md.
 */
public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    public static final TagKey<Item> REPAIRS_HARVESTERS_GARB =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_harvesters_garb"));
    public static final TagKey<Item> REPAIRS_PROSPECTORS_PLATE =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_prospectors_plate"));
    public static final TagKey<Item> REPAIRS_ANGLERS_WEAR =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_anglers_wear"));

    /** Farming set: leather-durability, iron-adjacent protection (13 total). */
    public static final ArmorMaterial HARVESTERS_GARB = new ArmorMaterial(
            5, ArmorMaterials.makeDefense(2, 4, 5, 2, 0), 15, SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F, 0.0F, REPAIRS_HARVESTERS_GARB, EquipmentAssets.LEATHER);

    /** Mining set: iron-durability, diamond-adjacent protection (17 total). */
    public static final ArmorMaterial PROSPECTORS_PLATE = new ArmorMaterial(
            15, ArmorMaterials.makeDefense(3, 5, 6, 3, 0), 9, SoundEvents.ARMOR_EQUIP_IRON,
            1.0F, 0.0F, REPAIRS_PROSPECTORS_PLATE, EquipmentAssets.IRON);

    /** Fishing set: turtle-scute-adjacent durability, iron-adjacent protection (13 total). */
    public static final ArmorMaterial ANGLERS_WEAR = new ArmorMaterial(
            25, ArmorMaterials.makeDefense(2, 4, 5, 2, 0), 9, SoundEvents.ARMOR_EQUIP_TURTLE,
            0.0F, 0.0F, REPAIRS_ANGLERS_WEAR, EquipmentAssets.TURTLE_SCUTE);

    // --- Four Pillars Progression: Farming Tier II / Tier III ---

    public static final TagKey<Item> REPAIRS_CULTIVATORS_GARB =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_cultivators_garb"));
    public static final TagKey<Item> REPAIRS_WARDENS_GARB =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_wardens_garb"));

    /** Farming Tier II: diamond-equivalent protection (20 total) and toughness. */
    public static final ArmorMaterial CULTIVATORS_GARB = new ArmorMaterial(
            33, ArmorMaterials.makeDefense(3, 6, 8, 3, 0), 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0F, 0.0F, REPAIRS_CULTIVATORS_GARB, EquipmentAssets.DIAMOND);

    /**
     * Farming Tier III: deliberately exceeds netherite (20 total / 3.0 toughness / 0.4 kb),
     * smithing-upgraded from Cultivator's Garb. This is the one place in the project that
     * intentionally breaks the original Gear update's "never exceed netherite" guardrail —
     * a later, more specific instruction (Tier III must "genuinely outclass Netherite") for
     * a capstone tier supersedes that general one. Numbers mirror the Combat pillar's own
     * Reaper's Aegis table (24 total / 4.0 toughness / 0.6 kb) so every pillar's Tier III
     * lands at the same power ceiling.
     */
    public static final ArmorMaterial WARDENS_GARB = new ArmorMaterial(
            45, ArmorMaterials.makeDefense(4, 7, 9, 4, 0), 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            4.0F, 0.15F, REPAIRS_WARDENS_GARB, EquipmentAssets.NETHERITE);

    // --- Four Pillars Progression: Mining Tier II / Tier III ---

    public static final TagKey<Item> REPAIRS_EXCAVATORS_PLATE =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_excavators_plate"));
    public static final TagKey<Item> REPAIRS_BEDROCK_PLATE =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_bedrock_plate"));

    /** Mining Tier II: diamond-equivalent protection (20 total) and toughness, mirrors Cultivator's Garb exactly. */
    public static final ArmorMaterial EXCAVATORS_PLATE = new ArmorMaterial(
            33, ArmorMaterials.makeDefense(3, 6, 8, 3, 0), 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0F, 0.0F, REPAIRS_EXCAVATORS_PLATE, EquipmentAssets.DIAMOND);

    /** Mining Tier III: exceeds netherite (24 total / 4.0 toughness / 0.6 kb across a full set),
     *  smithing-upgraded from Excavator's Plate. Same ceiling as every other pillar's Tier III. */
    public static final ArmorMaterial BEDROCK_PLATE = new ArmorMaterial(
            45, ArmorMaterials.makeDefense(4, 7, 9, 4, 0), 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            4.0F, 0.15F, REPAIRS_BEDROCK_PLATE, EquipmentAssets.NETHERITE);

    // --- Four Pillars Progression: Fishing Tier II / Tier III ---

    public static final TagKey<Item> REPAIRS_TIDAL_WEAR =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_tidal_wear"));
    public static final TagKey<Item> REPAIRS_LEVIATHANS_WEAR =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_leviathans_wear"));

    /** Fishing Tier II: diamond-equivalent protection (20 total) and toughness, mirrors Cultivator's Garb exactly. */
    public static final ArmorMaterial TIDAL_WEAR = new ArmorMaterial(
            33, ArmorMaterials.makeDefense(3, 6, 8, 3, 0), 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0F, 0.0F, REPAIRS_TIDAL_WEAR, EquipmentAssets.DIAMOND);

    /** Fishing Tier III: exceeds netherite (24 total / 4.0 toughness / 0.6 kb across a full set),
     *  smithing-upgraded from Tidal Wear. Same ceiling as every other pillar's Tier III. */
    public static final ArmorMaterial LEVIATHANS_WEAR = new ArmorMaterial(
            45, ArmorMaterials.makeDefense(4, 7, 9, 4, 0), 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            4.0F, 0.15F, REPAIRS_LEVIATHANS_WEAR, EquipmentAssets.NETHERITE);

    // --- Four Pillars Progression: Combat Tier I / II / III (no pre-existing Tier I to build on) ---

    public static final TagKey<Item> REPAIRS_MARROWGUARD =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_marrowguard"));
    public static final TagKey<Item> REPAIRS_ASHWROUGHT =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_ashwrought"));
    public static final TagKey<Item> REPAIRS_REAPERS_AEGIS =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_reapers_aegis"));

    /** Combat Tier I: iron-durability, iron-adjacent protection (15 total) — this pillar has no
     *  prior gear to build on, so it starts at the same power level the other three pillars' Tier I did. */
    public static final ArmorMaterial MARROWGUARD = new ArmorMaterial(
            15, ArmorMaterials.makeDefense(2, 5, 6, 2, 0), 9, SoundEvents.ARMOR_EQUIP_CHAIN,
            0.0F, 0.0F, REPAIRS_MARROWGUARD, EquipmentAssets.CHAINMAIL);

    /** Combat Tier II: diamond-equivalent protection (20 total) and toughness, mirrors every other pillar's Tier II. */
    public static final ArmorMaterial ASHWROUGHT = new ArmorMaterial(
            33, ArmorMaterials.makeDefense(3, 6, 8, 3, 0), 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0F, 0.0F, REPAIRS_ASHWROUGHT, EquipmentAssets.DIAMOND);

    /** Combat Tier III — Reaper's Aegis, named directly in the spec: exceeds netherite
     *  (24 total / 4.0 toughness / 0.6 kb across a full set), smithing-upgraded from Ashwrought.
     *  This is the table every other pillar's Tier III was built to match. */
    public static final ArmorMaterial REAPERS_AEGIS = new ArmorMaterial(
            45, ArmorMaterials.makeDefense(4, 7, 9, 4, 0), 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            4.0F, 0.15F, REPAIRS_REAPERS_AEGIS, EquipmentAssets.NETHERITE);
}
