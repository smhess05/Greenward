package com.green.ward;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

/**
 * Threat's mob-scaling half (Design Program Update 7 § 7.2/7.4). Every hostile mob gets
 * transient attribute modifiers scaled to the current world Threat value, applied on
 * load and stripped on unload/server-stop — "transient" is load-bearing here, not a
 * style choice: § 1.2 rule 6 requires it, since attribute modifiers DO persist in entity
 * NBT otherwise, which would leave scaled-up mobs baked into a save even after the mod
 * is removed. Drop/XP bonuses only apply to direct, player-caused kills (§ 2.2's general
 * rule, already established for The Marked and reused everywhere else in this project)
 * — a fall-damage or lava-pit kill gets base drops and base XP.
 */
public final class ThreatMobHandler {
    private ThreatMobHandler() {}

    private static final Identifier HEALTH_MODIFIER = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "threat_health");
    private static final Identifier DAMAGE_MODIFIER = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "threat_damage");
    private static final Identifier SPEED_MODIFIER = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "threat_speed");

    /** XP is a flat per-kill approximation (5, vanilla's own common hostile-mob baseline)
     *  rather than reading each mob's real reward — same documented tradeoff as Bone/
     *  Gunpowder collection counting elsewhere in this project. */
    private static final int BASE_XP_APPROXIMATION = 5;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_THREAT) {
            return;
        }
        ServerEntityEvents.ENTITY_LOAD.register(ThreatMobHandler::onLoad);
        ServerEntityEvents.ENTITY_UNLOAD.register(ThreatMobHandler::onUnload);
        ServerLifecycleEvents.SERVER_STOPPING.register(ThreatMobHandler::onServerStopping);
        LootTableEvents.MODIFY_DROPS.register(ThreatMobHandler::onLootDrops);
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register(ThreatMobHandler::onBossDeath);
    }

    /** Design Program Update 10 § 10.1/10.3 — the Ender Dragon/Wither feed Threat's own
     *  "bosses defeated" input, and at Threat 75+ drop Boss Essence (Reaper's Core's new
     *  non-automatable God Potion ingredient, § 10.3). Both bosses require sustained
     *  direct player combat to kill at all, so no extra player-kill check is needed here
     *  the way ordinary mob drops need one. */
    private static void onBossDeath(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        if (!GreenwardConfig.ENABLE_ENDGAME || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        boolean isBoss = entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss;
        if (!isBoss) {
            return;
        }
        ThreatData.get(level.getServer()).recordBossDefeated();
        int threat = ThreatManager.computeThreat(level.getServer());
        if (threat >= 75) {
            entity.spawnAtLocation(level, new ItemStack(ModItems.BOSS_ESSENCE, 1));
        }
    }

    private static void onLoad(Entity entity, ServerLevel level) {
        boolean isBoss = entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss;
        if (!(entity instanceof Monster monster) && !isBoss) {
            return;
        }
        LivingEntity living = (LivingEntity) entity;
        int threat = ThreatManager.computeThreat(level.getServer());
        // Update 10 § 10.1 — the Dragon/Wither scale on a steeper curve than ordinary
        // hostiles (health x6 at Threat 100, vs. x3 for everything else).
        double healthFraction = isBoss ? threat / 100.0 * 5.0 : threat / 100.0 * 2.0;
        applyModifier(living, Attributes.MAX_HEALTH, HEALTH_MODIFIER, healthFraction);
        if (GreenwardConfig.THREAT_DAMAGE_SCALING) {
            applyModifier(living, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER, threat / 100.0 * 0.8);
        }
        applyModifier(living, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER, threat / 100.0 * 0.35);
        living.setHealth(living.getMaxHealth());
    }

    private static void onUnload(Entity entity, ServerLevel level) {
        stripModifiers(entity);
    }

    private static void onServerStopping(net.minecraft.server.MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                stripModifiers(entity);
            }
        }
    }

    /** Used by {@code /greenward decommission} to sweep loaded chunks clean too. */
    static void stripModifiers(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        removeIfPresent(living, Attributes.MAX_HEALTH, HEALTH_MODIFIER);
        removeIfPresent(living, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER);
        removeIfPresent(living, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER);
    }

    private static void applyModifier(LivingEntity entity, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                       Identifier id, double fraction) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null || fraction <= 0.0) {
            return;
        }
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, fraction, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void removeIfPresent(LivingEntity entity, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, Identifier id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private static void onLootDrops(Holder<LootTable> table, LootContext context, List<ItemStack> drops) {
        if (!GreenwardConfig.ENABLE_THREAT || drops.isEmpty()) {
            return;
        }
        if (!context.hasParameter(LootContextParams.THIS_ENTITY) || !context.hasParameter(LootContextParams.DIRECT_ATTACKING_ENTITY)) {
            return;
        }
        Entity dying = context.getParameter(LootContextParams.THIS_ENTITY);
        Entity directAttacker = context.getParameter(LootContextParams.DIRECT_ATTACKING_ENTITY);
        if (!(dying instanceof Monster) || !(directAttacker instanceof ServerPlayer player)) {
            return;
        }

        int threat = ThreatManager.computeThreat(((ServerLevel) dying.level()).getServer());
        double multiplier = 1.0 + threat / 100.0 * 1.5; // up to x2.5 at Threat 100
        for (ItemStack stack : drops) {
            int scaled = (int) Math.round(stack.getCount() * multiplier);
            stack.setCount(Math.max(stack.getCount(), scaled));
        }

        double xpMultiplier = 1.0 + threat / 100.0 * 2.0; // up to x3.0 at Threat 100
        int bonusXp = (int) Math.round(BASE_XP_APPROXIMATION * (xpMultiplier - 1.0));
        if (bonusXp > 0 && dying.level() instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, dying.position(), bonusXp);
        }
    }
}
