package me.tr.survival.main.trading;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player && command.getLabel().equalsIgnoreCase("trade")) {

            Player player = (Player) sender;


        }

        return true;
    }
}
