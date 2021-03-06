package me.tr.survival.main.other;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.data.Balance;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;
import java.util.List;

public class CustomEconomy implements Economy {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "SorsaEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
      /*  float epsilon = 0.004f; // 4 tenths of a cent
        if (Math.abs(Math.round(amount) - amount) < epsilon) {
            return String.format("%10.0f", amount); // sdb
        } else {
            return String.format("%10.2f", amount); // dj_segfault
        } */
        return new DecimalFormat("#.##").format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return "€";
    }

    @Override
    public String currencyNameSingular() {
        return "€";
    }

    @Override
    public boolean hasAccount(String playerName) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return false;
    }

    @Override
    public double getBalance(String playerName) {
        return Balance.get(Bukkit.getOfflinePlayer(playerName).getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return Balance.get(player.getUniqueId());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return this.has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            Sorsa.async(() -> PlayerData.loadPlayer(player.getUniqueId(), r -> {}));
            return false;
        }
        return Balance.canRemove(player.getUniqueId(),amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return this.withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            Sorsa.async(() -> PlayerData.loadPlayer(player.getUniqueId(), r -> {}));
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Player is not loaded into database...");
        }

        if(has(player, amount)) {
            Balance.remove(player.getUniqueId(), amount);
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot afford");
        }

    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return this.depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            Sorsa.async(() -> PlayerData.loadPlayer(player.getUniqueId(), r -> {}));
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Player is not loaded into database...");
        }

        Balance.add(player.getUniqueId(), amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return false;
    }
}

