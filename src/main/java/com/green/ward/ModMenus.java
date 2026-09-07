package com.green.ward;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {

    public static MenuType<AutoHarvesterMenu> AUTO_HARVESTER;
    public static MenuType<AutoMinerMenu> AUTO_MINER;
    public static MenuType<AutoFisherMenu> AUTO_FISHER;
    /** One shared MenuType for all five Effigies — see {@link EffigyMenu}'s own doc
     *  comment for why one Menu class (and so one MenuType) covers all of them. */
    public static MenuType<EffigyMenu> EFFIGY;
    public static MenuType<HeartwoodMenu> HEARTWOOD;
    public static MenuType<VillagerShopMenu> VILLAGER_SHOP;

    public static void initialize() {
        if (GreenwardConfig.ENABLE_HEARTWOOD) {
            HEARTWOOD = register("heartwood", HeartwoodMenu::new);
        }

        if (GreenwardConfig.ENABLE_ECONOMY) {
            VILLAGER_SHOP = register("villager_shop", VillagerShopMenu::new);
        }

        if (!GreenwardConfig.ENABLE_AUTOMATION) {
            return;
        }

        AUTO_HARVESTER = register("auto_harvester", AutoHarvesterMenu::new);
        AUTO_MINER = register("auto_miner", AutoMinerMenu::new);
        AUTO_FISHER = register("auto_fisher", AutoFisherMenu::new);

        if (GreenwardConfig.ENABLE_EFFIGIES) {
            EFFIGY = register("effigy", EffigyMenu::new);
        }
    }

    private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> factory) {
        ResourceKey<MenuType<?>> key = ResourceKey.create(Registries.MENU, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, name));
        return Registry.register(BuiltInRegistries.MENU, key, new MenuType<>(factory, FeatureFlagSet.of()));
    }
}
