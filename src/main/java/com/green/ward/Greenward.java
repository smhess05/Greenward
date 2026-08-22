package com.green.ward;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Greenward implements ModInitializer {

    @Override
    public void onInitialize() {
        ModItems.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModMenus.initialize();
        ModArmor.initialize();
        ModTools.initialize();
        ModPotions.initialize();
        SetBonusHandler.initialize();
        SeaCreatureHandler.initialize();
        MarkedMobHandler.initialize();
        ModCreativeTab.initialize();

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);

            if (!(held.getItem() instanceof HoeItem) || level.isClientSide()) {
                return InteractionResult.PASS;
            }

            BlockPos center = hitResult.getBlockPos();
            BlockState state = level.getBlockState(center);

            if (!HarvestLogic.isMatureCrop(state)) {
                return InteractionResult.PASS;
            }

            int radius = radiusForHoe(held.getItem(), player);
            harvestArea((ServerLevel) level, center, radius, player, hand);

            return InteractionResult.SUCCESS;
        });

        if (GreenwardConfig.ENABLE_FERTILIZER) {
            UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
                ItemStack held = player.getItemInHand(hand);

                if (held.getItem() != ModItems.FERTILIZER || level.isClientSide()) {
                    return InteractionResult.PASS;
                }

                BlockPos pos = hitResult.getBlockPos();
                BlockState clicked = level.getBlockState(pos);

                if (clicked.getBlock() != Blocks.FARMLAND) {
                    return InteractionResult.PASS;
                }

                int moisture = clicked.getValue(FarmlandBlock.MOISTURE);
                level.setBlockAndUpdate(pos,
                        ModBlocks.FERTILIZED_FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, moisture));

                if (!player.isCreative()) {
                    held.shrink(1);
                }

                return InteractionResult.SUCCESS;
            });
        }
    }

    /**
     * Base radius per hoe tier, plus a tier-matched armor bonus. Deliberately NOT a single
     * uniform "+1 for any hoe" rule past Tier I — that would let a Tier II tool + Tier I
     * armor reach the Tier III-only cap of 4 (9x9) by accident. Each tier's bonus only
     * applies to that tier's own tool:
     *   - diamond/netherite/Harvester's Scythe (T1): base 2, +1 with full Harvester's Garb → 3
     *   - Cultivator's Scythe (T2): flat base 3, no armor-driven bonus → caps at 3 too
     *   - Harvest Warden (T3): base 3, +1 ONLY with full Warden's Garb → 4, the true cap,
     *     reachable only by the matched Tier III tool+armor combo.
     */
    private static int radiusForHoe(Item item, Player player) {
        boolean gearEnabled = GreenwardConfig.ENABLE_GEAR_SETS;

        if (item == ModTools.HARVEST_WARDEN) {
            return 3 + (gearEnabled && ModArmor.hasFullWardenSet(player) ? 1 : 0);
        }
        if (item == ModTools.CULTIVATORS_SCYTHE) {
            return 3;
        }
        if (item == Items.NETHERITE_HOE || item == Items.DIAMOND_HOE || item == ModTools.HARVESTERS_SCYTHE) {
            return 2 + (gearEnabled && ModArmor.hasFullHarvesterSet(player) ? 1 : 0);
        }
        if (item == Items.IRON_HOE) {
            return 1;
        }
        return 0;
    }

    /** Extra-drop-roll chance escalates with the Farming pillar's armor tier: 10/20/35%. */
    private static float extraDropChanceFor(Player player) {
        if (!GreenwardConfig.ENABLE_GEAR_SETS) {
            return 0.0F;
        }
        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION && ModArmor.hasFullWardenSet(player)) {
            return 0.35F;
        }
        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION && ModArmor.hasFullCultivatorSet(player)) {
            return 0.20F;
        }
        return ModArmor.hasFullHarvesterSet(player) ? 0.10F : 0.0F;
    }

    private static void harvestArea(ServerLevel level, BlockPos center, int radius,
                                    Player player, InteractionHand hand) {
        float extraDropChance = extraDropChanceFor(player);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (HarvestLogic.isMatureCrop(state)) {
                        HarvestLogic.harvestAndReplant(level, pos, state, player, player.getItemInHand(hand), extraDropChance);
                        player.getItemInHand(hand).hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        break;
                    }
                }
            }
        }
    }
}
