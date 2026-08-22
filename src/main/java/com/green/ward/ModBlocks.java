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

    // Non-final, assigned conditionally in initialize() so a disabled feature flag
    // simply leaves the field null instead of registering.
    public static Block WHEAT_BALE_BLOCK;
    public static Block FERTILIZED_FARMLAND;

    public static Block AUTO_HARVESTER;
    public static Block AUTO_MINER;
    public static Block AUTO_FISHER;

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

    public static void initialize() {
        if (GreenwardConfig.ENABLE_WHEAT_COMPRESSION) {
            WHEAT_BALE_BLOCK = register("wheat_bale",
                    properties -> new Block(properties),
                    BlockBehaviour.Properties.of()
                        .sound(SoundType.GRASS));
        }

        if (GreenwardConfig.ENABLE_FERTILIZER) {
            FERTILIZED_FARMLAND = register("fertilized_farmland",
                    FertilizedFarmlandBlock::new,
                    BlockBehaviour.Properties.of()
                        .strength(0.6f)
                        .sound(SoundType.GRAVEL)
                        .randomTicks());
        }

        if (GreenwardConfig.ENABLE_AUTOMATION) {
            AUTO_HARVESTER = register("auto_harvester",
                    AutoHarvesterBlock::new,
                    BlockBehaviour.Properties.of()
                        .strength(3.5f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops());

            AUTO_MINER = register("auto_miner",
                    AutoMinerBlock::new,
                    BlockBehaviour.Properties.of()
                        .strength(3.5f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops());

            AUTO_FISHER = register("auto_fisher",
                    AutoFisherBlock::new,
                    BlockBehaviour.Properties.of()
                        .strength(3.5f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops());
        }
    }
}
