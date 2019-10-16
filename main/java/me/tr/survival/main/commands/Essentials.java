package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.other.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class Essentials implements CommandExecutor, Listener {

    public static List<UUID> afk = new ArrayList<UUID>();
    public static Map<UUID, Long> moved = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if(cmd.getLabel().equalsIgnoreCase("afk")) {

                Chat.sendMessage(player, Chat.Prefix.ERROR, "Komento ei käytössä!");

              /*  if(afk.contains(uuid)) {
                    Chat.sendMessage(player, "Et ole enää AFK!");
                    Bukkit.broadcastMessage("§6§l» §7Pelaaja §6" + player.getName() + "§7 ei ole enää AFK!");
                } else {
                    Chat.sendMessage(player, "Olet nyt AFK!");
                    Bukkit.broadcastMessage("§6§l» §7Pelaaja §6" + player.getName() + "§7 on nyt AFK");
                } */

            } else if(cmd.getLabel().equalsIgnoreCase("apua")) {

            } else if(cmd.getLabel().equalsIgnoreCase("broadcast")) {

                if(Ranks.isStaff(uuid)) {

                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < args.length; i++) {
                        sb.append(args[i] + " ");
                    }

                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', sb.toString()));

                }

            } else if(cmd.getLabel().equalsIgnoreCase("discord")) {

                Chat.sendMessage(player, "Discord: §6https://discord.gg/TBrTmZn");

            } else if(cmd.getLabel().equalsIgnoreCase("clear")) {

                if(Ranks.isStaff(uuid)) {

                }

            } else if(cmd.getLabel().equalsIgnoreCase("kordinaatit")) {

            } else if(cmd.getLabel().equalsIgnoreCase("world")) {

            }

        }

        return true;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();

        // Has moved a block
        if(from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ() || from.getBlockY() != to.getBlockY()) {

            if(afk.contains(player.getUniqueId())) {
                Bukkit.broadcastMessage("§6§l» §7Pelaaja §6" + player.getName() + "§7 ei ole enää AFK!");
            }

            moved.put(player.getUniqueId(), System.currentTimeMillis());
            Autio.afterAsync(120, () -> {
                long currentTime = System.currentTimeMillis();

                if(moved.containsKey(player.getUniqueId())) {

                    long lastMoved = moved.get(player.getUniqueId());

                    // If not moved in more than 2 minutes
                    if((currentTime - lastMoved) / 1000 / 60 / 60 >= 120 && Bukkit.getPlayer(player.getUniqueId()) != null) {
                        afk.add(player.getUniqueId());
                        Bukkit.broadcastMessage("§6§l» §7Pelaaja §6" + player.getName() + "§7 on nyt AFK");
                    }

                }

            });

        }

    }

}
