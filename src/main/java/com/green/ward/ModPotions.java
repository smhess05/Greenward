package com.green.ward;

import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The God Potion. Potions are still a plain built-in registry in 26.2 (unlike
 * enchantments, which moved to a reloadable data-pack registry) — {@code Potion} is a
 * plain Java class taking a name + MobEffectInstance varargs, so it registers exactly
 * like ModItems/ModBlocks, no deferred/lookup dance needed.
 *
 * Brewing is registered through Fabric's own {@code FabricPotionBrewingBuilder.BUILD}
 * event (fabric-content-registries-v0) rather than a mixin — vanilla's own
 * {@code PotionBrewing} is an immutable object built once from a builder, and this event
 * is Fabric's sanctioned hook into that builder before it's finalized.
 *
 * Not splash/lingering-craftable by design (only registering the plain potion recipe,
 * never wiring a splash/lingering mix). Not given a custom color override — that would
 * need a per-ItemStack PotionContents.customColor, which the brewing-recipe API here has
 * no hook to set; the potion instead gets vanilla's automatic color blend from its own
 * nine effects, which already reads as distinctly different from any single vanilla
 * potion. Noted as an approximation of the spec's "distinct gold" ask.
 */
public final class ModPotions {
    private ModPotions() {}

    private static final int DURATION = 9600; // 8 minutes

    public static Holder<Potion> GOD_POTION;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_GOD_POTION) {
            return;
        }

        Potion potion = new Potion("god",
                new MobEffectInstance(MobEffects.STRENGTH, DURATION, 0, true, false),
                new MobEffectInstance(MobEffects.RESISTANCE, DURATION, 1, true, false),
                new MobEffectInstance(MobEffects.SPEED, DURATION, 1, true, false),
                new MobEffectInstance(MobEffects.HASTE, DURATION, 1, true, false),
                new MobEffectInstance(MobEffects.REGENERATION, DURATION, 0, true, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0, true, false),
                new MobEffectInstance(MobEffects.WATER_BREATHING, DURATION, 0, true, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, true, false),
                new MobEffectInstance(MobEffects.JUMP_BOOST, DURATION, 0, true, false));

        ResourceKey<Potion> key = ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "god"));
        Registry.register(BuiltInRegistries.POTION, key, potion);
        GOD_POTION = BuiltInRegistries.POTION.wrapAsHolder(potion);

        FabricPotionBrewingBuilder.BUILD.register(builder ->
                ((FabricPotionBrewingBuilder) builder).registerPotionRecipe(
                        Potions.AWKWARD, Ingredient.of(ModItems.GODLY_CATALYST), GOD_POTION));
    }
}
