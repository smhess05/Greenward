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
    public static BlockEntityType<EffigyBlockEntity> EFFIGY_ROTTING;
    public static BlockEntityType<EffigyBlockEntity> EFFIGY_BONEPILE;
    public static BlockEntityType<EffigyBlockEntity> EFFIGY_WEBBED;
    public static BlockEntityType<EffigyBlockEntity> EFFIGY_VOLATILE;
    public static BlockEntityType<EffigyBlockEntity> EFFIGY_VOID;
    public static BlockEntityType<HeartwoodBlockEntity> HEARTWOOD;

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

        if (GreenwardConfig.ENABLE_EFFIGIES) {
            // Each Effigy needs its own BlockEntityType (bound to its own Block), but all
            // five instantiate the same EffigyBlockEntity class — see that class's doc
            // comment. EffigyBlockEntity resolves its own type back from these very
            // fields via a lazy switch (EffigyBlockEntity.resolveType), the same
            // enum-init-order-safe pattern CompressionLadder/SatchelType already use.
            EFFIGY_ROTTING = register("rotting_effigy",
                    FabricBlockEntityTypeBuilder.create((pos, state) -> new EffigyBlockEntity(EffigyType.ROTTING, pos, state), ModBlocks.ROTTING_EFFIGY).build());
            EFFIGY_BONEPILE = register("bonepile_effigy",
                    FabricBlockEntityTypeBuilder.create((pos, state) -> new EffigyBlockEntity(EffigyType.BONEPILE, pos, state), ModBlocks.BONEPILE_EFFIGY).build());
            EFFIGY_WEBBED = register("webbed_effigy",
                    FabricBlockEntityTypeBuilder.create((pos, state) -> new EffigyBlockEntity(EffigyType.WEBBED, pos, state), ModBlocks.WEBBED_EFFIGY).build());
            EFFIGY_VOLATILE = register("volatile_effigy",
                    FabricBlockEntityTypeBuilder.create((pos, state) -> new EffigyBlockEntity(EffigyType.VOLATILE, pos, state), ModBlocks.VOLATILE_EFFIGY).build());
            EFFIGY_VOID = register("void_effigy",
                    FabricBlockEntityTypeBuilder.create((pos, state) -> new EffigyBlockEntity(EffigyType.VOID, pos, state), ModBlocks.VOID_EFFIGY).build());
        }

        if (GreenwardConfig.ENABLE_HEARTWOOD) {
            HEARTWOOD = register("heartwood",
                    FabricBlockEntityTypeBuilder.create(HeartwoodBlockEntity::new, ModBlocks.HEARTWOOD).build());
        }
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        ResourceKey<BlockEntityType<?>> key =
                ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, name));
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
    }
}
