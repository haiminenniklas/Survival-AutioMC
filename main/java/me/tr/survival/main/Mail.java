package me.tr.survival.main;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Lottery;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.UUID;

public class Mail {

    public static void panel(Player player) {

        Gui gui = new Gui("Posti", 27);

        String canOpen = (canOpenDaily(player)) ?
                "§a§lAVATTAVISSA" :
                "§7Täytyy odottaa vielä §c" + (timeLeftForNextMail(player.getUniqueId()) / 1000 / 60 / 60) + "h" ;

        Material dailyMat = Material.CHEST_MINECART;
        if(!canOpenDaily(player)) {
            dailyMat = Material.MINECART;
        }

        String multiplierText = (getMultiplier(player) > 1) ? "§7(§b§l" + getMultiplier(player) + "x§7)" : "";

        int multiplier = getMultiplier(player);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(dailyMat, 1, "§aPäivittäinen", Arrays.asList(
                "§7§m--------------------",
                "",
                "§7 " + canOpen,
                "",
                "§7 Putki: §6" + getStreak(player) + " " + multiplierText,
                "",
                "§7 Sinua odottaa:",
                "§7 - §b+" + (3 * multiplier) + " kristallia",
                "§7 - §f+" + (10 * multiplier) + " rautaa",
                "§7 - §c+" +  + (16 * multiplier) + " pihviä",
                "§7 - §b+ " + (2 * multiplier) + " timanttia §7(§6Premium§7)",
                "§7 - §a+ " + (multiplier) + " emeraldia §7(§6Premium§7)",
                "§7 - §b+ " + (5 * multiplier) + " kristallia §7(§6Premium§e+§7)",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                gui.close(clicker);

                if(!canOpenDaily(clicker)) {
                    Chat.sendMessage(clicker, "Et voi avata tätä vielä! Odota vielä §c" + (timeLeftForNextMail(player.getUniqueId()) / 1000 / 60 / 60) + "h");
                    return;
                }

                if(Mail.getStreak(clicker) < 1) {
                    Mail.setStreak(clicker, 1);
                } else if(Mail.getStreak(clicker) >= 1 && timeFromLastMail(clicker.getUniqueId()) < (1000 * 60 * 60 * 24 * 2)) {
                    Mail.addStreak(clicker);
                } else {
                    Mail.setStreak(clicker, 1);
                }

                int multiplier = getMultiplier(clicker);

                clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                clicker.sendMessage("§7§m--------------------");

                if(multiplier > 1) {
                    clicker.sendMessage("§7Kerroin §b§l" + multiplier + "x§7!");
                }

                if(Ranks.hasRank(clicker.getUniqueId(), "premiumplus")) {
                    Crystals.add(clicker.getUniqueId(), 5 * multiplier);
                    clicker.sendMessage("§b§l+" + (5 * multiplier) + " kristallia");
                } else {
                    Crystals.add(clicker.getUniqueId(), 3 * multiplier);
                    clicker.sendMessage("§b§l+" + (3 * multiplier) + " kristallia");
                }

                if(Mail.getStreak(clicker) >= 14 && Mail.getStreak(clicker) <= 21) {
                    Crystals.add(clicker.getUniqueId(), 3);
                    clicker.sendMessage("§b§l+ 3 Kristallia §7(Streak)");
                }

                clicker.getInventory().addItem(ItemUtil.makeItem(Material.IRON_INGOT, 10 * multiplier));
                clicker.sendMessage("§f§l+" + (10 * multiplier) + " rautaa");
                clicker.getInventory().addItem(ItemUtil.makeItem(Material.COOKED_BEEF, 16 * multiplier));
                clicker.sendMessage("§c§l+" + (16 * multiplier) + " pihviä");

                if(Ranks.isVIP(clicker.getUniqueId())) {
                    clicker.getInventory().addItem(ItemUtil.makeItem(Material.DIAMOND, 2 * multiplier));
                    clicker.sendMessage("§b§l+"  + (2 * multiplier) + " timanttia");
                    clicker.getInventory().addItem(ItemUtil.makeItem(Material.EMERALD, 1 * multiplier));
                    clicker.sendMessage("§a§l+" + (1 * multiplier) + " emerald");
                }

                clicker.sendMessage("§7§m--------------------");

                Mail.setLastMail(clicker.getUniqueId());

            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.PAPER, 1, "§6Arvat", Arrays.asList(
                "§7§m--------------------",
                "",
                " §7Kun äänestät palvelinta       ",
                " §7komennolla §a/vote, saat      ",
                " §7itsellesi §6yhden arvan§7,    ",
                " §7jolla voit voittaa itsellesi  ",
                " §7jopa §6§lPremium§7-arvon!     ",
                "",
                " §7Arvat: §e" + Mail.getTickets(player),
                "",
                " §6Klikkaa avataksesi arvan!     ",
                "",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(player);
                Lottery.lot(player);
            }
        });

        gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Streakit", Arrays.asList(
                "§7§m--------------------",
                "",
                "§7Mitä pitempään käyt putkeen",
                "§7hakemassa palvelimen §apäivittäisen",
                "§7postitoimituksen, niin toimituksen",
                "§7ns. §ekerroin §7nousee. Kertoimet:",
                "",
                "§7- §a§l>7pv §7-> §b§l2x",
                "§7- §e§l>14pv §7-> §b§l+3 kristallia",
                "§7 - §c§l>21pv §7-> §b§l4x",
                "",
                "§7§m--------------------"
        )), 18);

        gui.open(player);

    }

    public static int getMultiplier(OfflinePlayer player) {
        int multiplier = 1;
        int streak = Mail.getStreak(player);
        if(streak >= 7 && streak <= 21) {
            multiplier = 2;
        } else if(streak >= 21) {
            multiplier = 3;
        }
        return multiplier;
    }

    public static boolean canOpenDaily(OfflinePlayer player) {
        return (timeLeftForNextMail(player.getUniqueId()) / 1000 / 60 / 60) <= 0;
    }

    public static int getTickets(OfflinePlayer player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        return (int) PlayerData.getValue(player.getUniqueId(), "tickets");

    }

    public static void addTickets(UUID uuid, int amnt) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        PlayerData.add(uuid, "tickets", amnt);

    }

    public static int getStreak(OfflinePlayer player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        return (int) PlayerData.getValue(player.getUniqueId(), "streak");

    }

    public static void setStreak(OfflinePlayer player, int val) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        PlayerData.set(player.getUniqueId(), "streak", val);
    }

    public static void addStreak(OfflinePlayer player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        PlayerData.add(player.getUniqueId(), "streak", 1);

    }

    public static long getLastMail(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        return (long) PlayerData.getValue(uuid, "last_mail");
    }

    public static void setLastMail(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        PlayerData.set(uuid, "last_mail", System.currentTimeMillis());

    }

    public static void setLastMail(UUID uuid, long value) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        PlayerData.set(uuid, "last_mail", value);

    }

    public static long timeFromLastMail(UUID uuid) {
        return System.currentTimeMillis() - getLastMail(uuid);
    }

    public static long timeLeftForNextMail(UUID uuid) {
        long next = getLastMail(uuid) + 1000 * 60 * 60 * 24;
        return next - System.currentTimeMillis();
    }

}
