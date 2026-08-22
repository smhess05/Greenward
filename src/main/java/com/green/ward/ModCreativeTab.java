package com.green.ward;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * A single "Greenward" creative tab holding every item this mod registers. Built by
 * filtering {@code BuiltInRegistries.ITEM} down to the mod's own namespace at display
 * time rather than listing fields by hand — a disabled feature flag simply never
 * registers its items in the first place, so this stays correct with zero maintenance
 * as content is added or gated off, instead of needing a hand-kept list to match
 * ModItems/ModArmor/ModTools across three files.
 */
public final class ModCreativeTab {
    private ModCreativeTab() {}

    public static void initialize() {
        ResourceKey<CreativeModeTab> key = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(ModItems.MOD_ID, "greenward"));

        CreativeModeTab tab = FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.greenward"))
                .icon(() -> new ItemStack(ModItems.FIELD_GUIDE))
                .displayItems((parameters, output) -> BuiltInRegistries.ITEM.stream()
                        .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ModItems.MOD_ID))
                        .forEach(output::accept))
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
    }
}
