package me.tr.survival.main.quests;

import me.tr.survival.main.util.callback.Callback;

public enum Quest {

    ;

    private String displayName;
    private int crystalPrize, moneyPrize;
    private Callback callback;

    Quest(String displayName, int crystalPrize, int moneyPrize, Callback callback) {
        this.displayName = displayName;
        this.crystalPrize = crystalPrize;
        this.moneyPrize = moneyPrize;
        this.callback = callback;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCrystalPrize() {
        return crystalPrize;
    }

    public int getMoneyPrize() {
        return moneyPrize;
    }

    public Callback getCallback() {
        return callback;
    }
}
