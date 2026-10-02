package com.green.ward;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Greenward implements ModInitializer {

    @Override
    public void onInitialize() {
        GreenwardComponents.initialize();
        GreenwardTierCriterion.initialize();
        ModItems.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModMenus.initialize();
        ModEntities.initialize();
        ModArmor.initialize();
        ModTools.initialize();
        ModRecipes.initialize();
        ModPotions.initialize();
        SetBonusHandler.initialize();
        SeaCreatureHandler.initialize();
        MarkedMobHandler.initialize();
        ModCreativeTab.initialize();
        GreenwardDamageHandler.initialize();
        GreenwardFortuneHandler.initialize();
        GreenwardRareOreHandler.initialize();
        MiningAbilityHandler.initialize();
        VoidstepAbilityHandler.initialize();
        TridentAbilityHandler.initialize();
        LavaFishingHandler.initialize();
        ThreatMobHandler.initialize();
        CommissionHandler.initialize();
        VillagerShopHandler.initialize();
        SlayerHandler.initialize();
        GreenwardCombatHandler.initialize();
        SatchelHandler.initialize();
        GreenwardCommands.initialize();

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

        if (GreenwardConfig.ENABLE_FARMING_FISHING_DEPTH) {
            UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
                if (level.isClientSide()) {
                    return InteractionResult.PASS;
                }
                ItemStack held = player.getItemInHand(hand);
                boolean clearingTool = held.getItem() instanceof HoeItem || held.getItem() == Items.BONE_MEAL;
                if (!clearingTool) {
                    return InteractionResult.PASS;
                }

                BlockPos pos = hitResult.getBlockPos();
                if (level.getBlockState(pos).getBlock() != Blocks.FARMLAND) {
                    return InteractionResult.PASS;
                }
                BlightData blight = BlightData.get((ServerLevel) level);
                if (!blight.isBlighted(pos)) {
                    return InteractionResult.PASS;
                }

                blight.clearBlighted(pos);
                player.sendSystemMessage(Component.literal("Blight cleared."));
                if (held.getItem() == Items.BONE_MEAL && !player.isCreative()) {
                    held.shrink(1);
                }
                return InteractionResult.SUCCESS;
            });
        }

        if (GreenwardConfig.ENABLE_FERTILIZER) {
            UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
                ItemStack held = player.getItemInHand(hand);

                if (held.getItem() != ModItems.FERTILIZER || level.isClientSide()) {
                    return InteractionResult.PASS;
                }

                BlockPos pos = hitResult.getBlockPos();
                BlockState clicked = level.getBlockState(pos);

                // User-requested — fertilizing should visibly and permanently swap the
                // block to real Fertilized Farmland (its own dry/wet textures), not an
                // invisible data-only record with a chat message standing in for feedback.
                // The block itself is now the "already fertilized?" check too — no data
                // lookup needed, the texture already answers that on sight.
                if (clicked.getBlock() == ModBlocks.FERTILIZED_FARMLAND) {
                    return InteractionResult.FAIL;
                }
                if (clicked.getBlock() != Blocks.FARMLAND) {
                    return InteractionResult.PASS;
                }

                ServerLevel serverLevel = (ServerLevel) level;
                int moisture = clicked.getValue(FarmlandBlock.MOISTURE);
                serverLevel.setBlockAndUpdate(pos, ModBlocks.FERTILIZED_FARMLAND.defaultBlockState()
                        .setValue(FarmlandBlock.MOISTURE, Math.max(moisture, FarmlandBlock.MAX_MOISTURE)));
                // Kept as a belt-and-suspenders record alongside the block swap above —
                // HarvestLogic's own fertilized check reads this, unaffected by whether the
                // visible block ever reverts through some other vanilla-inherited mechanic.
                FertilizedFarmlandData.get(serverLevel).setFertilized(pos);

                if (!player.isCreative()) {
                    held.shrink(1);
                }

                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.3, 0.2, 0.3, 0.0);
                serverLevel.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

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

    /** Design Program Update 9 § 9.1 retired the flat 10/20/35% extra-drop-roll chance
     *  entirely in favor of real Farming Fortune grants — see each Harvester's/
     *  Cultivator's/Warden's piece's own {@code extraStats} in {@link ModArmor#piece}.
     *  Always 0 now; kept as a real method (not deleted outright) since {@link
     *  HarvestLogic#harvestAndReplant(ServerLevel, BlockPos, BlockState, Entity, ItemStack, float)}
     *  still accepts the parameter for the Auto-Harvester's own call path. */
    private static float extraDropChanceFor(Player player) {
        return 0.0F;
    }

    private static void harvestArea(ServerLevel level, BlockPos center, int radius,
                                    Player player, InteractionHand hand) {
        float extraDropChance = extraDropChanceFor(player);
        int totalColumns = 0;
        int harvestedColumns = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                totalColumns++;
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (HarvestLogic.isMatureCrop(state)) {
                        HarvestLogic.harvestAndReplant(level, pos, state, player, player.getItemInHand(hand), extraDropChance);
                        player.getItemInHand(hand).hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        harvestedColumns++;
                        break;
                    }
                }
            }
        }

        // Full Yield / Unbroken Harvest (Design Program Update 2 § 2.2) — "a complete mature
        // field in one swing," radius-agnostic (see GreenwardProof's own doc comment for why:
        // the literal 9x9/radius-4 reading is circular against the Wheat IX retrofit gate).
        // Radius 0 (a single clicked tile) never counts — that would trivialize the proof.
        if (GreenwardConfig.ENABLE_COLLECTIONS_PROOFS && player instanceof ServerPlayer serverPlayer && radius >= 1) {
            if (harvestedColumns == totalColumns) {
                PlayerProgress.completeProof(serverPlayer, GreenwardProof.FULL_YIELD);
                PlayerProgress.incrementStreak(serverPlayer, GreenwardProof.UNBROKEN_HARVEST, 1, 20);
            } else if (harvestedColumns > 0) {
                PlayerProgress.resetStreak(serverPlayer, GreenwardProof.UNBROKEN_HARVEST);
            }
        }
    }
}
