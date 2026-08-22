package com.green.ward;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/** Central fuel registry for all automation blocks. */
public final class AutomationFuel {
    private AutomationFuel() {}

    private static final Map<Item, Integer> BURN_VALUES = new HashMap<>();

    static {
        BURN_VALUES.put(Items.COAL, 8);
        BURN_VALUES.put(Items.CHARCOAL, 8);
        BURN_VALUES.put(Items.COAL_BLOCK, 80);
        BURN_VALUES.put(Items.DRIED_KELP, 4);
        BURN_VALUES.put(Items.DRIED_KELP_BLOCK, 40);
        if (GreenwardConfig.ENABLE_WHEAT_COMPRESSION && ModBlocks.WHEAT_BALE_BLOCK != null) {
            BURN_VALUES.put(ModBlocks.WHEAT_BALE_BLOCK.asItem(), 100);
        }
        // Potato Crate is the same kind of dense, farmable organic bale as Wheat Bale —
        // keeps the fuel economy scaling once a player reaches Farming Tier II instead of
        // capping out at whatever Wheat Bale alone provides.
        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION && ModItems.POTATO_CRATE != null) {
            BURN_VALUES.put(ModItems.POTATO_CRATE, 100);
        }
    }

    /** @return number of operations this item powers, or 0 if not a fuel. */
    public static int burnValue(Item item) {
        return BURN_VALUES.getOrDefault(item, 0);
    }

    public static boolean isFuel(Item item) {
        return burnValue(item) > 0;
    }
}
