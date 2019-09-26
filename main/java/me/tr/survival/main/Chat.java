package me.tr.survival.main;

import me.tr.survival.main.other.Ranks;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Chat {

    public static String getPrefix() {
        return Prefix.DEFAULT.text;
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(Chat.getPrefix() + " §7" + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendMessage(Player player, Chat.Prefix prefix, String message) {

        player.sendMessage(prefix.text + " §7" + ChatColor.translateAlternateColorCodes('&', message));

    }

    public static String getFormat(Player player, String message) {

        String format = ChatColor.translateAlternateColorCodes('&', Autio.getConfig().getString("chat.format"));
        format = ChatColor.translateAlternateColorCodes('&',
                format.replaceAll("%rank%", "&" + Ranks.getRankColor(Ranks.getRank(player.getUniqueId())).getChar()));
        format = format.replaceAll("%name%", player.getName());
        format = format.replaceAll("%message%", message);

        return format;

    }

    public enum Prefix {

        DEFAULT(ChatColor.translateAlternateColorCodes('&', Autio.getConfig().getString("chat.prefixes.default")).trim()),
        ERROR(ChatColor.translateAlternateColorCodes('&', Autio.getConfig().getString("chat.prefixes.error")).trim()),
        AFK(ChatColor.translateAlternateColorCodes('&', Autio.getConfig().getString("chat.prefixes.afk")).trim()),
        DEBUG(ChatColor.translateAlternateColorCodes('&', Autio.getConfig().getString("chat.prefixes.debug")).trim());


        public String text;

        Prefix(String text) {
            this.text = text;
        }
    }

}
