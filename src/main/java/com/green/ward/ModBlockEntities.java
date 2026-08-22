package com.green.ward;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static BlockEntityType<AutoHarvesterBlockEntity> AUTO_HARVESTER;
    public static BlockEntityType<AutoMinerBlockEntity> AUTO_MINER;
    public static BlockEntityType<AutoFisherBlockEntity> AUTO_FISHER;

    // Must run after ModBlocks.initialize() — the block entity types are bound to the
    // already-registered machine blocks.
    public static void initialize() {
        if (!GreenwardConfig.ENABLE_AUTOMATION) {
            return;
        }

        AUTO_HARVESTER = register("auto_harvester",
                FabricBlockEntityTypeBuilder.create(AutoHarvesterBlockEntity::new, ModBlocks.AUTO_HARVESTER).build());
        AUTO_MINER = register("auto_miner",
                FabricBlockEntityTypeBuilder.create(AutoMinerBlockEntity::new, ModBlocks.AUTO_MINER).build());
        AUTO_FISHER = register("auto_fisher",
                FabricBlockEntityTypeBuilder.create(AutoFisherBlockEntity::new, ModBlocks.AUTO_FISHER).build());
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        ResourceKey<BlockEntityType<?>> key =
                ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, name));
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
    }
}
