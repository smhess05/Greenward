package com.green.ward;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Coins-per-unit for the universal sell mechanic (Coin Purse). Not exhaustive by design —
 * covers common ore/farm/mob/fish drops plus Greenward's own raw-tier and rung-1/2
 * compressed goods, priced as roughly the raw-material value of what went into them minus
 * a small margin (nobody should make money compressing something purely to sell it).
 * Extend this map as new sellable items come up rather than trying to cover everything
 * up front — same "extend as needed" philosophy as {@link GemMaterial}'s own price ladder.
 */
public final class SellPriceTable {
    private SellPriceTable() {}

    private static final Map<Item, Long> PRICES = new HashMap<>();

    static {
        // --- Common raw materials ---
        price(Items.COBBLESTONE, 1);
        price(Items.COBBLED_DEEPSLATE, 1);
        price(Items.DIRT, 1);
        price(Items.SAND, 1);
        price(Items.GRAVEL, 1);
        price(Items.ROTTEN_FLESH, 1);
        price(Items.STRING, 2);
        price(Items.SPIDER_EYE, 2);
        price(Items.WHEAT, 2);
        price(Items.CARROT, 2);
        price(Items.POTATO, 2);
        price(Items.BEETROOT, 2);
        price(Items.BONE, 2);
        price(Items.SUGAR_CANE, 2);
        price(Items.COCOA_BEANS, 2);
        price(Items.NETHER_WART, 3);
        price(Items.REDSTONE, 3);
        price(Items.LAPIS_LAZULI, 3);
        price(Items.COPPER_INGOT, 3);
        price(Items.GUNPOWDER, 4);
        price(Items.COAL, 4);
        price(Items.QUARTZ, 4);
        price(Items.PRISMARINE_SHARD, 4);
        price(Items.PRISMARINE_CRYSTALS, 5);
        price(Items.SLIME_BALL, 5);
        price(Items.AMETHYST_SHARD, 6);
        price(Items.IRON_INGOT, 6);
        price(Items.COD, 3);
        price(Items.SALMON, 4);
        price(Items.TROPICAL_FISH, 6);
        price(Items.PUFFERFISH, 6);
        price(Items.DRIED_KELP, 1);
        price(Items.GOLD_INGOT, 8);
        price(Items.GLOWSTONE_DUST, 3);
        price(Items.MAGMA_CREAM, 6);

        // --- Uncommon / boss-adjacent ---
        price(Items.BLAZE_ROD, 12);
        price(Items.ENDER_PEARL, 15);
        price(Items.NAUTILUS_SHELL, 20);
        price(Items.EMERALD, 25);
        price(Items.GHAST_TEAR, 25);
        price(Items.DIAMOND, 40);
        price(Items.NETHERITE_SCRAP, 150);
        price(Items.HEART_OF_THE_SEA, 250);
        price(Items.DRAGON_BREATH, 150);
        price(Items.NETHERITE_INGOT, 400);
        price(Items.NETHER_STAR, 1000);

        // --- Greenward raw + rung-1/2 compressed goods (see § 0.1's compression tables) ---
        price(ModItems.WHEAT_SHEAF, 16);
        price(Items.HAY_BLOCK, 130); // Wheat's own rung 2 reuses vanilla's Hay Block
        price(ModItems.POTATO_SACK, 16);
        price(ModItems.POTATO_CRATE, 130);
        price(ModItems.COBBLE_CLUSTER, 8);
        price(ModItems.COBBLE_MONOLITH, 65);
        price(ModItems.DEEPSLATE_CLUSTER, 8);
        price(ModItems.DEEPSLATE_MONOLITH, 65);
        price(ModItems.COD_SCHOOL, 24);
        price(ModItems.COD_SHOAL, 195);
        price(ModItems.KELP_REEF, 8);
        price(ModItems.BONE_BUNDLE, 16);
        price(ModItems.BONE_RELIQUARY, 130);
        price(ModItems.POWDER_SATCHEL, 32);
        price(ModItems.POWDER_CACHE, 260);
        price(ModItems.GILDED_CARROT, 16);
        price(ModItems.RADIANT_CARROT, 130);
    }

    private static void price(Item item, long coins) {
        if (item != null) {
            PRICES.put(item, coins);
        }
    }

    /** @return the per-unit price, or 0 if this item can't be sold. */
    public static long unitPrice(Item item) {
        return PRICES.getOrDefault(item, 0L);
    }

    public static boolean isSellable(Item item) {
        return PRICES.containsKey(item);
    }
}
