package me.tr.survival.main.database.data;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.other.events.LevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@Deprecated
public class Level {

    public static int get(UUID player){
        if(!PlayerData.isLoaded(player)) PlayerData.loadNull(player, false);
        return Integer.parseInt(PlayerData.getValue(player, "level").toString());
    }

    public static void add(OfflinePlayer player, int value){
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);

        PlayerData.add(player.getUniqueId(), "level", value);
    }

    public static void set(OfflinePlayer player, int value) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }
        PlayerData.set(player.getUniqueId(), "level", value);
    }

    private static void levelUp(Player player){
        Level.add(player, 1);
        PlayerData.set(player.getUniqueId(), "xp", 0);
        LevelUpEvent event = new LevelUpEvent(player, Level.get(player.getUniqueId()));
        Bukkit.getPluginManager().callEvent(event);
    }


    public static String format(int level){
        return "§7[" + formatChat(level) + "§7]";
    }

    public static String formatChat(int level) {
        if(level <= 10) return "" + ChatColor.GRAY + level + "";
        else if( level <= 30) return "" + ChatColor.GREEN + level + "";
        else if(level <= 40) return "" + ChatColor.BLUE + level + "";
        else if(level <= 50) return "" + ChatColor.LIGHT_PURPLE + level + "";
        else if(level <= 60) return "" + ChatColor.YELLOW + level + "";
        else if(level <= 70) return "" + ChatColor.GOLD + level + "";
        else if(level <= 80) return "" + ChatColor.AQUA + level + "";
        else if(level <= 99) return "" + ChatColor.RED + level + "";
        else return "" + ChatColor.RED + ChatColor.BOLD + level + "";
    }


    private static int getXP(UUID player){
        return Integer.parseInt(PlayerData.getValue(player, "xp").toString());
    }

    public static void addXP(Player player, int value){

        UUID uuid = player.getUniqueId();

        int level = Level.get(player.getUniqueId());
        if(level >= 100) return;

        int xpNeeded = Util.roundInt(level) * 10;
        int currentXP = Level.getXP(player.getUniqueId());
        int xpLeft = xpNeeded - currentXP;


        PlayerData.add(player.getUniqueId(), "total_xp", value);

        if(value > xpLeft){

            Level.levelUp(player);
            Level.addXP(player, value - xpLeft);

        } else if(value == xpLeft){
            Level.levelUp(player);
        } else {
            PlayerData.add(uuid, "xp", value);
        }


    }

    private static float getXPToNextLevel(UUID player){
        float maxXP = Util.roundInt(Level.get(player)) * 10;
        if(maxXP % 10 <= 5 && maxXP % 10 > 0) maxXP += (10 - (maxXP % 10));
        return maxXP;
    }

    public static String getProgressText(UUID player, int length, ChatColor color){

        float currentXP = Level.getXP(player);
        float maxXP = Level.getXPToNextLevel(player);
        float percent = currentXP / maxXP;
        float blocksToColor = length * percent;

        StringBuilder text = new StringBuilder();
        for(int i = 0; i < length; i++){

            if(i < blocksToColor){
                text.append(color + ":");
            } else {
                text.append("§7:");
            }
        }

        return "§8[" + text.toString() + "§8]";

    }

}