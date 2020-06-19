package me.tr.survival.main.commands;

import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Settings;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.managers.teleport.TeleportManager;
import me.tr.survival.main.managers.teleport.TeleportRequest;
import me.tr.survival.main.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public class TpaCommand implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(command.getLabel().equalsIgnoreCase("tpa")) {

                if(!player.getWorld().getName().equals("world")) {
                    Chat.sendMessage(player, "§7Tämä toimii vain tavallisessa maailmassa!");
                    return true;
                }

                if(args.length < 1) Chat.sendMessage(player, "Käyttö: §a/tpa <pelaaja>");
                else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                        return true;
                    }

                    if(Settings.get(target.getUniqueId(), "privacy")) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajalla §e" + target.getName() + " §7on yksityinen tila päällä!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        return true;
                    }

                    if(TeleportManager.getRequestsSentByPlayer(target.getUniqueId()).size() >= 1) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "§7Rauhoituthan noiden pyyntöjen kanssa! Pystyt lähettämään vain §ayhden §7teleport-pyynnön kerrallaan!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        return true;
                    }

                    TeleportRequest request = new TeleportRequest(player, target, TeleportManager.Teleport.REQUEST);
                    request.ask();
                }

            } else if(command.getLabel().equalsIgnoreCase("tpaccept")) {

                if(!player.getWorld().getName().equals("world")) {
                    Chat.sendMessage(player, "§7Tämä toimii vain tavallisessa maailmassa!");
                    return true;
                }

                if(TeleportManager.getRequestsFromRecipient(player.getUniqueId()).isEmpty()) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole aktiivisia §eTeleport§7-pyyntöjä tällä hetkellä!");
                    return true;
                }

                if(Util.isInRegion(player, "pvp-kuoppa")) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä ei toimi PvP-alueella!");
                    return true;
                }

                if(args.length < 1) {

                    // Get the first request found
                    TeleportRequest request = TeleportManager.getRequestsFromRecipient(player.getUniqueId()).get(0);
                    request.accept();

                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                        return true;
                    }

                    List<TeleportRequest> requests = TeleportManager.getRequestsFromRecipient(player.getUniqueId());
                    for(TeleportRequest request : requests) {
                        if(request.getRequestor().getUniqueId().equals(target.getUniqueId())) {
                            request.accept();
                            return true;
                        }
                    }
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei löydetty §eTeleport§7-pyyntöjä pelaajalta §a" + target.getName() + "§7...");
                }

            } else if(command.getLabel().equalsIgnoreCase("tpahere")) {

                if(!player.getWorld().getName().equals("world")) {
                    Chat.sendMessage(player, "§7Tämä toimii vain tavallisessa maailmassa!");
                    return true;
                }

                if(args.length < 1) Chat.sendMessage(player, "Käyttö: §a/tpahere");
                else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                        return true;
                    }
                    TeleportRequest request = new TeleportRequest(player, target, TeleportManager.Teleport.REQUEST);
                    request.ask(true);
                }

            } else if(command.getLabel().equalsIgnoreCase("tp")) {

                if(Ranks.isStaff(player.getUniqueId())) {

                    if(args.length < 1) Chat.sendMessage(player, "Käyttö: §a/tp (<pelaaja> | <x> <y> <z>) ([x] [y] [z] | [pelaaja2])");
                    else {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(args.length == 1) {
                            if(target == null) {
                                Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaaja ei löydetty!");
                                return true;
                            }
                            TeleportRequest request = new TeleportRequest(player, target, TeleportManager.Teleport.FORCE);
                            request.ask();
                        } else if(args.length == 2) {
                            Player target2 = Bukkit.getPlayer(args[1]);
                            if(target == null || target2 == null) {
                                Chat.sendMessage(player, Chat.Prefix.ERROR, "Varmista että kaikki pelaajat ovat paikalla!");
                                return true;
                            }
                            if(target.getName().equalsIgnoreCase(target2.getName())) {
                                Chat.sendMessage(player, Chat.Prefix.ERROR, "Et voi lähettää pelaajaa hänen omaan sijaintiinsa!");
                                return true;
                            }
                            TeleportRequest request = new TeleportRequest(target, target2, TeleportManager.Teleport.FORCE);
                            request.ask();
                        } else if(args.length == 3){

                            double x, y, z;
                            try {
                                x = Double.parseDouble(args[0]);
                                y = Double.parseDouble(args[1]);
                                z = Double.parseDouble(args[2]);
                            } catch(NumberFormatException ex) {
                                Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytäthän numeroita!");
                                return true;
                            }

                            Location loc = new Location(player.getWorld(), x, y, z);
                            player.teleport(loc);
                            Chat.sendMessage(player, "Sinua viedään sijaintiin §a" + ((int)x) + ", " + ((int)y) + ", " + ((int)z) + "§7!");

                        } else {
                            if(target == null) {
                                Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaaja ei löydetty!");
                                return true;
                            }
                            double x, y, z;
                            try {
                                x = Double.parseDouble(args[1]);
                                y = Double.parseDouble(args[2]);
                                z = Double.parseDouble(args[3]);
                            } catch(NumberFormatException ex) {
                                Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytäthän numeroita!");
                                return true;
                            }

                            Location loc = new Location(player.getWorld(), x, y, z);
                            target.teleport(loc);
                            Chat.sendMessage(player, "Pelaaja §a" + target.getName() + " §7viety sijaintiin §a" + ((int)x) + ", " + ((int)y) + ", " + ((int)z) + "§7!");
                        }
                    }
                } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
            } else if(command.getLabel().equalsIgnoreCase("tphere")) {
                if(Ranks.isStaff(player.getUniqueId())) {

                    if(args.length < 1) Chat.sendMessage(player, "Käyttö: §a/tphere <pelaaja>");
                    else {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaaja ei löydetty!");
                            return true;
                        }
                        TeleportRequest request = new TeleportRequest(player, target, TeleportManager.Teleport.FORCE);
                        request.ask(true);
                    }
                } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
            } else if(command.getLabel().equalsIgnoreCase("tpdeny")) {
                if(TeleportManager.getRequestsFromRecipient(player.getUniqueId()).isEmpty()) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole aktiivisia §eTeleport§7-pyyntöjä tällä hetkellä!");
                    return true;
                }
                if(args.length < 1) {
                    // Get the first request found
                    TeleportRequest request = TeleportManager.getRequestsFromRecipient(player.getUniqueId()).get(0);
                    request.deny();

                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                        return true;
                    }
                    List<TeleportRequest> requests = TeleportManager.getRequestsFromRecipient(player.getUniqueId());
                    for(TeleportRequest request : requests) {
                        if(request.getRequestor().getUniqueId().equals(target.getUniqueId())) {
                            request.deny();
                            return true;
                        }
                    }
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei löydetty Teleport-pyyntöjä pelaajalta §a" + target.getName() + "§7...");
                }
            }
        }
        return true;
    }
}

