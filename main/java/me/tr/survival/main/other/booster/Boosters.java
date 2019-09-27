package me.tr.survival.main.other.booster;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.other.Enchant;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.callback.Callback;
import me.tr.survival.main.util.data.Crystals;
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

    private static HashMap<String, HashMap<UUID, Long>> active = new HashMap<>();

    public static void panel(Player player) {

        int size = 18 + (9 * ((int) Math.ceil((double) Booster.values().length / 7)));

        Gui gui = new Gui("Tehostukset", size);

        for(Booster booster : Booster.values()) {

            for(int i = 10; i < size - 10; i++) {
                Inventory inv = gui.getPages().get(1);

                if(gui.getButton(i) != null) continue;
                if(inv.getItem(i) != null) continue;

                if(i == 18 || i == 27 || i == 36 || i == 45 || i == 17 || i == 26 || i == 35 || i == 44) {
                    continue;
                }

                List<String> lore = new ArrayList<>();
                lore.add("§7§m--------------------");
                lore.add("§7 ");

                if(isActive(booster)) {
                    lore.add("§7 Aikaa jäljellä: §c" + getTimeLeft(booster) + "min");
                    lore.add("§7 Aktivoinut: §a" + Bukkit.getOfflinePlayer(getActivator(booster)).getName());
                    lore.add("§7 ");
                } else {
                    lore.add("§7 Hinta: §b" + booster.getCost() + " kristallia");
                    lore.add("§7 ");
                }

                String[] text = Util.splitPreservingWords(booster.getDescription(), 23);
                for(int j = 0; j < text.length; j++) {
                    lore.add(" §7" + ChatColor.translateAlternateColorCodes('&', text[j]));
                }

                lore.add("§7 ");
                lore.add("§7§m--------------------");
                if(!isActive(booster)) {
                    lore.add("§a§lKLIKKAA AKTIVOIDAKSESI!");
                }
                ItemStack item = ItemUtil.makeItem(Material.EMERALD, 1, booster.getDisplayName(), lore);
                if(isActive(booster)) {
                    item = Util.makeEnchanted(item);
                }

                gui.addButton(new Button(1, i, item) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        if(!isActive(booster)) {
                            if(Crystals.canRemove(clicker.getUniqueId(), booster.getCost())) {
                                Crystals.add(clicker.getUniqueId(), -booster.getCost());
                                Boosters.activate(booster, clicker.getUniqueId());
                                Chat.sendMessage(clicker, "Aktivoit tehostuksen " + booster.getDisplayName() + "§7!");
                            } else {
                                Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tuohon!");
                            }
                        } else {
                            Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Tuo tehostus on jo käynnistetty!");
                        }
                    }
                });

                System.out.println(getActive());

                break;

            }

        }

        gui.open(player);

    }

    public static Booster getBoosterByName(String name) {
        for(Booster booster : Booster.values()) {
            if(booster.getDisplayName().equalsIgnoreCase(name)) {
                return booster;
            }
        }
        return null;
    }

    public static void activateManager() {

        Autio.every(60, () -> {

            for(Map.Entry<String, HashMap<UUID, Long>> e : getActive().entrySet()) {
                for(Map.Entry<String, Long> e1 : e.getValue().entrySet()) {
                    long start = e1.getValue();
                    Booster booster = getBoosterByName(e1.getKey());
                    int time = booster.getDuration();
                    // Check if the booster's time is up

                    if(time < 0) continue;

                    if(System.currentTimeMillis() >= start + (time * 60 * 1000)) {
                        Bukkit.broadcastMessage("§7§m--------------------");
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(" §c§lTEHOSTUS LOPPUI §7(" + booster.getDisplayName() + "§7)");
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(" §7Aktivoinut: §6" + getActivatorPlayer(booster).getName());
                        Bukkit.broadcastMessage(" §7Kesto: §c" + booster.getDuration() + "min");
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage("§7§m--------------------");

                        Boosters.getActive().remove(e.getKey(), e.getValue());
                        Util.broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING);
                    }
                }
            }

        }, true);

    }

    public static long getTimeLeft(Booster booster) {
        for(Map.Entry<UUID, HashMap<String, Long>> e : getActive().entrySet()) {
            for(Map.Entry<String, Long> e1 : e.getValue().entrySet()) {
                if(booster.getDisplayName().equalsIgnoreCase(e1.getKey())) {
                    return (System.currentTimeMillis() - e1.getValue()) / 1000 / 60 / 60;
                }
            }
        }
        return 0L;
    }

    public static void activate(Booster booster, UUID uuid) {

        long time = System.currentTimeMillis();
        HashMap<String, Long> map = new HashMap<>();
        map.put(booster.getDisplayName(), time);
        Boosters.getActive().put(uuid, map);

        Bukkit.broadcastMessage("§7§m--------------------");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §a§lTEHOSTUS AKTIVOITU §7(" + booster.getDisplayName() + "§7)");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §7Aktivoinut: §6" + getActivatorPlayer(booster).getName());
        if(booster.getDuration() >= 1) {
            Bukkit.broadcastMessage(" §7Kesto: §c" + booster.getDuration() + "min");
        }
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m--------------------");

        Util.broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING);

        // Execute if booster has some immediate functionality
        booster.getCallback().execute();


    }

    public static OfflinePlayer getActivatorPlayer(Booster booster) {
        return Bukkit.getOfflinePlayer(getActivator(booster));
    }

    public static UUID getActivator(Booster booster) {
        for(Map.Entry<String, HashMap<UUID, Long>> e : getActive().entrySet()) {
            if(e.getValue().containsKey(booster.getDisplayName())) {
                return e.getKey();
            }
        }
        return null;
    }

    public static boolean isActive(Booster booster) {
        for(Map.Entry<String, HashMap<UUID, Long>> e : getActive().entrySet()) {

            return e.getValue().containsKey(booster.getDisplayName());

        }
        return false;
    }

    public static HashMap<String, HashMap<UUID, Long>> getActive() {
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

            for(Player player : Bukkit.getOnlinePlayers()) {
                Util.heal(player);
                player.setHealth(22d);
            }

        }),
        FIX_ITEMS(-1, "§6§lITEMIEN KORJAUS",
                "§7Tämä korjaa kaikki inventoryssasi olevat itemit.", 150, () -> {

            for(Player player : Bukkit.getOnlinePlayers()) {
                for(ItemStack item : player.getInventory().getContents()) {
                    if(item != null) {
                        Util.fixItem(item);
                    }
                }
                for(ItemStack armor : player.getInventory().getArmorContents()) {
                    if(armor != null) {
                        Util.fixItem(armor);
                    }
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
