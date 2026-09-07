package com.green.ward;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Central fuel registry for all automation blocks — Design Program Update 4 § 4.2.
 * Fuel is a temporary SPEED BOOST, not an operation budget: an unfueled machine still
 * runs forever at its base rate (see {@link AbstractMachineBlockEntity}), and dropping a
 * fuel item into the fuel slot consumes it instantly for a percentage speed bonus that
 * decays over real time rather than being spent operation-by-operation. This replaces the
 * old op-counter model entirely — fuel upgrades throughput, it no longer gates it.
 */
public final class AutomationFuel {
    private AutomationFuel() {}

    private static final Map<Item, Integer> BOOST_PERCENT = new HashMap<>();
    private static final Map<Item, Integer> BOOST_DURATION_TICKS = new HashMap<>();

    private static final int MINUTES = 1200; // 60 seconds * 20 ticks

    static {
        put(Items.COAL, 5, 30 * MINUTES);
        put(Items.CHARCOAL, 5, 30 * MINUTES);
        put(Items.COAL_BLOCK, 5, 4 * 60 * MINUTES);
        put(Items.DRIED_KELP_BLOCK, 8, 60 * MINUTES);
        put(Items.LAVA_BUCKET, 25, 12 * 60 * MINUTES);

        if (GreenwardConfig.ENABLE_WHEAT_COMPRESSION && ModBlocks.WHEAT_BALE_BLOCK != null) {
            put(ModBlocks.WHEAT_BALE_BLOCK.asItem(), 10, 2 * 60 * MINUTES);
        }
        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION && ModItems.POTATO_CRATE != null) {
            put(ModItems.POTATO_CRATE, 10, 2 * 60 * MINUTES);
        }
        if (GreenwardConfig.ENABLE_WHEAT_COMPRESSION && ModItems.WHEAT_RICK != null) {
            put(ModItems.WHEAT_RICK, 15, 6 * 60 * MINUTES);
        }
        if (GreenwardConfig.ENABLE_FARMING_PROGRESSION && ModItems.POTATO_PALLET != null) {
            put(ModItems.POTATO_PALLET, 15, 6 * 60 * MINUTES);
        }
    }

    private static void put(Item item, int percent, int durationTicks) {
        BOOST_PERCENT.put(item, percent);
        BOOST_DURATION_TICKS.put(item, durationTicks);
    }

    /** @return the speed-boost percentage this item grants, or 0 if not a fuel. */
    public static int boostPercent(Item item) {
        if (item == ModItems.SUNWHEEL) {
            return 25;
        }
        return BOOST_PERCENT.getOrDefault(item, 0);
    }

    /** @return how long (ticks) one item's boost lasts. Meaningless for the Sunwheel,
     *  whose effect is permanent — see {@link AbstractMachineBlockEntity}'s separate
     *  handling of {@code sunwheelInstalled}. */
    public static int boostDuration(Item item) {
        return BOOST_DURATION_TICKS.getOrDefault(item, 0);
    }

    public static boolean isFuel(Item item) {
        return item == ModItems.SUNWHEEL || boostPercent(item) > 0;
    }

    public static boolean isSunwheel(Item item) {
        return item == ModItems.SUNWHEEL;
    }
}
