package com.green.ward;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Voidstep Blade's ability — a short combat teleport modeled directly on Hypixel
 * SkyBlock's Aspect of the End: right-click to blink forward along your look vector.
 * Originally shipped with a 10s cooldown (matching SkyBlock's own), but the user asked
 * for it removed entirely after playtesting — every right-click blinks, gated only by
 * how fast the player can click.
 *
 * <p>The destination is found by raycasting forward from eye height so the blink stops
 * just short of a wall instead of teleporting the player inside one — the same COLLIDER
 * clip vanilla itself uses for block-placement/interaction reach, not a custom solid-block
 * scan. This is a simpler safety net than chorus fruit's full multi-attempt search (no
 * suffocation/void fallback), which is an accepted, documented gap given how short the
 * blink distance is.
 */
public final class VoidstepAbilityHandler {
    private VoidstepAbilityHandler() {}

    private static final double TELEPORT_DISTANCE = 8.0;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_VOIDSTEP) {
            return;
        }
        UseItemCallback.EVENT.register(VoidstepAbilityHandler::onUseItem);
    }

    private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() != ModTools.VOIDSTEP_BLADE) {
            return InteractionResult.PASS;
        }

        Vec3 eyePos = serverPlayer.getEyePosition();
        Vec3 look = serverPlayer.getLookAngle();
        Vec3 target = eyePos.add(look.scale(TELEPORT_DISTANCE));

        BlockHitResult hit = level.clip(new ClipContext(eyePos, target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, serverPlayer));
        Vec3 destinationEye = hit.getType() == HitResult.Type.MISS ? target : hit.getLocation().subtract(look.scale(0.5));

        double footY = destinationEye.y - serverPlayer.getEyeHeight();
        serverPlayer.teleportTo(destinationEye.x, footY, destinationEye.z);

        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverPlayer.sendSystemMessage(Component.literal("Voidstep."), true);
        return InteractionResult.SUCCESS;
    }
}
