package com.green.ward;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Vein Blast and Stoneflow (Design Program Update 5 § 5.3) — both live on Bedrock
 * Reaver, gated on the Heartwood Stone branch's "Reaver's Wrath" node (Update 6 § 6.2,
 * matching the source text's own "unlocked on the Heartwood's Mining branch"). Before
 * Update 6 shipped, wielding the pickaxe alone was an interim substitute gate — now that
 * the real Heartwood node exists, both are required: hold the Reaver AND have unlocked
 * Reaver's Wrath. Sneak + right-click in empty air throws Vein Blast; plain right-click
 * in empty air triggers Stoneflow. Each has its own independent 120s cooldown — the spec
 * gives both the same cooldown value but never says they share one meter, and separate
 * cooldowns read more like two distinct abilities than one shared "pickaxe special."
 */
public final class MiningAbilityHandler {
    private MiningAbilityHandler() {}

    private static final long COOLDOWN_TICKS = 2400L; // 120s
    private static final long STONEFLOW_DURATION_TICKS = 300L; // 15s
    private static final double STONEFLOW_MULTIPLIER = 2.5; // "+250%"
    private static final float VEIN_BLAST_POWER = 1.5F;

    private static final Map<UUID, Long> LAST_VEIN_BLAST = new HashMap<>();
    private static final Map<UUID, Long> LAST_STONEFLOW = new HashMap<>();

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_MINING_DEPTH) {
            return;
        }
        UseItemCallback.EVENT.register(MiningAbilityHandler::onUseItem);
    }

    private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() != ModTools.BEDROCK_REAVER) {
            return InteractionResult.PASS;
        }
        if (!HeartwoodData.get(((net.minecraft.server.level.ServerLevel) player.level()).getServer())
                .isNodeUnlocked(HeartwoodBranch.STONE, HeartwoodBranch.STONE_REAVERS_WRATH)) {
            return InteractionResult.PASS;
        }

        return player.isShiftKeyDown() ? tryVeinBlast(serverPlayer, held) : tryStoneflow(serverPlayer);
    }

    private static InteractionResult tryVeinBlast(ServerPlayer player, ItemStack held) {
        if (onCooldown(LAST_VEIN_BLAST, player)) {
            return InteractionResult.FAIL;
        }
        LAST_VEIN_BLAST.put(player.getUUID(), player.level().getGameTime());

        Projectile.spawnProjectileUsingShoot(
                VeinBlastProjectile::new, (net.minecraft.server.level.ServerLevel) player.level(),
                held.copyWithCount(1), player, 0.0, 0.0, 0.0, VEIN_BLAST_POWER, 0.2F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.TRIDENT_THROW, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
        player.sendSystemMessage(Component.literal("Vein Blast thrown."), true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult tryStoneflow(ServerPlayer player) {
        if (onCooldown(LAST_STONEFLOW, player)) {
            return InteractionResult.PASS;
        }
        LAST_STONEFLOW.put(player.getUUID(), player.level().getGameTime());

        double currentMiningSpeed = PlayerStatManager.get(player, GreenwardStat.MINING_SPEED);
        double bonus = currentMiningSpeed * STONEFLOW_MULTIPLIER;
        AbilityBuffContributor.grant(player, GreenwardStat.MINING_SPEED, bonus, STONEFLOW_DURATION_TICKS);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.4F);
        player.sendSystemMessage(Component.literal("Stoneflow! +250% Mining Speed for 15s."), true);
        return InteractionResult.SUCCESS;
    }

    private static boolean onCooldown(Map<UUID, Long> lastUse, ServerPlayer player) {
        Long last = lastUse.get(player.getUUID());
        return last != null && player.level().getGameTime() - last < COOLDOWN_TICKS;
    }
}
