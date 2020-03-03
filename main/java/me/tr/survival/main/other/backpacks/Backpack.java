package me.tr.survival.main.other.backpacks;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Backpack implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getLabel().equalsIgnoreCase("reppu")) {

            if(sender instanceof Player) {

                Player player = (Player) sender;

                if(args.length < 1) {
                    openBackpack(player);
                } else {

                   if(args[0].equalsIgnoreCase("päivitä") || args[0].equalsIgnoreCase("upgrade")) {
                        Backpack.upgradeConfirm(player);
                   }

                   if(args.length >= 2) {

                       if(Ranks.isStaff(player.getUniqueId())) {
                           if(args[0].equalsIgnoreCase("katso")) {

                               OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                               if(!PlayerData.isLoaded(target.getUniqueId())) {

                                   Chat.sendMessage(player, "Pelaajaa ei löydetty... Yritetään ladata tiedot tietokannasta");

                                   Autio.async(() -> {
                                       boolean result = PlayerData.loadPlayer(target.getUniqueId());
                                       if(result) {
                                           openOther(player, target);
                                       } else {
                                           Chat.sendMessage(player, "Pelaajaa ei löydetty... Ei voida avata reppua!");
                                       }
                                   });

                               } else {
                                   openOther(player, target);
                               }

                           }
                       } else {
                           Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
                       }

                   }

                }

            }

        }

        return true;
    }

    public static void openOther(Player opener, OfflinePlayer target) {

        ItemStack[] items = getSavedInventory(target.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, "Tarkastele reppua (" + target.getName() + ")");

        for(ItemStack item : items) {
            if(item == null) continue;
            if(item.getType() == Material.AIR) continue;
            inv.addItem(item);
        }

        opener.openInventory(inv);

    }

    public static void openBackpack(Player player) {

        UUID uuid = player.getUniqueId();

        Level level =  getLevel(player.getUniqueId());

        ItemStack[] items = getSavedInventory(uuid);
        Inventory inv = Bukkit.createInventory(null, level.size, "Reppu (" + level.displayName + "§8)");

        for(ItemStack item : items) {
            if(item == null) continue;
            if(item.getType() == Material.AIR) continue;
            inv.addItem(item);
        }

        player.openInventory(inv);


    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInvClose(InventoryCloseEvent e) {

        Player player = (Player) e.getPlayer();

        Inventory inv = e.getInventory();
        if(e.getView().getTitle().startsWith("Reppu")) {

            saveInventory(player.getUniqueId(), inv.getContents());
            //Chat.sendMessage(player, "Reppusi tallennettiin!");

        } else if(e.getView().getTitle().startsWith("Tarkastele reppua")) {

            String title = e.getView().getTitle();

            String playerName = title.substring(title.indexOf('('));
            playerName = playerName.replace(")", "");
            playerName = playerName.replace("(", "");

            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

            saveInventory(target.getUniqueId(), inv.getContents());

            Chat.sendMessage(player, "Tallennettiin pelaajan §6" + playerName + " §7reppu!");

        }

    }

    public static String getRawSavedInventory(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        return String.valueOf(PlayerData.getValue(uuid, "backpack_inventory"));
    }

    public static void setRawSavedInventory(UUID uuid, String inv) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        PlayerData.set(uuid, "backpack_inventory", inv);
    }

    public static ItemStack[] getSavedInventory(UUID uuid) {

        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        String raw = getRawSavedInventory(uuid);
        if(raw.equalsIgnoreCase("null")) {
            return new ItemStack[0];
        } else {
            try {
                return Util.itemStackArrayFromBase64(raw);
            } catch(IOException ex) {
                ex.printStackTrace();
            }

        }

        return new ItemStack[0];

    }

    public static void saveInventory(UUID uuid, ItemStack[] inv) {

        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        String raw = Util.itemStackArrayToBase64(inv);
        setRawSavedInventory(uuid, raw);

    }

    public static Level getLevel(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        return Level.valueOf(String.valueOf(PlayerData.getValue(uuid, "backpack_level")));

    }

    public static void setLevel(UUID uuid, Level level) {

        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        PlayerData.set(uuid, "backpack_level", level.toString());
    }

    public static void upgradeConfirm(Player player) {

        UUID uuid = player.getUniqueId();
        Level current = getLevel(uuid);

        int price = 300;
        if(current == Level.TWO) {
            price = 450;
        }

        final int finalPrice = price;

        Gui.openGui(player, "Päivitä reppusi", 27, (gui) -> {

            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahivsta", Arrays.asList(
                    "§7§m--------------------",
                    " §7Klikkaa vahvistaaksesi päivityksen!",
                    " §7Päivitys maksaa: §b" + finalPrice + " kristallia§7!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {

                    upgrade(clicker);
                    gui.close(player);

                }
            });

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                    "§7§m--------------------",
                    "§7 Klikkaa peruuttaaksesi päivityksen!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {

                    gui.close(clicker);

                }
            });

        });

    }

    public static void upgrade(Player player) {

        UUID uuid = player.getUniqueId();
        Level current = getLevel(uuid);

        int price = 300;
        if(current == Level.TWO) {
            price = 450;
        }

        if(Crystals.canRemove(uuid, price)) {

            Crystals.add(uuid, -price);

            if(addLevel(uuid)) {
                Chat.sendMessage(player, "Reppusi päivitettiin! Avaa reppusi komennolla §6/reppu");
            } else {
                Chat.sendMessage(player, "Reppusi on jo ylimmällä tasolla!");
            }

        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän! Päivitys maksaa §b§l" + price + " kristallia§7!");
        }


    }

    public static boolean addLevel(UUID uuid) {

        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        Level current = getLevel(uuid);
        Level newLevel = null;
        if(current == Level.ONE) {
            newLevel = Level.TWO;
        } else if(current == Level.TWO) {
            newLevel = Level.THREE;
        } else {
            return false;
        }

        setLevel(uuid, newLevel);
        return true;

    }


    public enum Level {

        ONE(9, "§a§lLEVEL 1"),
        TWO(27, "§e§lLEVEL 2"),
        THREE(54, "§c§lLEVEL 3");

        private int size;
        private String displayName;

        Level(int size, String displayName) {

            this.size = size;
            this.displayName = displayName;

        }

    }

}
