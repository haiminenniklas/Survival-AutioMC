package me.tr.survival.main.other;

import me.tr.survival.main.database.PlayerData;
import org.bukkit.ChatColor;

import java.util.UUID;

public class Ranks {

    public static final String[] RANKS = new String[] {

            "default",
            "premium",
            "premiumplus",
            "youtube",
            "twitch",
            "valvoja",
            "builder",
            "admin"

    };

    public static String getRank(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        return String.valueOf(PlayerData.getValue(uuid, "rank"));
    }

    public static String getPrefix(String rank) {
        switch(rank) {
            case "default":
                return "";
            case "premium":
                return "§6§lPremium";
            case "premiumplus":
                return "§6§lPremium§6§l+";
            case "youtube":
                return "§c§lYOUTUBE";
            case "twitch":
                return "§5§lTWITCH";
            case "valvoja":
                return "§3§lValvoja";
            case "builder":
                return "§2§lRakentaja";
            case "admin":
                return "§c§lYlläpitäjä";
            default:
                return "";

        }
    }

    public static String getDisplayName(String rank) {
        switch(rank) {
            case "default":
                return "§7Pelaaja";
            case "premium":
                return "§6Premium";
            case "premiumplus":
                return "§6Premium§6+";
            case "youtube":
                return "§cYOUTUBE";
            case "twitch":
                return "§5TWITCH";
            case "valvoja":
                return "§3Valvoja";
            case "builder":
                return "§2Rakentaja";
            case "admin":
                return "§cYlläpitäjä";
            default:
                return "";

        }
    }

    public static ChatColor getRankColor(String rank) {
        switch(rank) {
            case "default":
                return ChatColor.GRAY;
            case "premium":
                return ChatColor.GOLD;
            case "premiumplus":
                return ChatColor.GOLD;
            case "youtube":
                return ChatColor.DARK_RED;
            case "twitch":
                return ChatColor.DARK_PURPLE;
            case "valvoja":
                return ChatColor.DARK_AQUA;
            case "builder":
                return ChatColor.DARK_GREEN;
            case "admin":
                return ChatColor.RED;
            default:
                return ChatColor.GRAY;

        }
    }

    public static boolean hasRank(UUID uuid, String rank) {
        return getRank(uuid).equals(rank.toLowerCase());
    }

    public static boolean isVIP(UUID uuid) {
        if(isStaff(uuid)) return true;
        return hasRank(uuid, "premium") || hasRank(uuid, "premiumplus");
    }

    public static boolean isPartner(UUID uuid) {
        if(isStaff(uuid)) return true;
        return hasRank(uuid, "youtube") || hasRank(uuid, "twitch");
    }

    public static boolean isStaff(UUID uuid) { return hasRank(uuid, "admin") || hasRank(uuid, "valvoja") || hasRank(uuid, "builder"); }

}
