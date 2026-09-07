package com.green.ward;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Custom (non-data-driven) recipe serializers — currently just {@link GemSocketRecipe}. */
public final class ModRecipes {
    private ModRecipes() {}

    public static void initialize() {
        if (GreenwardConfig.ENABLE_MINING_DEPTH) {
            register("gem_socket", GemSocketRecipe.SERIALIZER);
        }
        if (GreenwardConfig.ENABLE_VILLAGERS_SEALS) {
            register("tempering", TemperingRecipe.SERIALIZER);
        }
        if (GreenwardConfig.ENABLE_ELYTRA_FUSION) {
            register("elytra_fusion", ElytraFusionRecipe.SERIALIZER);
        }
    }

    private static void register(String name, RecipeSerializer<?> serializer) {
        ResourceKey<RecipeSerializer<?>> key =
                ResourceKey.create(Registries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, name));
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, key, serializer);
    }
}
