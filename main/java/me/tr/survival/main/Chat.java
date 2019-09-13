package me.tr.survival.main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Chat {

    public static String getPrefix() {

        FileConfiguration config = Main.getInstance().getConfig();
        return ChatColor.translateAlternateColorCodes('&', config.getString("other.chat-format-prefix")).trim();

    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(Chat.getPrefix() + " §7" + ChatColor.translateAlternateColorCodes('&', message));
    }

}
