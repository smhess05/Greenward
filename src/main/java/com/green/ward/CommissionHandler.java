package com.green.ward;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Commissions and Prosperity (Design Program Update 8 § 8.1/8.2) — sneak-right-click a
 * villager to see or deliver its current Commission. Distinct from vanilla trading
 * (which is a plain, un-sneaked right-click, untouched here) — § 8.6's "do not modify
 * vanilla villager trades" is honored by never touching the trade interaction at all.
 *
 * <p>Librarian's spec'd Commission ("complete any outstanding Proof") is not implemented
 * — Proofs aren't a deliverable item, and rigging a delivery-shaped interaction around a
 * non-material objective is a different enough mechanic to warrant its own pass rather
 * than a rushed fit into this one. The four material-delivery professions (Farmer,
 * Mason/Toolsmith, Fisherman, Weaponsmith/Butcher) all work.
 */
public final class CommissionHandler {
    private CommissionHandler() {}

    private static final double PROSPERITY_RADIUS = 32.0;

    public static void initialize() {
        if (!GreenwardConfig.ENABLE_VILLAGERS_SEALS) {
            return;
        }
        UseEntityCallback.EVENT.register(CommissionHandler::onUseEntity);
    }

    private static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity target, EntityHitResult hit) {
        if (level.isClientSide() || !player.isShiftKeyDown() || !(player instanceof ServerPlayer serverPlayer)
                || !(target instanceof Villager villager) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        // Wearing a Coin Purse in the offhand switches this same sneak-right-click gesture
        // over to VillagerShopHandler instead — see that class for why the Purse is the mode
        // switch rather than a second gesture.
        if (GreenwardConfig.ENABLE_ECONOMY && player.getOffhandItem().is(ModItems.COIN_PURSE)) {
            return InteractionResult.PASS;
        }

        GreenwardSkill pillar = pillarFor(villager);
        if (pillar == null) {
            return InteractionResult.PASS;
        }

        VillagerCommissionData data = VillagerCommissionData.get(serverLevel.getServer());
        int today = (int) (serverLevel.getOverworldClockTime() / 24000L);
        VillagerCommissionData.Commission commission = data.get(villager.getUUID());

        if (commission == null || commission.dayAssigned() != today) {
            commission = generateCommission(serverLevel, villager, pillar, today);
            data.set(villager.getUUID(), commission);
            announce(serverPlayer, villager, commission);
            return InteractionResult.SUCCESS;
        }

        if (hasEnough(serverPlayer, commission.item(), commission.count())) {
            removeFromInventory(serverPlayer, commission.item(), commission.count());
            giveSeals(serverPlayer, commission.sealReward());
            serverPlayer.sendSystemMessage(Component.literal(
                    "Commission delivered! +" + commission.sealReward() + " Seals."));
            data.clear(villager.getUUID());
        } else {
            announce(serverPlayer, villager, commission);
        }
        return InteractionResult.SUCCESS;
    }

    private static void announce(ServerPlayer player, Villager villager, VillagerCommissionData.Commission commission) {
        player.sendSystemMessage(Component.literal(String.format(java.util.Locale.ROOT,
                "%s's Commission: bring %d× %s for %d Seals (Prosperity %d).",
                villager.getVillagerData().profession().getRegisteredName(), commission.count(),
                new ItemStack(commission.item()).getHoverName().getString(), commission.sealReward(),
                prosperity(villager))), true);
    }

    private static VillagerCommissionData.Commission generateCommission(ServerLevel level, Villager villager, GreenwardSkill pillar, int today) {
        RandomSource random = level.getRandom();
        GreenwardCollection collection = pickCollection(pillar, random);
        Item item = CompressionLadder.rung1For(collection);
        if (item == null) {
            item = collection.item();
        }
        int prosperity = prosperity(villager);
        int count = 8 + prosperity / 5 + random.nextInt(5);
        int reward = Math.max(2, count / 3);
        return new VillagerCommissionData.Commission(item, count, reward, today);
    }

    private static GreenwardCollection pickCollection(GreenwardSkill pillar, RandomSource random) {
        List<GreenwardCollection> matching = new ArrayList<>();
        for (GreenwardCollection collection : GreenwardCollection.values()) {
            if (collection.skill() == pillar) {
                matching.add(collection);
            }
        }
        return matching.get(random.nextInt(matching.size()));
    }

    /** A simplified stand-in for a real village-detection system (bell/bed/workstation
     *  census) — nearby villager count within {@link #PROSPERITY_RADIUS}, scaled 0-100. */
    private static int prosperity(Villager villager) {
        AABB area = villager.getBoundingBox().inflate(PROSPERITY_RADIUS);
        int nearby = villager.level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Villager.class), area, v -> true).size();
        return Math.min(100, nearby * 10);
    }

    private static GreenwardSkill pillarFor(Villager villager) {
        var profession = villager.getVillagerData().profession();
        if (profession.is(VillagerProfession.FARMER)) {
            return GreenwardSkill.FARMING;
        }
        if (profession.is(VillagerProfession.MASON) || profession.is(VillagerProfession.TOOLSMITH)) {
            return GreenwardSkill.MINING;
        }
        if (profession.is(VillagerProfession.FISHERMAN)) {
            return GreenwardSkill.FISHING;
        }
        if (profession.is(VillagerProfession.WEAPONSMITH) || profession.is(VillagerProfession.BUTCHER)) {
            return GreenwardSkill.COMBAT;
        }
        return null;
    }

    private static boolean hasEnough(Player player, Item item, int count) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total >= count;
    }

    private static void removeFromInventory(Player player, Item item, int count) {
        int remaining = count;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (remaining <= 0) {
                break;
            }
            if (stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
    }

    private static void giveSeals(ServerPlayer player, int count) {
        ItemStack seals = new ItemStack(ModItems.SEAL, count);
        if (!player.getInventory().add(seals)) {
            player.drop(seals, false);
        }
    }
}
