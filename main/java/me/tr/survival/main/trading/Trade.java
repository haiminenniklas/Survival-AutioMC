package me.tr.survival.main.trading;

import java.util.UUID;

public class Trade {

    private UUID trader, target;


    public Trade(UUID trader, UUID target) {
        this.trader = trader;
        this.target = target;
    }

}
