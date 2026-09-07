package com.green.ward;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lava fishing (user-requested, post-Design-Program) — a late-game fishing track parallel
 * to Update 9's water fishing, using the Scorched Leviathan Rod. Vanilla's
 * {@code FishingHook} never bites over lava (its whole "in open water" bobbing state
 * assumes water), so this can't
 * reuse {@link SeaCreatureHandler}'s hook-into-{@code LootTableEvents} approach — there is
 * no vanilla fishing loot roll to intercept here at all. Instead this is a self-contained
 * cast/bite/resolve loop: right-click while looking at lava starts a timed "cast" (no
 * visible bobber entity, which keeps this fully inside the Permanence Charter's rules with
 * no new persistent state), and a server-tick sweep resolves it when the timer elapses.
 */
public final class LavaFishingHandler {
    private LavaFishingHandler() {}

    private static final double REACH = 5.0;
    private static final int MIN_BITE_TICKS = 60;  // 3s
    private static final int MAX_BITE_TICKS = 200; // 10s

    private record Cast(long resolveAtTick, BlockPos lavaPos) {}

    private static final Map<UUID, Cast> ACTIVE_CASTS = new HashMap<>();

    private enum LavaCatch {
        NETHERRACK(30, drop(Items.NETHERRACK, 1, 3)),
        MAGMA_CREAM(20, drop(Items.MAGMA_CREAM, 1, 2)),
        GLOWSTONE_DUST(20, drop(Items.GLOWSTONE_DUST, 2, 4)),
        QUARTZ(15, drop(Items.QUARTZ, 2, 4)),
        BLAZE_POWDER(10, drop(Items.BLAZE_POWDER, 1, 2)),
        GHAST_TEAR(4, drop(Items.GHAST_TEAR, 1, 1)),
        MAGMA_WYRM(1, null);

        final int weight;
        final DropRange dropRange;

        LavaCatch(int weight, DropRange dropRange) {
            this.weight = weight;
            this.dropRange = dropRange;
        }
    }

    private record DropRange(Item item, int min, int max) {}

    private static DropRange drop(Item item, int min, int max) {
        return new DropRange(item, min, max);
    }

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_LAVA_FISHING) {
            return;
        }
        UseItemCallback.EVENT.register(LavaFishingHandler::onUseItem);
        ServerTickEvents.END_SERVER_TICK.register(LavaFishingHandler::onServerTick);
        ServerLivingEntityEvents.AFTER_DEATH.register(LavaFishingHandler::onDeath);
    }

    /** Calibrated to sit slightly above the Abyssal Warden's own drop (Heart of the Sea +
     *  1 Nautilus Shell + 4-8 Prismarine Crystals) per the user's "should slightly surpass
     *  late game water fishing." */
    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (entity.getAttached(MagmaWyrmMarker.ATTACHMENT) == null || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        RandomSource random = level.getRandom();
        entity.spawnAtLocation(level, new ItemStack(ModItems.CINDER_HEART, 1));
        entity.spawnAtLocation(level, new ItemStack(Items.BLAZE_ROD, 2 + random.nextInt(2)));
        entity.spawnAtLocation(level, new ItemStack(Items.GHAST_TEAR, 1 + random.nextInt(2)));
    }

    private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (player.getItemInHand(hand).getItem() != ModTools.SCORCHED_LEVIATHAN_ROD) {
            return InteractionResult.PASS;
        }
        if (ACTIVE_CASTS.containsKey(serverPlayer.getUUID())) {
            return InteractionResult.FAIL;
        }

        Vec3 eyePos = serverPlayer.getEyePosition();
        Vec3 target = eyePos.add(serverPlayer.getLookAngle().scale(REACH));
        BlockHitResult hit = level.clip(new ClipContext(eyePos, target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, serverPlayer));
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.LAVA)) {
            return InteractionResult.PASS;
        }

        double speedStat = PlayerStatManager.get(serverPlayer, GreenwardStat.FISHING_SPEED);
        double reduction = Math.min(0.5, speedStat / 200.0);
        int minTicks = (int) (MIN_BITE_TICKS * (1 - reduction));
        int maxTicks = (int) (MAX_BITE_TICKS * (1 - reduction));
        int delay = minTicks + level.getRandom().nextInt(Math.max(1, maxTicks - minTicks));

        ACTIVE_CASTS.put(serverPlayer.getUUID(), new Cast(level.getGameTime() + delay, hit.getBlockPos().immutable()));
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.6F, 0.7F);
        serverPlayer.sendSystemMessage(Component.literal("You cast your line into the lava..."), true);
        return InteractionResult.SUCCESS;
    }

    private static void onServerTick(MinecraftServer server) {
        if (ACTIVE_CASTS.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        ACTIVE_CASTS.entrySet().removeIf(entry -> {
            if (now < entry.getValue().resolveAtTick()) {
                return false;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                resolveCatch(player, entry.getValue());
            }
            return true;
        });
    }

    private static void resolveCatch(ServerPlayer player, Cast cast) {
        ServerLevel level = player.level();
        double chance = PlayerStatManager.get(player, GreenwardStat.LAVA_CREATURE_CHANCE) / 100.0;
        RandomSource random = level.getRandom();

        LavaCatch result = random.nextFloat() < chance ? LavaCatch.MAGMA_WYRM : rollJunk(random);

        if (result == LavaCatch.MAGMA_WYRM) {
            spawnMagmaWyrm(level, cast.lavaPos(), player);
            player.sendSystemMessage(Component.literal("A Magma Wyrm erupts from the lava!"));
        } else {
            DropRange dr = result.dropRange;
            int count = dr.min() + random.nextInt(dr.max() - dr.min() + 1);
            ItemStack stack = new ItemStack(dr.item(), count);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            player.sendSystemMessage(Component.literal("You reeled in " + count + " " +
                    new ItemStack(dr.item()).getHoverName().getString() + "."), true);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** Weighted among every non-Wyrm entry — the Wyrm's own weight only matters against
     *  {@link GreenwardStat#LAVA_CREATURE_CHANCE} above, not in this junk table. */
    private static LavaCatch rollJunk(RandomSource random) {
        LavaCatch[] junk = {LavaCatch.NETHERRACK, LavaCatch.MAGMA_CREAM, LavaCatch.GLOWSTONE_DUST,
                LavaCatch.QUARTZ, LavaCatch.BLAZE_POWDER, LavaCatch.GHAST_TEAR};
        int totalWeight = 0;
        for (LavaCatch c : junk) totalWeight += c.weight;
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (LavaCatch c : junk) {
            cumulative += c.weight;
            if (roll < cumulative) {
                return c;
            }
        }
        return LavaCatch.NETHERRACK;
    }

    /** The lava-fishing capstone — a reskinned, buffed Blaze, since Blaze is the vanilla
     *  mob that already lives in lava-adjacent terrain and flies (so it doesn't just sink).
     *  Drops are calibrated to sit slightly above the Abyssal Warden's (Heart of the Sea +
     *  1 Nautilus Shell + 4-8 Prismarine Crystals), per the user's explicit "should
     *  slightly surpass late game water fishing" — Cinder Heart is a new, otherwise
     *  unobtainable item, matching how Heart of the Sea is otherwise unobtainable too. */
    private static void spawnMagmaWyrm(ServerLevel level, BlockPos lavaPos, Player player) {
        Mob mob = EntityTypes.BLAZE.create(level, EntitySpawnReason.TRIGGERED);
        if (mob == null) {
            return;
        }
        mob.setPos(lavaPos.getX() + 0.5, lavaPos.getY() + 1.0, lavaPos.getZ() + 0.5);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(lavaPos), EntitySpawnReason.TRIGGERED, null);
        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * 2.5);
            mob.setHealth(mob.getMaxHealth());
        }
        mob.setCustomName(Component.literal("Magma Wyrm"));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.setAttached(MagmaWyrmMarker.ATTACHMENT, Boolean.TRUE);
        level.addFreshEntity(mob);
    }

    /** Split into its own tiny holder purely so {@link MagmaWyrmDeathHandler} can share
     *  the attachment type without this class needing to expose more than one field. */
    static final class MagmaWyrmMarker {
        private MagmaWyrmMarker() {}
        static final net.fabricmc.fabric.api.attachment.v1.AttachmentType<Boolean> ATTACHMENT =
                net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry.createPersistent(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "magma_wyrm"),
                        com.mojang.serialization.Codec.BOOL);
    }
}
