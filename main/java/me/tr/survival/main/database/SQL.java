package me.tr.survival.main.database;

import com.zaxxer.hikari.HikariDataSource;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.Main;
import me.tr.survival.main.util.Result;
import me.tr.survival.main.util.callback.QueryPromise;
import me.tr.survival.main.util.callback.TypedCallback;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQL {

    public static HikariDataSource source = null;

    public static void setup() {

        FileConfiguration config = Main.getInstance().getConfig();
        if(!config.getBoolean("mysql.enabled")) {
            File dataFolder = new File(Main.getInstance().getDataFolder().getAbsolutePath() + File.separator + "database.db");
            if (!dataFolder.exists()) {
                try {
                    if(!dataFolder.createNewFile()){
                        System.out.println("Could not create Database file (#createNewFile())");
                    }
                } catch (IOException e) {
                    System.out.println("Could not create Database file");
                    e.printStackTrace();
                }

            }

            try {
                Class.forName("org.sqlite.JDBC");
                System.out.println("Opened database successfully");

                queries();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            setupDataSource();

            try {
                queries();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void setupDataSource() {
        source = new HikariDataSource();
        String database = "autiomc";
        if(Main.getInstance().getConfig().getString("mysql.database") != null)
            database = Main.getInstance().getConfig().getString("mysql.database");
        source.setJdbcUrl("jdbc:mysql://172.18.0.1:3306/" + database);
        source.setMaximumPoolSize(10);
        source.addDataSourceProperty("user", "survival");
        source.addDataSourceProperty("password", "uu4L3Ks3EhBMfP8u");
    }

    private static void queries(){

        String[] queries = new String[] {

                "CREATE TABLE IF NOT EXISTS `players` (`uuid` VARCHAR(120), `player_name` TEXT, `money` int(11), `rank` TEXT, `joined` TEXT, `crystals` int(11) , PRIMARY KEY(`uuid`));",
                "CREATE TABLE IF NOT EXISTS `homes` (`uuid` VARCHAR(120), first_home TEXT, second_home TEXT, third_home TEXT, `fourth_home` TEXT, `fifth_home` TEXT, `sixth_home` TEXT, PRIMARY KEY(`uuid`));",
                "CREATE TABLE IF NOT EXISTS `mined_ores` (`uuid` VARCHAR(120), diamond int(11), gold int(11), iron int(11), coal int(11), total int(11), PRIMARY KEY (`uuid`));",
               // "CREATE TABLE IF NOT EXISTS `levels` (`uuid` VARCHAR(120), level int(11), xp int(11), total_xp int(11), PRIMARY KEY (`uuid`));",
               // "CREATE TABLE IF NOT EXISTS `player_aliases` (`player_name` VARCHAR(32), `addresses` LONGTEXT, PRIMARY KEY(`uuid`));",
                "CREATE TABLE IF NOT EXISTS `settings` (`uuid` VARCHAR(120), scoreboard TEXT, privacy TEXT, chat TEXT, `treefall` TEXT, `chat_mentions` TEXT, PRIMARY KEY(`uuid`));",
                "CREATE TABLE IF NOT EXISTS `mail` (`uuid` VARCHAR(120), `last_mail` BIGINT(11), `streak` int(11), `tickets` int(11), PRIMARY KEY(`uuid`));",
                "CREATE TABLE IF NOT EXISTS `warps` (`name` VARCHAR(32), `display_name` TEXT, `loc_x` int(11), `loc_y` int(11), `loc_z` int(11), `loc_pitch` float, `loc_yaw` float, `world` TEXT, `description` LONGTEXT, PRIMARY KEY(`name`));",
                "CREATE TABLE IF NOT EXISTS `backpacks` (`uuid` VARCHAR(120) PRIMARY KEY NOT NULL, `level` TEXT, `saved_inventory` TEXT);",
                "CREATE TABLE IF NOT EXISTS `particles` (`uuid` VARCHAR(120) PRIMARY KEY NOT NULL, `arrowtrail` TEXT, `particle` TEXT);",
                "CREATE TABLE IF NOT EXISTS `vip_settings` (`uuid` VARCHAR(120) PRIMARY KEY NOT NULL, `glow_effect` TEXT, `death_message` TEXT, `kill_message` TEXT);"

        };

        for(String query : queries) {
            try {
                if(!update(query)) Sorsa.logColored("§cCould not execute query (" + query + ")");

            } catch(SQLException ex){
                ex.printStackTrace();
                System.err.println("Could not setup the database");
            }
        }
    }

    private static Connection getConnection() throws SQLException {
        if(source.isClosed()) {
            setupDataSource();
        }
        return source.getConnection();
    }

    // Not working properly with connection pools. Use the async function instead
    // with callbacks
    @Deprecated
    public static Result query(String sql) throws SQLException {
        Connection conn = null;
        Result result = null;
        try {
            conn = getConnection();
            ResultSet r = conn.createStatement().executeQuery(sql);
            result = Result.fromResultSet(r);
        } catch(SQLException ex) {
            ex.printStackTrace();
        } finally {
            if(conn != null) {
                conn.close();
            }
        }
        return result;
    }

    public static boolean update(String sql) throws SQLException {

        Connection conn = null;
        boolean result = false;

        try {
            conn = getConnection();
            int r = conn.createStatement().executeUpdate(sql);
            result = r > 0;
        } catch(SQLException ex) {
            ex.printStackTrace();
        } finally {
            if(conn != null) {
                conn.close();
            }
        }
        return result;
    }

    public static void query(final String sql, QueryPromise<ResultSet, Connection> cb) {
        Sorsa.async(() -> {
            try {
                Connection c = getConnection();
                ResultSet result = c.createStatement().executeQuery(sql);
                cb.join(result,c);
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void update(String sql, TypedCallback<Boolean> cb) {
        Sorsa.async(() -> {
            try {
                Boolean result = SQL.update(sql);
                cb.execute(result);
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

}
