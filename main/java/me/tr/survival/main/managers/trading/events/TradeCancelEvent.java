package me.tr.survival.main.managers.trading.events;

import me.tr.survival.main.managers.trading.Trade;

@Deprecated
public class TradeCancelEvent extends TradeEvent {

    public TradeCancelEvent(Trade trade) {
        super(trade);
    }

}
