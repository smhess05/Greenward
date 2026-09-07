package com.green.ward;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * The one genuine custom entity type this mod has — see {@link VeinBlastProjectile}'s
 * own doc comment for why the Permanence Charter allows it (transient projectiles only,
 * § 1.2 rule 5's explicit exception).
 */
public final class ModEntities {
    private ModEntities() {}

    public static EntityType<VeinBlastProjectile> VEIN_BLAST;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_MINING_DEPTH) {
            return;
        }
        ResourceKey<EntityType<?>> key =
                ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "vein_blast"));
        EntityType<VeinBlastProjectile> type = EntityType.Builder
                .<VeinBlastProjectile>of(VeinBlastProjectile::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(key);
        VEIN_BLAST = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }
}
