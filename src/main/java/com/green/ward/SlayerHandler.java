package com.green.ward;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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

import java.util.Locale;

/**
 * Slayers (user-requested, post-Design-Program) — the deliberate "break from the grind"
 * moment SkyBlock's own Slayer quests provide, which nothing in Greenward covered before
 * this (The Marked is a passive random bonus, not a goal a player chooses to chase).
 * Reuses The Marked's own five vanilla mob types rather than adding anything new (fully
 * Permanence Charter-safe), summoned on demand by consuming a themed Horn, buffed via the
 * same transient-attribute-modifier technique {@link ThreatMobHandler} already uses.
 *
 * <p>A kill is only rewarded on a genuine player-caused death (checked the same way
 * {@link ThreatMobHandler}'s loot-drop scaling and The Marked's own trigger are — no
 * automatable path exists) and pays out Combat skill XP, a themed guaranteed drop, and a
 * Coins bounty, tying this into the new economy as a real faucet alongside Commissions.
 */
public final class SlayerHandler {
    private SlayerHandler() {}

    private static final AttachmentType<String> MARKER = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "slayer_boss"), Codec.STRING);

    private enum SlayerBoss {
        ZOMBIE("Rotten Colossus", EntityTypes.ZOMBIE, () -> ModItems.SLAYER_HORN_ZOMBIE,
                Items.ROTTEN_FLESH, 4, 8, 150),
        SKELETON("Bonebreaker", EntityTypes.SKELETON, () -> ModItems.SLAYER_HORN_SKELETON,
                Items.BONE, 4, 8, 150),
        SPIDER("Broodmother", EntityTypes.SPIDER, () -> ModItems.SLAYER_HORN_SPIDER,
                Items.STRING, 4, 8, 150),
        CREEPER("Fulminant", EntityTypes.CREEPER, () -> ModItems.SLAYER_HORN_CREEPER,
                Items.GUNPOWDER, 4, 8, 150),
        ENDERMAN("Voidcaller", EntityTypes.ENDERMAN, () -> ModItems.SLAYER_HORN_ENDERMAN,
                Items.ENDER_PEARL, 2, 4, 250);

        final String title;
        final EntityType<? extends Mob> type;
        final java.util.function.Supplier<Item> horn;
        final Item dropItem;
        final int dropMin;
        final int dropMax;
        final long coinReward;

        SlayerBoss(String title, EntityType<? extends Mob> type, java.util.function.Supplier<Item> horn,
                   Item dropItem, int dropMin, int dropMax, long coinReward) {
            this.title = title;
            this.type = type;
            this.horn = horn;
            this.dropItem = dropItem;
            this.dropMin = dropMin;
            this.dropMax = dropMax;
            this.coinReward = coinReward;
        }
    }

    private static final double HEALTH_MULTIPLIER = 8.0;
    private static final double DAMAGE_MULTIPLIER = 2.0;
    private static final long SKILL_XP_REWARD = 50L;
    private static final double SUMMON_DISTANCE = 5.0;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_SLAYERS) {
            return;
        }
        UseItemCallback.EVENT.register(SlayerHandler::onUseItem);
        ServerLivingEntityEvents.AFTER_DEATH.register(SlayerHandler::onDeath);
    }

    private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        SlayerBoss boss = forHorn(held.getItem());
        if (boss == null) {
            return InteractionResult.PASS;
        }

        Vec3 eyePos = serverPlayer.getEyePosition();
        Vec3 target = eyePos.add(serverPlayer.getLookAngle().scale(SUMMON_DISTANCE));
        BlockHitResult hit = serverLevel.clip(new ClipContext(eyePos, target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, serverPlayer));
        Vec3 summonAt = hit.getType() == HitResult.Type.MISS ? target : hit.getLocation();

        Mob mob = boss.type.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (mob == null) {
            return InteractionResult.PASS;
        }
        mob.setPos(summonAt.x, summonAt.y, summonAt.z);
        mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(BlockPos.containing(summonAt)), EntitySpawnReason.TRIGGERED, null);

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * HEALTH_MULTIPLIER);
            mob.setHealth(mob.getMaxHealth());
        }
        AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damage.getBaseValue() * DAMAGE_MULTIPLIER);
        }

        mob.setCustomName(Component.literal(boss.title));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.setAttached(MARKER, boss.name());
        serverLevel.addFreshEntity(mob);

        held.shrink(1);
        serverLevel.playSound(null, summonAt.x, summonAt.y, summonAt.z,
                net.minecraft.sounds.SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.4F);
        serverPlayer.sendSystemMessage(Component.literal("The " + boss.title + " rises!"));
        return InteractionResult.SUCCESS;
    }

    private static SlayerBoss forHorn(Item item) {
        for (SlayerBoss boss : SlayerBoss.values()) {
            if (boss.horn.get() == item) {
                return boss;
            }
        }
        return null;
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        String marker = entity.getAttached(MARKER);
        if (marker == null || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        SlayerBoss boss;
        try {
            boss = SlayerBoss.valueOf(marker);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (!source.isDirect() || !(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        RandomSource random = level.getRandom();
        int count = boss.dropMin + random.nextInt(boss.dropMax - boss.dropMin + 1);
        entity.spawnAtLocation(level, new ItemStack(boss.dropItem, count));
        GreenwardCurrency.add(player, boss.coinReward);
        if (GreenwardConfig.ENABLE_COLLECTIONS_PROOFS) {
            PlayerProgress.addSkillXp(player, GreenwardSkill.COMBAT, SKILL_XP_REWARD);
        }
        player.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
                "%s slain! +%d %s, +%,d Coins, +%d Combat XP.",
                boss.title, count, new ItemStack(boss.dropItem).getHoverName().getString(),
                boss.coinReward, SKILL_XP_REWARD)));
    }
}
