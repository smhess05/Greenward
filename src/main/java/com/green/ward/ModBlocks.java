package com.green.ward;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {

    public static final Block WHEAT_BALE_BLOCK = register("wheat_bale",
    properties -> new Block(properties), 
    BlockBehaviour.Properties.of()
        .sound(SoundType.GRASS));

    public static final Block FERTILIZED_FARMLAND = register("fertilized_farmland",
    FertilizedFarmlandBlock::new,
    BlockBehaviour.Properties.of()
        .strength(0.6f)
        .sound(SoundType.GRAVEL)
        .randomTicks());

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        // Two keys: one for the BLOCK registry, one for the ITEM registry.
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, name));
        ResourceKey<Item> itemKey  = ResourceKey.create(Registries.ITEM,  Identifier.fromNamespaceAndPath(ModItems.MOD_ID, name));

        // Create the block, giving it its block key in settings (the setId rule, same as items).
        Block block = factory.apply(properties.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        // Create the matching BlockItem — the inventory form that places the block.
        BlockItem blockItem = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }

    public static void initialize() { }
}