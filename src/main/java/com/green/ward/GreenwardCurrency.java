package com.green.ward;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/**
 * Thin helper over {@link PlayerPurseData} — every Coin gain/spend in the mod should go
 * through here rather than touching the data store directly, so the action-bar toast
 * ("+42 Coins") stays consistent everywhere Coins change hands, the same way vanilla XP
 * orbs always show the same pickup feedback regardless of source.
 */
public final class GreenwardCurrency {
    private GreenwardCurrency() {}

    public static long get(ServerPlayer player) {
        return PlayerPurseData.get(server(player)).get(player.getUUID());
    }

    public static void add(ServerPlayer player, long amount) {
        if (amount == 0) {
            return;
        }
        PlayerPurseData.get(server(player)).add(player.getUUID(), amount);
        toast(player, amount);
    }

    /** @return false (no balance change) if the player can't afford it. */
    public static boolean spend(ServerPlayer player, long amount) {
        if (amount <= 0) {
            return true;
        }
        PlayerPurseData data = PlayerPurseData.get(server(player));
        if (data.get(player.getUUID()) < amount) {
            return false;
        }
        data.add(player.getUUID(), -amount);
        toast(player, -amount);
        return true;
    }

    private static void toast(ServerPlayer player, long delta) {
        String sign = delta >= 0 ? "+" : "-";
        player.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
                "%s%,d Coins  (balance: %,d)", sign, Math.abs(delta), get(player))), true);
    }

    private static net.minecraft.server.MinecraftServer server(ServerPlayer player) {
        return ((ServerLevel) player.level()).getServer();
    }
}
