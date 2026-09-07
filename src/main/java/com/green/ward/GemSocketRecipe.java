package com.green.ward;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Socketing a cut gem into a Tier II/III item with room (Design Program Update 5 § 5.2).
 * A plain data-driven recipe can't express "merge this gem's stat into whatever stats
 * the gear already has, and only if a socket is free" — the output depends on the input
 * gear's own current state, not a fixed result — so this is a {@link CustomRecipe}, the
 * same mechanism vanilla itself uses for map cloning, banner duplication, and repairing
 * two damaged items of the same type. Exactly two items in the grid: one socket-eligible
 * item (carries {@link GreenwardComponents#SOCKETS} with room) and one recognized cut gem
 * ({@link GemMaterial}); any other arrangement doesn't match.
 */
public class GemSocketRecipe extends CustomRecipe {
    public static final GemSocketRecipe INSTANCE = new GemSocketRecipe();
    public static final MapCodec<GemSocketRecipe> MAP_CODEC = MapCodec.unit(() -> INSTANCE);
    public static final RecipeSerializer<GemSocketRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findPair(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        Pair pair = findPair(input);
        if (pair == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = pair.gear.copy();
        result.setCount(1);

        Map<GreenwardStat, Double> gemStats = GemMaterial.statsFor(pair.gem.getItem());
        Map<GreenwardStat, Double> existing = result.getOrDefault(GreenwardComponents.STATS, Map.of());
        Map<GreenwardStat, Double> merged = new HashMap<>(existing);
        gemStats.forEach((stat, value) -> merged.merge(stat, value, Double::sum));
        result.set(GreenwardComponents.STATS, merged);

        SocketData sockets = result.get(GreenwardComponents.SOCKETS);
        result.set(GreenwardComponents.SOCKETS, sockets.withGemAdded(pair.gem.getItem()));

        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    private record Pair(ItemStack gear, ItemStack gem) {}

    private static Pair findPair(CraftingInput input) {
        ItemStack gear = null;
        ItemStack gem = null;
        int nonEmptyCount = 0;

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            nonEmptyCount++;
            if (nonEmptyCount > 2) {
                return null;
            }

            SocketData sockets = stack.get(GreenwardComponents.SOCKETS);
            boolean isEligibleGear = sockets != null && !sockets.isFull();
            boolean isGem = GemMaterial.isGem(stack.getItem());

            if (isEligibleGear && gear == null) {
                gear = stack;
            } else if (isGem && gem == null) {
                gem = stack;
            } else {
                return null;
            }
        }

        return (gear != null && gem != null) ? new Pair(gear, gem) : null;
    }
}
