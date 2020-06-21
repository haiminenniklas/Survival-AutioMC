package me.tr.survival.main.commands;

import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.managers.Settings;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import me.tr.survival.main.managers.StaffManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.*;

public class Essentials implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if(cmd.getLabel().equalsIgnoreCase("broadcast")) {
                if(Ranks.isStaff(uuid)) {
                    if(args.length >= 1) {
                        StringBuilder sb = new StringBuilder();
                        for(int i = 0; i < args.length; i++) {
                            sb.append(args[i] + " ");
                        }
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "➤ &2&lILMOITUS " + sb.toString()));
                    } else Chat.sendMessage(player, "Täytyyhän sinun hyvä ihminen kirjoittaakin jotain! Käytä §a/broadcast <viesti>§7!");
                }
            } else if(cmd.getLabel().equalsIgnoreCase("clear")) {

                if(!Main.getStaffManager().hasStaffMode(player)) {
                    Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                    return true;
                }

                if(args.length < 1) {
                    if(Ranks.isStaff(uuid)) {
                        Util.clearInventory(player);
                        Chat.sendMessage(player, "Inventorysi tyhjennettiin!");
                    }
                } else {
                    if(Ranks.isStaff(uuid)) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                            return true;
                        }
                        Util.clearInventory(target);
                        Chat.sendMessage(target, "Pelaajan §a" + target.getName() + " §7inventory tyhjennettiin!");
                    }
                }

            } else if(cmd.getLabel().equalsIgnoreCase("world")) {

                if(Ranks.isStaff(uuid)) {

                    if(args.length < 1) {
                        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                        player.sendMessage(" §7Maailmat (" + Bukkit.getWorlds().size() +  "):");

                        for(World w : Bukkit.getWorlds()) {
                            if(player.getWorld().getName().equalsIgnoreCase(w.getName())) player.sendMessage("§7- §a" + w.getName() + " §8(sinä)");
                            else player.sendMessage("§7- §a" + w.getName());
                        }

                        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    } else {
                        String worldName = args[0];
                        World w = Bukkit.getWorld(worldName);
                        if(w != null) {
                            Chat.sendMessage(player, "Viedään maailmaan §a" + w.getName() + "§7...");
                            player.teleport(w.getSpawnLocation());
                        } else Chat.sendMessage(player, "Maailmaa ei löydetty...");
                    }
                }
            } else if(cmd.getLabel().equalsIgnoreCase("invsee")) {

                if(Ranks.isStaff(player.getUniqueId())) {

                    if(!Main.getStaffManager().hasStaffMode(player)) {
                        Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                        return true;
                    }

                    if(args.length < 1) Chat.sendMessage(player, "Käytä §a/invsee <pelaaja>");
                    else {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                            return true;
                        }
                        invsee(player, target);
                    }

                } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");

            }
        }
        return true;
    }

    private void invsee(Player opener, Player target) {
        opener.openInventory(target.getInventory());
    }

    @Deprecated
    public static ItemStack createCustomXPBottle(int experienceAmount) {

        ItemStack item = Util.makeEnchanted(ItemUtil.makeItem(Material.EXPERIENCE_BOTTLE, 1, "§a§lXP-Pullo", Arrays.asList(
                "§7Tämä pullo sisältää",
                "§d" + experienceAmount,
                "§7kokemusta! Klikkaa tätä itemiä",
                "§7saadaksesi kokemukset!"
        )));

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "xp-amount");
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getCustomTagContainer().setCustomTag(key, ItemTagType.INTEGER, experienceAmount);
        item.setItemMeta(itemMeta);

        return item;

    }

    @Deprecated
    private static int getExperienceFromCustomXPBottle(ItemStack bottle) {
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "xp-amount");
        ItemMeta itemMeta = bottle.getItemMeta();
        CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();

        if(tagContainer.hasCustomTag(key, ItemTagType.INTEGER)) {
            return tagContainer.getCustomTag(key, ItemTagType.INTEGER);
        }

        return 0;

    }

}
