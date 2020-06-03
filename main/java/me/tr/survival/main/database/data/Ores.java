package me.tr.survival.main.database.data;

import me.tr.survival.main.database.PlayerData;

import java.util.UUID;

public class Ores  {

    public static int getDiamonds(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (int) PlayerData.getValue(uuid, "diamond");
    }

    public static int getGold(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (int) PlayerData.getValue(uuid, "gold");
    }

    public static int getIron(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (int) PlayerData.getValue(uuid, "iron");
    }

    public static int getCoal(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (int) PlayerData.getValue(uuid, "coal");
    }

    public static int getTotal(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (int) PlayerData.getValue(uuid, "total");
    }

    public static int getOther(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        int total = getTotal(uuid);
        total -= getDiamonds(uuid);
        total -= getGold(uuid);
        total -= getIron(uuid);
        total -= getCoal(uuid);
        return total;
    }

}
