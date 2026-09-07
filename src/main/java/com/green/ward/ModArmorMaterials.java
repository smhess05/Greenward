package com.green.ward;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

/**
 * Twelve custom {@link ArmorMaterial}s, one per pillar tier. In 26.2 armor material is a
 * plain data record (durability, per-{@code ArmorType} defense map, enchantment value,
 * equip sound, toughness, knockback resistance, repair-ingredient tag, and a worn-armor
 * asset id) — there is no {@code ArmorItem} class or pre-1.20.5 enum/interface to
 * extend; a set's pieces are plain {@link net.minecraft.world.item.Item}s built with
 * {@code Item.Properties.humanoidArmor(material, armorType)} (see {@link ModArmor}).
 *
 * <p><b>Design Program Revision 3, Update 1 § 1.4:</b> every material below carries
 * {@code makeDefense(0,0,0,0,0)} and toughness/knockback 0 — Greenward armor grants
 * <em>zero</em> vanilla protection. All mitigation is the Defense stat instead, applied
 * via {@link GreenwardComponents#STATS} on each piece and resolved by
 * {@link GreenwardDamageHandler} through {@link GreenwardFormulas#applyDefense}. Durability,
 * enchantment value, equip sound, repair tag, and worn-armor asset are all unaffected by
 * this — those aren't "protection," they're separate mechanics.
 *
 * <p>Per-piece Defense values are each set's original vanilla defense-point split
 * (boots/legs/chest/helm), scaled so every Tier III set lands at 350 — the midpoint of
 * the spec's stated 320–380 target band (≈78% reduction at {@link GreenwardFormulas}'s
 * {@code Defense/(Defense+100)} curve). The literal formula in the spec text
 * ({@code defensePoints × 8 + toughness × 15}) undershoots that stated target on its
 * own (Tier III comes out to 252, ≈72% reduction) — treated as "roughly," per the spec's
 * own hedge, with the explicit 320–380 target taking precedence. See
 * {@code UPDATING.md} for the full working.
 *
 * <p>Repair ingredients must be a {@link TagKey}, not a bare Item, so each set gets its
 * own single-item tag under data/greenward/tags/item/. Worn-armor textures (the
 * equipment asset) are placeholders reusing vanilla's own assets — see
 * GREENWARD_README.md.
 *
 * <p><b>Post-Design-Program, during playtesting:</b> every per-piece Defense value below
 * was re-rounded to the nearest multiple of 5 (the user's explicit request) — every set's
 * total is unchanged or within 5 of its original value; see {@link ModArmor#piece} for
 * the actual numbers. Farming and Mining armor also each grant their pillar's Fortune
 * stat directly per-piece now (was an invisible, full-set-only bonus for Farming; Mining
 * armor had no Fortune grant at all before), so the number shows on every individual
 * item's tooltip instead of only appearing once all 4 pieces are worn.
 */
public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    private static final Map<ArmorType, Integer> ZERO = ArmorMaterials.makeDefense(0, 0, 0, 0, 0);

    public static final TagKey<Item> REPAIRS_HARVESTERS_GARB =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_harvesters_garb"));
    public static final TagKey<Item> REPAIRS_PROSPECTORS_PLATE =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_prospectors_plate"));
    public static final TagKey<Item> REPAIRS_ANGLERS_WEAR =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_anglers_wear"));

    /** Farming Tier I: leather-durability. Defense stat total 100 (helm15/chest40/legs30/boots15, each a multiple of 5). */
    public static final ArmorMaterial HARVESTERS_GARB = new ArmorMaterial(
            5, ZERO, 15, SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F, 0.0F, REPAIRS_HARVESTERS_GARB, EquipmentAssets.LEATHER);

    /** Mining Tier I: iron-durability. Defense stat total 140 (helm25/chest50/legs40/boots25, each a multiple of 5). */
    public static final ArmorMaterial PROSPECTORS_PLATE = new ArmorMaterial(
            15, ZERO, 9, SoundEvents.ARMOR_EQUIP_IRON,
            0.0F, 0.0F, REPAIRS_PROSPECTORS_PLATE, EquipmentAssets.IRON);

    /** Fishing Tier I: turtle-scute-durability. Defense stat total 100 (cap15/coat40/waders30/fins15, each a multiple of 5). */
    public static final ArmorMaterial ANGLERS_WEAR = new ArmorMaterial(
            25, ZERO, 9, SoundEvents.ARMOR_EQUIP_TURTLE,
            0.0F, 0.0F, REPAIRS_ANGLERS_WEAR, EquipmentAssets.TURTLE_SCUTE);

    // --- Four Pillars Progression: Farming Tier II / Tier III ---

    public static final TagKey<Item> REPAIRS_CULTIVATORS_GARB =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_cultivators_garb"));
    public static final TagKey<Item> REPAIRS_WARDENS_GARB =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_wardens_garb"));

    /** Farming Tier II. Defense stat total 225 (helm35/chest90/legs65/boots35, each a multiple of 5). */
    public static final ArmorMaterial CULTIVATORS_GARB = new ArmorMaterial(
            33, ZERO, 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            0.0F, 0.0F, REPAIRS_CULTIVATORS_GARB, EquipmentAssets.DIAMOND);

    /** Farming Tier III, smithing-upgraded from Cultivator's Garb. Defense stat total 350
     *  (helm60/chest130/legs100/boots60, each a multiple of 5) — every pillar's Tier III lands at this same ceiling. */
    public static final ArmorMaterial WARDENS_GARB = new ArmorMaterial(
            45, ZERO, 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            0.0F, 0.0F, REPAIRS_WARDENS_GARB, EquipmentAssets.NETHERITE);

    // --- Four Pillars Progression: Mining Tier II / Tier III ---

    public static final TagKey<Item> REPAIRS_EXCAVATORS_PLATE =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_excavators_plate"));
    public static final TagKey<Item> REPAIRS_BEDROCK_PLATE =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_bedrock_plate"));

    /** Mining Tier II. Defense stat total 225 (helm35/chest90/legs65/boots35, each a multiple of 5), mirrors Cultivator's Garb. */
    public static final ArmorMaterial EXCAVATORS_PLATE = new ArmorMaterial(
            33, ZERO, 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            0.0F, 0.0F, REPAIRS_EXCAVATORS_PLATE, EquipmentAssets.DIAMOND);

    /** Mining Tier III, smithing-upgraded from Excavator's Plate. Defense stat total 350
     *  (helm60/chest130/legs100/boots60, each a multiple of 5) — same ceiling as every other pillar's Tier III. */
    public static final ArmorMaterial BEDROCK_PLATE = new ArmorMaterial(
            45, ZERO, 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            0.0F, 0.0F, REPAIRS_BEDROCK_PLATE, EquipmentAssets.NETHERITE);

    // --- Four Pillars Progression: Fishing Tier II / Tier III ---

    public static final TagKey<Item> REPAIRS_TIDAL_WEAR =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_tidal_wear"));
    public static final TagKey<Item> REPAIRS_LEVIATHANS_WEAR =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_leviathans_wear"));

    /** Fishing Tier II. Defense stat total 225 (cap35/coat90/waders65/fins35, each a multiple of 5), mirrors Cultivator's Garb. */
    public static final ArmorMaterial TIDAL_WEAR = new ArmorMaterial(
            33, ZERO, 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            0.0F, 0.0F, REPAIRS_TIDAL_WEAR, EquipmentAssets.DIAMOND);

    /** Fishing Tier III, smithing-upgraded from Tidal Wear. Defense stat total 350
     *  (cap60/coat130/waders100/fins60, each a multiple of 5) — same ceiling as every other pillar's Tier III. */
    public static final ArmorMaterial LEVIATHANS_WEAR = new ArmorMaterial(
            45, ZERO, 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            0.0F, 0.0F, REPAIRS_LEVIATHANS_WEAR, EquipmentAssets.NETHERITE);

    // --- Four Pillars Progression: Combat Tier I / II / III (no pre-existing Tier I to build on) ---

    public static final TagKey<Item> REPAIRS_MARROWGUARD =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_marrowguard"));
    public static final TagKey<Item> REPAIRS_ASHWROUGHT =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_ashwrought"));
    public static final TagKey<Item> REPAIRS_REAPERS_AEGIS =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_reapers_aegis"));

    /** Combat Tier I — this pillar has no prior gear to build on, so it starts at the same
     *  power level the other three pillars' Tier I did. Defense stat total 120 (helmet15/chest50/legs40/boots15, each a multiple of 5). */
    public static final ArmorMaterial MARROWGUARD = new ArmorMaterial(
            15, ZERO, 9, SoundEvents.ARMOR_EQUIP_CHAIN,
            0.0F, 0.0F, REPAIRS_MARROWGUARD, EquipmentAssets.CHAINMAIL);

    /** Combat Tier II. Defense stat total 225 (helmet35/chest90/legs65/boots35, each a multiple of 5), mirrors every other pillar's Tier II. */
    public static final ArmorMaterial ASHWROUGHT = new ArmorMaterial(
            33, ZERO, 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            0.0F, 0.0F, REPAIRS_ASHWROUGHT, EquipmentAssets.DIAMOND);

    /** Combat Tier III — Reaper's Aegis, named directly in the spec: this is the table every
     *  other pillar's Tier III was built to match. Defense stat total 350 (helmet60/chest130/legs100/boots60, each a multiple of 5). */
    public static final ArmorMaterial REAPERS_AEGIS = new ArmorMaterial(
            45, ZERO, 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            0.0F, 0.0F, REPAIRS_REAPERS_AEGIS, EquipmentAssets.NETHERITE);
}
