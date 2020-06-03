package me.tr.survival.main.commands;

import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.data.Homes;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(!player.isOp()) {
                Homes.panel(player, player);
            } else {
                if(args.length >= 1) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                    if(!PlayerData.isLoaded(target.getUniqueId())) {
                        Chat.sendMessage(player, "Pelaajan §a" + target.getName() + " §7koteja ei ole ladattu. Tee §a/debug load "
                                + target.getName() + " §7ja kokeile uudestaan!");
                        return true;
                    } else {
                        Homes.panel(player, target);
                    }

                } else {
                    Homes.panel(player, player);
                }
            }
        }

        return false;
    }
}
