package com.green.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The thrown Bedrock Reaver from Vein Blast (Design Program Update 5 § 5.3). A genuinely
 * new entity type — the Permanence Charter's own § 1.2 rule 5 carves out exactly this
 * exception ("Transient projectiles (Vein Blast) are the only exception, and they never
 * persist"): it never survives a save (discarded the instant it detonates or the tick
 * after spawning at the latest) and never has attribute modifiers or a custom name, so it
 * carries none of the risk that rule exists to prevent.
 *
 * <p>On impact (block or entity, whichever comes first), mines every {@code #c:ores}
 * block within radius 3 by re-firing {@code PlayerBlockBreakEvents.AFTER} for each one —
 * the same event {@link GreenwardFortuneHandler}/{@link GreenwardRareOreHandler} already
 * listen to — so Mining Fortune, collection tracking, and rare-ore rolls all apply
 * exactly as if the player had mined each block by hand. This is deliberate: Vein Blast
 * is a rare (120s cooldown), player-aimed, skill-gated action, not a passive multiplier,
 * so it doesn't violate § 2.1's "machines are the floor" doctrine — it IS a player action.
 */
public class VeinBlastProjectile extends ThrowableItemProjectile {
    private static final int RADIUS = 3;

    public VeinBlastProjectile(EntityType<? extends VeinBlastProjectile> type, Level level) {
        super(type, level);
    }

    public VeinBlastProjectile(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.VEIN_BLAST, owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModTools.BEDROCK_REAVER;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel serverLevel && !level().isClientSide()) {
            BlockPos center = impactCenter(result);
            if (center != null && getOwner() instanceof ServerPlayer player) {
                detonate(serverLevel, player, center);
            }
            discard();
        }
    }

    private static BlockPos impactCenter(HitResult result) {
        if (result instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos();
        }
        if (result instanceof EntityHitResult entityHit) {
            return entityHit.getEntity().blockPosition();
        }
        return null;
    }

    private void detonate(ServerLevel level, ServerPlayer player, BlockPos center) {
        List<BlockPos> targets = new ArrayList<>();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockState(pos).is(GreenwardRareOreHandler.VANILLA_ORES)) {
                        targets.add(pos.immutable());
                    }
                }
            }
        }

        for (BlockPos pos : targets) {
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            for (ItemStack drop : Block.getDrops(state, level, pos, blockEntity, player, player.getMainHandItem())) {
                Block.popResource(level, pos, drop);
            }
            level.removeBlock(pos, false);
            net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.invoker()
                    .afterBlockBreak(level, player, pos, state, blockEntity);
        }
    }
}
