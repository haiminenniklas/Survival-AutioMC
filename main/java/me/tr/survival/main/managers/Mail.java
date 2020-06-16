package me.tr.survival.main.managers;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.features.Lottery;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class Mail {

    public static void panel(Player player) {

        Gui gui = new Gui("Posti", 27);

        long hours = (timeLeftForNextMail(player.getUniqueId()) / 1000 / 60 / 60);
        String time = hours + "h";
        if(hours < 1) {
            hours = hours / 60;
            time = hours + "min";
        }

        String canOpen = (canOpenDaily(player)) ? "§a§lAVATTAVISSA" : "§7Odota vielä §c" + time;

        Material dailyMat = Material.CHEST_MINECART;
        if(!canOpenDaily(player)) dailyMat = Material.MINECART;

        String multiplierText = (getMultiplier(player) > 1) ? "§7(§b§l" + getMultiplier(player) + "x§7)" : "";
        final int multiplier = getMultiplier(player);

        if(timeFromLastMail(player.getUniqueId()) > (1000 * 60 * 60 * 24 * 2)) Mail.setStreak(player, 1);

        final int moneyToReceive = (150 * multiplier);
        final int ironToReceive = (16 * multiplier);
        final int foodToReceive = (16 * multiplier);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(dailyMat, 1, "§aPäivittäinen", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "",
                "§7 " + canOpen,
                "",
                "§7 Putki: §6" + getStreak(player) + " " + multiplierText,
                "",
                "§7 Sinua odottaa:",
                "§7 - §a+" + moneyToReceive + "€",
                "§7 - §f+" + ironToReceive + "kpl rautaa",
                "§7 - §c+" + foodToReceive + " pihviä",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                if(!canOpenDaily(clicker)) clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1,1 );
                else {
                    gui.close(clicker);
                    if(Mail.getStreak(clicker) < 1) Mail.setStreak(clicker, 1);
                    else if(Mail.getStreak(clicker) >= 1 && timeFromLastMail(clicker.getUniqueId()) < (1000 * 60 * 60 * 24 * 2)) Mail.addStreak(clicker);
                    else Mail.setStreak(clicker, 1);

                    int multiplier = getMultiplier(clicker);

                    clicker.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                    if(multiplier > 1) clicker.sendMessage("§7Kerroin §b§l" + multiplier + "x§7!");

                    Balance.add(clicker.getUniqueId(), moneyToReceive);
                    clicker.sendMessage("§a§l+ " + moneyToReceive + "€");

                    if(Mail.getStreak(clicker) >= 14 && Mail.getStreak(clicker) <= 21) {
                        Balance.add(clicker.getUniqueId(), 275);
                        clicker.sendMessage("§e§l+ 275€ §7§o(Streak)");
                    }

                    double chance = new Random().nextDouble();
                    if(chance < .20) {
                        Mail.addTickets(player.getUniqueId(), 1);
                        clicker.sendMessage("§6§l+1 Arpa!!!");
                        if(chance < .05) {
                            clicker.sendMessage("§b§l+5kpl timanttia!!!");
                            clicker.getInventory().addItem(ItemUtil.makeItem(Material.DIAMOND, 5));
                        }
                        clicker.playSound(clicker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    } else {
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                    }


                    clicker.getInventory().addItem(ItemUtil.makeItem(Material.IRON_INGOT, ironToReceive));
                    clicker.sendMessage("§f§l+" + ironToReceive + "kpl rautaa");
                    clicker.getInventory().addItem(ItemUtil.makeItem(Material.COOKED_BEEF, foodToReceive));
                    clicker.sendMessage("§c§l+" + foodToReceive + " pihviä");
                    clicker.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    Mail.setLastMail(clicker.getUniqueId());
                }


            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.PAPER, 1, "§2Arvat", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "",
                " §7Kun äänestät palvelinta",
                " §7komennolla §a/vote§7, saat",
                " §7itsellesi §ayhden arvan§7,",
                " §7jolla voit voittaa itsellesi",
                " §7jopa §e§lPremium§7-arvon!",
                " §7Arpoja voi myös voittaa",
                " §atoimituksista§7.",
                "",
                " §7Arvat: §e" + Mail.getTickets(player),
                "",
                " " + (Mail.getTickets(player) >= 1 ? "§aKlikkaa avataksesi!" : "§cEt omista arpoja!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                if(Mail.getTickets(clicker) >= 1) {
                    gui.close(clicker);
                    Lottery.lot(clicker);
                } else clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            }
        });

        gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Bonukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "",
                "§7Mitä pitempään käyt putkeen",
                "§7hakemassa palvelimen §apäivittäisen",
                "§7postitoimituksen, niin toimitukseen",
                "§7ns. §ebonuksia§7! Tässä bonukset: ",
                "",
                "§7- §a+7pv §7» §b2x kerroin",
                "§7- §e+14pv §7» §b+275€",
                "§7- §c+21pv §7» §b3x kerroin",
                "",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 26);

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(player);
                Profile.openProfile(player, clicker.getUniqueId());
            }
        });

        int[] glassSlots = new int[] { 11,13,15 };
        for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE), slot); }

        for(int i = 0; i < 27; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }
        gui.open(player);
    }

    private static int getMultiplier(OfflinePlayer player) {
        int multiplier = 1;
        int streak = Mail.getStreak(player);
        if(streak >= 7 && streak <= 21) multiplier = 2;
        else if(streak >= 21) multiplier = 3;
        return multiplier;
    }

    private static boolean canOpenDaily(OfflinePlayer player) {
        return (timeLeftForNextMail(player.getUniqueId()) / 1000 / 60 / 60) <= 0;
    }

    public static int getTickets(OfflinePlayer player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        return (int) PlayerData.getValue(player.getUniqueId(), "tickets");
    }

    public static void addTickets(UUID uuid, int amnt) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.add(uuid, "tickets", amnt);
    }

    public static int getStreak(OfflinePlayer player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        return (int) PlayerData.getValue(player.getUniqueId(), "streak");
    }

    private static void setStreak(OfflinePlayer player, int val) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        PlayerData.set(player.getUniqueId(), "streak", val);
    }

    public static void addStreak(OfflinePlayer player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        PlayerData.add(player.getUniqueId(), "streak", 1);

    }

    private static long getLastMail(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (long) PlayerData.getValue(uuid, "last_mail");
    }

    private static void setLastMail(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.set(uuid, "last_mail", System.currentTimeMillis());
    }

    public static void setLastMail(UUID uuid, long value) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.set(uuid, "last_mail", value);
    }

    private static long timeFromLastMail(UUID uuid) {
        return System.currentTimeMillis() - getLastMail(uuid);
    }

    private static long timeLeftForNextMail(UUID uuid) {
        long next = getLastMail(uuid) + 1000 * 60 * 60 * 24;
        return next - System.currentTimeMillis();
    }

}
