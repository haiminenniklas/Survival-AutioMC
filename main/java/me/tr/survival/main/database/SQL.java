package me.tr.survival.main.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.tr.survival.main.Autio;
import me.tr.survival.main.Main;
import me.tr.survival.main.util.callback.QueryPromise;
import me.tr.survival.main.util.callback.TypedCallback;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQL {

    public static Connection conn = null;
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
                SQL.conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
                System.out.println("Opened database successfully");

                queries();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            String user = config.getString("mysql.user");
            String password = config.getString("mysql.password");
            String address = config.getString("mysql.address");

            // pswd: uu4L3Ks3EhBMfP8u

            HikariConfig hc = new HikariConfig();
            hc.setJdbcUrl("jdbc:mysql://" + address + ":3306/autiomc");
            hc.setUsername(user);
            hc.setPassword(password);

            HikariDataSource ds = new HikariDataSource(hc);

            source = ds;

            try {
                SQL.conn = ds.getConnection();
                queries();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void queries(){

        String[] queries = new String[] {

                "CREATE TABLE IF NOT EXISTS `players` (`uuid` VARCHAR(120), `player_name` TEXT, `money` int(11), `rank` TEXT, `joined` TEXT, `crystals` int(11) , PRIMARY KEY(`uuid`));",
                "CREATE TABLE IF NOT EXISTS `homes` (`uuid` VARCHAR(120), first_home TEXT, second_home TEXT, third_home TEXT, PRIMARY KEY(`uuid`));",
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
                if(!update(query)) {
                    System.out.println("Could not execute query (" + query + ")");
                }
            } catch(SQLException ex){
                ex.printStackTrace();
                System.out.println("Could not setup the database");
            }
        }

    }

    public static Connection getConnection() throws SQLException {
        return SQL.conn;
    }

    public static boolean hasConnection() throws SQLException {

        if(source == null) return false;

        return SQL.conn != null && !SQL.conn.isClosed();
    }

    public static ResultSet query(String sql) throws SQLException {
        Statement s = getConnection().createStatement();
        return s.executeQuery(sql);
    }

    public static boolean update(String sql) throws SQLException {

        if(!SQL.hasConnection()) {
            SQL.setup();
        }
        int result = getConnection().createStatement().executeUpdate(sql);
        return result > 0 ? true : false ;
    }

    public static void query(final String sql, QueryPromise<ResultSet, Connection> cb) {
        Autio.async(() -> {

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
        Autio.async(() -> {

            try {
                Boolean result = SQL.update(sql);
                cb.execute(result);
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

}
