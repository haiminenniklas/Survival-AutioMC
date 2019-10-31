package me.tr.survival.main.trading;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeManager implements CommandExecutor, Listener {

    public static List<Trade> trades = new ArrayList<>();

    public static List<Trade> getOngoingTrades() {
        return TradeManager.trades;
    }

    public static Trade findTrade(UUID uuid) {
        for(Trade trade : getOngoingTrades()) {
            if(trade.getTargetUUID().equals(uuid) || trade.getTraderUUID().equals(uuid)) {
                return trade;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player && command.getLabel().equalsIgnoreCase("trade")) {

            Player player = (Player) sender;


        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equalsIgnoreCase("Vaihtokauppa")) {

            Trade trade = TradeManager.findTrade(player.getUniqueId());
            boolean isTrader = trade.getTraderUUID().equals(player.getUniqueId());

            if(e.getClickedInventory() == null) return;
            Inventory inv = e.getClickedInventory();

            int slot = e.getSlot();
            if(slot == 19 || slot == 20 || slot == 24 || slot == 25 || slot == 28 || slot == 29 || slot == 33 || slot == 34 ||
                slot == 47 || slot == 49 || slot == 51) {

                if(slot == 19 || slot == 20 || slot == 24 || slot == 25 || slot == 28 || slot == 29 || slot == 33 || slot == 34) {

                    if(e.getCurrentItem() == null) return;

                    if(isTrader && (slot == 28 || slot == 29 || slot == 33 || slot == 34)) {
                        e.setCancelled(true);
                        return;
                    } else if(!isTrader && (slot == 19 || slot == 20 || slot == 24 || slot == 25 )) {
                        e.setCancelled(true);
                        return;
                    } else {

                        // Do not allow the user to take his own stuff out of the inv
                        if(inv.getItem(slot) != null || inv.getItem(slot).getType() != Material.AIR) {
                            e.setCancelled(true);
                            return;
                        }

                        Player oppositePlayer = (isTrader) ? trade.getTarget() : trade.getTrader();
                        if(oppositePlayer.getOpenInventory().getTitle().equalsIgnoreCase("Vaihtokauppa")) {

                            player.updateInventory();
                            Inventory oppositeInv = oppositePlayer.getOpenInventory().getTopInventory();
                            oppositeInv.setItem(slot, e.getCurrentItem());
                            oppositePlayer.updateInventory();

                        } else {
                            trade.close();
                        }

                    }


                } else {

                    if(slot == 47) {

                    } else if(slot == 49) {

                    } else {

                    }

                }

            } else {
                e.setCancelled(true);
                return;
            }

        }

    }

}
