package me.tr.survival.main.other.booster;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.other.Enchant;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.callback.Callback;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.*;

public class Boosters implements Listener {

    private static HashMap<UUID, HashMap<Booster, Long>> active = new HashMap<>();

    public static void panel(Player player) {

        int size = 18 + (9 * ((int) Math.ceil((double) Booster.values().length / 7)));

        Gui gui = new Gui("Tehostukset", size);

        for(Booster booster : Booster.values()) {

            for(int i = 10; i < size - 10; i++) {
                Inventory inv = gui.getPages().get(1);

                if(gui.getButton(i) != null || inv.getItem(i) != null) continue;

                if(i == 18 || i == 27 || i == 36 || i == 45 || i == 17 || i == 26 || i == 35 || i == 44) {
                    continue;
                }

                List<String> lore = new ArrayList<>();
                lore.add("§7§m--------------------");
                lore.add("§7 ");

                if(isActive(booster)) {
                    lore.add("§7 Aikaa jäljellä: §c30min");
                    lore.add("§7 Aktivoinut: §a" + Bukkit.getOfflinePlayer(getActivator(booster)).getName());
                } else {
                    lore.add("§7 Hinta: §b" + booster.getCost() + " kristallia");
                }

                String[] text = Util.splitStringEvery(booster.getDescription(), 23);
                for(int j = 0; j < text.length; j++) {
                    lore.add("  §7" + ChatColor.translateAlternateColorCodes('&', text[j]));
                }

                lore.add("§7§m--------------------");
                if(!isActive(booster)) {
                    lore.add("§a§l KLIKKAA AKTIVOIDAKSESI!");
                }
                ItemStack item = ItemUtil.makeItem(Material.EMERALD, 1, booster.getDisplayName(), lore);
                if(isActive(booster)) {
                    item = Util.makeEnchanted(item);
                }

                gui.addButton(new Button(1, i, item) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        Boosters.activate(booster, clicker.getUniqueId());
                        Chat.sendMessage(clicker, "Aktivoit tehostuksen " + booster.getDisplayName() + "§7!");
                    }
                });

            }

        }

        gui.open(player);

    }

    public static void activateManager() {

        Autio.every(60, () -> {

            for(Map.Entry<UUID, HashMap<Booster, Long>> e : getActive().entrySet()) {
                for(Map.Entry<Booster, Long> e1 : e.getValue().entrySet()) {
                    long start = e1.getValue();
                    Booster booster = e1.getKey();
                    int time = booster.getDuration();
                    // Check if the booster's time is up
                    if(System.currentTimeMillis() >= start + (time * 60 * 1000)) {
                        Boosters.getActive().remove(e.getKey(), e.getValue());
                        Bukkit.broadcastMessage("§7§m--------------------");
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(" §c§lTEHOSTUS LOPPUI §7(" + booster.getDisplayName() + "§7)");
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(" §7Aktivoinut: §6" + getActivatorPlayer(booster).getName());
                        Bukkit.broadcastMessage(" §7Kesto: §c" + booster.getDuration() + "min");
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage("§7§m--------------------");

                        Util.broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING);
                    }
                }
            }

        }, true);

    }

    public static void activate(Booster booster, UUID uuid) {

        Bukkit.broadcastMessage("§7§m--------------------");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §a§lTEHOSTUS AKTIVOITU §7(" + booster.getDisplayName() + "§7)");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §7Aktivoinut: §6" + getActivatorPlayer(booster).getName());
        Bukkit.broadcastMessage(" §7Kesto: §c" + booster.getDuration() + "min");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m--------------------");

        Util.broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING);

        // Execute if booster has some immediate functionality
        booster.getCallback().execute();

        long time = System.currentTimeMillis();
        HashMap<Booster, Long> map = new HashMap<>();
        map.put(booster, time);
        Boosters.getActive().put(uuid, map);

    }

    public static OfflinePlayer getActivatorPlayer(Booster booster) {
        return Bukkit.getOfflinePlayer(getActivator(booster));
    }

    public static UUID getActivator(Booster booster) {
        for(Map.Entry<UUID, HashMap<Booster, Long>> e : getActive().entrySet()) {
            if(e.getValue().containsKey(booster)) {
                return e.getKey();
            }
        }
        return null;
    }

    public static boolean isActive(Booster booster) {
        return getActive().containsValue(booster);
    }

    public static HashMap<UUID, HashMap<Booster, Long>> getActive() {
        return Boosters.active;
    }

    public enum Booster {

        INSTANT_MINING(15, "§6§lVÄLITTÖMÄT ORET!",
                "§7Rikot oret välittömästi nopeammin millä tahansa työkalulla! Tehostus kestää §6§l15MIN§7!", 50, () -> {



        }),
        MORE_ORES(30, "§b§lENEMMÄN MINERAALEJA!",
                "§7Kun rikot oren, siitä putoaa §a2x §7enemmän mineraalia millä tahansa työkalulla. Tehostus kehtää §6§l30MIN§7!", 30, () -> {


        }),
        EXTRA_HEARTS(60, "§c§lLISÄSYDÄMET",
                "§7Kun tämä tehostus on päällä, sinulla on §c2 lisäsydäntä§7! Tehostus kestää §6§l1H§7!", 15, () -> {

        }),
        FIX_ITEMS(-1, "§6§lITEMIEN KORJAUS",
                "§7Tämä korjaa kaikki inventoryssasi olevat itemit.", 150, () -> {

            for(Player player : Bukkit.getOnlinePlayers()) {
                for(ItemStack item : player.getInventory().getContents()) {
                    Util.fixItem(item);
                }
                for(ItemStack armor : player.getInventory().getArmorContents()) {
                    Util.fixItem(armor);
                }
                Util.sendNotification(player, "§a§lTEHOSTUS §7Itemisi korjattiin!", true);
            }

        }),
        NO_HUNGER(25, "§6§lEI NÄLKÄÄ!",
                "§7Tällä tehostuksella et koe nälkää! Tehostus kestää §6§l25MIN§7!", 20, () -> {

        })
        ;

        // Duration in minutes
        int duration;
        String displayName;
        String description;
        int cost;
        Callback cb;

        Booster(int duration, String displayName, String description, int cost, Callback cb) {
            this.duration = duration;
            this.displayName = displayName;
            this.description = description;
            this.cost = cost;
            this.cb = cb;
        }

        public Callback getCallback() {
            return cb;
        }

        public int getDuration() {
            return duration;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public int getCost() {
            return cost;
        }
    }


}
