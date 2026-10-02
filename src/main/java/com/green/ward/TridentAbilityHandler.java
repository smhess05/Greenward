package com.green.ward;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Riptide Slash — Leviathan's Trident's tier-III-only right-click ability (user-requested:
 * "the final tier having a right click ability that sends out an arc of damage in front of
 * the player using splash particles, the range being about 6 blocks"). Triggered by
 * sneak + right-click specifically, the same disambiguation {@link MiningAbilityHandler}
 * already uses for Bedrock Reaver's two abilities — vanilla's {@code TridentItem} already
 * owns plain right-click for its own charge-and-throw behavior (confirmed against the
 * decompiled source: {@code use()} always calls {@code startUsingItem}, no identity checks
 * that would block a modded trident), and the user was explicit that throwing must keep
 * working, so this only ever intercepts the sneaking case and returns {@code PASS}
 * otherwise, letting vanilla's own handling run untouched.
 */
public final class TridentAbilityHandler {
    private TridentAbilityHandler() {}

    private static final long COOLDOWN_TICKS = 2400L; // 120s, matching every other ability
    private static final double RANGE = 6.0;
    private static final double ARC_COS_THRESHOLD = 0.4; // roughly a 130-degree-wide cone
    private static final float DAMAGE = 12.0F;

    private static final Map<UUID, Long> LAST_USE = new HashMap<>();

    public static void initialize() {
        UseItemCallback.EVENT.register(TridentAbilityHandler::onUseItem);
    }

    private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() != ModTools.LEVIATHANS_TRIDENT || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        long now = level.getGameTime();
        Long last = LAST_USE.get(serverPlayer.getUUID());
        if (last != null && now - last < COOLDOWN_TICKS) {
            return InteractionResult.FAIL;
        }
        LAST_USE.put(serverPlayer.getUUID(), now);

        riptideSlash(serverPlayer, (ServerLevel) level);
        return InteractionResult.SUCCESS;
    }

    private static void riptideSlash(ServerPlayer player, ServerLevel level) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RANGE),
                e -> e != player && e.isAlive())) {
            Vec3 toTarget = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(eyePos);
            double distance = toTarget.length();
            if (distance > RANGE || distance < 0.001) {
                continue;
            }
            if (toTarget.normalize().dot(look) < ARC_COS_THRESHOLD) {
                continue;
            }
            target.hurtServer(level, level.damageSources().playerAttack(player), DAMAGE);
        }

        for (int i = 0; i <= 8; i++) {
            double t = i / 8.0;
            Vec3 point = eyePos.add(look.scale(RANGE * t));
            level.sendParticles(ParticleTypes.SPLASH, point.x, point.y, point.z, 4, 0.25, 0.2, 0.25, 0.03);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.0F, 0.9F);
        player.sendSystemMessage(Component.literal("Riptide Slash!"), true);
    }
}
