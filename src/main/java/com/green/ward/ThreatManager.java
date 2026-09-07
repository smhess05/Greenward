package com.green.ward;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

/**
 * Computes the current Threat value (Design Program Update 7 § 7.1) — 0-100, derived
 * from the single most-progressed online player (this world's own "how far has anyone
 * gotten" signal, since Threat is per-world rather than per-player). Recomputed live on
 * every read rather than cached/persisted, since every input already has its own live
 * source of truth (skills, equipped gear, Heartwood nodes) except bosses defeated, which
 * {@link ThreatData} does persist.
 */
public final class ThreatManager {
    private ThreatManager() {}

    /** Gated behind the Heartwood (user-requested, post-Design-Program): Threat computes
     *  to a flat 0 — no mob scaling, no drop/XP bonus, nothing — until a Heartwood has
     *  been placed AND its Threat toggle switched on. Every other computeThreatFor input
     *  still accrues normally in the background regardless, so flipping the toggle on
     *  later reflects real progress immediately rather than starting from scratch. */
    public static int computeThreat(net.minecraft.server.MinecraftServer server) {
        if (!GreenwardConfig.ENABLE_HEARTWOOD || !HeartwoodData.get(server).isThreatEnabled()) {
            return 0;
        }
        int best = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            best = Math.max(best, computeThreatFor(player));
        }
        return best;
    }

    private static int computeThreatFor(ServerPlayer player) {
        double skillPart = Math.min(40.0, sumSkillLevels(player) / 200.0 * 40.0);
        double gearPart = Math.min(20.0, sumGearTiers(player) / 12.0 * 20.0);
        double heartwoodPart = Math.min(25.0, sumHeartwoodNodes(player) / 32.0 * 25.0);
        double bossPart = Math.min(15.0, ThreatData.get(((net.minecraft.server.level.ServerLevel) player.level()).getServer()).bossesDefeated() / 4.0 * 15.0);
        return (int) Math.round(skillPart + gearPart + heartwoodPart + bossPart);
    }

    private static int sumSkillLevels(ServerPlayer player) {
        int total = 0;
        for (GreenwardSkill skill : GreenwardSkill.values()) {
            total += PlayerProgress.getSkillLevel(player, skill);
        }
        return total;
    }

    private static int sumHeartwoodNodes(ServerPlayer player) {
        if (!GreenwardConfig.ENABLE_HEARTWOOD) {
            return 0;
        }
        HeartwoodData data = HeartwoodData.get(((net.minecraft.server.level.ServerLevel) player.level()).getServer());
        int total = 0;
        for (HeartwoodBranch branch : HeartwoodBranch.values()) {
            total += data.unlockedCount(branch);
        }
        return total;
    }

    private static int sumGearTiers(ServerPlayer player) {
        return gearTier(player, EquipmentSlot.CHEST);
    }

    /** Highest tier (0-3) worn across the four pillars' chestplates, one contribution
     *  per pillar summed together (max 3 each, 12 total) — the chestplate alone is used
     *  as a proxy for "which set tier is this player wearing" rather than checking all
     *  four pieces per pillar. */
    private static int gearTier(ServerPlayer player, EquipmentSlot slot) {
        Item chest = player.getItemBySlot(slot).getItem();
        int total = 0;
        total += tierOf(chest, ModArmor.HARVESTERS_TUNIC, ModArmor.CULTIVATORS_TUNIC, ModArmor.WARDENS_TUNIC);
        total += tierOf(chest, ModArmor.PROSPECTORS_PLATE, ModArmor.EXCAVATORS_PLATE, ModArmor.BEDROCK_PLATE);
        total += tierOf(chest, ModArmor.ANGLERS_COAT, ModArmor.TIDAL_COAT, ModArmor.LEVIATHANS_COAT);
        total += tierOf(chest, ModArmor.MARROWGUARD_CHESTPLATE, ModArmor.ASHWROUGHT_CHESTPLATE, ModArmor.REAPERS_AEGIS_CHESTPLATE);
        return total;
    }

    private static int tierOf(Item worn, Item tier1, Item tier2, Item tier3) {
        if (worn == tier3) {
            return 3;
        }
        if (worn == tier2) {
            return 2;
        }
        if (worn == tier1) {
            return 1;
        }
        return 0;
    }

    public static String bandName(int threat) {
        if (threat >= 90) {
            return "Merciless";
        }
        if (threat >= 75) {
            return "Savage";
        }
        if (threat >= 50) {
            return "Hostile";
        }
        if (threat >= 25) {
            return "Restless";
        }
        return "Settled";
    }
}
