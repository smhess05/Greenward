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
 * Applying a Tempering (Design Program Update 8 § 8.4) — one gear item (anything
 * already carrying {@link GreenwardComponents#STATS}) + one Tempering Stone + one Seal,
 * merges that Tempering's stat grants into the gear. A {@link CustomRecipe} for the same
 * reason {@link GemSocketRecipe} is: the output depends on the gear's own current
 * stats, which no static recipe result can express.
 *
 * <p>Exactly 1 Seal, not a bulk quantity — a normal crafting grid always consumes
 * exactly 1 item per occupied slot per craft (vanilla's own generic consumption logic,
 * not something a recipe's {@code matches()} can override), so requiring more than 1
 * would need the player to feed the same gear back in repeatedly, re-applying the
 * Tempering each time. One Seal per Tempering keeps a single craft click correct.
 */
public class TemperingRecipe extends CustomRecipe {
    public static final TemperingRecipe INSTANCE = new TemperingRecipe();
    public static final MapCodec<TemperingRecipe> MAP_CODEC = MapCodec.unit(() -> INSTANCE);
    public static final RecipeSerializer<TemperingRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findMatch(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        Match match = findMatch(input);
        if (match == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = match.gear.copy();
        result.setCount(1);
        Map<GreenwardStat, Double> existing = result.getOrDefault(GreenwardComponents.STATS, Map.of());
        Map<GreenwardStat, Double> merged = new HashMap<>(existing);
        match.tempering.grants().forEach((stat, value) -> merged.merge(stat, value, Double::sum));
        result.set(GreenwardComponents.STATS, merged);
        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    private record Match(ItemStack gear, TemperingType tempering) {}

    private static Match findMatch(CraftingInput input) {
        ItemStack gear = null;
        TemperingType tempering = null;
        boolean hasSeal = false;
        int nonEmptyCount = 0;

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            nonEmptyCount++;

            if (stack.is(ModItems.SEAL)) {
                hasSeal = true;
                continue;
            }
            TemperingType asStone = TemperingType.forStone(stack.getItem());
            if (asStone != null && tempering == null) {
                tempering = asStone;
                continue;
            }
            if (stack.has(GreenwardComponents.STATS) && gear == null) {
                gear = stack;
                continue;
            }
            return null;
        }

        if (gear == null || tempering == null || !hasSeal || nonEmptyCount != 3) {
            return null;
        }
        return new Match(gear, tempering);
    }
}
