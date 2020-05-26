package me.tr.survival.main.trading.events;

import me.tr.survival.main.trading.Trade;

@Deprecated
public class TradeCancelEvent extends TradeEvent {

    public TradeCancelEvent(Trade trade) {
        super(trade);
    }

}
