package me.tr.survival.main.managers.villages;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Settings;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.callback.SpigotCallback;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlayerVillage {

    private UUID uuid;
    private String title;

    private UUID leader;
    private List<UUID> coLeaders;
    private List<UUID> citizens;
    private List<UUID> invited;
    private List<UUID> requested;

    private int taxRate;
    private int maxPlayers;

    private Location spawn;
    private boolean closed;

    private List<String> tags;

    private double balance;
    private double totalMoneyGathered;

    private long created;

    public PlayerVillage(UUID uuid, String title, UUID leader, List<UUID> coLeaders, List<UUID> citizens, int taxRate,
                         Location spawn, int maxPlayers, boolean closed, List<String> tags, double balance,
                         double totalMoneyGathered, List<UUID> invited, List<UUID> requested, long created) {

        this.uuid = uuid;
        this.title = title;
        this.leader = leader;
        this.coLeaders = coLeaders;
        this.citizens = citizens;

        this.spawn = spawn;

        this.taxRate = taxRate;
        this.maxPlayers = maxPlayers;
        this.closed = closed;
        this.tags = tags;

        this.balance = balance;
        this.totalMoneyGathered = totalMoneyGathered;

        this.invited = invited;
        this.requested = requested;

        this.created = created;

    }

    public long getCreated() {
        return created;
    }

    public String getCreationDate() {
        return new Timestamp(this.created).
                toLocalDateTime().
                format(DateTimeFormatter.
                        ofPattern("dd.MM.yyyy"));
    }

    public void invite(UUID uuid) {
        if(!this.isInvited(uuid)) {
            this.invited.add(uuid);
            Player invitedPlayer = Bukkit.getPlayer(uuid);
            if(invitedPlayer != null) {
                if(!Settings.get(uuid, "privacy")) {
                    invitedPlayer.playSound(invitedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                    invitedPlayer.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    invitedPlayer.sendMessage(" §e§lLiittymispyyntö");
                    invitedPlayer.sendMessage(" ");
                    invitedPlayer.sendMessage(" §7Sinua on pyydetty liittymään");
                    invitedPlayer.sendMessage( "§7pelaajakylään nimeltä:");
                    invitedPlayer.sendMessage(" §e" + this.getTitle() + "§7!");
                    invitedPlayer.sendMessage(" ");
                    TextComponent openMsg = new TextComponent(TextComponent.fromLegacyText(" §a§lNäytä kylä!"));
                    openMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klikkaa avataksesi kylän tiedot!").create()));
                    SpigotCallback.createCommand(
                            openMsg, opener -> Main.getVillageManager().openVillageView(opener, this, true)
                    );
                    invitedPlayer.spigot().sendMessage(openMsg);
                    invitedPlayer.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                } else {
                    Chat.sendMessage(invitedPlayer, "Sinut on kutsuttu kylään §e" + this.getTitle() + "§7! " +
                            "Kirjoita chattiin §a/kylä " + this.getTitle() + " §7ja liity! Voit myös ottaa tämän kutsun huomiotta.");
                }
            }
        }
    }

    public void requestToJoin(Player requester) {
        if(!this.hasRequested(requester.getUniqueId())) {
            if(this.isClosed()) {
                this.requested.add(requester.getUniqueId());
                requester.playSound(requester.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                Chat.sendMessage(requester, "Kysyit liittymislupaa kylään §a" + this.getTitle() + "§7!");
            }
        }
    }

    public boolean isLeader(UUID uuid) {
        return this.getLeader().equals(uuid);
    }

    public List<UUID> getInvited() {
        return invited;
    }

    public List<UUID> getRequested() {
        return requested;
    }

    public boolean isInvited(UUID uuid) {
        if(this.isMember(uuid)) return true;
        return this.invited.contains(uuid);
    }

    public boolean hasRequested(UUID uuid) {
        if(this.isMember(uuid)) return true;
        return this.requested.contains(uuid);
    }

    public void addTag(String tag) { if(!tags.contains(tag)) tags.add(tag); }

    public void addCoLeader(UUID uuid) {
        if(!this.getCoLeaders().contains(uuid)) this.getCoLeaders().add(uuid);
    }

    public void removeCoLeader(UUID uuid) {
        this.getCoLeaders().remove(uuid);
    }

    public void removePlayer(UUID uuid) {
        this.coLeaders.remove(uuid);
        this.citizens.remove(uuid);
        final Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            Chat.sendMessage(player, "Sinut potkittiin juuri kylästä... Harmillista!");
        }
    }

    public void join(OfflinePlayer player) {
        if(this.isMember(uuid)) return;
        this.getRequested().remove(player.getUniqueId());
        this.getInvited().remove(player.getUniqueId());
        this.getCitizens().add(player.getUniqueId());
    }

    public void teleport(final Player player) {

        if(!player.getWorld().getName().equals("world")) {
            Chat.sendMessage(player, "§7Tämä toimii vain tavallisessa maailmassa!");
            return;
        }

        Chat.sendMessage(player, "Sinut viedään kylälle §a" + this.title + " §e3 sekunnin §7kuluttua!");
        Sorsa.after(3, () -> {
            player.teleport(this.spawn);
        });

    }

    public boolean isMember(UUID uuid) { return this.citizens.contains(uuid); }

    public boolean canModify(UUID uuid, boolean checkForStaff) {
        if(this.getLeader().equals(uuid)) return true;
        if(checkForStaff) {
            if(Ranks.isStaff(uuid)) return true;
        }
        if(this.getCoLeaders().contains(uuid)) return true;
        return false;
    }

    public boolean canModify(UUID uuid) {
        return this.canModify(uuid, false);
    }

    public boolean ownsVillage(UUID uuid) {
        if(this.getLeader().equals(uuid)) return true;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if(player.isOp()) return true;
        return false;
    }

    public void taxPlayers() {

        if(this.getTaxRate() > 0) {

            double returnTaxAmountPerPlayer = 0d;

            if(this.getBalance() > 100) {
                returnTaxAmountPerPlayer = Util.round((this.getBalance() / this.getCitizens().size()) * .2, 2);
            }

            double totalTaxation = 0d;
            for(UUID memberUUID : this.getCitizens()) {
                double taxPayed = this.taxPlayer(memberUUID);
                if(returnTaxAmountPerPlayer > 0 && taxPayed >= this.getTaxRate()) {
                    Balance.add(memberUUID, returnTaxAmountPerPlayer);
                    this.removeBalance(returnTaxAmountPerPlayer);
                }
                totalTaxation += taxPayed;
            }

            double totalCuts = 0d;
            for(UUID coLeaderUUID : this.getCoLeaders()) {
                double cut = totalTaxation * 0.05;
                Balance.add(coLeaderUUID, cut);
                totalCuts += cut;
            }

            double leadersPay = totalTaxation * 0.1;
            totalCuts += leadersPay;

            Balance.add(this.getLeader(), leadersPay);

            double moneyToAddToBalance = totalTaxation - totalCuts;
            this.addBalance(moneyToAddToBalance);


        }
    }

    public boolean hasDefaultName() {
        OfflinePlayer leader = Bukkit.getOfflinePlayer(this.getLeader());
        return this.getTitle().equalsIgnoreCase(leader.getName() + ":n uusi kylä!");
    }

    public double taxPlayer(UUID uuid) {
        double moneyToRemove = this.taxRate;
        if(!Balance.canRemove(uuid, moneyToRemove)) {
            moneyToRemove = Balance.get(uuid);
        }
        Balance.remove(uuid, moneyToRemove);
        return moneyToRemove;
    }

    public void addBalance(double amount) {
        this.balance += amount;
        this.totalMoneyGathered += amount;
    }

    public void removeBalance(double amount) {
        this.balance -= amount;
        if(this.balance < 0) this.balance = 0;
    }

    public void setBalance(double balance) {
        this.balance = Util.round(balance, 2);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public int getTaxRate() {
        return taxRate;
    }

    public List<UUID> getCitizens() {
        return citizens;
    }

    public int getMaxCoLeaders() {
        return 3;
    }

    public List<UUID> getCoLeaders() {
        return coLeaders;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public UUID getLeader() { return leader; }

    public Location getSpawn() {
        return spawn;
    }

    public double getBalance() {
        return balance;
    }

    public double getTotalMoneyGathered() {
        return totalMoneyGathered;
    }

    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }
    public void setTaxRate(int taxRate) { this.taxRate = taxRate; }
    public void setTitle(String title) { this.title = title; }
    public List<String> getTags() { return tags; }

    public boolean isFull() {
        return this.citizens.size() >= this.getMaxPlayers();
    }

}
