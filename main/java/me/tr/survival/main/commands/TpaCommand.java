package me.tr.survival.main.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class TpaCommand implements CommandExecutor, Listener {

    public static HashMap<Player, Player> tpaRequests = new HashMap<>();



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(command.getLabel().equalsIgnoreCase("tpa")) {

            } else if(command.getLabel().equalsIgnoreCase("tpaccept")) {

            } else if(command.getLabel().equalsIgnoreCase("tpahere")) {

            }


        }

        return true;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        Location from = e.getFrom(), to = e.getTo();

        // If player moves a block
        if(from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ() || from.getBlockX() != to.getBlockX()) {



        }

    }

}

