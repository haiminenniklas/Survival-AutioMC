package me.tr.survival.main.other.events;

import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class GuiClickEvent extends PlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private Gui gui;
    private Button button;

    public GuiClickEvent(Player player, Gui gui, Button button){
        super(player);
        this.gui = gui;
        this.button = button;
    }

    public Button getButton() {
        return button;
    }

    public Gui getGui() {
        return gui;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
