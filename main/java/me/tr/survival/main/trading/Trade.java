package me.tr.survival.main.trading;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.trading.events.TradeRequestSendEvent;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Trade {

    private Player sender, target;
    private TradeState state;
    private Map<UUID, Boolean> accepted;
    private Inventory inv;
    private int[][] playerSlots;

    private final InventoryAction[] illegalActions;

    public Trade(Player sender, Player target) {
        this.sender = sender;
        this.target = target;
        this.state = TradeState.DEFAULT;
        this.accepted = new HashMap<>();

        this.playerSlots = new int[][] {
                new int[] {
                        10,11,12,
                        19,20,21,
                        28,29,30,
                },
                new int[] {
                        14,15,16,
                        23,24,25,
                        32,33,34
                }
        };

        this.accepted.put(sender.getUniqueId(), false);
        this.accepted.put(target.getUniqueId(), false);

        this.illegalActions = new InventoryAction[] {
                InventoryAction.HOTBAR_SWAP,
                InventoryAction.CLONE_STACK,
                InventoryAction.UNKNOWN,
                InventoryAction.COLLECT_TO_CURSOR,
                InventoryAction.SWAP_WITH_CURSOR
        };

        this.inv = Bukkit.createInventory(null, 54, "Vaihtokauppa - Käynnissä");
        this.initGui();

        TradeManager.getOngoingTrades().add(this);

    }

    private void initGui() {

        // Setup glass
        int[][] glassSlots = new int[][] {
                // Sender color glass
                new int[] { 9,18,27 },
                // Target color glass
                new int[] { 17,26,35 },
                // Background class
                new int[] { 0,1,2,3,4,5,6,7,8, 13,22,31, 36,37,38,39,40,41,42,43,44 }
        };

        // Place glass
        for(int i = 0; i < glassSlots.length; i++) {
            for(int j = 0; j < glassSlots[i].length; j++) {
                Material mat;
                if(i == 0) mat = Material.LIME_STAINED_GLASS_PANE;
                else if(i == 1) mat = Material.YELLOW_STAINED_GLASS_PANE;
                else mat = Material.GRAY_STAINED_GLASS_PANE;
                inv.setItem(glassSlots[i][j], new ItemStack(mat, 1));
            }
        }

        // Place default player heads
        inv.setItem(45, ItemUtil.makeSkullItem(this.sender.getName(), 1, "§a" + this.sender.getName(), Arrays.asList(
                "§cEi hyväksynyt",
                " ",
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(sender.getUniqueId())) + "€"
        )));

        inv.setItem(53, ItemUtil.makeSkullItem(this.target.getName(), 1, "§e" + this.target.getName(), Arrays.asList(
                "§cEi hyväksynyt",
                " ",
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(target.getUniqueId())) + "€"
        )));

        // Buttons
        inv.setItem(48, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§aHyväksy"));
        inv.setItem(50, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§cKieltäydy"));

    }

    public void updateGui() {

        // Place player heads
        inv.setItem(45, ItemUtil.makeSkullItem(this.sender.getName(), 1, "§a" + this.sender.getName(), Arrays.asList(
                (this.hasAccepted(this.sender) ? "§aHyväksynyt"  : "§cEi hyväksynyt"),
                " ",
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(sender.getUniqueId())) + "€"
        )));

        inv.setItem(53, ItemUtil.makeSkullItem(this.target.getName(), 1, "§e" + this.target.getName(), Arrays.asList(
                (this.hasAccepted(this.target) ? "§aHyväksynyt"  : "§cEi hyväksynyt"),
                " ",
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(target.getUniqueId())) + "€"
        )));

        // The glass slots
        int[][] glassSlots = new int[][] {
                // Sender color glass
                new int[] { 9,18,27 },
                // Target color glass
                new int[] { 17,26,35 },
                // Background class
                new int[] { 0,1,2,3,4,5,6,7,8, 13,22,31, 36,37,38,39,40,41,42,43,44 }
        };

        if(this.getState() == TradeState.CONFIRMING) {

            // Place new glass
            for(int i = 0; i < glassSlots.length; i++) {
                for(int j = 0; j < glassSlots[i].length; j++) {
                    // We only want to change the sender and target glass slots
                    if(i == 0 || i == 1) {
                        inv.setItem(glassSlots[i][j], new ItemStack(Material.RED_STAINED_GLASS_PANE, 1));
                    }
                }
            }

            // Remove the old buttons
            inv.setItem(48, ItemUtil.makeItem(Material.AIR));
            inv.setItem(50, ItemUtil.makeItem(Material.AIR));

        }

        this.getSender().updateInventory();
        this.getTarget().updateInventory();
    }

    public void ask() {
        this.setState(TradeState.WAITING);
        // The event handles the request, just to make this class file cleaner
        // For this very purpose, this approach should be just fine
        TradeRequestSendEvent event = new TradeRequestSendEvent(this, this.sender, this.target);
        if(!event.isCancelled()) {
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    public void denyRequest() {
        Chat.sendMessage("Vaihtokauppapyyntöä ei hyväksytty...", sender);
        Chat.sendMessage("Kielsit vaihtokauppapyynnön!", target);
        this.end();
    }

    public void acceptRequest() {
        // The trade is accepted, so just open the inventory and start the trade
        this.start();
    }

    public void accept(Player player) {

        // If player has already accepted, stop
        if(this.accepted.getOrDefault(player.getUniqueId(), false)) return;

        this.accepted.put(player.getUniqueId(), true);
        this.updateGui(); // Update the gui

        this.sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
        this.target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);

        // Check if we can go into "confirming" state.
        if(this.canTrade()) {
            confirm();
        }
    }

    private boolean canTrade() {
        // Check if the trade is still valid and can continue
        if(canContinue(true)) {
            // Check if  both players have accepted the trade
            if(this.accepted.getOrDefault(sender.getUniqueId(), false) &&
                    this.accepted.getOrDefault(target.getUniqueId(), false)) {
                return true;
            }
        }
        return false;
    }

    private void confirm() {

        Chat.sendMessage("Vaihtokauppa hyväksyttiin! Teillä molemmilla on §c10 sekuntia §7aikaa kieltäytyä vielä!", this.sender, this.target);
        this.setState(TradeState.CONFIRMING);

        this.updateGui(); // Just in case..?

        new BukkitRunnable() {
            int counter = 11;
            @Override
            public void run() {

                counter -= 1;

                if(getState() != TradeState.CONFIRMING) {
                    this.cancel();
                    return;
                }

                if(counter < 0) {
                    // If neither of the players declined
                    if(getState() == TradeState.CONFIRMING) {
                        trade();
                    }
                    this.cancel();
                } else {
                    sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,1,1);
                    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,1,1);
                    inv.setItem(49, ItemUtil.makeItem(Material.BARRIER, 1, "§cLopeta", Arrays.asList(
                            " §7Mikäli nykyiset tavarat, eivät",
                            " §7ole niitä niinkuin olette",
                            " §7sopineet, pystyt vielä kieltäytymään.",
                            " ",
                            " §7Aikaa jäljellä: §c" + counter + "s"
                    )));
                }

            }
        }.runTaskTimer(Main.getInstance(), 0, 20);

    }

    public void trade() {

        // Let's check the Trade's state just to be sure
        if(this.getState() == TradeState.CONFIRMING) {
            Chat.sendMessage("Vaihtokauppa onnistui! Tavarat vaihtavat nyt omistajaa!", this.sender, this.target);

            // Return target's items to the sender
            giveItems(sender, target);
            // Return sender's items to the target
            giveItems(target, sender);

            this.end();
        }

    }

    public void start() {

        if(canContinue(true)) {

            this.setState(TradeState.RUNNING); // Change the state
            // Open the gui, nothing else, I think...?
            this.sender.openInventory(this.inv);
            this.target.openInventory(this.inv);

        }

    }

    public void deny() {

        // Just in case, put 'false' for both players, since we're closing the
        // whole thing
        this.accepted.put(sender.getUniqueId(), false);
        this.accepted.put(target.getUniqueId(), false);


        this.sender.playSound(sender.getLocation(), Sound.BLOCK_ANVIL_PLACE,1,1);
        this.target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_PLACE,1,1);

        this.returnItems();
        this.end();

        Chat.sendMessage("Tavarat palautettiin ja vaihtokauppa suljettiin!", sender, target);

    }

    public boolean addItem(Player player, ItemStack newItem) {

        boolean success = false;

        // We need to calculate the next best slot to add the new item
       if(canContinue(true)) {
           int[] playerSlots = (this.sender.getUniqueId().equals(player.getUniqueId())) ? getSenderSlots() : getTargetSlots();

           for(int i = 0; i < playerSlots.length; i++) {
               int slot = playerSlots[i];
               // Let's check if the slot is not taken.
               ItemStack item = this.inv.getItem(slot);
               if(item != null) continue;
               success = true;
               addItem(newItem, slot); // Let's add it to the inventory
               break;
           }
       }
       return success;
    }

    public void addItem(ItemStack newItem, int slot) {
        this.inv.setItem(slot, newItem);
        this.updateGui();
    }

    // Just a function to check different things
    // if the current trade is still able to continue
    public boolean canContinue(boolean doesCancel) {
        if(this.sender == null || this.target == null) {
            if(doesCancel) this.cancel();
            return false;
        }
        return true;
    }

    private void giveItems(Player receiver, Player giver) {
        HashMap<Integer, ItemStack> rest = receiver.getInventory().addItem(getItems(giver));
        if(rest.size() >= 1) {
            // Drop the rest of the items to the ground
            for(Map.Entry<Integer, ItemStack> e : rest.entrySet()) {
                receiver.getWorld().dropItemNaturally(receiver.getLocation(), e.getValue());
            }
        }
    }

    private void returnItems() {
        returnItems(this.sender);
        returnItems(this.target);
    }

    private void returnItems(Player player) {
        if(player == null) return;
        HashMap<Integer, ItemStack> rest = player.getInventory().addItem(getItems(player));
        if(rest.size() >= 1) {
            // Drop the rest of the items to the ground
            for(Map.Entry<Integer, ItemStack> e : rest.entrySet()) {
                player.getWorld().dropItemNaturally(player.getLocation(), e.getValue());
            }
        }
    }

    // Public function just to end the trade
    public void cancel() {
        // Let's just check if the trade hasn't been closed already.
        if(this.getState() != TradeState.OVER) {
            if(canContinue(false)) { // Let's not try to cancel here :D Just to check if both players are online
                returnItems();
                Chat.sendMessage("Vaihtokauppa peruutettiin! Jos uskot tämän olleen virhe, ole yhteydessä meidän Discord-palvelimella. §9/discord§7!", this.sender, this.target);
            }
            this.end();
        }
    }

    private void end() {
        this.setState(TradeState.OVER);
        TradeManager.getOngoingTrades().remove(this);
        if(this.sender != null) this.sender.closeInventory();
        if(this.target != null) this.target.closeInventory();
    }

    public ItemStack[] getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        if(player != null && this.isParticipant(player)) {
            int[] slots = (this.sender.getUniqueId().equals(player.getUniqueId())) ? getSenderSlots() : getTargetSlots();
            for(int j = 0; j < slots.length; j++) {
                int slot = slots[j];
                ItemStack item = player.getOpenInventory().getTopInventory().getItem(slot);
                if(item == null) continue;
                items.add(item);
            }
        }
        return items.toArray(new ItemStack[0]);
    }


    public boolean hasAccepted(Player player) {
        if(accepted.containsKey(player.getUniqueId())) {
            return accepted.get(player.getUniqueId());
        }
        return false;
    }

    public boolean isParticipant(Player player) {
        return this.target.getUniqueId().equals(player.getUniqueId()) || this.sender.getUniqueId().equals(player.getUniqueId());
    }

    public boolean isOnGoing() {
        return this.getState() != TradeState.DEFAULT && this.getState() != TradeState.OVER && this.getState() != TradeState.WAITING;
    }

    public InventoryAction[] getIllegalActions() {
        return this.illegalActions;
    }

    public void setState(TradeState state) {
        this.state = state;
    }
    public int[] getSenderSlots() {
        return this.getPlayerSlots()[0];
    }
    public int[] getTargetSlots() {
        return this.getPlayerSlots()[1];
    }
    public int[][] getPlayerSlots() {
        return playerSlots;
    }

    public Player getSender() {
        return sender;
    }

    public Inventory getInv() {
        return inv;
    }

    public Player getTarget() {
        return target;
    }
    public TradeState getState() {
        return state;
    }

    public enum TradeState {
        WAITING, RUNNING, OVER, CONFIRMING, DEFAULT;
    }

}
