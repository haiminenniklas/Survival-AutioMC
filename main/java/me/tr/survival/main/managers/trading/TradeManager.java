package me.tr.survival.main.managers.trading;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.managers.Settings;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.managers.trading.events.TradeRequestSendEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeManager implements CommandExecutor, Listener {

    private boolean ENABLED = true;

    private final List<Trade> trades = new ArrayList<>();
    List<Trade> getOngoingTrades() { return trades; }

    private List<Trade> getTradesForPlayer(Player player) {

        final List<Trade> list = new ArrayList<>();
        for(final Trade trade : getOngoingTrades()) {
            if(trade == null) continue;
            if(trade.isParticipant(player)) list.add(trade);
        }
        return list;
    }

    private Trade getCurrentTrade(Player player) {
        for(final Trade trade : getOngoingTrades()) { if(trade.isParticipant(player) && trade.isOnGoing()) return trade; }
        return null;
    }

    private boolean isInTrade(Player player) {
        return getCurrentTrade(player) != null;
    }

    private boolean hasAsked(Player sender, Player target) {
        boolean hasAsked = false;
        for(Trade trade : getOngoingTrades()) {
            if(trade.getSender().getUniqueId().equals(sender.getUniqueId())
                    && trade.getTarget().getUniqueId().equals(target.getUniqueId())) hasAsked = true;
        }
        return hasAsked;
    }

    @Override
    public boolean onCommand(CommandSender cmdSender, Command command, String label, String[] args) {

        if(cmdSender instanceof Player && command.getLabel().equalsIgnoreCase("trade")) {

            Player player = (Player) cmdSender;

            if(!player.getWorld().getName().equals("world")) {
                Chat.sendMessage(player, "Vaihtokaupat toimivat vain tavallisessa maailmassa!");
                return true;
            }

            if(!player.isOp() && !ENABLED) {
                Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä. Yritähän myöhemmin uudelleen.");
                return true;
            }

            if(args.length < 1) Chat.sendMessage(player, "Käytä: §a/trade <pelaaja>");
            else {
                if(args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("hyväksy")) {
                    if(args.length < 2) {

                        // If there are no trade invitations...
                        if(getTradesForPlayer(player).size() < 1) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulle ei ole lähetetty yhtään vaihtokauppapyyntöä...");
                            return true;
                        }

                        // Get the first Trade in the list
                        Trade trade = getTradesForPlayer(player).get(0);

                        // If there are no trade invitations...
                        if(trade == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Kävi virhe etsiessä vaihtokauppatarjouksia. Yritäthän kohta uudestaan!");
                            return true;
                        } else trade.acceptRequest();

                    } else {

                        Player sender = Bukkit.getPlayer(args[1]);
                        if(sender == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                            return true;
                        }

                        // If there are no trade invitations...
                        if(getTradesForPlayer(player).size() < 1) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulle ei ole lähetetty yhtään vaihtokauppapyyntöä...");
                            return true;
                        }

                        boolean found = false;
                        for(Trade trade : getTradesForPlayer(player)) {
                            // If we have found the sender
                            if(trade.getSender().getUniqueId().equals(sender.getUniqueId())) {
                                trade.acceptRequest();
                                found = true;
                            }
                        }

                        // If no invitations were found with the given name...
                        if(!found) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pyyntöjä ei löydetty tuolla pelaajanimellä...");
                            return true;
                        }

                    }

                } else if(args[0].equalsIgnoreCase("kiellä") || args[0].equalsIgnoreCase("deny")) {

                    if(args.length < 2) {

                        // If there are no trade invitations...
                        if(getTradesForPlayer(player).size() < 1) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulle ei ole lähetetty yhtään vaihtokauppapyyntöä...");
                            return true;
                        }

                        // Get the first Trade in the list
                        Trade trade = getTradesForPlayer(player).get(0);

                        // If there are no trade invitations...
                        if(trade == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Kävi virhe etsiessä vaihtokauppatarjouksia. Yritäthän kohta uudestaan!");
                            return true;
                        } else {
                            trade.denyRequest();
                        }

                    } else {

                        Player sender = Bukkit.getPlayer(args[1]);
                        if(sender == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                            return true;
                        }

                        // If there are no trade invitations...
                        if(getTradesForPlayer(player).size() < 1) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulle ei ole lähetetty yhtään vaihtokauppapyyntöä...");
                            return true;
                        }

                        boolean found = false;
                        for(Trade trade : getTradesForPlayer(player)) {
                            // If we have found the sender
                            if(trade.getSender().getUniqueId().equals(sender.getUniqueId())) {
                                trade.denyRequest();
                                found = true;
                            }
                        }

                        // If no invitations were found with the given name...
                        if(!found) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pyyntöjä ei löydetty tuolla pelaajanimellä...");
                            return true;
                        }

                    }


                } else {

                    if(player.isOp()) {

                        if(args[0].equalsIgnoreCase("help")) {
                            Chat.sendMessage(player, "/trade (enable | disable)");
                            return true;
                        } else if(args[0].equalsIgnoreCase("disable")) {
                            ENABLED = false;
                            Chat.sendMessage(player, "Trade on nyt §cpois päältä§7!");
                        } else if(args[0].equalsIgnoreCase("enable")) {
                            ENABLED = true;
                            Chat.sendMessage(player, "Trade on nyt §apäällä§7!");
                        }
                        return true;

                    }

                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                        return true;
                    }
                    if(target.getName().equals(player.getName())) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Et voi lähettää vaihtokauppapyyntöä itsellesi!");
                        return true;
                    }
                    if(isInTrade(player)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Olet jo lähettänyt pyynnön tai olet jo vaihtokaupassa toisen pelaajan kanssa!");
                        return true;
                    }
                    if(isInTrade(target)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Kyseinen pelaaja on jo vaihtokaupassa tai hänellä on pyyntö hyväksymättä. Odotathan hetken.");
                        return true;
                    }
                    if(hasAsked(player, target)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Olet jo lähettänyt tälle pelaajalle vaihtokauppapyynnön. Odota kunnes nykyinen pyyntö umpeutuu!");
                        return true;
                    }
                    // If the given target has already asked our player for a trade, accept it
                    if(hasAsked(target, player)) Bukkit.dispatchCommand(player, "trade accept " + target.getName());
                    else {
                        Trade trade = new Trade(player, target);
                        trade.ask();
                    }
                }
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        // Just some normal checks
        if(e.getClickedInventory() == null) return;
        if(!e.getView().getTitle().contains("Vaihtokauppa")) return;

        int clickedSlot = e.getRawSlot(); // #getRawSlot(); gives the number we want

        // Check if player is in a trade
        if(isInTrade(player)) {
            final Trade trade = getCurrentTrade(player);

            final InventoryAction action = e.getAction();
            // Check for illegal inventory actions with trades
            for(InventoryAction illegal : trade.getIllegalActions()) {
                // Just cancel the event, if there's an illegal action and return
                if(action == illegal) {
                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);
                    return;
                }
            }

            // If the trade is in the 'confirming' state, no action
            // is allowed. (Except, denying the trade)
            if(trade.getState() == Trade.TradeState.CONFIRMING) {
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
            }

            // Just disable some obvious click types
            if(e.getClick() == ClickType.DOUBLE_CLICK || e.getClick() == ClickType.MIDDLE || e.getClick() == ClickType.CONTROL_DROP) {
                e.setCancelled(true);
                return;
            }

            // If the player clicked the bottom inventory (tries to place items)
            if(clickedSlot >= 54) {

                e.setCancelled(false); // By default, we don't want the player NOT to be able to click their own inventory

                // Here we're checking if the player tries to shift-click
                // their items onto the trading gui. By default, this might put the
                // items to the wrong side. We're trying to fix it here

                if(e.getClick() == ClickType.SHIFT_RIGHT || e.getClick() == ClickType.SHIFT_LEFT) {

                    if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        ItemStack item = e.getCurrentItem();
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
                        // Remove the item from the player ONLY if the item can be added to the trade
                        if(trade.addItem(player, item)) {
                            item.setAmount(0);
                            item.setType(Material.AIR);
                            e.getView().getBottomInventory().setItem(e.getSlot(), item);
                        }
                        return;
                    }

                }
            }

            // Check if player has clicked the player slots or other inventory
            // If the player did not click on the player slots, cancel the event

            boolean clickedPlayerSlots = false;
            boolean clickedSenderSlots = false; // This variable will be used to check which side the player clicked
            for(int i = 0; i < trade.getPlayerSlots().length; i++) {
                int[] playerSlots = trade.getPlayerSlots()[i];
                for(int j = 0; j < playerSlots.length; j++) {
                    int slot = playerSlots[j];
                    // Just checking if the player clicked the player slots (or item slots)
                    if(clickedSlot == slot) {
                        clickedPlayerSlots = true;
                        if(i == 0) clickedSenderSlots = true; // The player clicked the sender's side
                    }

                }
            }

            if(!clickedPlayerSlots) {
                // We want to check if the player clicked the GUI window, not their inventory
                if(clickedSlot < 54) e.setCancelled(true);

                if(clickedSlot == 48)
                    // Player presses the accept button
                    trade.accept(player);
                else if(clickedSlot == 50)
                    // Player presses the decline button
                    trade.deny();
                else if(clickedSlot == 49) {
                    // This is the new cancel button when confirming is on.
                    if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        if(trade.getState() == Trade.TradeState.CONFIRMING) trade.deny();
                    }
                }
            } else {

                // Now we need to check if the player has clicked their side of the inventory
                // If they're trying to click the wrong side, just cancel the event.

                // First, let's check if the player is the sender, if not, he's the target
                if(trade.getSender().getUniqueId().equals(player.getUniqueId())) {
                    if(clickedSlot < 54 && !clickedSenderSlots){
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
                        return;
                    }
                }
                // Now the other way around
                else {
                    if(clickedSlot < 54 && clickedSenderSlots){
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
                        return;
                    }
                }

                // We need to wait a couple of ticks, before we tell the trade that a new
                // item has been added.
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), (runnable) -> {
                    ItemStack item = e.getCurrentItem();
                    if(item != null && item.getType() != Material.AIR) trade.addItem(item, clickedSlot);
                }, 2);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        // If a player closes their current inventory, check
        // whether they're in a trade, if they are, cancel the trade
        if(isInTrade(player)) {
            Trade trade = getCurrentTrade(player);
            if(trade != null) trade.deny();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrag(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        // Dragging or "painting" the items onto the
        // trade gui is not allowed, as this causes many problems
        if(isInTrade(player)) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onTradeRequestSend(TradeRequestSendEvent e) {

        Trade trade = e.getTrade();
        if(trade == null) {
            e.setCancelled(true);
            return;
        }

        Player sender = e.getSender();
        Player target = e.getTarget();

        if(!Settings.get(target.getUniqueId(), "chat_mentions")) Util.sendNotification(target, "§a§lILMOITUS! Sait uuden vaihtokauppapyynnön!", true);

        target.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        target.sendMessage(" ");
        target.sendMessage(" §a§lUUSI VAIHTOKAUPPAPYYNTÖ!");
        target.sendMessage(" ");
        target.sendMessage(" §7Lähettäjä: §e" + sender.getName());
        target.sendMessage(" ");

        TextComponent accept = new TextComponent("  §a§lHYVÄKSY");
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa hyväksyäksesi!")));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade hyväksy " + sender.getName()));

        accept.addExtra(new TextComponent("  §8|  "));

        TextComponent deny = new TextComponent("§c§lKIELLÄ");
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa kieltääksesi!")));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + sender.getName()));
        accept.addExtra(deny);

        target.spigot().sendMessage(accept);

        target.sendMessage(" ");

        target.sendMessage(" §eUmpeutuu 60s päästä!");
        target.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        Chat.sendMessage(sender,"Lähetit vaihtokauppapyynnön pelaajalle §a" + target.getName() + "§7! Hänellä on §e60s §7aikaa vastata tähän pyyntöön.");

        Sorsa.after(60, () -> {
            // If nothing has happened to the trade request, remove it
            if(trade.getState() == Trade.TradeState.WAITING) trade.cancel();
        });

    }

}
