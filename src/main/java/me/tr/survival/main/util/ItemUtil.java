package me.tr.survival.main.util;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemUtil {

    public static ItemStack makeSkullItem(String targetName, int amount, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, amount, (short)3);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwner(targetName);
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeSkullItem(String targetName, int amount, String displayName) {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, amount, (short)3);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwner(targetName);
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeSkullItem(String targetName, int amount) {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, amount, (short)3);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwner(targetName);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeSkullItem(String targetName) {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwner(targetName);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(Material mat, int amount, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        meta.setLore(lore);
        item.setAmount(amount);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(Material mat, int amount, String displayName) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        item.setAmount(amount);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(Material mat, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        return item;
    }

    public static ItemStack makeItem(Material mat) {
        return new ItemStack(mat);
    }

    public static ItemStack makeWoolItem(DyeColor color) {
        ItemStack wool = new ItemStack(Material.LEGACY_WOOL, 1, color.getDyeData());
        ItemMeta meta = wool.getItemMeta();
        wool.setItemMeta(meta);
        return wool;
    }

    public static ItemStack makeWoolItem(DyeColor color, String displayName) {
        ItemStack wool = new ItemStack(Material.LEGACY_WOOL, 1, color.getDyeData());
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        wool.setItemMeta(meta);
        return wool;
    }

    public static ItemStack makeWoolItem(DyeColor color, String displayName, List<String> lore) {
        ItemStack wool = new ItemStack(Material.LEGACY_WOOL, 1, color.getDyeData());
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        meta.setLore(lore);
        wool.setItemMeta(meta);
        return wool;
    }
}
