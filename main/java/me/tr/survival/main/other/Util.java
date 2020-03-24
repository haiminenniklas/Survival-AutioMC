package me.tr.survival.main.other;

import com.sun.management.OperatingSystemMXBean;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Util {

    public static HashMap<UUID, Long> joined = new HashMap<>();

    public static long getWhenLogged(UUID player) {
        if (joined.containsKey(player)) {
            return joined.get(player);
        }
        return 0L;
    }

    public static void fixItem(ItemStack item) {
        if(!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        ((Damageable)meta).setDamage(0);
        item.setItemMeta(meta);
    }

    public static void broadcastStaff(String message) {

        for(Player online : Bukkit.getOnlinePlayers()) {
            if(Ranks.isStaff(online.getUniqueId())) {
                online.sendMessage(message);
            }
        }

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

    public static String getOreDisplayName(Material mat) {

        switch(mat) {
            case DIAMOND_ORE:
                return "§b§lTimantti";
            case EMERALD_ORE:
                return "§a§lEmerald";
            case GOLD_ORE:
                return "§6§lKulta";
            case IRON_ORE:
                return "§f§lRauta";
            case COAL_ORE:
                return "§8§lHiili";
            case LAPIS_ORE:
                return "§9§lLapis";
            case REDSTONE_ORE:
                return "§4§lRedstone";
            default:
                return "§7Muu";
        }

    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);


        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
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

    public static double getFreeMemory() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        return round(osBean.getFreePhysicalMemorySize() * 0.00000095367432);
    }

    public static double getMaxMemory() {

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        return round(osBean.getTotalPhysicalMemorySize() * 0.00000095367432);
    }

    public static double getProcessCPULoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(osBean.getProcessCpuLoad()*100));
    }

    public static double getSystemCPULoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(osBean.getSystemCpuLoad()*100));
    }

    /**
     * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
     *
     * @param playerInventory to turn into an array of strings.
     * @return Array of strings: [ main content, armor content ]
     * @throws IllegalStateException
     */
    public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
        //get the main content part, this doesn't return the armor
        String content = toBase64(playerInventory);
        String armor = itemStackArrayToBase64(playerInventory.getArmorContents());

        return new String[] { content, armor };
    }

    /**
     *
     * A method to serialize an {@link ItemStack} array to Base64 String.
     *
     * <p />
     *
     * Based off of {@link #toBase64(Inventory)}.
     *
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     * @throws IllegalStateException
     */
    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * A method to serialize an inventory to Base64 string.
     *
     * <p />
     *
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     *
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     *
     * @param inventory to serialize
     * @return Base64 string of the provided inventory
     * @throws IllegalStateException
     */
    public static String toBase64(Inventory inventory) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(inventory.getSize());

            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     *
     * A method to get an {@link Inventory} from an encoded, Base64, string.
     *
     * <p />
     *
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     *
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     *
     * @param data Base64 string of data containing an inventory.
     * @return Inventory created from the Base64 string.
     * @throws IOException
     */
    public static Inventory fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    /**
     * Gets an array of ItemStacks from Base64 string.
     *
     * <p />
     *
     * Base off of {@link #fromBase64(String)}.
     *
     * @param data Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     * @throws IOException
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public static void sendClickableText(Player player, String title, String command, String hoverText) {
        TextComponent comp = new TextComponent(title);

        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));

        player.spigot().sendMessage(comp);
    }

}
