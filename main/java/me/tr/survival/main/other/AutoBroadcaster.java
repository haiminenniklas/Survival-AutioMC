package me.tr.survival.main.other;

import me.tr.survival.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class AutoBroadcaster {

    public static BukkitTask start() {
        BukkitTask task = Main.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
            Bukkit.broadcastMessage("§7§m-----------------------------");

            int random = new Random().nextInt(getMessages().size());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', getMessages().get(random)));

            Bukkit.broadcastMessage("§7§m-----------------------------");
        }, 20, 20 * 60 * Main.getInstance().getConfig().getInt("auto-broadcaster.interval-in-minutes"));

        return task;
    }

    public static List<String> getMessages() {

        return Main.getInstance().getConfig().getStringList("auto-broadcaster.messages");

    }


}