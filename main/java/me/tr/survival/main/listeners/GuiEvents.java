package me.tr.survival.main.listeners;

import me.tr.survival.main.other.events.GuiClickEvent;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class GuiEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null) return;

        final Player player = (Player) e.getWhoClicked();
        final Gui gui = Gui.getGui(player);
        final InventoryView view = e.getView();

        if(gui != null) {
            if(view.getTitle().contains("§r")) {

                final ItemStack item = e.getCurrentItem();
                final int slot = e.getRawSlot();

                if(item != null) {
                    if(gui.isPartiallyTouchable()) {
                        // If user didn't do the allowed procedures with a partially touchable inventory
                        if(!view.getBottomInventory().contains(item) && !gui.clickedAllowedSlot(slot)) e.setCancelled(true);
                    } else e.setCancelled(true);

                    for(final Button b : gui.getButtons()) {
                        // Check for slot, don't compare items
                        if(slot == b.pos) {
                            // Call GuiClickEvent
                            final GuiClickEvent guiClickEvent = new GuiClickEvent(player, gui, b);
                            Bukkit.getPluginManager().callEvent(guiClickEvent);
                            if(!guiClickEvent.isCancelled()) b.onClick(player, e.getClick());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent e) {
        final Player player = (Player) e.getPlayer();
        if(e.getView().getTitle().contains("§r")) {
            final Gui gui = Gui.getGui(player);
            if(gui != null) gui.close(player);
        }
    }

    @EventHandler
    public void onGuiClick(GuiClickEvent e) {

        final Player player = e.getPlayer();
        if(!e.isCancelled()) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);

    }

}
