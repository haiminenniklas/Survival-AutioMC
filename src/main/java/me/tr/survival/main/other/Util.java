package me.tr.survival.main.other;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class Util {

    public static String getToday() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static int roundInt(int value){
        return (int) (Math.rint((double) value / 10) * 10);
    }

    public static void heal(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
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

    public static double round(double round){
        double rounded = Math.round(round * 10.0D) / 10.0D;
        return rounded;
    }

    public static float round(float round){
        float rounded = Math.round(round * 10.0F) / 10.0F;
        return rounded;
    }

    public static void sendNotification(Player player, String message){

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));

    }

}
