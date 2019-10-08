package me.tr.survival.main.commands;

import me.tr.survival.main.Chat;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.teleport.TeleportManager;
import me.tr.survival.main.util.teleport.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;

public class TpaCommand implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(command.getLabel().equalsIgnoreCase("tpa")) {

                if(args.length < 1) {

                    Chat.sendMessage(player, "Käyttö: §a/tpa <pelaaja>");

                } else {

                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                        return true;
                    }

                    TeleportRequest request = new TeleportRequest(player, target, TeleportManager.Teleport.REQUEST);
                    request.ask();


                }

            } else if(command.getLabel().equalsIgnoreCase("tpaccept")) {

                if(TeleportManager.getRequestsFromRecipient(player.getUniqueId()).isEmpty()) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole aktiivisia Teleport-pyyntöjä tällä hetkellä!");
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

                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei löydetty Teleport-pyyntöjä pelaajalta §6" + target.getName() + "§7...");

                }

            } else if(command.getLabel().equalsIgnoreCase("tpahere")) {

                if(args.length < 1) {

                    Chat.sendMessage(player, "Käyttö: §6/tpahere");

                } else {
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

                    if(args.length < 1) {
                        Chat.sendMessage(player, "Käyttö: §6/tp <pelaaja>");
                    } else {

                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaaja ei löydetty!");
                            return true;
                        }

                        TeleportRequest request = new TeleportRequest(player, target, TeleportManager.Teleport.FORCE);
                        request.ask();

                    }

                } else {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
                }

            } else if(command.getLabel().equalsIgnoreCase("tphere")) {
                if(Ranks.isStaff(player.getUniqueId())) {

                    if(args.length < 1) {
                        Chat.sendMessage(player, "Käyttö: §6/tphere <pelaaja>");
                    } else {

                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaaja ei löydetty!");
                            return true;
                        }

                        TeleportRequest request = new TeleportRequest(player, target, TeleportManager.Teleport.FORCE);
                        request.ask(true);

                    }

                } else {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
                }
            } else if(command.getLabel().equalsIgnoreCase("tpdeny")) {
                if(TeleportManager.getRequestsFromRecipient(player.getUniqueId()).isEmpty()) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole aktiivisia Teleport-pyyntöjä tällä hetkellä!");
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

                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei löydetty Teleport-pyyntöjä pelaajalta §6" + target.getName() + "§7...");

                }
            }



        }

        return true;
    }

}
