package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Heartwood (Design Program Update 6 § 6.1). Deliberately holds almost no state of
 * its own — branch progress and socketed talismans live in {@link HeartwoodData}, shared
 * world-wide rather than per-block, matching the spec's "one per world" framing. This
 * class is mostly a menu-opening handle plus the ambient-particle tick (§ 6.1: "add
 * ambient particles from a block entity tick" — the animated-texture pulse itself is
 * pure client-side `.mcmeta`, zero Java).
 */
public class HeartwoodBlockEntity extends BlockEntity implements MenuProvider {

    public HeartwoodBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEARTWOOD, pos, state);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            HeartwoodData data = HeartwoodData.get(serverLevel.getServer());
            if (worldPosition.equals(data.position())) {
                data.clearPosition();
                data.setThreatEnabled(false);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HeartwoodBlockEntity be) {
        if (level.isClientSide() && level.getGameTime() % 10 == 0) {
            Vec3 center = Vec3.atCenterOf(pos);
            double x = center.x + (level.getRandom().nextDouble() - 0.5) * 0.8;
            double y = pos.getY() + 0.6 + level.getRandom().nextDouble() * 0.5;
            double z = center.z + (level.getRandom().nextDouble() - 0.5) * 0.8;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.GLOW, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.greenward.heartwood");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            return new HeartwoodMenu(containerId, inventory, serverPlayer);
        }
        return new HeartwoodMenu(containerId, inventory);
    }
}
