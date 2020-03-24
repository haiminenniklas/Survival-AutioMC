package me.tr.survival.main.commands;

import me.tr.survival.main.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuctionCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("huutokauppa")) {

                if(args.length < 1) {
                    player.performCommand("ah");
                } else {

                    if(args[0].equalsIgnoreCase("myy")) {

                        if(args.length >= 2) {
                            player.performCommand("ah sell " + args[1]);
                        } else {
                            Chat.sendMessage(player, "Käytä §a/huutokauppa myy <hinta>");
                        }

                    } else if(args[0].equalsIgnoreCase("avaa")) {
                        player.performCommand("ah");
                    } else if(args[0].equalsIgnoreCase("vanhentuneet")) {
                        player.performCommand("ah expired");
                    } else if(args[0].equalsIgnoreCase("palauta")) {
                        player.performCommand("ah return");
                    } else if(args[0].equalsIgnoreCase("myydyt")) {
                        player.performCommand("ah sold");
                    }

                }

            }

        }

        return true;
    }
}
