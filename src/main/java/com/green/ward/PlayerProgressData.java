package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The persisted half of a player's progress — collection quantities, completed Proofs,
 * skill XP, and the handful of Proof counters that are genuinely lifetime-cumulative
 * (Rainfed, Down Deep, Bedrock Bound, Cold Iron, Bare-Knuckle, Graveyard Shift). Day-
 * scoped and streak-style counters are deliberately *not* here — they live in
 * {@link PlayerProgress}'s in-memory-only maps instead, the same choice already made for
 * Grim Resolve's cooldown: losing a same-day or in-progress streak counter to a rare
 * server restart is an acceptable, self-healing inconvenience, not lost progress.
 *
 * Stored via a persistent, {@code copyOnDeath} player attachment — plain item/entity
 * data (§ 1.1: "custom player data — dropped on next save — acceptable" even in the
 * worst case), so this carries no permanence risk regardless of what it holds.
 */
public final class PlayerProgressData {
    static final Codec<PlayerProgressData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(StringRepresentableCodecs.GREENWARD_COLLECTION, Codec.LONG)
                    .fieldOf("collections").forGetter(d -> d.collectionQuantity),
            Codec.unboundedMap(StringRepresentableCodecs.GREENWARD_SKILL, Codec.LONG)
                    .fieldOf("skill_xp").forGetter(d -> d.skillXp),
            StringRepresentableCodecs.GREENWARD_PROOF.listOf()
                    .fieldOf("completed_proofs").forGetter(d -> d.completedProofs.stream().toList()),
            Codec.unboundedMap(StringRepresentableCodecs.GREENWARD_PROOF, Codec.LONG)
                    .fieldOf("lifetime_proof_counters").forGetter(d -> d.lifetimeProofCounters)
    ).apply(instance, PlayerProgressData::new));

    final Map<GreenwardCollection, Long> collectionQuantity = new EnumMap<>(GreenwardCollection.class);
    final Map<GreenwardSkill, Long> skillXp = new EnumMap<>(GreenwardSkill.class);
    final Set<GreenwardProof> completedProofs = EnumSet.noneOf(GreenwardProof.class);
    final Map<GreenwardProof, Long> lifetimeProofCounters = new EnumMap<>(GreenwardProof.class);

    PlayerProgressData() {}

    private PlayerProgressData(Map<GreenwardCollection, Long> collectionQuantity, Map<GreenwardSkill, Long> skillXp,
                                java.util.List<GreenwardProof> completedProofs, Map<GreenwardProof, Long> lifetimeProofCounters) {
        this.collectionQuantity.putAll(collectionQuantity);
        this.skillXp.putAll(skillXp);
        this.completedProofs.addAll(completedProofs);
        this.lifetimeProofCounters.putAll(lifetimeProofCounters);
    }

    PlayerProgressData copy() {
        PlayerProgressData copy = new PlayerProgressData();
        copy.collectionQuantity.putAll(this.collectionQuantity);
        copy.skillXp.putAll(this.skillXp);
        copy.completedProofs.addAll(this.completedProofs);
        copy.lifetimeProofCounters.putAll(this.lifetimeProofCounters);
        return copy;
    }
}
