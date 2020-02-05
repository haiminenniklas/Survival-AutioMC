package me.tr.survival.main.trading;

import me.tr.survival.main.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Deprecated
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

        if(sender instanceof Player) {
            Chat.sendMessage(((Player)sender), Chat.Prefix.ERROR, "Ei käytössä!");
        }
        return true;

        /*if(sender instanceof Player && command.getLabel().equalsIgnoreCase("trade")) {

            Player player = (Player) sender;

            if(args.length < 1) {

                Chat.sendMessage(player, "Käytä: §e/trade <pelaaja>");

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

                    Trade trade = new Trade(player.getUniqueId(), target.getUniqueId());
                    trade.ask();

                }

            }


        }

        return true; */
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        if(e.getView().getTitle().contains("Vaihtokauppa")) {

            System.out.println(1);
            Trade trade = TradeManager.findTrade(player.getUniqueId());
            if(trade == null) {
                e.setCancelled(true);
                player.closeInventory();
                return;
            }
            boolean isTrader = trade.getTraderUUID().equals(player.getUniqueId());

            if(e.getClickedInventory() == null) return;
            Inventory inv = e.getClickedInventory();

            if(e.getCurrentItem() == null) return;

            int slot = e.getSlot();
            if(slot == 19 || slot == 20 || slot == 24 || slot == 25 || slot == 28 || slot == 29 || slot == 33 || slot == 34 ||
                slot == 47 || slot == 49 || slot == 51) {


                System.out.println(2);

                if(slot == 19 || slot == 20 || slot == 24 || slot == 25 || slot == 28 || slot == 29 || slot == 33 || slot == 34) {

                    System.out.println(2);

                    if(isTrader && (slot == 28 || slot == 29 || slot == 33 || slot == 34)) {
                        System.out.println("(3)");
                        e.setCancelled(true);
                    } else if(!isTrader && (slot == 19 || slot == 20 || slot == 24 || slot == 25 )) {
                        System.out.println("(3)");
                        e.setCancelled(true);
                    } else {
                        System.out.println("5");

                        // Do not allow the user to take his own stuff out of the inv
                        if(inv.getItem(slot) != null || inv.getItem(slot).getType() != Material.AIR) {
                            System.out.println("(6)");
                            e.setCancelled(true);
                            return;
                        }

                        Player oppositePlayer = (isTrader) ? trade.getTarget() : trade.getTrader();
                        if(oppositePlayer.getOpenInventory().getTitle().contains("Vaihtokauppa")) {
                            System.out.println("7");
                            trade.addItem(player, e.getCurrentItem());
                        } else {
                            System.out.println("(8)");
                            trade.close();
                        }

                    }


                } else {

                    e.setCancelled(true);

                    if(slot == 47) {
                        System.out.println("(9)");
                        trade.acceptPlayer(player);
                    } else if(slot == 49) {
                        System.out.println("(10)");
                        trade.returnItems(player);
                    } else {
                        System.out.println("(11)");
                        trade.denyItems();
                    }

                }

            } else {
                System.out.println("(12)");
                if(e.getRawSlot() == e.getSlot()) {
                    System.out.println("(13)");
                    e.setCancelled(true);
                }


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
