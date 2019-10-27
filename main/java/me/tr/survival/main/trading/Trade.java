package me.tr.survival.main.trading;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Trade {

    private UUID trader, target;


    public Trade(UUID trader, UUID target) {
        this.trader = trader;
        this.target = target;
    }

    public void ask() {

    }

    public void accept() {

    }

    public void deny() {

    }

    private void openExchangeGui() {

    }

    public void acceptItems() {

    }

    public UUID getTargetUUID() {
        return target;
    }

    public UUID getTraderUUID() {
        return trader;
    }

    public Player getTarget() {
        Player player = Bukkit.getPlayer(this.target);
        if(player == null) throw new IllegalArgumentException("Player is null!");
        return player;
    }

    public Player getTrader() {
        Player player = Bukkit.getPlayer(this.trader);
        if(player == null) throw new IllegalArgumentException("Player is null!");
        return player;
    }

}
