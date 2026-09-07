package com.green.ward;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Shared name-keyed codecs for the mod's own enums, built via vanilla's own
 *  {@link StringRepresentable#fromEnum} — same pattern vanilla uses for {@code Rarity}. */
final class StringRepresentableCodecs {
    private StringRepresentableCodecs() {}

    static final Codec<GreenwardStat> GREENWARD_STAT = StringRepresentable.fromEnum(GreenwardStat::values);
    static final Codec<GreenwardRarity> GREENWARD_RARITY = StringRepresentable.fromEnum(GreenwardRarity::values);
    static final Codec<GreenwardCollection> GREENWARD_COLLECTION = StringRepresentable.fromEnum(GreenwardCollection::values);
    static final Codec<GreenwardSkill> GREENWARD_SKILL = StringRepresentable.fromEnum(GreenwardSkill::values);
    static final Codec<GreenwardProof> GREENWARD_PROOF = StringRepresentable.fromEnum(GreenwardProof::values);
}
