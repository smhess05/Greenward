package com.green.ward;

import net.minecraft.server.level.ServerPlayer;

/** Skill levels' per-level stat grants (Design Program Update 2 § 2.3), applied as a
 *  {@link StatContributor} alongside {@link EquipmentStatContributor}. */
public final class SkillStatContributor implements StatContributor {

    @Override
    public String label() {
        return "Skills";
    }

    @Override
    public StatProfile contribute(ServerPlayer player) {
        StatProfile profile = StatProfile.empty();
        if (!GreenwardConfig.ENABLE_COLLECTIONS_PROOFS) {
            return profile;
        }
        for (GreenwardSkill skill : GreenwardSkill.values()) {
            int level = PlayerProgress.getSkillLevel(player, skill);
            if (level <= 0) {
                continue;
            }
            skill.perLevel().forEach((stat, amount) -> profile.add(stat, amount * level));
        }
        return profile;
    }
}
