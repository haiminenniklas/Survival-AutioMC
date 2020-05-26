package me.tr.survival.main.trading.events;

import me.tr.survival.main.trading.Trade;
import org.bukkit.entity.Player;

public class TradeRequestSendEvent extends TradeEvent {

    private final Player sender, target;

    public TradeRequestSendEvent(Trade trade, Player sender, Player target) {
        super(trade);
        this.sender = sender;
        this.target = target;
    }

    public Player getTarget() {
        return target;
    }

    public Player getSender() {
        return sender;
    }
}
