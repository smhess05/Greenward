package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Enforces Design Program Update 4 § 4.1③'s per-world placement cap ({@link
 * GreenwardConfig#MAX_MACHINES_PER_WORLD}) on the three automation blocks. The per-chunk
 * cap the spec also called for was removed at the user's explicit request — "I want the
 * peak of automation to be very powerful" — so dense single-chunk automation farms are
 * intentionally allowed now. Each block's {@code setPlacedBy} calls {@link
 * #enforceOnPlace} immediately after vanilla has already set the block in the world —
 * there is no vanilla hook to refuse a placement before it commits without a mixin, so a
 * refusal here means undoing it: pop the block back off, return the item, and tell the
 * placer why. {@link AbstractMachineBlockEntity#setRemoved()} calls {@link #onRemoved}
 * for every automation block that leaves the world, by any means (break, explosion,
 * decommission), keeping the count accurate without needing a matching override in each
 * Block subclass for the removal side.
 */
final class MachinePlacementGuard {
    private MachinePlacementGuard() {}

    /** @return true if the placement was allowed to stand. */
    static boolean enforceOnPlace(ServerLevel level, BlockPos pos, LivingEntity placer, ItemStack placedStack, Block block) {
        MachineCountData data = MachineCountData.get(level.getServer());
        int worldCount = data.worldTotal();

        if (worldCount >= GreenwardConfig.MAX_MACHINES_PER_WORLD) {
            refuse(level, pos, placer, placedStack, block,
                    "Refused: this world already has " + GreenwardConfig.MAX_MACHINES_PER_WORLD + " automation blocks (the maximum).");
            return false;
        }

        data.register(level.dimension(), pos);
        return true;
    }

    static void onRemoved(ServerLevel level, BlockPos pos) {
        MachineCountData.get(level.getServer()).unregister(level.dimension(), pos);
    }

    private static void refuse(ServerLevel level, BlockPos pos, LivingEntity placer, ItemStack placedStack, Block block, String message) {
        BlockState current = level.getBlockState(pos);
        if (current.is(block)) {
            level.removeBlock(pos, false);
        }
        if (placer instanceof Player player) {
            ItemStack returned = new ItemStack(block.asItem(), placedStack.isEmpty() ? 1 : placedStack.getCount());
            if (!player.getInventory().add(returned)) {
                Block.popResource(level, pos, returned);
            }
            player.sendSystemMessage(Component.literal(message));
        } else {
            Block.popResource(level, pos, new ItemStack(block.asItem()));
        }
    }
}
