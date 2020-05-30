package me.tr.survival.main.util.data;

import me.tr.survival.main.Autio;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.util.callback.TypedCallback;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Balance {

    private static Map<UUID, Double> topBalance = new HashMap<>();

    public static double get(UUID player) {
        return (double) PlayerData.getValue(player, "money");
    }

    public static void add(UUID player, double value) {
        double current = get(player);
        PlayerData.set(player, "money", current + value);
    }

    public static void remove(UUID player, double value) {
        double current = get(player);
        if(current - value < 0) {
            PlayerData.set(player, "money", 0d);
        } else {
            PlayerData.set(player, "money", current - value);
        }
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

    public static void fetchTopBalance() {

        Autio.async(() -> {

            SQL.query("SELECT * FROM `players`;", (result, conn) -> {
                try {
                    Map<UUID, Double> temp = new HashMap<>();
                    while(result.next()) {
                        // Load the OfflinePlayer when server starts, so it doens't take that long later
                        OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("uuid")));
                        temp.put(op.getUniqueId(), result.getDouble("money"));
                    }
                    topBalance = temp;
                } catch(SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    if(conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });


        });
    }

}
