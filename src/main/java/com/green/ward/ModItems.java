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
    // Item Declarations
    public static final Item WHEAT_SHEAF = register("wheat_sheaf", properties -> new Item(properties), new Item.Properties());
    public static final Item GILDED_CARROT = register("gilded_carrot", properties -> new Item(properties), new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build()));
    public static final Item RADIANT_CARROT = register("radiant_carrot", properties -> new Item(properties), new Item.Properties().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.8f).build()));

    public static final Item FERTILIZER = register("fertilizer", properties -> new Item(properties), new Item.Properties());

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
        // can stay empty — referencing the class is enough to run the static fields
    }
}