package me.tr.survival.main.trading;

import me.tr.survival.main.Chat;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Trade {

    private Player sender, target;
    private TradeState state;

    private Map<UUID, Boolean> accepted;

    private Inventory inv;

    private int[][] playerSlots;

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

        this.inv = Bukkit.createInventory(null, 54, "Vaihtokauppa");
        this.initGui();

        TradeManager.getTrades().add(this);

    }

    private void initGui() {

        // Setup glass
        int[][] glassSlots = new int[][] {

                // Sender color glass
                new int[] { 9,18,27 },
                // Target color glass
                new int[] { 17,26,35 },
                // Background class
                new int[] {
                        0,1,2,3,4,5,6,7,8,
                        13,22,31,
                        36,37,38,39,40,41,42,43,44
                }

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

        // Place player heads
        inv.setItem(45, ItemUtil.makeSkullItem(this.sender.getName(), 1, "§a" + this.sender.getName(), Arrays.asList(
                "§cEi hyväksynyt",
                " ",
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(sender.getUniqueId()))
        )));

        inv.setItem(53, ItemUtil.makeSkullItem(this.target.getName(), 1, "§e" + this.target.getName(), Arrays.asList(
                "§cEi hyväksynyt",
                " ",
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(target.getUniqueId()))
        )));

        // Buttons
        inv.setItem(48, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§aHyväksy"));
        inv.setItem(50, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§cKieltäydy"));

    }

    public void updateGui() {

        // Place player heads
        inv.setItem(45, ItemUtil.makeSkullItem(this.sender.getName(), 1, "§a" + this.sender.getName(), Arrays.asList(
                "§cEi hyväksynyt",
                " ",
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(sender.getUniqueId()))
        )));

        inv.setItem(53, ItemUtil.makeSkullItem(this.target.getName(), 1, "§e" + this.target.getName(), Arrays.asList(
                "§cEi hyväksynyt",
                " ",
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(target.getUniqueId()))
        )));

        this.getSender().updateInventory();
        this.getTarget().updateInventory();
    }

    public void ask() {

    }

    private void denyRequest() {
        Chat.sendMessage("Vaihtokauppapyyntöä ei hyväksytty...", sender);
        Chat.sendMessage("Kielsit vaihtokauppapyynnön!", target);
        this.end();

    }

    private void acceptRequest() {



    }

    public void accept(Player player) {

        this.accepted.put(player.getUniqueId(), true);

    }

    public void start() {



    }

    public void deny() {

        // Just in case
        this.accepted.put(sender.getUniqueId(), false);
        this.accepted.put(target.getUniqueId(), false);

        this.returnItems();
        this.end();

        Chat.sendMessage("Tavarat palautettiin ja vaihtokauppa suljettiin!", sender, target);

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


    private void end() {
        if(this.getState() != TradeState.OVER || this.getState() != TradeState.DEFAULT) {
            if(this.sender != null) this.sender.closeInventory();
            if(this.target != null) this.target.closeInventory();
        }
        this.setState(TradeState.OVER);
        TradeManager.getTrades().remove(this);
    }

    public ItemStack[] getItems(Player player) {

        List<ItemStack> items = new ArrayList<>();

        if(player != null && this.isParticipant(player)) {
            for(int i = 0; i < playerSlots.length; i++) {

                int[] slots = playerSlots[i];

                for(int j = 0; j < slots.length; j++) {
                    int slot = slots[j];
                    ItemStack item = inv.getItem(slot);
                    if(item == null || item.getType() == Material.AIR) item = new ItemStack(Material.AIR);
                    items.add(item);
                }

            }
        }

        return items.toArray(new ItemStack[0]);
    }

    public boolean hasAccepted(Player player) {
        if(accepted.containsKey(player.getUniqueId())) {
            return accepted.get(player.getUniqueId());
        }
        return true;
    }

    public boolean isParticipant(Player player) {
        return this.target.getUniqueId().equals(player.getUniqueId()) || this.sender.getUniqueId().equals(player.getUniqueId());
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

    public Inventory getInv() {
        return inv;
    }

    public Player getSender() {
        return sender;
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
