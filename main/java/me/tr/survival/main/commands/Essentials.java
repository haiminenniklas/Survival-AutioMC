package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.*;
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

                Gui gui = new Gui("Apua", 27);

                gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Hyödylliset komennot", Arrays.asList(
                        "§7§m--------------------",
                        " §7Hyödylliset komennot:",
                        "  §6/profiili §7tämä valikko",
                        "  §6/rtp §7vie sinut arämaahan",
                        "  §6/msg §7yksityisviestit",
                        "  §6/tpa §7teleporttauspyyntö",
                        "  §6/warp §7palvelimen warpit",
                        "  §9/discord §7Discord-yhteisö",
                        "  §6/vaihda §7vaihtokauppa",
                        "  §a/osta §7verkkokauppa",
                        "§7§m--------------------"
                )), 13);

                gui.open(player);

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
                    Util.clearInventory(player);
                    Chat.sendMessage(player, "Inventorysi tyhjennettiin!");
                }

            } else if(cmd.getLabel().equalsIgnoreCase("kordinaatit")) {

                if(Ranks.isStaff(uuid)) {
                    Location loc = player.getLocation();
                    Chat.sendMessage(player, "Sijaintisi: §a" + loc.getX() + "§7, §a" + loc.getY() + "§7, §a" + loc.getZ() + " §7-> §a" + loc.getWorld().getName());
                }

            } else if(cmd.getLabel().equalsIgnoreCase("world")) {

                if(Ranks.isStaff(uuid)) {

                    player.sendMessage("§7§m---------------------");
                    player.sendMessage(" §7Maailmat (" + Bukkit.getWorlds().size() +  "):");

                    for(World w : Bukkit.getWorlds()) {
                        if(player.getWorld().getName().equalsIgnoreCase(w.getName())) {
                            player.sendMessage("§7- §6" + w.getName() + " §8(sinä)");
                        } else {
                            player.sendMessage("§7- §6" + w.getName());
                        }
                    }

                    player.sendMessage("§7§m---------------------");

                }

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