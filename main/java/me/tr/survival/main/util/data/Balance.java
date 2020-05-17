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

public class Balance {

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

        final Map<UUID, Double> balances = new HashMap<>();

        Autio.async(() -> {

            try {
                ResultSet result = SQL.query("SELECT * FROM `players`;");

                while(result.next()) {

                    balances.put(UUID.fromString(result.getString("uuid")), result.getDouble("money"));

                }

                cb.execute(balances);

            } catch (SQLException ex) {
                ex.printStackTrace();
            }


        });

    }

}
