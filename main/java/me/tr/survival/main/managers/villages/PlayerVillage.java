package me.tr.survival.main.managers.villages;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.other.Ranks;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PlayerVillage {

    private UUID uuid;
    private String title;

    private UUID leader;
    private List<UUID> coLeaders;
    private List<UUID> citizens;

    private int taxRate;
    private int maxPlayers;

    private Location spawn;
    private boolean closed;

    private List<String> tags;

    public PlayerVillage(UUID uuid, String title, UUID leader, List<UUID> coLeaders, List<UUID> citizens, int taxRate, Location spawn, int maxPlayers, boolean closed, List<String> tags) {

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

    }

    public void addTag(String tag) { if(!tags.contains(tag)) tags.add(tag); }

    public void addCoLeader(UUID uuid) {
        if(!this.getCoLeaders().contains(uuid)) this.getCoLeaders().add(uuid);
    }

    public void join(Player player) {
        this.getCitizens().add(player.getUniqueId());
    }

    public void teleport(final Player player) {

        Chat.sendMessage(player, "Sinut viedään kylälle §a" + this.title + " §e3 sekunnin §7kuluttua!");
        Sorsa.after(3, () -> {
            player.teleport(this.spawn);
        });

    }

    public boolean isMember(UUID uuid) { return this.citizens.contains(uuid); }

    public boolean canModify(UUID uuid) {
        if(this.getLeader().equals(uuid)) return true;
        if(Ranks.isStaff(uuid)) return true;
        if(this.getCoLeaders().contains(uuid)) return true;
        return false;
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
