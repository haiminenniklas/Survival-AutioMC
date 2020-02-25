package me.tr.survival.main;

import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Chat implements Listener {

    private static HashMap<String, Object> settings = new HashMap<>();
    private static HashMap<UUID, Long> sentMessages = new HashMap<>();

    public static void init() {
        Chat.settings.put("mute", false);
        Chat.settings.put("slow", false);
    }

    public static String getPrefix() {
        return Prefix.DEFAULT.text;
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(Chat.getPrefix() + " §7" + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendMessage(Player player, Chat.Prefix prefix, String message) {

        player.sendMessage(prefix.text + " §7" + ChatColor.translateAlternateColorCodes('&', message));

    }

    @Deprecated
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

    public static void panel(Player opener) {

        Gui gui = new Gui("Chat-asetukset", 27);

        String isSilenced = ((boolean) Chat.settings.get("mute")) ? "§c§lHILJENNETTY" : "§a§lAVOIN";

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAPER, 1, "§b§lTyhjennä Chat", Arrays.asList(
                "§7§m--------------------",
                " §7Klikkaa hiljentääksesi chatin!",
                " ",
                " §7Tila: " + isSilenced,
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);

                Chat.settings.put("mute",  !(boolean)Chat.settings.get("mute"));
                String isSilenced = ((boolean) Chat.settings.get("mute")) ? "§c§lHILJENNETTY" : "§a§lAVOIN";

                Chat.sendMessage(clicker, "Chatin tila: " + isSilenced);

            }
        });

        String isSlowed = ((boolean)Chat.settings.get("slow")) ? "§c§lHIDASTETTU" : "§a§lTAVALLINEN";

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.OAK_SIGN, 1, "§b§lHidasta Chattia", Arrays.asList(
                "§7§m--------------------",
                " §7Hidastaaksesi chattia.",
                "",
                " §7Nopeus: " + isSlowed,
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.settings.put("slow", !(boolean)Chat.settings.get("slow"));
                String isSlowed = ((boolean)Chat.settings.get("slow")) ? "§c§lHIDASTETTU" : "§a§lTAVALLINEN";
                Chat.sendMessage(clicker, "Chatin nopeus: " + isSilenced);
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.BARRIER, 1, "§b§lHiljennä Chat", Arrays.asList(
                "§7§m--------------------",
                " §7Klikkaa tyhjentääksesi chatin!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.clear();
                Chat.sendMessage(clicker, "Chat tyhjennetty!");
            }
        });

        gui.open(opener);

    }

    public static void clear() {

        for(Player online : Bukkit.getOnlinePlayers()) {
            for(int i = 0; i < 200; i++) {
                online.sendMessage(" ");
            }
        }

        Bukkit.broadcastMessage("§6§lChat tyhjennetty!");
        Util.broadcastSound(Sound.BLOCK_ANVIL_BREAK);

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if(!Settings.get(player.getUniqueId(), "chat")) {
            Chat.sendMessage(player, "Sinulla on chat poissa päältä!");
            return;
        }

        for(Player r : e.getRecipients()) {
            if(!Settings.get(r.getUniqueId(), "chat") && !r.getName().equalsIgnoreCase(player.getName())) {
                e.getRecipients().remove(r);
            }
        }

        if((boolean) Chat.settings.get("mute")) {
            e.setCancelled(true);
            Chat.sendMessage(player, Prefix.ERROR, "Chat on hiljennetty!");
            return;
        }

        if(sentMessages.containsKey(uuid)) {
            long lastSent = sentMessages.get(uuid);
            if((System.currentTimeMillis() - lastSent) / 1000 <= 3 && (boolean) Chat.settings.get("slow")) {
                e.setCancelled(true);
                Chat.sendMessage(player, "Chat on hidastetussa tilassa! Voit lähettää viestin §63s §7välein!");
                return;
            }
        }

        sentMessages.put(uuid, System.currentTimeMillis());

        //e.setFormat(Chat.getFormat(player, e.getMessage()));
        e.setFormat((ChatColor.translateAlternateColorCodes('&', Autio.getPrefix(player) + " " + player.getName()).trim() + "§r: %2$s"));

        /*if(e.getMessage().startsWith("#") && Ranks.isStaff(uuid)) {
            e.setCancelled(true);
            for(Player online : Bukkit.getOnlinePlayers()) {
                if(Ranks.isStaff(online.getUniqueId())) {
                    online.sendMessage("§7§l(§6§lYLLÄPITO§7§l) §6" + player.getName() + " §7» §f" + e.getMessage().substring(1));
                }
            }
        } */

    }


}
