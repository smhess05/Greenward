package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Locale;

/**
 * Fast travel (user-requested Coin sink — "there needs to be a payoff to spend the coins
 * on"). No BlockEntity: every Waystone's identity lives in {@link WaystoneData}, keyed by
 * dimension + position, so the block itself is a dumb marker. Right-clicking an unbound
 * Waystone binds it to you under an auto-generated name; right-clicking one you've
 * already bound opens a chat-clickable travel list to every other Waystone you know,
 * priced by live distance to the destination (not distance from this Waystone — simpler,
 * and arguably more intuitive: "how far you're actually going" is what should cost Coins).
 *
 * <p>Deliberately plain per-player lists rather than a shared network — two players each
 * bind their own copy of the same physical Waystone independently, matching how a
 * personal fast-travel system (not a public transit system) should feel.
 */
public class WaystoneBlock extends Block {
    private static final long MIN_COST = 10;
    private static final long COST_PER_BLOCK = 1;
    private static final long CROSS_DIMENSION_SURCHARGE = 250;

    public WaystoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        WaystoneData data = WaystoneData.get(serverLevel.getServer());
        String dimensionId = serverLevel.dimension().identifier().toString();
        WaystoneData.Waypoint here = data.findAt(serverPlayer.getUUID(), dimensionId, pos);

        if (here == null) {
            List<WaystoneData.Waypoint> existing = data.get(serverPlayer.getUUID());
            String name = "Waystone " + (existing.size() + 1);
            data.add(serverPlayer.getUUID(), new WaystoneData.Waypoint(name, dimensionId, pos.immutable()));
            serverPlayer.sendSystemMessage(Component.literal("Waystone bound: " + name), true);
            return InteractionResult.SUCCESS;
        }

        List<WaystoneData.Waypoint> known = data.get(serverPlayer.getUUID());
        if (known.size() <= 1) {
            serverPlayer.sendSystemMessage(Component.literal("No other Waystones known yet — go bind one."), true);
            return InteractionResult.SUCCESS;
        }

        serverPlayer.sendSystemMessage(Component.literal("=== Waystone Travel ==="));
        for (WaystoneData.Waypoint target : known) {
            if (target == here) {
                continue;
            }
            long cost = travelCost(serverPlayer, target);
            Component line = Component.literal(String.format(Locale.ROOT, "-> %s (%,d Coins)", target.name(), cost))
                    .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/greenward waystone tp " + target.name())));
            serverPlayer.sendSystemMessage(line, false);
        }
        return InteractionResult.SUCCESS;
    }

    /** @return the Coin cost to travel from the player's CURRENT position to {@code target}
     *  — used both for the travel-list preview here and for {@code /greenward waystone tp}
     *  itself, so the two always agree regardless of how much the player moved in between. */
    static long travelCost(ServerPlayer player, WaystoneData.Waypoint target) {
        String here = ((ServerLevel) player.level()).dimension().identifier().toString();
        if (!here.equals(target.dimension())) {
            return CROSS_DIMENSION_SURCHARGE;
        }
        double distance = Math.sqrt(player.blockPosition().distSqr(target.pos()));
        return Math.max(MIN_COST, (long) (distance * COST_PER_BLOCK));
    }

    static void playTeleportEffects(ServerLevel level, double x, double y, double z) {
        level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
