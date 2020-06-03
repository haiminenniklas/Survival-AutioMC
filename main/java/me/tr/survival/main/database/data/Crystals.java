package me.tr.survival.main.database.data;

import me.tr.survival.main.database.PlayerData;

import java.util.UUID;

public class Crystals {

    public static int get(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (int) PlayerData.getValue(uuid, "crystals");
    }

    public static void set(UUID uuid, int value) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.set(uuid, "crystals", value);
    }

    public static boolean canRemove(UUID player, int value) {
        int current_balance = Crystals.get(player);
        return current_balance - value >= 0;

    }

    public static void add(UUID uuid, int value) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.add(uuid, "crystals", value);
    }

}
