package me.tr.survival.main.commands;

import me.tr.survival.main.Chat;
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

        sender.sendMessage("§cEi käytössä!");

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(!player.isOp()) {

                Chat.sendMessage(player, "Arvosi on: " + Ranks.getDisplayName(Ranks.getRank(player.getUniqueId())));

            } else {

                if(args.length == 0 || (args.length >= 1 && args[0].equalsIgnoreCase("help"))) {

                    Chat.sendMessage(player, "/rank get <player>");
                    Chat.sendMessage(player, "/rank set <player> <rank>");

                } else if(args.length == 2 && args[0].equalsIgnoreCase("get")) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                    Chat.sendMessage(player, "Pelaajan " + target.getName() + " rank: " + Ranks.getRank(target.getUniqueId()));

                } else if(args.length == 3) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if(args[0].equalsIgnoreCase("set")) {

                        String rank = args[2].toLowerCase();
                        if(!PlayerData.isLoaded(target.getUniqueId())) {
                            PlayerData.loadNull(target.getUniqueId(),  false);
                        }

                        PlayerData.set(target.getUniqueId(), "rank", rank);
                        Chat.sendMessage(player, "Pelaajan " + target.getName() + " rankki vaihdettu!");

                    }

                }

            }


        }

        return false;
    }
}
