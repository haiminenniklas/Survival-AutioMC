package me.tr.survival.main.util.data;

import me.tr.survival.main.database.PlayerData;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Balance {

    public static int get(UUID player) {
        return (int) PlayerData.getValue(player, "money");
    }

    public static void add(UUID player, int value) {
        PlayerData.add(player, "money",  value);
    }

    public static void remove(UUID player, int value) {
        if(!canRemove(player, value)) {
            PlayerData.set(player, "money", 0);
        } else {
            PlayerData.add(player, "money",  -value);
        }
    }



    public static boolean canRemove(UUID player, int value) {
        int current_balance = Balance.get(player);

        return canRemove(current_balance, value);

    }

    public static boolean canRemove(int value1, int value2) {
        return value1 - value2 >= 0;
    }

}
