package me.tr.survival.main.other.backpacks;

import me.tr.survival.main.Chat;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.data.Crystals;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
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
                        Backpack.upgrade(player);
                   }

                }

            }

        }

        return true;
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
