package me.tr.survival.main.trading.events;

import me.tr.survival.main.trading.Trade;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class TradeEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final Trade trade;

    public TradeEvent(Trade trade) {
        super(false); // Make sure event is NOT async
        this.trade = trade;
        this.cancelled = false;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public final Trade getTrade() {
        return trade;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
