package me.tr.survival.main.managers.other;

import me.tr.survival.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class AutoBroadcaster {

    public static BukkitTask start() {
        BukkitTask task = Main.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> sendMessage(),
                20, 20 * Main.getInstance().getConfig().getInt("auto-broadcaster.interval-in-minutes"));

        return task;
    }

    public static void test() {
        sendMessage();
    }

    private static void sendMessage() {
        Bukkit.broadcastMessage("§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");

        int random = new Random().nextInt(getMessages().size());
        String messageRaw = getMessages().get(random);
        String[] splitted = messageRaw.split("<br>");
        for(int i = 0; i < splitted.length; i++) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', splitted[i]));
        }
        //Bukkit.broadcastMessage("§7§m-----------------------------");
        Bukkit.broadcastMessage("§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");
    }

    public static List<String> getMessages() {

        return Main.getInstance().getConfig().getStringList("auto-broadcaster.messages");

    }


}