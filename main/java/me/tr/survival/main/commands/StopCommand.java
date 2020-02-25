package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getLabel().equalsIgnoreCase("stop")) {
            if(sender.isOp()) {
                Autio.stopServer();
            }
        } else if(command.getLabel().equalsIgnoreCase("forcestop")) {
            if(sender.isOp()) {
                Bukkit.shutdown();
            }
        }

        return true;
    }
}
