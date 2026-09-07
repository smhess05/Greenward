package com.green.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** The one persisted Threat input that isn't derivable live from other systems — vanilla
 *  bosses defeated (Design Program Update 7 § 7.1's "up to 15" contribution). Everything
 *  else Threat reads (skill levels, gear tier, Heartwood nodes) already has its own
 *  live source of truth elsewhere. {@link #bossesDefeated} is incremented by Update 10's
 *  boss-kill hook once that exists; reserved here now so {@link ThreatManager} has
 *  somewhere real to read it from rather than a hardcoded zero. */
public final class ThreatData extends SavedData {
    private static final Codec<ThreatData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("bosses_defeated").forGetter(data -> data.bossesDefeated)
    ).apply(instance, ThreatData::new));

    public static final SavedDataType<ThreatData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "threat"),
            ThreatData::new, CODEC, DataFixTypes.LEVEL);

    private int bossesDefeated;

    private ThreatData() {
        this(0);
    }

    private ThreatData(int bossesDefeated) {
        this.bossesDefeated = bossesDefeated;
    }

    public static ThreatData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public int bossesDefeated() {
        return bossesDefeated;
    }

    public void recordBossDefeated() {
        bossesDefeated++;
        setDirty();
    }
}
