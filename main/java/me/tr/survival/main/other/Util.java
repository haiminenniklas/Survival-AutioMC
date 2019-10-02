package me.tr.survival.main.other;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {

    public static void fixItem(ItemStack item) {
        if(!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        ((Damageable)meta).setDamage(0);
        item.setItemMeta(meta);
    }

    public static String getToday() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static int roundInt(int value){
        return (int) (Math.rint((double) value / 10) * 10);
    }

    public static boolean isSmeltable(Material material) {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();

        while(iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (!(recipe instanceof FurnaceRecipe)) continue;
            if (((FurnaceRecipe) recipe).getInput().getType() != material) continue;

            return true;
        }

        return false;

    }

    public static ItemStack smelt(ItemStack item) {

        if(isSmeltable(item.getType())) {

            Iterator<Recipe> iterator = Bukkit.recipeIterator();

            while(iterator.hasNext()) {
                Recipe recipe = iterator.next();
                if (!(recipe instanceof FurnaceRecipe)) continue;
                if (((FurnaceRecipe) recipe).getInput().getType() != item.getType()) continue;

                ItemStack result = recipe.getResult();
                result.setAmount(item.getAmount());
                if(item.hasItemMeta())
                    result.setItemMeta(item.getItemMeta());


                return result;
            }

        }

        return null;
    }

    public static boolean isMineralOre(Block block) {
        if(block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE ||
                block.getType() == Material.GOLD_ORE || block.getType() == Material.IRON_ORE ||
                block.getType() == Material.COAL_ORE || block.getType() == Material.LAPIS_ORE ||
                block.getType() == Material.REDSTONE_ORE) {
            return true;
        }
        return false;
    }

    public static String[] splitPreservingWords(String text, int length) {
        return text.replaceAll("(?:\\s*)(.{1,"+ length +"})(?:\\s+|\\s*$)", "$1\n").split("\n");
    }

    public static void heal(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        removePotionEffects(player);
    }
    public static void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
    }

    public static void removePotionEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
    }

    public static ItemStack makeSplashPotion(PotionType type, int amount){
        ItemStack item = new ItemStack(Material.POTION, amount);
        Potion pot = new Potion(type);
        pot.setSplash(true);
        pot.apply(item);
        return item;
    }

    public static ItemStack makeEnchanted(ItemStack item) {
        item = makeEnchanted(item, new Enchant(Enchantment.ARROW_DAMAGE, 1));
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeEnchanted(Material mat, int amount, Enchant... enchantments){
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        for(Enchant ench : enchantments){
            meta.addEnchant(ench.getEnchantment(), ench.getLevel(), true);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeEnchanted(ItemStack item, Enchant... enchantments){
        ItemMeta meta = item.getItemMeta();
        for(Enchant ench : enchantments){
            meta.addEnchant(ench.getEnchantment(), ench.getLevel(), true);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeEnchanted(ItemStack item, ItemMeta old_meta, Enchant... enchantments){
        item.setItemMeta(old_meta);
        ItemMeta meta = item.getItemMeta();
        for(Enchant ench : enchantments){
            meta.addEnchant(ench.getEnchantment(), ench.getLevel(), true);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeEnchanted(Material mat, int amount, String displayName, List<String> lore, Enchant... enchantments){
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        for(Enchant ench : enchantments){
            meta.addEnchant(ench.getEnchantment(), ench.getLevel(), true);
        }
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void replaceItem(Player player, Material mat1, ItemStack item2, boolean first) {
        for(int i = 0; i<player.getInventory().getSize()-1; ++i)
        {
            ItemStack item = player.getInventory().getItem(i);
            if(item.getType().equals(mat1))
            {
                player.getInventory().setItem(i, item2);
                player.updateInventory();
                if(first) break;
            }
        }
    }

    public static void bounceBack(Player player, Location from, Location to) {
        Vector direction = from.toVector().subtract(to.toVector());
        direction.setY(0);
        player.setVelocity(direction.multiply(2));
    }

    public static double round(double round){
        double rounded = Math.round(round * 10.0D) / 10.0D;
        return rounded;
    }

    public static float round(float round){
        float rounded = Math.round(round * 10.0F) / 10.0F;
        return rounded;
    }

    public static void sendNotification(Player player, String message){

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));

    }

    public static void sendNotification(Player player, String message, boolean sound){

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        if(sound) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
        }

    }

    public static void broadcastSound(Sound sound) {
        broadcastSound(sound, 1, 1);
    }

    public static void broadcastSound(Sound sound, long volume, long pitch) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            int ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
            return ping;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String[] splitStringEvery(String s, int interval) {
        int arrayLength = (int) Math.ceil(((s.length() / (double)interval)));
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        } //Add the last bit
        result[lastIndex] = s.substring(j);

        return result;
    }


}
