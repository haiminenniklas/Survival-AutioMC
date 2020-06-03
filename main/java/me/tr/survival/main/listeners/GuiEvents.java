package me.tr.survival.main.listeners;

import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null) return;
        if(e.getCurrentItem() == null) return;
        Player player = (Player) e.getWhoClicked();
        Gui gui = Gui.getGui(player);
        if(gui != null) {
            if(e.getView().getTitle().contains("§r")) {
                if(gui.isPartiallyTouchable()) {
                    // If user didn't do the allowed procedures with a partially touchable inventory
                    if(!e.getView().getBottomInventory().contains(e.getCurrentItem()) && !gui.clickedAllowedSlot(e.getSlot())) e.setCancelled(true);
                } else e.setCancelled(true);
            }
            if(e.getCurrentItem() != null) {
                for(Button b : gui.getButtons()) {
                    if(b.item.isSimilar(e.getCurrentItem()))
                        b.onClick(player, e.getClick());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if(e.getView().getTitle().contains("§r") && Gui.getGui(player) != null) Gui.getGui(player).close(player);
    }

}
