package me.tr.survival.main.util.data;

import me.tr.survival.main.Autio;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.util.callback.TypedCallback;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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
