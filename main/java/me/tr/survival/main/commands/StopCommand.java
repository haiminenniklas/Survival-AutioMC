package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getLabel().equalsIgnoreCase("stop")) {
            if(sender.isOp()) {
                Autio.stopServer();
            }
        } else if(command.getLabel().equalsIgnoreCase("forcestop")) {
            if(sender.isOp()) {

                for(Player player : Bukkit.getOnlinePlayers()) {
                   // player.kickPlayer("§cPalvelin sammui \n §7Palvelin käynnistyy uudelleen §anoin minuutin §7kuluttua! Nähdään taas pian!");
                    Autio.sendBungeeMessage(player, "Connect", "lobby");
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.shutdown();
                    }
                }.runTaskLater(Main.getInstance(), 20);
            }
        }

        return true;
    }
}
