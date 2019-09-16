package me.tr.survival.main;

import me.tr.survival.main.other.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Autio {

    public static void teleportToSpawn(Player player) {
        player.teleport(Autio.getSpawn());
    }

    public static void setSpawn(Location loc) {

        FileConfiguration config = Main.getInstance().getConfig();
        config.set("spawn.x", loc.getX());
        config.set("spawn.y", loc.getY());
        config.set("spawn.z", loc.getZ());
        config.set("spawn.yaw", String.valueOf(loc.getYaw()));
        config.set("spawn.pitch", String.valueOf(loc.getPitch()));
        config.set("spawn.world", loc.getWorld().getName());

        Main.getInstance().saveConfig();

    }

    public static Location getSpawn() {

        FileConfiguration config = Main.getInstance().getConfig();

        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");

        float yaw = Float.parseFloat(config.getString("spawn.yaw"));
        float pitch = Float.parseFloat(config.getString("spawn.pitch"));

        World world = Bukkit.getWorld(config.getString("spawn.world"));

        return new Location(world, x, y, z, yaw, pitch);

    }

    public static void updateTag(Player player) {

        String color = "&" + Ranks.getRankColor(Ranks.getRank(player.getUniqueId())).getChar();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nte player " + player.getName() + " prefix " + color);

    }

    public static FileConfiguration getConfig() {
        return Main.getInstance().getConfig();
    }

    public static void updatePlayer(Player player) {

        Autio.updateTag(player);
        Settings.scoreboard(player);

    }

    public static Main getPlugin() {
        return Main.getInstance();
    }

}
