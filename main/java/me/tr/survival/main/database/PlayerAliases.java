package me.tr.survival.main.database;

import org.bukkit.OfflinePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerAliases {

    public static HashMap<String, String[]> aliases = new HashMap<>();

    public static String[] load(OfflinePlayer player) {

        try {
            ResultSet result = SQL.query("SELECT * FROM `player_aliases` WHERE `player_name` = '" + player.getName() + "';");

            if(result.next()) {
                String addressesRaw = result.getString("addresses");
                String[] addresses = addressesRaw.split(";");
                aliases.put(player.getName(), addresses);
                return addresses;
            }

        } catch(SQLException ex) {
            ex.printStackTrace();
            return null;
        }

        return null;

    }

    public static void add(OfflinePlayer player, String address) {

        if(!aliases.containsKey(player.getName())) {
            aliases.put(player.getName(), new String[] {address});
        } else {

            String[] current = aliases.get(player.getName());

            if(aliases.containsValue(address)) return;

            ArrayList<String> arrayList = new ArrayList<>();
            for(String s : current) {
                arrayList.add(s);
            }
            arrayList.add(address);
            aliases.put(player.getName(), arrayList.toArray(new String[arrayList.size()]));

        }

    }

    public static void save(OfflinePlayer player) {

        if(!aliases.containsKey(player.getName())) return;

        String[] addresses = aliases.get(player.getName());
        String addressesRaw = String.join(";", addresses);

        try {

            System.out.println("Updating player aliases for " + player.getName() + "...");
            if(!SQL.update("UPDATE `player_aliases` SET `addresses` = '" + addressesRaw + "' WHERE `player_name` = '" + player.getName() + "';")) {

                System.out.println("Could not update player aliases for " + player.getName() + ", trying to save...");
                if(SQL.update("INSERT INTO `player_aliases` VALUES('" + player.getName() + "', '" + addressesRaw + "');")) {
                    System.out.println("Saved player aliases for " + player.getName() + "!");
                } else {
                    System.out.println("Did not save player aliases for " + player.getName() + "...");
                }

            }

        } catch(SQLException ex) {
            ex.printStackTrace();
        }

    }

    public static String[] addresses(OfflinePlayer player) {
        if(!aliases.containsKey(player.getName())) {
            return null;
        }
        return aliases.get(player.getName());
    }

}
