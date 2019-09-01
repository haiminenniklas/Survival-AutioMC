package me.tr.survival.main.commands;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(!player.isOp()) {

                player.sendMessage("§c§lAutio §7» Arvosi on: " + Ranks.getDisplayName(Ranks.getRank(player.getUniqueId())));

            } else {

                if(args.length == 0 || (args.length >= 1 && args[0].equalsIgnoreCase("help"))) {

                    player.sendMessage("§c/rank get <player>");
                    player.sendMessage("§c/rank set <player> <rank>");

                } else if(args.length == 2 && args[0].equalsIgnoreCase("get")) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                    player.sendMessage("§cPelaajan " + target.getName() + " rank: " + Ranks.getRank(target.getUniqueId()));

                } else if(args.length == 3) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if(args[0].equalsIgnoreCase("set")) {

                        String rank = args[2].toLowerCase();
                        if(!PlayerData.isLoaded(target.getUniqueId())) {
                            PlayerData.loadNull(target.getUniqueId(),  false);
                        }

                        PlayerData.set(target.getUniqueId(), "rank", rank);
                        player.sendMessage("§cPelaajan " + target.getName() + " rankki vaihdettu!");

                    }

                }

            }


        }

        return false;
    }
}
