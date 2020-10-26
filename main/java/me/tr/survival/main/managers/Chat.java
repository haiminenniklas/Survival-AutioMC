package me.tr.survival.main.managers;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.DefaultFontInfo;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class Chat implements Listener {

    private static final HashMap<String, Object> settings = new HashMap<>();
    private static final HashMap<UUID, Long> sentMessages = new HashMap<>();
    private static final HashMap<UUID, String> lastMessage = new HashMap<>();

    public static void init() {
        Chat.settings.put("mute", false);
        Chat.settings.put("slow", false);
    }

    public static String getPrefix() {
        return Prefix.DEFAULT.text;
    }

    public static void sendMessage(Player player, String message) { player.sendMessage(Chat.getPrefix() + " §7" + ChatColor.translateAlternateColorCodes('&', message)); }

    public static void sendMessage(Player player, Chat.Prefix prefix, String message) { player.sendMessage(prefix.text + " §7" + ChatColor.translateAlternateColorCodes('&', message)); }

    public static void sendMessage(String message, Player... players) {
        sendMessage(message, Prefix.DEFAULT, players);
    }

    public static void sendMessage(String message, Chat.Prefix prefix,  Player... players) {
        for(Player player : players) { sendMessage(player, prefix, message); }
    }

    public static void sendCenteredMessage(Player player, String message) {

        final int CENTER_PX = 154;
        final int MAX_PX = 320;

        message = ChatColor.translateAlternateColorCodes('&', message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        int charIndex = 0;
        int lastSpaceIndex = 0;
        String toSendAfter = null;
        String recentColorCode = "";
        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            } else if (previousCode == true) {
                previousCode = false;
                recentColorCode = "§" + c;
                if (c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                } else isBold = false;
            } else if (c == ' ')
                lastSpaceIndex = charIndex;
            else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
            if (messagePxSize >= MAX_PX) {
                toSendAfter = recentColorCode + message.substring(lastSpaceIndex + 1, message.length());
                message = message.substring(0, lastSpaceIndex + 1);
                break;
            }
            charIndex++;
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb.toString() + message);
        if (toSendAfter != null) sendCenteredMessage(player, toSendAfter);
    }

    @Deprecated
    public static String getFormat(Player player, String message) {
        String format = ChatColor.translateAlternateColorCodes('&', Sorsa.getConfig().getString("chat.format"));
        format = ChatColor.translateAlternateColorCodes('&',
                format.replaceAll("%rank%", "&" + Ranks.getRankColor(Ranks.getRank(player.getUniqueId())).getChar()));
        format = format.replaceAll("%name%", player.getName());
        format = format.replaceAll("%message%", message);
        return format;

    }

    public enum Prefix {

        DEFAULT(ChatColor.translateAlternateColorCodes('&', Sorsa.getConfig().getString("chat.prefixes.default")).trim()),
        ERROR(ChatColor.translateAlternateColorCodes('&', Sorsa.getConfig().getString("chat.prefixes.error")).trim()),
        AFK(ChatColor.translateAlternateColorCodes('&', Sorsa.getConfig().getString("chat.prefixes.afk")).trim()),
        DEBUG(ChatColor.translateAlternateColorCodes('&', Sorsa.getConfig().getString("chat.prefixes.debug")).trim());

        public String text;

        Prefix(String text) {
            this.text = text;
        }
    }

    public static void panel(Player opener) {

        Gui gui = new Gui("Chat-asetukset", 27);

        if(!Ranks.isStaff(opener.getUniqueId())) return;

        String isSilenced = ((boolean) Chat.settings.get("mute")) ? "§cHiljennetty" : "§aAvoin";

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.BARRIER, 1, "§b§lHiljennä Chat", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa hiljentääksesi chatin!",
                " ",
                " §7Tila: " + isSilenced,
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.settings.put("mute",  !(boolean)Chat.settings.get("mute"));
                String isSilenced = ((boolean) Chat.settings.get("mute")) ? "§c§lHILJENNETTY" : "§a§lAVOIN";
                Chat.sendMessage(clicker, "Chatin tila: " + isSilenced);
                panel(opener);

            }
        });

        String isSlowed = ((boolean)Chat.settings.get("slow")) ? "§cHidastettu" : "§aTavallinen";

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.OAK_SIGN, 1, "§b§lHidasta Chattia", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Hidastaaksesi chattia.",
                "",
                " §7Nopeus: " + isSlowed,
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.settings.put("slow", !(boolean)Chat.settings.get("slow"));
                String isSlowed = ((boolean)Chat.settings.get("slow")) ? "§cHidastettu" : "§aTavallinen";
                Chat.sendMessage(clicker, "Chatin nopeus: " + isSlowed);
                panel(opener);
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.PAPER, 1, "§b§lTyhjennä Chat", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa tyhjentääksesi chatin!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.clear();
                Chat.sendMessage(clicker, "Chat tyhjennetty!");
                panel(opener);
            }
        });

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.ARROW, 1, "§cYlläpitopaneeli", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa avataksesi",
                " §cylläpitopaneelin§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Main.getStaffManager().panel(clicker);
            }
        });

        gui.open(opener);

    }

    public static void clear() {

        for(Player online : Bukkit.getOnlinePlayers()) {
            if(!Ranks.isStaff(online.getUniqueId())) for(int i = 0; i < 200; i++) { online.sendMessage(" "); }
        }

        Bukkit.broadcastMessage("§c§lChat tyhjennetty!");
        Util.broadcastSound(Sound.BLOCK_ANVIL_BREAK);

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if ((boolean) Chat.settings.get("mute")) {
            if (!Ranks.isStaff(uuid)) {
                e.setCancelled(true);
                Chat.sendMessage(player, Prefix.ERROR, "Chat on hiljennetty!");
                return;
            }
        }

        if (lastMessage.containsKey(uuid)) {

            String last = lastMessage.get(uuid);
            if (!last.equalsIgnoreCase(e.getMessage())) {

                double similarity = Util.similarity(e.getMessage(), last);
                if (similarity >= 0.75 && !Ranks.isStaff(uuid)) {
                    e.setCancelled(true);
                    Chat.sendMessage(player, Prefix.ERROR, "Viestisi muistuttaa liikaa vanhaa viestiäsi!");
                    return;
                }

            } else {
                e.setCancelled(true);
                Chat.sendMessage(player, Prefix.ERROR, "Et voi lähettää samaa viestiä uudestaan!");
                return;
            }

        }
        if (sentMessages.containsKey(uuid)) {
            long lastSent = sentMessages.get(uuid);
            if ((System.currentTimeMillis() - lastSent) / 1000 <= 30 && (boolean) Chat.settings.get("slow")) {
                if (!Ranks.isStaff(uuid)) {
                    e.setCancelled(true);
                    Chat.sendMessage(player, "Chat on hidastetussa tilassa! Voit lähettää viestejä §c30 sekunnin §7välein!");
                    return;
                }
            } else if ((System.currentTimeMillis() - lastSent) / 1000 <= 3 && !Ranks.isVIP(uuid)) {
                e.setCancelled(true);
                Chat.sendMessage(player, "Voit lähettää viestejä vain §c3 sekunnin §7välein! Ohittaaksesi tämän rajan " +
                        " tarvitset vähintään §e§lPremium§7-arvon! Lisätietoa §a/kauppa§7!");
                return;
            }
        }

        lastMessage.put(uuid, e.getMessage());
        sentMessages.put(uuid, System.currentTimeMillis());

        String msg = e.getMessage();

        for (Player online : Bukkit.getOnlinePlayers()) {

            if (online.getName().equals(player.getName())) continue;

            if (msg.toLowerCase().contains(online.getName().toLowerCase())) {

                if (Main.getStaffManager().hasStaffMode(online)) continue;
                int startIndex = msg.toLowerCase().indexOf(online.getName().toLowerCase());

                if (startIndex > 0 && msg.toLowerCase().charAt(startIndex - 1) == '@')
                    msg = msg.replaceAll("@" + online.getName(), "§a@" + online.getName() + "§r");
                else
                    msg = msg.replaceAll(online.getName(), "§a@" + online.getName() + "§r");

                Util.sendNotification(online, "§a" + player.getName() + " §7mainitsi sinut Chatissa!", !Settings.get(online.getUniqueId(), "chat_mentions"));

            }
        }

        e.setMessage(msg);

        String name = player.getName();

        if (msg.startsWith("!") || Settings.get(uuid, "chat")) {
            // Global chat
            if(msg.startsWith("!")) {
                msg = msg.substring(1);
                e.setMessage(msg);
                if(msg.length() < 1) {
                    e.setCancelled(true);
                    Chat.sendMessage(player, "Sinun pitää kirjoittaa jotain chattiin!");
                    return;
                }
            }
            e.setFormat((ChatColor.translateAlternateColorCodes('&', Sorsa.getPrefix(player) + name).trim()) + "§r: %2$s");
        } else {
            // Local chat
            e.setCancelled(true);
            final int square = 200 * 200;
            final List<UUID> localPlayers = new ArrayList<>();

            for(Player other : player.getWorld().getPlayers()) {
                // Ignore self
                if(other.getName().equals(player.getName())) continue;
                // Check if player is nearby enough
                if(other.getLocation().clone().distanceSquared(player.getLocation().clone()) <= square) localPlayers.add(other.getUniqueId());
            }

            if(localPlayers.isEmpty()) {
                Util.sendClickableText(player, Chat.getPrefix() + " Lähelläsi ei ole pelaajia! Laita viestin eteen '§a!§7' tai vaihda Chat-tilaasi kirjoittaaksesi globaalisti!","/asetukset", "§7Vaihda Chat-tilaasi!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                return;
            }

            // Add player to local, just to make our life easier
            localPlayers.add(player.getUniqueId());

            final String format = " §e[L] " +(ChatColor.translateAlternateColorCodes('&', Sorsa.getPrefix(player) + name).trim()) + "§r: " + msg;
            Sorsa.logColored("§6[CHAT] " + format);
            for(Player online : Bukkit.getOnlinePlayers()) {
                if(Main.getStaffManager().hasStaffMode(online) && !online.getName().equals(player.getName()))
                    online.sendMessage("§c§lSPY §7» §e[L] §7" + Ranks.getRankColor(Ranks.getRank(uuid)) + name + "§r: " + msg);
                else {
                    if(localPlayers.contains(online.getUniqueId()))
                        // Send the message to the nearby players
                        online.sendMessage(format);
                }

            }
        }

    }



}
