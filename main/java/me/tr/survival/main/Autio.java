package me.tr.survival.main;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.booster.Boosters;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class Autio {

    public static void teleportToSpawn(Player player) {
        player.teleport(Autio.getSpawn());
    }

    public static void log(String msg) {
        if(!getConfig().getBoolean("other.logging")) return;
        Bukkit.getLogger().log(Level.INFO, msg);
    }

    public static void err(String msg) {
        Bukkit.getLogger().log(Level.WARNING, msg);
    }

    public static void logColored(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static void warn(String message) {
        Bukkit.getLogger().log(Level.WARNING, message);
    }

    public static void task(Runnable task) {
        Bukkit.getScheduler().runTask(Main.getInstance(), task);
    }

    public static void async(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), task);
    }

    public static void teleportToNether(Player player) {
        player.teleport(new Location(Bukkit.getWorld("world_nether"), 0.5, 52, 0.5));
    }

    public static void every(int seconds, Runnable task, boolean async) {
        if(async) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
        } else {
            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
        }
    }

    public static void after(int seconds, Runnable task) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), task, 20 * seconds);
    }

    public static void after(int seconds, Runnable task, boolean async) {
        if(async) {
            Autio.after(seconds, task);
        } else {
            Autio.afterAsync(seconds, task);
        }
    }

    public static void afterAsync(int seconds, Runnable task) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), task, (long) getCurrentTPS() * seconds);
    }

    public static void every(int seconds, Runnable task) {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
    }

    public static void everyAsync(int seconds, Runnable task) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
    }

    public static double getCurrentTPS() {
        return Bukkit.getTPS()[0];
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

    public static void savePlayer(Player player) {
        Autio.async(() -> {
            PlayerData.savePlayer(player.getUniqueId());
        });
    }

    public static void loadPlayer(Player player) {
        Autio.async(() -> {
           PlayerData.savePlayer(player.getUniqueId());
        });
    }

    public static void updatePlayer(Player player) {

        Autio.updateTag(player);
        Settings.scoreboard(player);
        loadPlayer(player);

    }

    public static Server getServer() {
        return Bukkit.getServer();
    }

    public static Main getPlugin() {
        return Main.getInstance();
    }

    public static void runDebug(Player player) {
        Chat.sendMessage(player, Chat.Prefix.DEBUG, "Korjataan yleiset virheet ja bugit...");
        Autio.updatePlayer(player);
        Boosters.debug();
        Chat.sendMessage(player, Chat.Prefix.DEBUG, "Korjattu! Jos mikään ei muuttunut yritä poistua ja liittyä palvelimelle uudestaan!");
    }

    public static World getEndWorld() {
        return Bukkit.getWorld("world_the_end");
    }

    public static World getNetherWorld() {
        return Bukkit.getWorld("world_nether");
    }

    public static ProtocolManager getProtocolManager(){
        return ProtocolLibrary.getProtocolManager();
    }

    public static void sendTablist(Player player) {
        PacketContainer pc = getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

        pc.getChatComponents().write(0, WrappedChatComponent.fromText("§6§lNUOTIO.XYZ"))
                .write(1, WrappedChatComponent.fromText("§6Survival"));
        try
        {
            getProtocolManager().sendServerPacket(player, pc);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
