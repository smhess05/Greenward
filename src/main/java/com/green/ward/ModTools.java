package com.green.ward;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;

import java.util.List;
import java.util.Map;

/**
 * Custom tools, three tiers per pillar climbing from diamond-equivalent up through and
 * just past netherite (Atk 7 -> 8 -> 9, every pillar's Tier III weapon-capable tool
 * matches this same ceiling — see the per-material doc comments below for how each
 * pillar hits it), plus three fishing rod tiers per the Sea Creatures addendum — each rod
 * both boosts the sea-creature chance (see SeaCreatureHandler) AND gatekeeps which
 * creatures it can land — and three trident tiers paired with the rods (Barbed/Tidal/
 * Leviathan's), the Fishing pillar's own weapon line.
 *
 * <p>The rods previously baked Lure/Luck of the Sea in as a default ENCHANTMENTS
 * component via {@code Item.Properties.delayedComponent(...)} — plausible-looking (it
 * reused vanilla's own enchantment-reading code, zero mixins) but the actual cause of a
 * user-reported bug where casting with any of the three would spawn a bobber that
 * vanished instantly and never threw; a plain vanilla rod (no baked component) was
 * unaffected. Dropped rather than chased further once it was the only remaining
 * difference from vanilla's own working registration — the rods' real progression lives
 * in their {@link GreenwardComponents#STATS} (Sea Creature Chance / Fishing Speed), which
 * this never touched.
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
    /** Farming/Mining Tier I: diamond-equivalent attack damage bonus (total Atk 7) once
     *  paired with this pillar's own tool registration passing a sword-style 3.0 baseline
     *  instead of a hoe's/pickaxe's usual weak one — see the user-requested "final tier
     *  weapon of each skill should be around netherite tier" pass, which raised every
     *  pillar's top tool onto the same 7 -> 8 -> 9 curve Combat already used. */
    private static final ToolMaterial SCYTHE_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1200, 8.0F, 3.0F, 10, REPAIRS_HARVESTERS_SCYTHE);
    private static final ToolMaterial DRILL_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1400, 9.2F, 3.0F, 10, ItemTags.DIAMOND_TOOL_MATERIALS);
    /** Farming Tier II: netherite-equivalent attack damage bonus (total Atk 8). */
    private static final ToolMaterial CULTIVATORS_SCYTHE_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1800, 8.0F, 4.0F, 10, REPAIRS_CULTIVATORS_SCYTHE);
    /** Farming Tier III — exceeds netherite (total Atk 9), matching every other pillar's
     *  Tier III ceiling. Mining speed matches netherite's own (9.0) — the same stat vanilla
     *  itself uses to say "beyond diamond," reused literally rather than inventing a new one. */
    private static final ToolMaterial HARVEST_WARDEN_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2400, 9.0F, 5.0F, 15, REPAIRS_HARVEST_WARDEN);

    // --- Four Pillars Progression: Mining Tier II / Tier III pickaxes ---
    private static final TagKey<Item> REPAIRS_EXCAVATORS_PICK =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_excavators_pick"));
    private static final TagKey<Item> REPAIRS_BEDROCK_REAVER =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_bedrock_reaver"));
    /** Mining Tier II: mines faster than Prospector's Drill (9.6 vs. 9.2), netherite-
     *  equivalent attack damage bonus (total Atk 8). */
    private static final ToolMaterial EXCAVATORS_PICK_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1800, 9.6F, 4.0F, 10, REPAIRS_EXCAVATORS_PICK);
    /** Mining Tier III — the true speed cap, tuned for a smoother curve off Excavator's
     *  Pick (8.0 diamond -> 9.2 Drill -> 9.6 Pick -> 10.5 Reaver) rather than the original
     *  12.0, which jumped too far past the Tier II step. Substituted for the spec's
     *  "toggleable 3x3 vein-mining + auto-smelt" flavor with a pure stat capstone; see
     *  UPDATING.md. Exceeds netherite on attack damage too (total Atk 9), matching every
     *  other pillar's Tier III ceiling. */
    private static final ToolMaterial BEDROCK_REAVER_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2400, 10.5F, 5.0F, 15, REPAIRS_BEDROCK_REAVER);

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

    /** Post-Design-Program: the Voidstep Blade. Ender Pearls as the repair material fits
     *  the item thematically; raw combat stats sit at Combat Tier I (diamond-equivalent) —
     *  the item's real value is {@link VoidstepAbilityHandler}'s teleport, not its damage. */
    private static final TagKey<Item> REPAIRS_VOIDSTEP_BLADE =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "repairs_voidstep_blade"));
    private static final ToolMaterial VOIDSTEP_BLADE_MATERIAL =
            new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1200, 8.0F, 3.0F, 10, REPAIRS_VOIDSTEP_BLADE);

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

    public static Item VOIDSTEP_BLADE;
    public static Item SCORCHED_LEVIATHAN_ROD;

    // Fishing Tier I / II / III weapons — a trident line paired with the rod tiers
    // (user-requested — "along with each tier of the fishing progression... there should
    // be a weapon that is obtainable"), same 7 -> 8 -> 9 attack-damage curve every other
    // pillar's weapon line uses. Tridents don't use ToolMaterial (vanilla's own is a fixed
    // ItemAttributeModifiers, not material-scaled — see TridentItem.createAttributes()),
    // so tridentAttributes(float) below builds the equivalent by hand per tier.
    public static Item BARBED_TRIDENT;
    public static Item TIDAL_TRIDENT;
    public static Item LEVIATHANS_TRIDENT;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_CUSTOM_TOOLS) {
            return;
        }

        HARVESTERS_SCYTHE = ModItems.register("harvesters_scythe",
                properties -> new HoeItem(SCYTHE_MATERIAL, 3.0F, 0.0F, properties),
                new Item.Properties().component(GreenwardComponents.STATS, Map.of(
                        GreenwardStat.FARMING_FORTUNE, 5.0)));

        PROSPECTORS_DRILL = ModItems.register("prospectors_drill",
                properties -> new Item(properties),
                new Item.Properties().pickaxe(DRILL_MATERIAL, 3.0F, -2.8F)
                        .component(GreenwardComponents.STATS, Map.of(
                                GreenwardStat.MINING_SPEED, 120.0,
                                GreenwardStat.MINING_FORTUNE, 10.0)));

        ANGLERS_LINE = ModItems.register("anglers_line",
                properties -> new FishingRodItem(properties),
                new Item.Properties().durability(256).enchantable(1)
                        .component(GreenwardComponents.STATS, Map.of(
                                GreenwardStat.SEA_CREATURE_CHANCE, 5.0, GreenwardStat.FISHING_SPEED, 10.0)));

        DEEP_SEA_ROD = ModItems.register("deep_sea_rod",
                properties -> new FishingRodItem(properties),
                new Item.Properties().durability(512).enchantable(1)
                        .component(GreenwardComponents.SOCKETS, new SocketData(2, List.of()))
                        .component(GreenwardComponents.STATS, Map.of(
                                GreenwardStat.SEA_CREATURE_CHANCE, 15.0, GreenwardStat.FISHING_SPEED, 20.0)));

        LEVIATHAN_ROD = ModItems.register("leviathan_rod",
                properties -> new FishingRodItem(properties),
                new Item.Properties().durability(1024).enchantable(1)
                        .component(GreenwardComponents.STATS, Map.of(
                                GreenwardStat.SEA_CREATURE_CHANCE, 30.0, GreenwardStat.FISHING_SPEED, 35.0))
                        .component(GreenwardComponents.SOCKETS, new SocketData(3, List.of())));

        BARBED_TRIDENT = ModItems.register("barbed_trident",
                properties -> new TridentItem(properties),
                new Item.Properties().durability(400).enchantable(1)
                        .attributes(tridentAttributes(6.0F))
                        .component(DataComponents.TOOL, TridentItem.createToolProperties())
                        .component(DataComponents.WEAPON, new Weapon(1))
                        .component(GreenwardComponents.STATS, Map.of(GreenwardStat.STRENGTH, 10.0)));

        TIDAL_TRIDENT = ModItems.register("tidal_trident",
                properties -> new TridentItem(properties),
                new Item.Properties().durability(700).enchantable(1)
                        .attributes(tridentAttributes(7.0F))
                        .component(DataComponents.TOOL, TridentItem.createToolProperties())
                        .component(DataComponents.WEAPON, new Weapon(1))
                        .component(GreenwardComponents.SOCKETS, new SocketData(2, List.of()))
                        .component(GreenwardComponents.STATS, Map.of(GreenwardStat.STRENGTH, 20.0)));

        // Not registered through a crafting recipe — obtained via smithing_transform,
        // see data/greenward/recipe/leviathans_trident.json (Tidal Trident + Ascension
        // Template + Heart of the Sea, the latter a rare Abyssal Warden drop — user-
        // requested: "a rare drop with the final fishing rod's sea creature should be
        // needed to craft the final weapon"). Ability lives in TridentAbilityHandler.
        LEVIATHANS_TRIDENT = ModItems.register("leviathans_trident",
                properties -> new TridentItem(properties),
                new Item.Properties().durability(1200).enchantable(1)
                        .attributes(tridentAttributes(8.0F))
                        .component(DataComponents.TOOL, TridentItem.createToolProperties())
                        .component(DataComponents.WEAPON, new Weapon(1))
                        .component(GreenwardComponents.SOCKETS, new SocketData(3, List.of()))
                        .component(GreenwardComponents.STATS, Map.of(
                                GreenwardStat.STRENGTH, 35.0, GreenwardStat.CRIT_DAMAGE, 15.0)));

        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION) {
            CULTIVATORS_SCYTHE = ModItems.register("cultivators_scythe",
                    properties -> new HoeItem(CULTIVATORS_SCYTHE_MATERIAL, 3.0F, 0.0F, properties),
                    new Item.Properties().component(GreenwardComponents.SOCKETS, new SocketData(2, List.of()))
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.FARMING_FORTUNE, 12.0)));

            // Not registered through a crafting recipe — obtained via smithing_transform,
            // see data/greenward/recipe/harvest_warden.json.
            HARVEST_WARDEN = ModItems.register("harvest_warden",
                    properties -> new HoeItem(HARVEST_WARDEN_MATERIAL, 3.0F, 0.0F, properties),
                    new Item.Properties().component(GreenwardComponents.SOCKETS, new SocketData(3, List.of()))
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.FARMING_FORTUNE, 25.0)));
        }

        if (GreenwardConfig.ENABLE_MINING_PROGRESSION) {
            EXCAVATORS_PICK = ModItems.register("excavators_pick",
                    properties -> new Item(properties),
                    new Item.Properties().pickaxe(EXCAVATORS_PICK_MATERIAL, 3.0F, -2.8F)
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.MINING_SPEED, 260.0,
                                    GreenwardStat.MINING_FORTUNE, 30.0))
                            .component(GreenwardComponents.SOCKETS, new SocketData(2, List.of())));

            // Not registered through a crafting recipe — obtained via smithing_transform,
            // see data/greenward/recipe/bedrock_reaver.json.
            BEDROCK_REAVER = ModItems.register("bedrock_reaver",
                    properties -> new Item(properties),
                    new Item.Properties().pickaxe(BEDROCK_REAVER_MATERIAL, 3.0F, -2.8F)
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.MINING_SPEED, 500.0,
                                    GreenwardStat.MINING_FORTUNE, 60.0))
                            .component(GreenwardComponents.SOCKETS, new SocketData(3, List.of())));
        }

        if (GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            MARROWGUARD_BLADE = ModItems.register("marrowguard_blade",
                    properties -> new Item(properties),
                    new Item.Properties().sword(MARROWGUARD_BLADE_MATERIAL, 3.0F, -2.4F)
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.STRENGTH, 10.0)));

            ASHWROUGHT_EDGE = ModItems.register("ashwrought_edge",
                    properties -> new Item(properties),
                    new Item.Properties().sword(ASHWROUGHT_EDGE_MATERIAL, 3.0F, -2.4F)
                            .component(GreenwardComponents.SOCKETS, new SocketData(2, List.of()))
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.STRENGTH, 20.0)));

            // Not registered through a crafting recipe — obtained via smithing_transform,
            // see data/greenward/recipe/reapers_edge.json.
            REAPERS_EDGE = ModItems.register("reapers_edge",
                    properties -> new Item(properties),
                    new Item.Properties().sword(REAPERS_EDGE_MATERIAL, 3.0F, -2.4F)
                            .component(GreenwardComponents.SOCKETS, new SocketData(3, List.of()))
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.STRENGTH, 35.0, GreenwardStat.CRIT_DAMAGE, 15.0)));
        }

        if (GreenwardConfig.ENABLE_VOIDSTEP) {
            VOIDSTEP_BLADE = ModItems.register("voidstep_blade",
                    properties -> new Item(properties),
                    new Item.Properties().sword(VOIDSTEP_BLADE_MATERIAL, 3.0F, -2.4F)
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.STRENGTH, 15.0)));
        }

        if (GreenwardConfig.ENABLE_LAVA_FISHING) {
            // Deliberately a plain Item, not FishingRodItem — vanilla's own use-action
            // would try to cast a real FishingHook, which does nothing useful over lava
            // and would fight with LavaFishingHandler's own UseItemCallback. Stats sit
            // slightly above Leviathan Rod's (30/35) per the user's "should slightly
            // surpass late game water fishing."
            SCORCHED_LEVIATHAN_ROD = ModItems.register("scorched_leviathan_rod",
                    properties -> new Item(properties),
                    new Item.Properties().durability(1024)
                            .component(GreenwardComponents.STATS, Map.of(
                                    GreenwardStat.LAVA_CREATURE_CHANCE, 35.0, GreenwardStat.FISHING_SPEED, 40.0))
                            .component(GreenwardComponents.SOCKETS, new SocketData(3, List.of())));
        }
    }

    /** Mirrors {@link TridentItem#createAttributes()} exactly (same Attack Speed, same
     *  {@link Item#BASE_ATTACK_DAMAGE_ID} tag so it displays and scales identically to
     *  vanilla's own trident) but with a caller-supplied Attack Damage instead of the
     *  hardcoded 8.0 — vanilla's trident has no ToolMaterial to hang a per-tier value off,
     *  so this is the equivalent of this file's other tiers' material-driven damage. */
    private static ItemAttributeModifiers tridentAttributes(float attackDamage) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.9F, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }
}
