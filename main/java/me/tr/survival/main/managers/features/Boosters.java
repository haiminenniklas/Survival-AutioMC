package me.tr.survival.main.managers.features;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Profile;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.callback.Callback;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Boosters implements Listener {

    private static HashMap<String, HashMap<UUID, Long>> active = new HashMap<>();
    private static Map<String, Long> cooldown = new HashMap<>();

    public static boolean ENABLED = true;

    public static void panel(Player player) {

        if(!ENABLED && !player.isOp()) {
            Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä. Yritähän myöhemmin uudelleen.");
            return;
        }

        debug();
        Gui gui = new Gui("Tehostukset", 45);
        int[] itemSlots = new int[] { 10,12, 14, 16, 28, 30, 32, 34 };
        int[] glassSlots = new int[] { 11, 13, 15, 29, 31, 33 };

        for(Booster booster : Booster.values()) {

            for(int i : itemSlots) {
                Inventory inv = gui.getPages().get(1);

                if(gui.getButton(i) != null) continue;
                if(inv.getItem(i) != null) continue;

                List<String> lore = new ArrayList<>();
                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                lore.add("§7 ");

                if(isActive(booster)) {
                    if(booster.getDuration() >= 1) lore.add("§7 Aikaa jäljellä: §c" + getTimeLeft(booster) + "min");
                    lore.add("§7 Aktivoinut: §a" + Bukkit.getOfflinePlayer(getActivator(booster)).getName());
                    lore.add("§7 ");
                } else {
                    lore.add("§7 Hinta: §e" + Util.formatDecimals(booster.getCost()) + "€");
                    lore.add("§7 ");

                    if(isInCooldown(booster)) {

                        if(getTimeForNextUsage(booster) >= 1) {
                            lore.add(" §c§lJÄÄHYLLÄ §7(§c" + getTimeForNextUsage(booster) + "min §7jäljellä)");
                            lore.add("§7 ");
                        } else getActive().remove(booster.getDisplayName());
                    }

                }

                String[] text = Util.splitPreservingWords(booster.getDescription(), 30);
                for(int j = 0; j < text.length; j++) { lore.add(" §7" + ChatColor.translateAlternateColorCodes('&', text[j])); }

                lore.add("§7 ");
                lore.add(" §7§oTehostus vaikuttaa kaikkiin");
                lore.add(" §7§opelaajiin palvelimella!");
                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                if(!isActive(booster)) {
                    lore.add("§a§lKLIKKAA AKTIVOIDAKSESI!");
                }
                ItemStack item = ItemUtil.makeItem(booster.getItem(), 1, booster.getDisplayName(), lore);
                if(isActive(booster)) item = Util.makeEnchanted(item);

                gui.addButton(new Button(1, i, item) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);

                        if(isInCooldown(booster)) {
                            if(getTimeForNextUsage(booster) > 0) {
                                Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Tuo tehostus on jäähyllä. Odota vielä §c" + getTimeForNextUsage(booster) + "min§7!");
                                return;
                            } else cooldown.remove(booster.getDisplayName());
                        }

                        if(!isActive(booster)) {
                            if(Balance.canRemove(clicker.getUniqueId(), booster.getCost())) {
                                Balance.add(clicker.getUniqueId(), -booster.getCost());
                                Boosters.activate(booster, clicker.getUniqueId());
                                Chat.sendMessage(clicker, "Aktivoit tehostuksen " + booster.getDisplayName() + "§7!");
                            } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tuohon!");
                        } else Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Tuo tehostus on jo käynnistetty!");
                    }
                });
                break;
            }
        }

        for(int i : itemSlots) {
            if(gui.getButton(i) == null) {
                gui.addItem(1, ItemUtil.makeItem(Material.TURTLE_EGG, 1, "§2Tulossa...", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Tähän ei ole vielä keksitty",
                        " §7mitään. Jos sinulla on joitain",
                        " §7mahdollisia ideoita, kerro se",
                        " §7meille meidän Discord-palvelimella!",
                        " §7(§9/discord§7)!",
                        " §7Hyvästä ideasta saattaa seurata",
                        " §7palkkio! ;)",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                )), i);
            }
        }

        for(int i : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.BROWN_STAINED_GLASS_PANE), i); }

        for(int i = 0; i < 45; i++) {

            if(gui.getButton(i) != null) continue;
            if(gui.getItem(i) != null) continue;

            if(i == 36) {
                gui.addButton(new Button(1, i, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        Profile.openProfile(clicker, clicker.getUniqueId());
                    }
                });
                continue;
            }

            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);

        }

        gui.open(player);

    }

    public static void debug() {
        for(Map.Entry<String, Long> entry : getInCooldown().entrySet()) {
            Booster booster = Boosters.getBoosterByName(entry.getKey());
            if(getTimeForNextUsage(booster) <= 0) getInCooldown().remove(entry.getKey());
        }

        for(Map.Entry<String, HashMap<UUID, Long>> entry : getActive().entrySet()) {
            Booster booster = Boosters.getBoosterByName(entry.getKey());
            if(booster == null) {
                getActive().remove(entry.getKey());
                continue;
            }
            if(getTimeLeft(booster) <= 0) deactivate(booster);
        }

    }

    public static Booster getBoosterByName(String name) {
        for(Booster booster : Booster.values()) {
            if(booster.getDisplayName().equalsIgnoreCase(name)) return booster;
        }
        return null;
    }

    public static Map<String, Long> getInCooldown() {
        return Boosters.cooldown;
    }

    public static boolean isInCooldown(Booster booster) {
        return getInCooldown().containsKey(booster.getDisplayName());
    }

    public static long getTimeForNextUsage(Booster booster) {
        if(!isInCooldown(booster)) return 0;
        long putInCooldown = getInCooldown().get(booster.getDisplayName());
        long shouldExpire = putInCooldown + (60 * 1000 * 60);
        long timeLeft = shouldExpire - System.currentTimeMillis();
        if(timeLeft <= 0) return 0;
        return timeLeft / 1000 / 60;

    }

    public static void activateManager() {

        Sorsa.every(60, () -> {
            if(getActive().size() < 1) return;
            for(Map.Entry<String, HashMap<UUID, Long>> e : getActive().entrySet()) {
                Booster booster = getBoosterByName(e.getKey());
                if(booster == null) continue;
                if(getTimeLeft(booster) <= 0) deactivate(booster);
            }
        }, true);

    }

    public static void deactivate(Booster booster) {
        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §c§lTEHOSTUS LOPPUI §7(" + booster.getDisplayName() + "§7)");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §7Aktivoinut: §a" + getActivatorPlayer(booster).getName());

        if(booster.getDuration() >= 1) Bukkit.broadcastMessage(" §7Kesto: §c" + booster.getDuration() + "min");

        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        Boosters.getActive().remove(booster.getDisplayName());
        getInCooldown().put(booster.getDisplayName(), System.currentTimeMillis());

        if(booster == Booster.EXTRA_HEARTS) {
            for(Player player : Bukkit.getOnlinePlayers()) { player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d); }
        }

        Util.broadcastSound(Sound.BLOCK_ANVIL_FALL);
    }

    public static long getTimeLeft(Booster booster) {
        if(getActive().containsKey(booster.getDisplayName())) {
            HashMap<UUID, Long> map = getActive().get(booster.getDisplayName());
            long value = (long) map.values().toArray()[0];
            long stopTime = value + (booster.getDuration() * 60 * 1000);
            long timeLeft = (stopTime - System.currentTimeMillis()) / 60 / 1000;
            return timeLeft;
        }
        return 0L;
    }

    public static void activate(Booster booster, UUID uuid) {

        long time = System.currentTimeMillis();

        HashMap<UUID, Long> map = new HashMap<>();
        map.put(uuid, time);
        Boosters.getActive().put(booster.getDisplayName(), map);


        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §a§lTEHOSTUS AKTIVOITU §7(" + booster.getDisplayName() + "§7)");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §7Aktivoinut: §6" + getActivatorPlayer(booster).getName());
        if(booster.getDuration() >= 1) Bukkit.broadcastMessage(" §7Kesto: §c" + booster.getDuration() + "min");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        Util.broadcastSound(Sound.ENTITY_ENDER_DRAGON_GROWL);
        // Execute if booster has some immediate functionality
        booster.getCallback().execute();
        if(booster.getDuration() < 1) {
            Boosters.getActive().remove(booster.getDisplayName());
            getInCooldown().put(booster.getDisplayName(), System.currentTimeMillis());
        }
    }

    public static OfflinePlayer getActivatorPlayer(Booster booster) {
        return Bukkit.getOfflinePlayer(getActivator(booster));
    }

    public static UUID getActivator(Booster booster) {
        if(getActive().containsKey(booster.getDisplayName())) {
            for(Map.Entry<UUID, Long> e : getActive().get(booster.getDisplayName()).entrySet()) {
                return e.getKey();
            }
        }
        return null;
    }

    public static boolean isActive(Booster booster) { return getActive().containsKey(booster.getDisplayName()); }

    public static HashMap<String, HashMap<UUID, Long>> getActive() { return Boosters.active; }

    public enum Booster {

        INSTANT_MINING(45, "§fVälittömät malmit",
                "§7Kun tämä tehostus on päällä §fRauta §7ja §6Kulta §7-malmit sulavat heti, eikä niitä tarvitse erikseen kierrättää uunissa." +
                        " §7Tehostus kestää §6§l45MIN§7!", 5000, () -> {
        }, Material.IRON_NUGGET),
        MORE_ORES(30, "§bMineeralimyllerrys!",
                "§7Kun tämä tehostus on päällä, niin §bTimantti§7, §aEmerald§7, §9Lapis §7-malmeista tippuu §a2x §7enemmän tavaraa millä tahansa työkalulla! Tehostus kestää §6§l30MIN§7!", 30000, () -> {
        }, Material.DIAMOND_ORE),
        EXTRA_HEARTS(60, "§cSote-uudistus",
                "§7Kun tämä tehostus on päällä, sinulla on §c2 lisäsydäntä§7! Tehostus kestää §6§l1H§7!", 3500, () -> {

            for(Player player : Bukkit.getOnlinePlayers()) {
                Util.heal(player);
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(24d);
                player.setHealth(24d);
            }

        }, Material.POTION),
        FIX_ITEMS(-1, "§fSepän armahdus",
                "§7Tämä korjaa kaikki inventoryssasi olevat itemit.", 25000, () -> {

            for(Player player : Bukkit.getOnlinePlayers()) {
                for(ItemStack item : player.getInventory().getContents()) {
                    if(item != null) Util.fixItem(item);
                }
                for(ItemStack armor : player.getInventory().getArmorContents()) {
                    if(armor != null) Util.fixItem(armor);
                }
                Util.sendNotification(player, "§a§lTEHOSTUS §7Itemisi korjattiin!", true);
            }

        }, Material.CHAINMAIL_CHESTPLATE),
        NO_HUNGER(25, "§6Leipäjono",
                "§7Tällä tehostuksella et koe nälkää! Tehostus kestää §6§l25MIN§7!", 7000, () -> {
        }, Material.COOKED_BEEF),

        DOUBLE_XP(45, "§eKokemuspisteiden kapina",
                "§7Tällä tehostuksella saat jokaisesta tappamastasi mobista §a2x §7enemmän §eXP:§7tä! Tehostus kestää §6§l45MIN§7!", 4000, () -> {

        }, Material.EXPERIENCE_BOTTLE);

        // Duration in minutes
        int duration;
        String displayName;
        String description;
        int cost;
        Callback cb;
        Material item;

        Booster(int duration, String displayName, String description, int cost, Callback cb, Material item) {
            this.duration = duration;
            this.displayName = displayName;
            this.description = description;
            this.cost = cost;
            this.cb = cb;
            this.item = item;
        }

        public Material getItem() {
            return item;
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
