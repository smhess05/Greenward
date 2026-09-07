package com.green.ward;

import net.minecraft.world.item.Item;

/**
 * Rung 1/2 lookup per collection, for the Press/Deep Press machine modules (Design
 * Program Update 3 § 3.3) — deliberately a method-based lookup rather than fields baked
 * into {@link GreenwardCollection}'s enum constructor: the enum's constants initialize
 * during class-loading, which can run before {@code ModItems.initialize()} has assigned
 * its fields, so eagerly capturing e.g. {@code ModItems.WHEAT_SHEAF} there risks
 * capturing {@code null}. A method call here only ever runs during actual gameplay,
 * long after mod init has finished, so it always sees the real registered item.
 *
 * <p>Diamond has no entry — it was never given a compression chain (see
 * {@link GreenwardCollection}'s own doc comment) — so Press/Deep Press correctly leave
 * diamond ore drops uncompressed, same as any other item with no known chain.
 *
 * <p>Only rungs 1/2 are needed here: Press/Deep Press auto-compress that far and no
 * further — rungs 3/4 (Rick/Mow, Massif/Batholith, etc.) stay a manual crafting-table
 * step, matching the spec's own "Machine output auto-compresses to rung 1 [rung 2]"
 * wording exactly (not "to the highest rung available").
 */
final class CompressionLadder {
    private CompressionLadder() {}

    static Item rung1For(GreenwardCollection collection) {
        return switch (collection) {
            case WHEAT -> ModItems.WHEAT_SHEAF;
            case POTATO -> ModItems.POTATO_SACK;
            case CARROT -> ModItems.GILDED_CARROT;
            case COBBLESTONE -> ModItems.COBBLE_CLUSTER;
            case DEEPSLATE -> ModItems.DEEPSLATE_CLUSTER;
            case COD -> ModItems.COD_SCHOOL;
            case BONE -> ModItems.BONE_BUNDLE;
            case GUNPOWDER -> ModItems.POWDER_SATCHEL;
            case DIAMOND -> null;
        };
    }

    static Item rung2For(GreenwardCollection collection) {
        return switch (collection) {
            case WHEAT -> ModBlocks.WHEAT_BALE_BLOCK == null ? null : ModBlocks.WHEAT_BALE_BLOCK.asItem();
            case POTATO -> ModItems.POTATO_CRATE;
            case CARROT -> ModItems.RADIANT_CARROT;
            case COBBLESTONE -> ModItems.COBBLE_MONOLITH;
            case DEEPSLATE -> ModItems.DEEPSLATE_MONOLITH;
            case COD -> ModItems.COD_SHOAL;
            case BONE -> ModItems.BONE_RELIQUARY;
            case GUNPOWDER -> ModItems.POWDER_CACHE;
            case DIAMOND -> null;
        };
    }

    /** @return the collection whose raw item is exactly {@code item}, or {@code null}. */
    static GreenwardCollection collectionForRawItem(Item item) {
        for (GreenwardCollection collection : GreenwardCollection.values()) {
            if (collection.item() == item) {
                return collection;
            }
        }
        return null;
    }
}
