package me.tr.survival.main.database.data;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.callback.TypedCallback;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Balance {

    private static Map<UUID, Double> topBalance = new HashMap<>();

    public static double get(UUID player) {

        if(!PlayerData.isLoaded(player)) {
            Sorsa.async(() -> PlayerData.loadPlayer(player, (r) -> {}));
            return 0.0;
        }

        return Util.round((double) PlayerData.getValue(player, "money"),2);
    }

    public static void getAsync(UUID uuid, TypedCallback<Double> cb) {
        if(PlayerData.isLoaded(uuid)) {
            cb.execute( Util.round((double) PlayerData.getValue(uuid, "money"),2));
        } else {
            Sorsa.async(() -> {
                PlayerData.loadPlayer(uuid, (r) -> {
                    cb.execute( Util.round((double) PlayerData.getValue(uuid, "money"),2));
                });
            });
        }
    }

    public static void add(UUID player, double value) {
        PlayerData.add(player, "money", value);
    }

    public static void remove(UUID player, double value) {
        PlayerData.remove(player, "money", value);
    }

    public static void set(UUID player, double value) {
        value = Util.round(value, 2);
        PlayerData.set(player, "money", value);
        Sorsa.logColored("§a[Balance] The balance of " + player + " (" + Bukkit.getOfflinePlayer(player).getName() + ") was set to " + value + "!");
    }

    public static boolean canRemove(UUID player, double value) {
        double current_balance = Balance.get(player);
        return canRemove(current_balance, value);
    }

    public static boolean canRemove(double value1, double value2) {
        return value1 - value2 >= 0;
    }

    public static void getBalances(TypedCallback<Map<UUID, Double>> cb) {
        cb.execute(topBalance);
    }

    public void fetchTopBalance() {
        Sorsa.logColored("§a[BalanceTop] Fetching top balance from Database...");
        Sorsa.async(() -> {
            SQL.query("SELECT * FROM `players`;", (result, conn) -> {
                try {
                    final Map<UUID, Double> temp = new HashMap<>();
                    while(result.next()) {
                        // Load the OfflinePlayer when server starts, so it doens't take that long later
                        OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("uuid")));
                        temp.put(op.getUniqueId(), result.getDouble("money"));
                    }
                    Balance.topBalance = temp;
                } catch(SQLException ex) { ex.printStackTrace(); }
                finally {
                    if(conn != null) {
                        try { conn.close();
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                }
            });
        });
    }
}
