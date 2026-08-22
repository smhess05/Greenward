package com.green.ward;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.HoeItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Armor set bonuses. Checked every 20 ticks (not every tick) across online players —
 * cheap enough that several players wearing full sets has no measurable TPS impact.
 * All granted MobEffectInstances are ambient with hidden particles (no permanent swirl).
 * Radius/drop-chance bonuses are NOT applied here — those are read directly at their
 * point of use (see Greenward.radiusForHoe / harvestArea) since they only matter at the
 * instant of a hoe swing, not as persistent state.
 */
public final class SetBonusHandler {
    private SetBonusHandler() {}

    private static final int CHECK_INTERVAL = 20;
    private static final int EFFECT_DURATION = 60;

    private static final Identifier PROSPECTOR_SAFE_FALL_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "prospector_safe_fall");
    private static final AttributeModifier PROSPECTOR_SAFE_FALL = new AttributeModifier(
            PROSPECTOR_SAFE_FALL_ID, 1.0, AttributeModifier.Operation.ADD_VALUE);

    private static final Identifier ANGLER_LUCK_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "angler_fishing_luck");
    private static final AttributeModifier ANGLER_LUCK = new AttributeModifier(
            ANGLER_LUCK_ID, 1.0, AttributeModifier.Operation.ADD_VALUE);

    // --- Four Pillars Progression: Mining Tier II / III safe-fall (own ids so a tier swap
    // can't collide with Prospector's Tier I modifier or with each other) ---
    private static final Identifier EXCAVATOR_SAFE_FALL_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "excavator_safe_fall");
    private static final AttributeModifier EXCAVATOR_SAFE_FALL = new AttributeModifier(
            EXCAVATOR_SAFE_FALL_ID, 2.0, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier BEDROCK_SAFE_FALL_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "bedrock_safe_fall");
    private static final AttributeModifier BEDROCK_SAFE_FALL = new AttributeModifier(
            BEDROCK_SAFE_FALL_ID, 3.0, AttributeModifier.Operation.ADD_VALUE);

    // --- Four Pillars Progression: Fishing Tier II / III luck (own ids, same reasoning) ---
    private static final Identifier TIDAL_LUCK_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "tidal_fishing_luck");
    private static final AttributeModifier TIDAL_LUCK = new AttributeModifier(
            TIDAL_LUCK_ID, 2.0, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier LEVIATHAN_LUCK_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "leviathan_fishing_luck");
    private static final AttributeModifier LEVIATHAN_LUCK = new AttributeModifier(
            LEVIATHAN_LUCK_ID, 3.0, AttributeModifier.Operation.ADD_VALUE);

    // --- Four Pillars Progression: Combat attack-speed/damage (own ids per tier) ---
    private static final Identifier MARROWGUARD_SPEED_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "marrowguard_attack_speed");
    private static final AttributeModifier MARROWGUARD_SPEED = new AttributeModifier(
            MARROWGUARD_SPEED_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final Identifier ASHWROUGHT_SPEED_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "ashwrought_attack_speed");
    private static final AttributeModifier ASHWROUGHT_SPEED = new AttributeModifier(
            ASHWROUGHT_SPEED_ID, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final Identifier ASHWROUGHT_DAMAGE_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "ashwrought_attack_damage");
    private static final AttributeModifier ASHWROUGHT_DAMAGE = new AttributeModifier(
            ASHWROUGHT_DAMAGE_ID, 1.0, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier REAPER_SPEED_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "reaper_attack_speed");
    private static final AttributeModifier REAPER_SPEED = new AttributeModifier(
            REAPER_SPEED_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final Identifier REAPER_DAMAGE_ID = Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "reaper_attack_damage");
    private static final AttributeModifier REAPER_DAMAGE = new AttributeModifier(
            REAPER_DAMAGE_ID, 2.0, AttributeModifier.Operation.ADD_VALUE);

    /** Grim Resolve cooldown tracker (in-memory only — doesn't need to survive a relog). */
    private static final Map<UUID, Long> GRIM_RESOLVE_LAST_USE = new HashMap<>();
    private static final long GRIM_RESOLVE_COOLDOWN_TICKS = 6000; // 5 minutes

    private static int tickCounter;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_GEAR_SETS && !GreenwardConfig.ENABLE_FARMING_PROGRESSION
                && !GreenwardConfig.ENABLE_MINING_PROGRESSION && !GreenwardConfig.ENABLE_FISHING_PROGRESSION
                && !GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            return;
        }

        ServerTickEvents.END_SERVER_TICK.register(SetBonusHandler::onServerTick);

        if (GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
            ServerLivingEntityEvents.ALLOW_DEATH.register(SetBonusHandler::onAllowDeath);
        }
    }

    /**
     * Grim Resolve: once every 5 minutes, a hit that would kill a player wearing full
     * Reaper's Aegis instead leaves them at 1 HP. A safety net, not a Totem replacement —
     * the long cooldown means it never competes with an actual Totem of Undying, which still
     * fires first (that's vanilla's own death-prevention check, entirely separate from this
     * event). Uses Fabric's ALLOW_DEATH hook rather than a mixin — returning false here
     * cancels the death outright, same mechanism vanilla's own totem save uses internally.
     */
    private static boolean onAllowDeath(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer player) || !ModArmor.hasFullReapersAegisSet(player)) {
            return true;
        }
        long now = player.level().getGameTime();
        Long last = GRIM_RESOLVE_LAST_USE.get(player.getUUID());
        if (last != null && now - last < GRIM_RESOLVE_COOLDOWN_TICKS) {
            return true;
        }
        GRIM_RESOLVE_LAST_USE.put(player.getUUID(), now);
        player.setHealth(1.0F);
        player.removeEffect(MobEffects.MINING_FATIGUE);
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 2, true, true));
        return false;
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (GreenwardConfig.ENABLE_GEAR_SETS) {
                applyHarvesterBonus(player);
                applyProspectorBonus(player);
                applyAnglerBonus(player);
            }
            if (GreenwardConfig.ENABLE_FARMING_PROGRESSION) {
                applyCultivatorBonus(player);
                applyWardenBonus(player);
            }
            if (GreenwardConfig.ENABLE_MINING_PROGRESSION) {
                applyExcavatorBonus(player);
                applyBedrockBonus(player);
            }
            if (GreenwardConfig.ENABLE_FISHING_PROGRESSION) {
                applyTidalBonus(player);
                applyLeviathanBonus(player);
            }
            if (GreenwardConfig.ENABLE_COMBAT_PROGRESSION) {
                applyMarrowguardBonus(player);
                applyAshwroughtBonus(player);
                applyReapersAegisBonus(player);
            }
        }
    }

    private static void applyHarvesterBonus(ServerPlayer player) {
        if (!ModArmor.hasFullHarvesterSet(player)) {
            return;
        }
        if (holdingHoe(player)) {
            grant(player, MobEffects.HASTE, 0);
        }
    }

    private static void applyProspectorBonus(ServerPlayer player) {
        if (!ModArmor.hasFullProspectorSet(player)) {
            removeModifier(player, Attributes.SAFE_FALL_DISTANCE, PROSPECTOR_SAFE_FALL_ID);
            return;
        }

        grant(player, MobEffects.HASTE, 0);
        if (player.getY() < 0) {
            grant(player, MobEffects.NIGHT_VISION, 0);
        }
        addModifierIfMissing(player, Attributes.SAFE_FALL_DISTANCE, PROSPECTOR_SAFE_FALL);
    }

    private static void applyAnglerBonus(ServerPlayer player) {
        if (!ModArmor.hasFullAnglerSet(player)) {
            removeModifier(player, Attributes.LUCK, ANGLER_LUCK_ID);
            return;
        }

        grant(player, MobEffects.WATER_BREATHING, 0);
        grant(player, MobEffects.DOLPHINS_GRACE, 0);
        // Fishing wait-time reduction would need FishingHook's private countdown, which has
        // no clean Fabric hook — substituting a +1 Luck attribute instead (better treasure
        // odds), per the spec's own fallback guidance rather than building a fragile mixin.
        addModifierIfMissing(player, Attributes.LUCK, ANGLER_LUCK);
    }

    private static void applyCultivatorBonus(ServerPlayer player) {
        if (!ModArmor.hasFullCultivatorSet(player)) {
            return;
        }
        if (holdingHoe(player)) {
            grant(player, MobEffects.HASTE, 1); // Haste II
        }
    }

    private static void applyWardenBonus(ServerPlayer player) {
        if (!ModArmor.hasFullWardenSet(player)) {
            return;
        }
        if (holdingHoe(player)) {
            grant(player, MobEffects.HASTE, 1); // Haste II, same as Cultivator's — radius/yield are this tier's real edge
        }
        grant(player, MobEffects.REGENERATION, 0);
    }

    private static void applyExcavatorBonus(ServerPlayer player) {
        if (!ModArmor.hasFullExcavatorSet(player)) {
            removeModifier(player, Attributes.SAFE_FALL_DISTANCE, EXCAVATOR_SAFE_FALL_ID);
            return;
        }
        grant(player, MobEffects.HASTE, 1); // Haste II
        if (player.getY() < 0) {
            grant(player, MobEffects.NIGHT_VISION, 0);
        }
        addModifierIfMissing(player, Attributes.SAFE_FALL_DISTANCE, EXCAVATOR_SAFE_FALL);
    }

    private static void applyBedrockBonus(ServerPlayer player) {
        if (!ModArmor.hasFullBedrockSet(player)) {
            removeModifier(player, Attributes.SAFE_FALL_DISTANCE, BEDROCK_SAFE_FALL_ID);
            return;
        }
        grant(player, MobEffects.HASTE, 1); // Haste II
        grant(player, MobEffects.FIRE_RESISTANCE, 0);
        if (player.getY() < 0) {
            grant(player, MobEffects.NIGHT_VISION, 0);
        }
        addModifierIfMissing(player, Attributes.SAFE_FALL_DISTANCE, BEDROCK_SAFE_FALL);
    }

    private static void applyTidalBonus(ServerPlayer player) {
        if (!ModArmor.hasFullTidalSet(player)) {
            removeModifier(player, Attributes.LUCK, TIDAL_LUCK_ID);
            return;
        }
        grant(player, MobEffects.WATER_BREATHING, 0);
        grant(player, MobEffects.DOLPHINS_GRACE, 0);
        addModifierIfMissing(player, Attributes.LUCK, TIDAL_LUCK);
    }

    private static void applyLeviathanBonus(ServerPlayer player) {
        if (!ModArmor.hasFullLeviathanSet(player)) {
            removeModifier(player, Attributes.LUCK, LEVIATHAN_LUCK_ID);
            return;
        }
        grant(player, MobEffects.WATER_BREATHING, 0);
        grant(player, MobEffects.DOLPHINS_GRACE, 0);
        if (player.isUnderWater()) {
            grant(player, MobEffects.NIGHT_VISION, 0);
        }
        addModifierIfMissing(player, Attributes.LUCK, LEVIATHAN_LUCK);
    }

    private static void applyMarrowguardBonus(ServerPlayer player) {
        if (!ModArmor.hasFullMarrowguardSet(player)) {
            removeModifier(player, Attributes.ATTACK_SPEED, MARROWGUARD_SPEED_ID);
            return;
        }
        addModifierIfMissing(player, Attributes.ATTACK_SPEED, MARROWGUARD_SPEED);
    }

    private static void applyAshwroughtBonus(ServerPlayer player) {
        if (!ModArmor.hasFullAshwroughtSet(player)) {
            removeModifier(player, Attributes.ATTACK_SPEED, ASHWROUGHT_SPEED_ID);
            removeModifier(player, Attributes.ATTACK_DAMAGE, ASHWROUGHT_DAMAGE_ID);
            return;
        }
        addModifierIfMissing(player, Attributes.ATTACK_SPEED, ASHWROUGHT_SPEED);
        addModifierIfMissing(player, Attributes.ATTACK_DAMAGE, ASHWROUGHT_DAMAGE);
    }

    /**
     * Reaper's Aegis full set: Hunter's Mark Aura (see MarkedMobHandler.chanceFor, which
     * checks ModArmor.hasFullReapersAegisSet directly and jumps straight to the spawn-chance
     * cap) plus its own attack speed/damage bump. Grim Resolve is handled separately via
     * the ALLOW_DEATH hook registered in initialize(), not here — it's a death-time check,
     * not a per-tick one.
     */
    private static void applyReapersAegisBonus(ServerPlayer player) {
        if (!ModArmor.hasFullReapersAegisSet(player)) {
            removeModifier(player, Attributes.ATTACK_SPEED, REAPER_SPEED_ID);
            removeModifier(player, Attributes.ATTACK_DAMAGE, REAPER_DAMAGE_ID);
            return;
        }
        addModifierIfMissing(player, Attributes.ATTACK_SPEED, REAPER_SPEED);
        addModifierIfMissing(player, Attributes.ATTACK_DAMAGE, REAPER_DAMAGE);
    }

    private static boolean holdingHoe(ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof HoeItem
                || player.getOffhandItem().getItem() instanceof HoeItem;
    }

    private static void grant(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, amplifier, true, false));
    }

    private static void addModifierIfMissing(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, AttributeModifier modifier) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && !instance.hasModifier(modifier.id())) {
            instance.addTransientModifier(modifier);
        }
    }

    private static void removeModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, Identifier modifierId) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierId);
        }
    }
}
