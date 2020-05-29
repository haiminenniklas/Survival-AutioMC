package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
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
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Houkutin implements CommandExecutor {

    // Command

    private static boolean ENABLED;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length < 1) {
            if(sender instanceof Player) {
                panel((Player) sender);
            }
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

    private static final long durationMillis = 1000 * 60 * 60;
    private static final long durationMinutes = durationMillis / 1000 / 60;

    // Variables

    private static UUID activator;
    private static long started;
    private static EntityType entityType;

    public static void panel(Player player) {

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
            lore.add(" §7Hinta: §ealk. 10 000€");
            lore.add(" ");

            if(isActivated()) {

                lore.add(" §7Aktivoinut: §a" + getActivator().getName());
                lore.add(" §7Aikaa jäljellä: §a" + getTimeLeft());
                lore.add(" ");

            } else {
                lore.add(" §a§lKlikkaa aktivoidaksesi");
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

                        if(Balance.canRemove(clicker.getUniqueId(), 10000)) {

                            gui.close(clicker);
                            selectGui(clicker);

                        } else {
                            clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        }

                    } else {
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    }

                }
            });

        });

    }

    private static void selectGui(Player player) {

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

                            if(Balance.canRemove(clicker.getUniqueId(), 10000)) {

                                Balance.remove(clicker.getUniqueId(), 10000);
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

                        } else {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Houkutin on jo aktivoitu!");
                        }

                    }
                });

                slot += 1;
                if(slot > 15) break;

            }

        });

    }

    private static void activate() {

        if(!ENABLED) return;

        Block block = Bukkit.getWorld("world").getBlockAt(-16, 59, -33);
        block.setType(Material.SPAWNER);
        BlockState state = block.getState();
        CreatureSpawner spawner = (CreatureSpawner) state;
        spawner.setSpawnCount(4);
        spawner.setSpawnedType(entityType);
        state.update();

    }

    public static void deactivate() {
        activator = null;
        entityType = null;
        Block block = Bukkit.getWorld("world").getBlockAt(-16, 59, -33);
        block.setType(Material.EMERALD_BLOCK);
    }

    public static void activateManager() {

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

    private static String translateEntityType(EntityType type) {
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

    private static double getPrice(EntityType type) {
        switch (type) {
            case COW:
                return 10000;
            case PIG:
                return 10000;
            case SHEEP:
                return 10000;
            case CHICKEN:
                return 10000;
            case MUSHROOM_COW:
                return 10000;
        }
        return 10000;
    }

    private static EntityType[] allowedEntityTypes() {
        EntityType[] entities = new EntityType[] {
                EntityType.COW,
                EntityType.PIG,
                EntityType.SHEEP,
                EntityType.CHICKEN,
                EntityType.MUSHROOM_COW
        };
        return entities;
    }

    private static Material getSpawnEggMaterial(EntityType type) {
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

    public static long getTimeLeftMillis() {
        if(activator != null && entityType != null) {
            long now = System.currentTimeMillis();
            long shouldEnd = started + durationMillis;
            return shouldEnd - now;
        }
        return 0L;
    }

    private static String getTimeLeft() {
        long timeLeft = getTimeLeftMillis();
        long minutes = timeLeft / 1000 / 60;
        if(minutes >= 1) {
            return minutes + "min";
        } else {
            long seconds = timeLeft / 1000;
            return seconds + "s";
        }
    }

    private static boolean isActivated() {
        return getTimeLeftMillis() >= 1L;
    }

    private static OfflinePlayer getActivator() {
        if(isActivated()) {
            return Bukkit.getOfflinePlayer(activator);
        }
        return null;
    }

}
