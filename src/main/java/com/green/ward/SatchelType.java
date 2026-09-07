package com.green.ward;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * The four Satchels (Material Economy Update 3 § 3.4) — one per pillar, each vacuuming
 * its pillar's raw drops and compression-ladder items out of the player's main inventory
 * and into its own {@link SatchelContents}. Capacity is not a physical upgrade item —
 * it's computed live off the player's own progress: the highest Tier VI/VIII/X completed
 * among that pillar's collections maps to 256/1024/4096, so a Satchel gets roomier as the
 * player's own Field Guide progress grows, with no separate upgrade item to lose or forget.
 *
 * <p>The backing {@link Item} is resolved lazily via {@link #item()} rather than captured
 * in the constructor — same enum-init-order hazard as {@link CompressionLadder}, since this
 * enum's constants can initialize before {@code ModItems.initialize()} has run.
 */
public enum SatchelType {
    AGRONOMY("agronomy_satchel", "Agronomy Satchel", GreenwardSkill.FARMING, "farming_materials"),
    LITHIC("lithic_satchel", "Lithic Satchel", GreenwardSkill.MINING, "mining_materials"),
    TIDAL("tidal_satchel", "Tidal Satchel", GreenwardSkill.FISHING, "fishing_materials"),
    OSSUARY("ossuary_satchel", "Ossuary Satchel", GreenwardSkill.COMBAT, "combat_materials");

    private final String id;
    private final String displayName;
    private final GreenwardSkill skill;
    private final TagKey<Item> materialsTag;

    SatchelType(String id, String displayName, GreenwardSkill skill, String tagPath) {
        this.id = id;
        this.displayName = displayName;
        this.skill = skill;
        this.materialsTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, tagPath));
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public GreenwardSkill skill() {
        return skill;
    }

    public TagKey<Item> materialsTag() {
        return materialsTag;
    }

    public Item item() {
        return switch (this) {
            case AGRONOMY -> ModItems.AGRONOMY_SATCHEL;
            case LITHIC -> ModItems.LITHIC_SATCHEL;
            case TIDAL -> ModItems.TIDAL_SATCHEL;
            case OSSUARY -> ModItems.OSSUARY_SATCHEL;
        };
    }

    /** @return the Satchel type this stack's item is, or {@code null} if it isn't one. */
    public static SatchelType forItem(Item item) {
        for (SatchelType type : values()) {
            if (type.item() == item) {
                return type;
            }
        }
        return null;
    }
}
