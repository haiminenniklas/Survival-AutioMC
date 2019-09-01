package me.tr.survival.main.util.data;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.events.LevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Level {

    public static int get(UUID player){
        if(!PlayerData.isLoaded(player)) {
            PlayerData.loadNull(player, false);
        }
        return Integer.parseInt(PlayerData.getValue(player, "level").toString());
    }

    public static void add(OfflinePlayer player, int value){
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }
        PlayerData.add(player.getUniqueId(), "level", value);
    }

    public static void set(OfflinePlayer player, int value) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }
        PlayerData.set(player.getUniqueId(), "level", value);
    }

    public static void levelUp(Player player){
        Level.add(player, 1);
        PlayerData.set(player.getUniqueId(), "xp", 0);
        LevelUpEvent event = new LevelUpEvent(player, Level.get(player.getUniqueId()));
        Bukkit.getPluginManager().callEvent(event);
    }


    public static String format(int level){

        if(level <= 10){
            return "§7[" + ChatColor.GRAY + level + "§7]";
        } else if(level > 10 && level <= 30){
            return "§7[" + ChatColor.GREEN + level + "§7]";
        } else if(level > 30 && level <= 40){
            return "§7[" + ChatColor.BLUE + level + "§7]";
        } else if(level > 40 && level <= 50){
            return "§7[" + ChatColor.LIGHT_PURPLE + level + "§7]";
        } else if(level > 50 && level <= 60){
            return "§7[" + ChatColor.YELLOW + level + "§7]";
        } else if(level > 60 && level <= 70){
            return "§7[" + ChatColor.GOLD + level + "§7]";
        } else if(level > 70 && level <= 80){
            return "§7[" + ChatColor.AQUA + level + "§7]";
        } else if(level > 80 && level <= 99){
            return "§7[" + ChatColor.RED + level + "§7]";
        } else if(level > 99){
            return "§7[" + ChatColor.RED + ChatColor.BOLD + level + "§7]";
        }

        return "§7[" + ChatColor.GRAY + level + "§7]";

    }

    public static int getXP(UUID player){
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

    public static float getXPToNextLevel(UUID player){
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