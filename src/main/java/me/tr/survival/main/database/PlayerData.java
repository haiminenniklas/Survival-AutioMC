package me.tr.survival.main.database;

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

        player_data.put(uuid, empty);

    }

    public static void loadPlayer(UUID uuid){

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
                data.put("save", true);

                // User's Home Data
                ResultSet homeResult = SQL.query("SELECT * FROM `homes` WHERE `uuid` = '" + uuid.toString() + "';");
                if(homeResult.next()) {
                    data.put("first_home", homeResult.getString("first_home"));
                    data.put("second_home", homeResult.getString("second_home"));
                    data.put("third_home", homeResult.getString("third_home"));

                    // User's Mining Data
                    ResultSet mineResult = SQL.query("SELECT * FROM `mined_ores` WHERE `uuid` = '" + uuid.toString() + "';");
                    if(mineResult.next()) {

                        data.put("diamond", mineResult.getInt("diamond"));
                        data.put("gold", mineResult.getInt("gold"));
                        data.put("iron", mineResult.getInt("iron"));
                        data.put("coal", mineResult.getInt("coal"));
                        data.put("total", mineResult.getInt("total"));

                        // User's Level Data
                        ResultSet levelResult = SQL.query("SELECT * FROM `levels` WHERE `uuid` = '" + uuid.toString() + "';");
                        if(levelResult.next())
                            data.put("level", levelResult.getInt("level"));
                            data.put("xp", levelResult.getInt("xp"));
                            data.put("total_xp", levelResult.getInt("total_xp"));
                    }

                }


                player_data.put(uuid, data);

                System.out.println("Loaded player " + uuid + " (" + player.getName() + ") from Database");

            } else {
                loadNull(uuid, true);
            }


        } catch(SQLException ex) {
            ex.printStackTrace();
            loadNull(uuid, false);
        }

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
                "UPDATE `players` SET `player_name` = '" + data.get("player_name")
                        + "', `money` = " + data.get("money") + ", `rank` = '" + data.get("rank") + "', `joined` = '" + data.get("joined") + "', `crystals` = " + data.get("crystals") +
                        " WHERE `uuid` = '" + uuid + "';",

                "UPDATE `homes` SET `first_home` = '" + data.get("first_home") + "', `second_home` = '" + data.get("second_home") +
                        "', `third_home` = '" + data.get("third_home") + "' WHERE `uuid` = '" + uuid + "';",

                "UPDATE `mined_ores` SET `diamond` = " + data.get("diamond") + ", `gold` = " + data.get("gold") + ", `iron` = " + data.get("iron") +
                        ", `coal` = " + data.get("coal") + ", `total` = " + data.get("total") + " WHERE `uuid` = '" + uuid + "';",

                "UPDATE `levels` SET `level` = " + data.get("level") + ", `xp` = " + data.get("xp") + ", `total_xp` = " + data.get("xp") +
                        " WHERE `uuid` = '" + uuid + "';"
        };

        try {
            int successful_updates = 0;
            for(String update : updateQueries){

                System.out.println("Executing Database update query: " + update);

                if(!SQL.update(update)) {
                    System.out.println("Could not update the player " + uuid + " (" + player.getName() + ") to the Database. Trying to save...");

                    String[] saveQueries = new String[] {
                            "INSERT INTO `players` VALUES('" + uuid + "', '" + data.get("player_name") + "', " + data.get("money") + ", '" + data.get("rank") +  "', '" + data.get("joined") + "');",

                            "INSERT INTO `homes` VALUES('" + uuid +"', '" + data.get("first_home") + "', '" + data.get("second_home") + "', '" + data.get("third_home") + "');",

                            "INSERT INTO `mined_ores` VALUES('" + uuid + "', " + data.get("diamond") + ", " + data.get("gold") + ", " + data.get("iron") + ", " +
                                    "" + data.get("coal") + ", " + data.get("total") + ");",

                            "INSERT INTO `levels` VALUES('" + uuid + "', " + data.get("level") + ", " + data.get("xp") + ", " + data.get("total_xp") + ");"
                    };

                    int successful_saves = 1;
                    for(String query : saveQueries) {

                        System.out.println("Executing Database save query: " + update);
                        if(!SQL.update(query)) {
                            System.err.println("Could not execute query for " + uuid + " (" + player.getName() + "): " + query);
                        } else {
                            successful_saves += 1;
                        }
                    }

                    System.out.println("Successfully saved " + successful_saves + "/" + saveQueries.length + " tables for " + uuid + " (" + player.getName() + ") in the Database");
                    break;
                } else {
                    successful_updates += 1;
                }
            }

            System.out.println("Successfully updated " + successful_updates + "/" + updateQueries.length + " tables for " + uuid + " (" + player.getName() + ") in the Database");

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
