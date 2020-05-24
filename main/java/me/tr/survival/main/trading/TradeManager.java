package me.tr.survival.main.trading;

import lombok.Getter;
import me.tr.survival.main.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import java.util.ArrayList;
import java.util.List;

public class TradeManager implements CommandExecutor, Listener {

    private static final List<Trade> trades = new ArrayList<>();
    public static List<Trade> getTrades() { return trades; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        /*if(sender instanceof Player) {
            Chat.sendMessage(((Player)sender), Chat.Prefix.ERROR, "Ei käytössä!");
        }
        return true; */

        if(sender instanceof Player && command.getLabel().equalsIgnoreCase("trade")) {

            Player player = (Player) sender;

            if(!player.getWorld().getName().equals("world")) {
                Chat.sendMessage(player, "Vaihtokaupat toimivat vain tavallisessa maailmassa!");
                return true;
            }

            if(args.length < 1) {

                Chat.sendMessage(player, "Käytä: §a/trade <pelaaja>");

            } else {

                if(args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("hyväksy")) {



                } else if(args[0].equalsIgnoreCase("kiellä") || args[0].equalsIgnoreCase("deny")) {


                } else {

                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                        return true;
                    }

                    if(target.getName().equals(player.getName())) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Et voi lähettää vaihtokauppapyyntöä itsellesi!");
                        return true;
                    }


                }

            }


        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInvClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();


    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {

        Player player = (Player) e.getWhoClicked();



    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent e) {

        Player player = (Player) e.getPlayer();

    }

}
