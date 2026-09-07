package com.green.ward;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * Fusing an Elytra into a chestplate at the smithing table (the user's own suggestion,
 * matching how several real datapacks do it), so armor and glide no longer compete for
 * the chest slot. Base slot accepts any item whose {@link Equippable} component targets
 * {@link EquipmentSlot#CHEST} (vanilla armor and every Greenward chestplate alike);
 * addition slot is a plain vanilla Elytra; no template. This is a genuine
 * {@link net.minecraft.world.item.crafting.SmithingRecipe}, not a crafting-table
 * {@link net.minecraft.world.item.crafting.CustomRecipe} like {@link GemSocketRecipe}/
 * {@link TemperingRecipe} — the smithing menu's fixed 3-slot layout is the whole point
 * of the request, and vanilla's own armor-trim recipe ({@code SmithingTrimRecipe}) is the
 * precedent this class's shape follows: copy the base stack, stamp one component onto it.
 *
 * <p>Copying the base stack (rather than building a fresh result from a fixed template,
 * the way a data-driven {@code smithing_transform} recipe's JSON result would) is what
 * preserves the chestplate's own {@link GreenwardComponents#STATS}/SOCKETS/durability —
 * {@link DataComponents#GLIDER} is a bare marker component (type {@link Unit}), so adding
 * it changes nothing else about the item.
 */
public class ElytraFusionRecipe extends SimpleSmithingRecipe {
    public static final ElytraFusionRecipe INSTANCE = new ElytraFusionRecipe();
    public static final MapCodec<ElytraFusionRecipe> MAP_CODEC = MapCodec.unit(() -> INSTANCE);
    public static final RecipeSerializer<ElytraFusionRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, StreamCodec.unit(INSTANCE));

    private ElytraFusionRecipe() {
        super(new Recipe.CommonInfo(true));
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!input.template().isEmpty()) {
            return false;
        }
        ItemStack base = input.base();
        if (base.isEmpty() || base.has(DataComponents.GLIDER) || !baseIngredient().test(base)) {
            return false;
        }
        return input.addition().is(Items.ELYTRA);
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack result = input.base().copy();
        result.set(DataComponents.GLIDER, Unit.INSTANCE);
        return result;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.empty();
    }

    @Override
    public Ingredient baseIngredient() {
        return CHESTPLATE_INGREDIENT.get();
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.of(Ingredient.of(Items.ELYTRA));
    }

    @Override
    public RecipeSerializer<? extends SimpleSmithingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(List.of(templateIngredient(), Optional.of(baseIngredient()), additionIngredient()));
    }

    /** Built lazily (not at class-init) so every Greenward chestplate item is already
     *  registered by the time the registry scan runs — {@code ModRecipes.initialize()}
     *  only ever fires after {@code ModItems.initialize()} in {@link Greenward}, but a
     *  {@code static final} field on this class would evaluate at class-load time
     *  instead, which is the same enum-init-order hazard {@link TalismanType} works
     *  around elsewhere in this project. */
    private static final java.util.function.Supplier<Ingredient> CHESTPLATE_INGREDIENT = lazy(() -> {
        List<net.minecraft.core.Holder<Item>> chestplates = BuiltInRegistries.ITEM.stream()
                .filter(item -> {
                    Equippable equippable = new ItemStack(item).get(DataComponents.EQUIPPABLE);
                    return equippable != null && equippable.slot() == EquipmentSlot.CHEST;
                })
                .map(BuiltInRegistries.ITEM::wrapAsHolder)
                .toList();
        return Ingredient.of(HolderSet.direct(chestplates));
    });

    private static <T> java.util.function.Supplier<T> lazy(java.util.function.Supplier<T> delegate) {
        return new java.util.function.Supplier<>() {
            private T value;
            private boolean computed;

            @Override
            public T get() {
                if (!computed) {
                    value = delegate.get();
                    computed = true;
                }
                return value;
            }
        };
    }
}
