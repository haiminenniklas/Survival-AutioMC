package me.tr.survival.main.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sun.management.OperatingSystemMXBean;
import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Settings;
import me.tr.survival.main.other.Enchant;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.callback.TypedCallback;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class Util {

    public static final HashMap<UUID, Long> joined = new HashMap<>();

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

    public static String formatTime(int seconds, boolean letters) {
        String text = "0" + seconds;
        if(seconds >= 10) {
            text = String.valueOf(seconds);
        }
        return text + (letters ? "s" : "");
    }

    public static String formatTime(int minutes, int seconds, boolean letters) {
        String text = "0" + minutes;
        if(minutes >= 10) {
            text = String.valueOf(minutes);
        }
        return text + (letters ? "min " : ":") + formatTime(seconds, letters);
    }

    public static String formatTime(int hours, int minutes, int seconds, boolean letters) {
        String text = "0" + hours;
        if(hours >= 10) {
            text = String.valueOf(hours);
        }
        return text + (letters ? "h " : ":") + formatTime(minutes, seconds, letters);
    }

    public static String formatTime(int days, int hours, int minutes, int seconds, boolean letters) {
        String text = "0" + days;
        if(days >= 10) {
            text = String.valueOf(days);
        }
        return text + (letters ? "d " : ":") + formatTime(hours, minutes, seconds, letters);
    }

    public static String getToday() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static String formatDate(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH)+1) + "." + calendar.get(Calendar.YEAR);
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

    public static <K, V extends Comparable<? super V>> void sortByValue(Map<K, V> map, TypedCallback<Map<K, V>> cb) {
        Sorsa.async(() -> {
            List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
            list.sort(Map.Entry.comparingByValue());
            Collections.reverse(list);

            Map<K, V> result = new LinkedHashMap<>();
            for (Map.Entry<K, V> entry : list) {
                if(entry == null) continue;
                result.put(entry.getKey(), entry.getValue());
            }
            cb.execute(result);
        });
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

    public static String createRegexFromGlob(String glob) {
        StringBuilder out = new StringBuilder("^");
        for(int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch(c) {
                case '*': out.append(".*"); break;
                case '?': out.append('.'); break;
                case '.': out.append("\\."); break;
                case '\\': out.append("\\\\"); break;
                default: out.append(c);
            }
        }
        out.append('$');
        return out.toString();
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
        sendNotification(player, message, !Settings.get(player.getUniqueId(), "chat_mentions"));
    }

    public static void sendNotification(Player player, String message, boolean sound){
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        if(sound) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
    }

    public static void broadcastSound(Sound sound) {
        broadcastSound(sound, 1, 1);
    }

    public static void broadcastSound(Sound sound, long volume, long pitch) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(!Settings.get(player.getUniqueId(), "chat_mentions")) player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static boolean isEntityTypeAlive(World world, EntityType type) {
        if(world == null) return false;
        for(Entity e : world.getEntities()) {
            if(e.getType() == type) return true;
        }
        return false;
    }

    public static List<Entity> getEntities(World world, EntityType type) {
        List<Entity> list = new ArrayList<>();
        if(world == null) return list;
        for(Entity e : world.getEntities()) {
            if(e.getType() == type) list.add(e);
        }
        return list;
    }

    public static void broadcast(String message) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(!Settings.get(player.getUniqueId(), "chat_mentions"))
                player.sendMessage(message);
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

    public static boolean isNumberInRange(int number, int min, int max) {
        return number <= max && number >= min;
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

    public static String formatDecimals(double amount) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(new Locale("fi", "FI"));
        return formatter.format(amount).replaceAll("\u00a0", " ");
    }

    public static double getFreeMemory() {
        DecimalFormat df = new DecimalFormat("#.##");
        return Float.parseFloat(df.format(Runtime.getRuntime().freeMemory() * 0.00000095367432));
    }

    public static double getMaxMemory() {
        DecimalFormat df = new DecimalFormat("#.##");
        return Float.parseFloat(df.format(Runtime.getRuntime().maxMemory() * 0.00000095367432));
    }

    public static double getUsedMemory() {
        return getMaxMemory() - getFreeMemory();
    }

    public static double getProcessCPULoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(osBean.getProcessCpuLoad()*100));
    }

    public static String formatLocation(Location loc, boolean blockLoc) {
        String text;
        if(blockLoc) text = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
        else text = loc.getX() + ", " + loc.getY() + ", " + loc.getZ();
        return text;
    }

    public static String formatLocation(Location loc) {
        return formatLocation(loc, true);
    }

    public static void checkForIllegalItems(Player player) {
        Sorsa.async(() -> {
            for(int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if(item == null) continue;
                if(item.getType() == Material.AIR) continue;
                if(Util.isIllegalItem(item)) {
                    final int slot = i;
                    Sorsa.task(() -> player.getInventory().setItem(slot, new ItemStack(Material.AIR)));
                }
            }
        });
    }

    public static Material[] getMusicDiscs() {
        return new Material[] {
                Material.MUSIC_DISC_13,
                Material.MUSIC_DISC_11,
                Material.MUSIC_DISC_BLOCKS,
                Material.MUSIC_DISC_CAT,
                Material.MUSIC_DISC_CHIRP,
                Material.MUSIC_DISC_FAR,
                Material.MUSIC_DISC_MALL,
                Material.MUSIC_DISC_MELLOHI,
                Material.MUSIC_DISC_PIGSTEP,
                Material.MUSIC_DISC_STAL,
                Material.MUSIC_DISC_WARD,
                Material.MUSIC_DISC_WAIT
        };
    }

    public static int getLoadedChunksAmount() {
        int total = 0;
        for(World w : Bukkit.getWorlds()) {
            total += w.getLoadedChunks().length;
        }
        return total;
    }

    public static boolean isIllegalItem(ItemStack item) {

        if(item != null && item.getType() != Material.AIR) {
            if(item.getType() == Material.BARRIER) return true;
            ItemMeta meta = item.getItemMeta();
            if(meta != null && item.hasItemMeta()) {
                if(meta.hasDisplayName() && meta.hasLore()) {
                    if(!Main.getMoneyManager().isCheque(item) && !Main.getClaimBlockCouponsManager().isCoupon(item)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static void changeSlots(int slots) throws ReflectiveOperationException {
        Method serverGetHandle = Bukkit.getServer().getClass().getDeclaredMethod("getHandle");

        Object playerList = serverGetHandle.invoke(Bukkit.getServer());
        Field maxPlayersField = playerList.getClass().getSuperclass().getDeclaredField("maxPlayers");

        maxPlayersField.setAccessible(true);
        maxPlayersField.set(playerList, slots);
    }

    public static  void updateServerProperties() {
        Properties properties = new Properties();
        File propertiesFile = new File("server.properties");

        try {
            try (InputStream is = new FileInputStream(propertiesFile)) {
                properties.load(is);
            }

            String maxPlayers = Integer.toString(Bukkit.getServer().getMaxPlayers());

            if (properties.getProperty("max-players").equals(maxPlayers)) {
                return;
            }

            Bukkit.getLogger().info("Saving max players to server.properties...");
            properties.setProperty("max-players", maxPlayers);

            try (OutputStream os = new FileOutputStream(propertiesFile)) {
                properties.store(os, "Minecraft server properties");
            }
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while updating the server properties", e);
        }
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

    public static void sendClickableText(Player player, String text, String command, String hoverText) {
        TextComponent comp = new TextComponent(text);
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));
        player.spigot().sendMessage(comp);
    }

    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) { deleteDir(f); }
        }
        file.delete();
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
    /* // If you have Apache Commons Text, you can use it to calculate the edit distance:
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static Location getGroundLocation(Location loc) {
        double y = loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
        return new Location(loc.getWorld(), loc.getX(), y + 1, loc.getZ());
    }

    public static Material ChatColorToDye(ChatColor color) {
        switch(color) {
            case GREEN:
                return Material.GREEN_DYE;
            case BLUE:
                return Material.BLUE_DYE;
            case AQUA:
                return Material.LIGHT_BLUE_DYE;
            case RED:
                return Material.RED_DYE;
            case LIGHT_PURPLE:
                return Material.PINK_DYE;
            case YELLOW:
                return Material.YELLOW_DYE;
            case BLACK:
                return Material.BLACK_DYE;
            case GOLD:
                return Material.ORANGE_DYE;
            default:
                return Material.GRAY_DYE;
        }
    }

    public static boolean isBlockStair(Block block) { return block.getType().name().contains("_STAIRS"); }

    public static String locationToText(Location loc) {

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        float yaw = loc.getYaw();
        float pitch = loc.getPitch();

        World world = loc.getWorld();

        return world.getName() + ";" + x + ";" + y + ";" + z + ";" + yaw + ";" + pitch;

    }

    public static Location textToLocation(String text) {

        String[] values = text.split(";");

        double x = Double.parseDouble(values[1]);
        double y = Double.parseDouble(values[2]);
        double z = Double.parseDouble(values[3]);

        float yaw = Float.parseFloat(values[4]);
        float pitch = Float.parseFloat(values[5]);

        World world = Bukkit.getWorld(values[0]);

        return new Location(world, x, y, z, yaw, pitch);

    }

    public static void teleportHorse(Player player, Location loc) {
        if(player.getVehicle() != null) {
            Entity vehicle = player.getVehicle();
            vehicle.eject();
            vehicle.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    public static List<String> toStringList(List<?> list) {
        List<String> newList = new ArrayList<>();
        for(Object obj : list) {
            newList.add(obj.toString());
        }
        return newList;
    }

    public static int getEntityCount() {
        int total = 0;
        for(World w : Bukkit.getWorlds()) {
            total += w.getEntities().size();
        }
        return total;
    }

    public static int getAmountOfMaterialInInventory(Inventory inv, Material mat) {
        int amount = 0;

        for(ItemStack item : inv.getContents()) {
            if(item == null || item.getType() == Material.AIR) continue;
            if(item.getType() == mat) amount += item.getAmount();
        }

        return amount;
    }

    public static void removeItems(Inventory inventory, Material type, int amount) {
        if (amount <= 0) return;
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is == null) continue;
            if (type == is.getType()) {
                int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }

    public static String translateChatColor(ChatColor color) {
        switch(color) {
            case GREEN:
                return "Vihreä";
            case BLUE:
                return "Sininen";
            case AQUA:
                return "Turkoosi";
            case RED:
                return "Punainen";
            case LIGHT_PURPLE:
                return "Violetti";
            case YELLOW:
                return "Keltainen";
            case BLACK:
                return "Musta";
            case GOLD:
                return "Oranssi";
            default:
                return "";
        }
    }

    public static String firstLetterCapital(String text) {
        return StringUtils.capitalize(text);
    }

    public static RegionManager getRegionManager(World world) {
        RegionContainer container = Sorsa.getWorldGuard().getPlatform().getRegionContainer();
        return container.get(convertWorldGuardWorld(world));
    }

    public static com.sk89q.worldedit.world.World convertWorldGuardWorld(World world) { return BukkitAdapter.adapt(world); }

    public static com.sk89q.worldedit.util.Location convertWorldGuardLocation(Location loc) { return BukkitAdapter.adapt(loc); }

    public static Set<ProtectedRegion> getRegions(final Block block) { return getRegions(block.getLocation()); }

    public static Set<ProtectedRegion> getRegions(final Location loc) {
        final RegionManager rgm = getRegionManager(loc.getWorld());
        final ApplicableRegionSet ars = rgm.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        return ars.getRegions();
    }

    public static Set<ProtectedRegion> getRegions(final Player player) { return getRegions(player.getLocation()); }

    public static boolean isInRegion(final Location loc, final String id) {
        for(ProtectedRegion rg : getRegions(loc)) {
            if(rg.getId().equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    public static boolean isInRegion(final Player player, String id) {
        for(ProtectedRegion rg : getRegions(player)) {
            if(rg.getId().equalsIgnoreCase(id)) return true;
        }
        return false;
    }

}
