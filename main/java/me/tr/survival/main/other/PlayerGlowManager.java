package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Particles;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.UUID;

public class PlayerGlowManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("hehku")) {

                if(args.length < 1) {
                    toggle(player);
                } else {

                    if(!player.isOp()) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                        return true;
                    }

                    Chat.sendMessage(player, (toggle(target) ? "Hehku on §apäällä" : "Hehku on §cpois päältä") + " pelaajalla §a" + target.getName());

                }

            }

        }

        return true;
    }

    public static boolean toggle(Player player) {
        if(!Ranks.isStaff(player.getUniqueId()) && !Ranks.hasRank(player, "sorsa")) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän toimintoon tarvitset §2§lSORSA§7-arvon! Lisätietoa §a/kauppa§7!");
            return false;
        }

        if(!hasGlowEnabled(player)) {
            enableGlow(player);
            return true;
        } else {
            disableGlow(player);
            return false;
        }
    }

    public static void enableGlow(Player player) {

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        PlayerData.set(player.getUniqueId(), "glow_effect", true);
        player.setGlowing(true);

    }

    public static void disableGlow(Player player) {

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        PlayerData.set(player.getUniqueId(), "glow_effect", false);
        player.setGlowing(false);

    }

    public static boolean hasGlowEnabled(Player player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }
        return (boolean) PlayerData.getValue(player.getUniqueId(), "glow_effect");
    }

}
