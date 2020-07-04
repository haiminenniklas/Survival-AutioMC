package me.tr.survival.main.managers.features;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Houkutin implements CommandExecutor {

    // Command

    private static boolean ENABLED = true;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length < 1) {
            if(sender instanceof Player) panel((Player) sender);
        } else {
            if(sender.isOp()) {
                if(args[0].equals("deactivate")) {
                    deactivate();
                } else if(args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage("§c/houkutin deactivate");
                    sender.sendMessage("§c/houkutin (enable | disable)");
                } else if(args[0].equalsIgnoreCase("disable")) {
                    ENABLED = false;
                    sender.sendMessage("§7Houkutin on nyt §cpois päältä§7!");
                    deactivate();
                } else if(args[0].equalsIgnoreCase("enable")) {
                    ENABLED = true;
                    sender.sendMessage("Houkutin on nyt §apäällä§7!");
                }
            }
        }
        return true;
    }

    // Options

    private final long durationMillis = 1000 * 60 * 60;
    private final long durationMinutes = durationMillis / 1000 / 60;

    // Variables

    private UUID activator;
    private long started;
    private EntityType entityType;

    public void panel(Player player) {

        Gui.openGui(player, "Houkutin", 27, (gui) -> {

            List<String> lore = new ArrayList<>();

            lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
            lore.add(" ");
            lore.add(" §7Houkuttimella, pystyt");
            lore.add(" §7aktivoimaan spawnilla");
            lore.add(" §7sijaitsevan §espawnerin§7,");
            lore.add(" §7joka synnyttää valitsemaasi");
            lore.add(" §7eläintä.");
            lore.add(" ");
            lore.add(" §7Kesto: §e" + durationMinutes + "min");
            lore.add(" §7Hinta: §ealk. 2 500€");
            lore.add(" ");

            if(isActivated()) {

                lore.add(" §7Aktivoinut: §a" + getActivator().getName());
                lore.add(" §7Aikaa jäljellä: §a" + getTimeLeft());
                lore.add(" ");

            } else {
                if(ENABLED) {
                    if(Balance.canRemove(player.getUniqueId(), 2500)) lore.add(" §a§lKlikkaa aktivoidaksesi");
                    else lore.add(" §cEi varaa...");
                } else lore.add(" §cEi käytettävissä...");
            }
            lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            ItemStack item = ItemUtil.makeItem(Material.SPAWNER, 1, "§a§lHoukutin", lore);

            gui.addButton(new Button(1, 13, isActivated() ? Util.makeEnchanted(item) : item) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {

                    if(!ENABLED) {
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        return;
                    }

                    if(!isActivated()) {
                        if(Balance.canRemove(clicker.getUniqueId(), 2500)) {
                            gui.close(clicker);
                            selectGui(clicker);

                        } else clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    } else clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                }
            });

            int[] glassSlots = { 12,14 };
            for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, 1), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1), i);
            }

        });

    }

    private void selectGui(Player player) {

        Gui.openGui(player, "Houkutin - Valitse Eläin", 27, (gui) -> {

            int slot = 11;

            gui.addButton(new Button(1,18,ItemUtil.makeItem(Material.ARROW, 1, "§7Peruuta")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                }
            });

            for(final EntityType type : allowedEntityTypes()) {

                ItemStack item = new ItemStack(getSpawnEggMaterial(type));
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName("§a" + translateEntityType(type));

                List<String> lore = new ArrayList<>();
                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                lore.add(" §7Tämä houkutin alkaa");
                lore.add(" §7synnyttämään eläintä");
                lore.add(" §a" + translateEntityType(type) + "§7!");
                lore.add(" ");
                lore.add(" §7Hinta: §e" + Util.formatDecimals(getPrice(type)) + "€");
                lore.add(" §7Kesto: §e" + durationMinutes + "min");
                lore.add(" ");
                lore.add(" §aKlikkaa aktivoidaksesi!");
                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                meta.setLore(lore);
                item.setItemMeta(meta);

                gui.addButton(new Button(1, slot, item) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {

                        if(!ENABLED) {
                            clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                            return;
                        }

                        gui.close(clicker);
                        if(!isActivated()) {

                            if(Balance.canRemove(clicker.getUniqueId(), 2500)) {

                                Balance.remove(clicker.getUniqueId(), 2500);
                                activator = clicker.getUniqueId();
                                started = System.currentTimeMillis();
                                entityType = type;

                                Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                                Bukkit.broadcastMessage(" §a§lHoukutin aktivoitu!");
                                Bukkit.broadcastMessage(" ");
                                Bukkit.broadcastMessage(" §7Aktivoija: §e" + clicker.getName());
                                Bukkit.broadcastMessage(" §7Eläin: §e" + translateEntityType(type));
                                Bukkit.broadcastMessage(" §7Kesto: §e" + durationMinutes + "min");
                                Bukkit.broadcastMessage(" ");
                                Bukkit.broadcastMessage(" §aHoukutin löytyy Spawnilta! /spawn");
                                Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                                activate();

                            }

                        } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Houkutin on jo aktivoitu!");
                    }
                });

                slot += 1;
                if(slot > 15) break;

            }

            int[] glassSlots = { 10,16 };
            for(int s : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, 1), s); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1), i);
            }

        });

    }

    private void activate() {

        if(!ENABLED) return;

        Sorsa.logColored("§6[Houkutin] Houkutin was activated! Entity: " + this.entityType);

        Block block = Bukkit.getWorld("world").getBlockAt(-16, 59, -33);
        block.setType(Material.SPAWNER);
        BlockState state = block.getState();
        CreatureSpawner spawner = (CreatureSpawner) state;
        spawner.setSpawnCount(4);
        spawner.setSpawnedType(entityType);
        spawner.setDelay(2);
        spawner.update();

    }

    public void deactivate() {
        activator = null;
        entityType = null;
        Block block = Bukkit.getWorld("world").getBlockAt(-16, 59, -33);
        block.setType(Material.EMERALD_BLOCK);
    }

    public void activateManager() {

         new BukkitRunnable() {

            @Override
            public void run() {

                if(getTimeLeftMillis() < 1) {
                    if(activator != null && entityType != null) {

                        OfflinePlayer ac = Bukkit.getOfflinePlayer(activator);

                        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                        Bukkit.broadcastMessage(" §a§lHoukutin päättynyt!");
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(" §7Aktivoija: §e" + ac.getName());
                        Bukkit.broadcastMessage(" §7Eläin: §e" + translateEntityType(entityType));
                        Bukkit.broadcastMessage(" §7Hinta: §e" + Util.formatDecimals(getPrice(entityType)) + "€");
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(" §aHoukutin löytyy Spawnilta! /spawn");
                        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                        deactivate();

                    }
                }

            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 0, 60);

    }

    private String translateEntityType(EntityType type) {
        switch(type) {
            case COW:
                return "Lehmä";
            case PIG:
                return "Possu";
            case SHEEP:
                return "Lammas";
            case CHICKEN:
                return "Kana";
            case MUSHROOM_COW:
                return "Sienilehmä";
        }

        return "";
    }

    private double getPrice(EntityType type) {
        switch (type) {
            case COW:
                return 2500;
            case PIG:
                return 2500;
            case SHEEP:
                return 2500;
            case CHICKEN:
                return 2500;
            case MUSHROOM_COW:
                return 2500;
        }
        return 2500;
    }

    private EntityType[] allowedEntityTypes() {
        EntityType[] entities = new EntityType[] {
                EntityType.COW,
                EntityType.PIG,
                EntityType.SHEEP,
                EntityType.CHICKEN,
                EntityType.MUSHROOM_COW
        };
        return entities;
    }

    private Material getSpawnEggMaterial(EntityType type) {
        switch (type) {
            case COW:
                return Material.COW_SPAWN_EGG;
            case PIG:
                return Material.PIG_SPAWN_EGG;
            case SHEEP:
                return Material.SHEEP_SPAWN_EGG;
            case CHICKEN:
                return Material.CHICKEN_SPAWN_EGG;
            case MUSHROOM_COW:
                return Material.MOOSHROOM_SPAWN_EGG;
        }
        return Material.COW_SPAWN_EGG;
    }

    private long getTimeLeftMillis() {
        if(activator != null && entityType != null) {
            long now = System.currentTimeMillis();
            long shouldEnd = started + durationMillis;
            return shouldEnd - now;
        }
        return 0L;
    }

    private String getTimeLeft() {
        long timeLeft = getTimeLeftMillis();
        long minutes = timeLeft / 1000 / 60;
        if(minutes >= 1) return minutes + "min";
        else {
            long seconds = timeLeft / 1000;
            return seconds + "s";
        }
    }

    private boolean isActivated() {
        return getTimeLeftMillis() >= 1L;
    }

    private OfflinePlayer getActivator() {
        if(isActivated()) return Bukkit.getOfflinePlayer(activator);
        return null;
    }

}
