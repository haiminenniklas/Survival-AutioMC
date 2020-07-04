package me.tr.survival.main.other;

import me.tr.survival.main.Sorsa;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Ranks {

    public static String getRank(UUID uuid) {

        if(Sorsa.getLuckPerms() != null) {
            if(Sorsa.getLuckPerms().getUserManager() != null) {
                if(Sorsa.getLuckPerms().getUserManager().getUser(uuid) != null) {
                    if(Sorsa.getLuckPerms().getUserManager().getUser(uuid).getPrimaryGroup() != null) {
                        return Sorsa.getLuckPerms().getUserManager().getUser(uuid).getPrimaryGroup();
                    }
                }

            }

        }
        return "default";
    }

    @Deprecated
    public static String getPrefix(String rank) {
        switch(rank) {
            case "default":
                return "";
            case "premium":
                return "§ePremium";
            case "premiumplus":
                return "§a§lPremium§2§l+";
            case "youtube":
                return "§c§lYOUTUBE";
            case "twitch":
                return "§5§lTWITCH";
            case "mod":
                return "§3§lMOD";
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
                return "§ePremium";
            case "premiumplus":
                return "§6Premium§e+";
            case "sorsa":
                return "§2§lSORSA";
            case "youtube":
                return "§cYOUTUBE";
            case "twitch":
                return "§5TWITCH";
            case "mod":
                return "§3Moderaattori";
            case "rakentaja":
                return "§2Rakentaja";
            case "admin":
                return "§cYlläpitäjä";
            case "omistaja":
                return "§dOmistaja";
            default:
                return "§7Pelaaja";

        }
    }

    public static ChatColor getRankColor(String rank) {
        switch(rank) {
            case "default":
                return ChatColor.WHITE;
            case "premium":
                return ChatColor.YELLOW;
            case "premiumplus":
                return ChatColor.GOLD;
            case "sorsa":
                return ChatColor.GREEN;
            case "youtube":
                return ChatColor.DARK_RED;
            case "twitch":
                return ChatColor.DARK_PURPLE;
            case "mod":
                return ChatColor.AQUA;
            case "builder":
                return ChatColor.YELLOW;
            case "admin":
                return ChatColor.RED;
            case "omistaja":
                return ChatColor.LIGHT_PURPLE;
            default:
                return ChatColor.WHITE;

        }
    }

    public static boolean hasRank(UUID uuid, String rank) {
        if(Bukkit.getOfflinePlayer(uuid).isOp()) return true;
        User user = Sorsa.getLuckPerms().getUserManager().getUser(uuid);
        if(user == null) {
            return false;
        }
        return (user.getPrimaryGroup().equalsIgnoreCase(rank));
    }

    public static boolean hasRank(UUID uuid, String... ranks) {
        for(String rank : ranks) {
            if(hasRank(uuid, rank)) return true;
        }
        return false;
    }

    public static boolean isVIP(UUID uuid) {
        if(isStaff(uuid)) return true;
        if(isPartner(uuid)) return true;
        return hasRank(uuid, "premium") || hasRank(uuid, "premiumplus") || hasRank(uuid, "sorsa");
    }
    public static boolean isPartner(UUID uuid) {
        if(isStaff(uuid)) return true;
        return hasRank(uuid, "youtube") || hasRank(uuid, "twitch");
    }

    public static boolean isStaff(UUID uuid) {
        return hasRank(uuid, "admin") || hasRank(uuid, "mod") || hasRank(uuid, "rakentaja") || hasRank(uuid, "harjoittelija"); }

    public static boolean hasRank(Player player, String rank) {
        if(player.isOp()) return true;
        return hasRank(player.getUniqueId(), rank);
    }

}
