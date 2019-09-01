package me.tr.survival.main.util.data;

import me.tr.survival.main.database.PlayerData;
import org.bukkit.entity.Player;

public class Balance {

    public static int get(Player player) {
        return (int) PlayerData.getValue(player.getUniqueId(), "money");
    }

    public static void add(Player player, int value) {
        PlayerData.add(player.getUniqueId(), "money",  value);
    }

    public static void remove(Player player, int value) {
        if(!canRemove(player, value)) {
            PlayerData.set(player.getUniqueId(), "money", 0);
        } else {
            PlayerData.add(player.getUniqueId(), "money",  -value);
        }
    }



    public static boolean canRemove(Player player, int value) {
        int current_balance = Balance.get(player);

        return current_balance - value >= 0;

    }

    public static boolean canRemove(int value1, int value2) {
        return value1 - value2 >= 0;
    }

}
