package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Custom advancement criterion: fires when a player fully completes a collection tier
 * (quantity + Proof, per {@link PlayerProgress#isTierComplete}) — Update 2 § 2.5's
 * "fire a custom advancement criterion on tier completion; the advancement's reward
 * grants the recipe." Registered exactly like a vanilla trigger (e.g.
 * {@code UsedTotemTrigger}), just under the {@code greenward} namespace — no mixin
 * needed, `BuiltInRegistries.TRIGGER_TYPES` is a normal registry mods can add to.
 *
 * <p>An advancement using this trigger looks like:
 * <pre>{@code
 * "criteria": {
 *   "wheat_tier_4": {
 *     "trigger": "greenward:tier_reached",
 *     "conditions": { "collection": "wheat", "tier": 4 }
 *   }
 * }
 * }</pre>
 */
public final class GreenwardTierCriterion extends SimpleCriterionTrigger<GreenwardTierCriterion.TriggerInstance> {

    public static GreenwardTierCriterion TIER_REACHED;

    public static void initialize() {
        TIER_REACHED = Registry.register(BuiltInRegistries.TRIGGER_TYPES,
                ResourceKey.create(Registries.TRIGGER_TYPE, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "tier_reached")),
                new GreenwardTierCriterion());
    }

    /** Called from {@link PlayerProgress} whenever a tier newly becomes fully complete. */
    public static void trigger(ServerPlayer player, GreenwardCollection collection, int tier) {
        if (TIER_REACHED != null) {
            TIER_REACHED.trigger(player, instance -> instance.matches(collection, tier));
        }
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, GreenwardCollection collection,
                                   int tier) implements SimpleCriterionTrigger.SimpleInstance {
        static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                StringRepresentableCodecs.GREENWARD_COLLECTION.fieldOf("collection").forGetter(TriggerInstance::collection),
                Codec.INT.fieldOf("tier").forGetter(TriggerInstance::tier)
        ).apply(instance, TriggerInstance::new));

        boolean matches(GreenwardCollection collection, int tier) {
            return this.collection == collection && this.tier <= tier;
        }
    }
}
