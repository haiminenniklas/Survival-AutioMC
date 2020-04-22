package me.tr.survival.main.trading;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TradeManager implements CommandExecutor, Listener {

    public static List<Trade> trades = new ArrayList<>();

    public static List<Trade> getOngoingTrades() {
        return TradeManager.trades;
    }

    public static Trade findTrade(UUID uuid) {
        for(Trade trade : getOngoingTrades()) {
            if(trade.getTargetUUID().equals(uuid)) {
                return trade;
            } else if(trade.getTraderUUID().equals(uuid)) {
                return trade;
            }
        }
        return null;
    }

    public static Trade findTrade(UUID trader, UUID target) {
        for(Trade trade : getOngoingTrades()) {
            if(trade.getTraderUUID().equals(trader) && trade.getTargetUUID().equals(target)) {
                return trade;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        /*if(sender instanceof Player) {
            Chat.sendMessage(((Player)sender), Chat.Prefix.ERROR, "Ei käytössä!");
        }
        return true; */

        if(sender instanceof Player && command.getLabel().equalsIgnoreCase("trade")) {

            Player player = (Player) sender;

            if(args.length < 1) {

                Chat.sendMessage(player, "Käytä: §a/trade <pelaaja>");

            } else {

                if(args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("hyväksy")) {

                    if(args.length < 2) {
                        Trade trade = findTrade(player.getUniqueId());
                        if(trade == null || (trade != null && trade.getTargetUUID() != player.getUniqueId())) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei löydetty vaihtokauppapyyntöjä...");
                            return true;
                        }

                        trade.accept();

                    } else {

                        Player target = Bukkit.getPlayer(args[1]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                            return true;
                        }

                        Trade trade = findTrade(target.getUniqueId(), player.getUniqueId());
                        if(trade == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei löydetty vaihtokauppaa...");
                            return true;
                        }

                        trade.accept();

                    }

                } else if(args[0].equalsIgnoreCase("kiellä") || args[0].equalsIgnoreCase("deny")) {


                    if(args.length < 2) {
                        Trade trade = findTrade(player.getUniqueId());
                        if(trade == null || (trade != null && trade.getTargetUUID() != player.getUniqueId())) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei löydetty vaihtokauppapyyntöjä...");
                            return true;
                        }

                        trade.deny();
                    } else {

                        Player target = Bukkit.getPlayer(args[1]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                            return true;
                        }

                        Trade trade = findTrade(target.getUniqueId(), player.getUniqueId());
                        if(trade == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei löydetty vaihtokauppaa...");
                            return true;
                        }

                        trade.deny();

                    }

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

                    Trade trade = new Trade(player.getUniqueId(), target.getUniqueId());
                    trade.ask();

                }

            }


        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInvClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equals("Vaihtokauppa")) {

            Trade trade = TradeManager.findTrade(player.getUniqueId());
            if(trade == null) {
                e.setCancelled(true);
                player.closeInventory();
                return;
            }

            if(trade.getInventory() == null) {
                e.setCancelled(true);
                trade.close();
                return;
            }

            if(e.getAction() != InventoryAction.PLACE_ONE && e.getAction() != InventoryAction.PICKUP_ONE && e.getAction() != InventoryAction.PICKUP_SOME &&
                e.getAction() != InventoryAction.PICKUP_ALL && e.getAction() != InventoryAction.PICKUP_HALF && e.getAction() != InventoryAction.PLACE_ALL
                && e.getAction() != InventoryAction.PLACE_SOME) {
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
                return;
            }

            boolean isTrader = trade.getTraderUUID().equals(player.getUniqueId());
            boolean isTarget = trade.getTargetUUID().equals(player.getUniqueId());

            if(e.getClickedInventory() == null) return;
            Inventory inv = e.getClickedInventory();

            int slot = e.getRawSlot();
            if(slot == 19 || slot == 20 || slot == 24 || slot == 25 || slot == 28 || slot == 29 || slot == 33 || slot == 34 ||
                slot == 47 || slot == 49 || slot == 51) {

                if(slot == 19 || slot == 20 || slot == 24 || slot == 25 || slot == 28 || slot == 29 || slot == 33 || slot == 34) {

                    if(isTrader) {
                        if(slot == 24 || slot == 25 || slot == 33 || slot == 34) {
                            e.setCancelled(true);
                            e.setResult(Event.Result.DENY);
                            return;
                        }
                    } else if(isTarget) {
                        if(slot == 19 || slot == 20 || slot == 28 || slot == 29 ) {
                            e.setCancelled(true);
                            e.setResult(Event.Result.DENY);
                            return;
                        }
                    }

                    if(e.getCursor() == null) return;
                    // Do not allow the user to take his own stuff out of the inv
                    if(inv.getItem(slot) != null && inv.getItem(slot).getType() != Material.AIR) {
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
                        return;
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if(e.getCurrentItem() != null) {
                                ItemStack item = e.getCurrentItem().clone();
                                System.out.println("Current Item: " + item.getType() + ";" + item.getAmount());
                                trade.addItem(player, e.getCurrentItem());
                                cancel();
                            }
                        }
                    }.runTaskLater(Main.getInstance(), 2);


                } else {

                    e.setCancelled(true);

                    if(slot == 47) {
                        trade.acceptPlayer(player);
                    } else if(slot == 49) {
                        trade.returnItems(player);
                    } else {
                        trade.denyItems();
                    }

                }

            } else {

                if(e.getRawSlot() < e.getView().getTopInventory().getSize()) {
                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);
                }

            }

        }

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {

        Player player = (Player) e.getWhoClicked();

        if(e.getView().getTitle().equals("Vaihtokauppa")) {

            Trade trade = TradeManager.findTrade(player.getUniqueId());
            if(trade != null) {
                e.setCancelled(true);
            }

        }


    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent e) {

        Player player = (Player) e.getPlayer();
        if(TradeManager.findTrade(player.getUniqueId()) != null) {
            Trade trade = TradeManager.findTrade(player.getUniqueId());
            trade.close();
        }

    }

}
