package me.tr.survival.main.database;

import me.tr.survival.main.Autio;
import me.tr.survival.main.other.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {

    public static HashMap<UUID, HashMap<String, Object>> player_data = new HashMap<>();

    public static void loadNull(UUID uuid, boolean save) {
        HashMap<String, Object> empty = new HashMap<>();

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        // Basic User Data
        empty.put("player_name", player.getName());
        empty.put("money", 0);
        empty.put("rank", "default");
        empty.put("joined", Util.getToday());
        empty.put("save", save);
        empty.put("crystals", 0);

        // User's Home Data

        empty.put("first_home", "null");
        empty.put("second_home", "null");
        empty.put("third_home", "null");

        // User's Mining Data

        empty.put("diamond", 0);
        empty.put("gold", 0);
        empty.put("iron", 0);
        empty.put("coal", 0);
        empty.put("total", 0);

        // User's Level Data

        empty.put("level", 1);
        empty.put("xp", 0);
        empty.put("total_xp", 0);

        empty.put("scoreboard", false);
        empty.put("privacy", false);
        empty.put("chat", true);
        empty.put("treefall", false);

        empty.put("last_mail", System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        empty.put("streak", 0);
        empty.put("tickets", 0);

        empty.put("backpack_level", "ONE");

        player_data.put(uuid, empty);

    }

    public static boolean loadPlayer(UUID uuid){

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        HashMap<String, Object> data = new HashMap<>();

        try {
            ResultSet result = SQL.query("SELECT * FROM `players` WHERE `uuid` = '" + uuid.toString() + "';");
            if(result.next()) {

                // User's Basic Data

                data.put("player_name", result.getString("player_name"));
                data.put("money", result.getInt("money"));
                data.put("rank", result.getString("rank"));
                data.put("joined", result.getString("joined"));
                data.put("crystals", result.getInt("crystals"));
                data.put("save", true);

                // User's Home Data
                ResultSet homeResult = SQL.query("SELECT * FROM `homes` WHERE `uuid` = '" + uuid.toString() + "';");
                if(homeResult.next()) {
                    data.put("first_home", homeResult.getString("first_home"));
                    data.put("second_home", homeResult.getString("second_home"));
                    data.put("third_home", homeResult.getString("third_home"));
                } else {
                    data.put("first_home", "null");
                    data.put("second_home", "null");
                    data.put("third_home", "null");
                }

                // User's Mining Data
                ResultSet mineResult = SQL.query("SELECT * FROM `mined_ores` WHERE `uuid` = '" + uuid.toString() + "';");
                if(mineResult.next()) {

                    data.put("diamond", mineResult.getInt("diamond"));
                    data.put("gold", mineResult.getInt("gold"));
                    data.put("iron", mineResult.getInt("iron"));
                    data.put("coal", mineResult.getInt("coal"));
                    data.put("total", mineResult.getInt("total"));

                } else {
                    data.put("diamond", 0);
                    data.put("gold", 0);
                    data.put("iron", 0);
                    data.put("coal", 0);
                    data.put("total", 0);
                }

                /*
                // User's Level Data
                ResultSet levelResult = SQL.query("SELECT * FROM `levels` WHERE `uuid` = '" + uuid.toString() + "';");
                if(levelResult.next()) {
                    data.put("level", levelResult.getInt("level"));
                    data.put("xp", levelResult.getInt("xp"));
                    data.put("total_xp", levelResult.getInt("total_xp"));
                } else {
                    data.put("level", 0);
                    data.put("xp", 0);
                    data.put("total_xp", 0);
                } */

                ResultSet settingsResult = SQL.query("SELECT * FROM `settings` WHERE `uuid` = '" + uuid.toString() + "';");
                if(settingsResult.next()) {

                    data.put("scoreboard", settingsResult.getBoolean("scoreboard"));
                    data.put("privacy", settingsResult.getBoolean("privacy"));
                    data.put("chat", settingsResult.getBoolean("chat"));
                    data.put("treefall", settingsResult.getBoolean("treefall"));

                } else {
                    data.put("scoreboard", false);
                    data.put("privacy", false);
                    data.put("chat", true);
                    data.put("treefall", true);
                }

                ResultSet mailResult = SQL.query("SELECT * FROM `mail` WHERE `uuid` = '" + uuid + "';");
                if(mailResult.next()) {

                    data.put("last_mail", mailResult.getLong("last_mail"));
                    data.put("streak", mailResult.getInt("streak"));
                    data.put("tickets", mailResult.getInt("tickets"));

                } else {
                    data.put("last_mail", System.currentTimeMillis() - 1000 * 60 * 60 * 24);
                    data.put("streak", 0);
                    data.put("tickets", 0);
                }

                ResultSet backpackResult = SQL.query("SELECT * from `backpacks` WHERE `uuid` = '" + uuid + "';");
                if(backpackResult.next()) {

                    data.put("backpack_level", backpackResult.getString("level"));
                    data.put("backpack_inventory", backpackResult.getString("saved_inventory"));

                } else {

                    data.put("backpack_level", "ONE");
                    data.put("backpack_inventory", "null");

                }


                player_data.put(uuid, data);

                Autio.log("Loaded player " + uuid + " (" + player.getName() + ") from Database");
                return true;

            } else {
                loadNull(uuid, true);
            }


        } catch(SQLException ex) {
            ex.printStackTrace();
            loadNull(uuid, false);
        }

        return false;

    }

    public static HashMap<String, Object> getData(UUID uuid) {
        if(!isLoaded(uuid)) {
            loadNull(uuid, false);
        }
        return player_data.get(uuid);
    }

    public static void savePlayer(UUID uuid) {

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if(!isLoaded(uuid)) {
            loadNull(uuid, true);
        }

        HashMap<String, Object> data = player_data.get(uuid);
        if(!(boolean) data.get("save")) {
            return;
        }

        String[] updateQueries = new String[] {
                "UPDATE `players` SET `player_name` = '" + player.getName()
                        + "', `money` = " + data.get("money") + ", `rank` = '" + data.get("rank") + "', `joined` = '" + data.get("joined") + "', `crystals` = " + data.get("crystals") +
                        " WHERE `uuid` = '" + uuid + "';",

                "UPDATE `homes` SET `first_home` = '" + data.get("first_home") + "', `second_home` = '" + data.get("second_home") +
                        "', `third_home` = '" + data.get("third_home") + "' WHERE `uuid` = '" + uuid + "';",

                "UPDATE `mined_ores` SET `diamond` = " + data.get("diamond") + ", `gold` = " + data.get("gold") + ", `iron` = " + data.get("iron") +
                        ", `coal` = " + data.get("coal") + ", `total` = " + data.get("total") + " WHERE `uuid` = '" + uuid + "';",

                //"UPDATE `levels` SET `level` = " + data.get("level") + ", `xp` = " + data.get("xp") + ", `total_xp` = " + data.get("xp") + " WHERE `uuid` = '" + uuid + "';",

                "UPDATE `settings` SET `scoreboard` = '" + data.get("scoreboard") + "', `privacy` = '" + data.get("privacy") + "', `chat` = '" + data.get("chat") +
                        "', `treefall` = '" + data.get("treefall") + "' WHERE `uuid` = '" + uuid + "';",

                "UPDATE `mail` SET `last_mail` = " + data.get("last_mail") + ", `streak` = " + data.get("streak") + ", `tickets` = " + data.get("tickets") +
                        " WHERE `uuid` = '" + uuid + "';",

                "UPDATE `backpacks` SET `level` = '" + data.get("backpack_level") + "', `saved_inventory` = '" + data.get("backpack_inventory") + "', WHERE `uuid` = '" + uuid + "';"
        };

        String[] saveQueries = new String[] {
                "INSERT INTO `players` VALUES('" + uuid + "', '" + player.getName() + "', " + data.get("money") + ", '" + data.get("rank") +  "', '" + data.get("joined")
                        + "', " + data.get("crystals") + ");",

                "INSERT INTO `homes` VALUES('" + uuid +"', '" + data.get("first_home") + "', '" + data.get("second_home") + "', '" + data.get("third_home") + "');",

                "INSERT INTO `mined_ores` VALUES('" + uuid + "', " + data.get("diamond") + ", " + data.get("gold") + ", " + data.get("iron") + ", " +
                        "" + data.get("coal") + ", " + data.get("total") + ");",

               // "INSERT INTO `levels` VALUES('" + uuid + "', " + data.get("level") + ", " + data.get("xp") + ", " + data.get("total_xp") + ");",

                "INSERT INTO `settings` VALUES('" + uuid + "', '" + data.get("scoreboard") + "', '" + data.get("privacy") + "', '" + data.get("chat") + "', 'false');",

                "INSERT INTO `mail` VALUES('" + uuid  +"', " + data.get("last_mail") + ", " + data.get("streak") + ", " + data.get("tickets") + ");",

                "INSERT INTO `backpacks` VALUES('" + uuid + "', '" + data.get("backpack_level") + "', '" + data.get("backpack_inventory") + "');"
        };

        try {

            int successful = 0;

            for(int i = 0; i < updateQueries.length; i++) {

                String update = updateQueries[i];
                Autio.log("Executing Database update query: " + update);
                if(!SQL.update(update)) {
                    Autio.log("Could not execute update query " + update + " trying to execute the equivalent save query: " + saveQueries[i]);
                    if(SQL.update(saveQueries[i])) {
                        successful += 1;
                    } else {
                        System.err.println("Could not save or update the player " + uuid + " (" + player.getName() + ").. Maybe you should check it out?");
                    }

                } else {
                    successful += 1;
                }

            }

            if(successful >= 1) {
                Autio.log("Updated or Saved " + successful + "/" + updateQueries.length + " tables for " + uuid +  " (" + player.getName() + ")!");
            }

        } catch(SQLException ex) {
            ex.printStackTrace();
        }

    }

    public static boolean isLoaded(UUID uuid) {
        return player_data.containsKey(uuid);
    }

    public static Object getValue(UUID uuid, String key) {

        if(!isLoaded(uuid)) {
            loadNull(uuid, false);
        }

        HashMap<String, Object> data = player_data.get(uuid);
        if(!data.containsKey(key)) {
            return null;
        }

        return data.get(key);

    }

    public static void set(UUID uuid, String key, Object value) {
        if(!isLoaded(uuid)) {
            loadNull(uuid, false);
        }

        HashMap<String, Object> data = player_data.get(uuid);
        if(!data.containsKey(key)) {
            return;
        }

        data.put(key, value);
        player_data.put(uuid, data);

    }

    public static void add(UUID uuid, String key, int value) {
        if(!isLoaded(uuid)) {
            loadNull(uuid, false);
        }

        HashMap<String, Object> data = player_data.get(uuid);
        if(!data.containsKey(key)) {
            return;
        }

        try {
            int obj = (int) data.get(key);
            int newVal = obj += value;
            data.put(key, newVal);
            player_data.put(uuid, data);
        } catch(NumberFormatException ex) {
            ex.printStackTrace();
        }

    }

}
