package me.tr.survival.main.util;

import java.util.List;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.util.callback.TypedCallback;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemUtil {

    public static ItemStack makeSkullItem(OfflinePlayer target, int amount, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void makeSkullItem(OfflinePlayer target, int amount, String displayName, List<String> lore, TypedCallback<ItemStack> cb) {
        Sorsa.async(() -> {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, amount);
            SkullMeta meta = (SkullMeta)item.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
            cb.execute(item);
        });
    }

    public static ItemStack makeSkullItem(OfflinePlayer target, int amount, String displayName) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeSkullItem(OfflinePlayer target, int amount) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwningPlayer(target);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeSkullItem(OfflinePlayer target) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwningPlayer(target);
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

}
