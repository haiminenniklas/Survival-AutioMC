package me.tr.survival.main.managers.villages;

import org.bukkit.Location;

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

    public PlayerVillage(UUID uuid, String title, UUID leader, List<UUID> coLeaders, List<UUID> citizens, int taxRate, Location spawn, int maxPlayers) {

        this.uuid = uuid;
        this.title = title;
        this.leader = leader;
        this.coLeaders = coLeaders;
        this.citizens = citizens;

        this.spawn = spawn;

        this.taxRate = taxRate;
        this.maxPlayers = maxPlayers;

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

    public UUID getLeader() {
        return leader;
    }

    public Location getSpawn() {
        return spawn;
    }

}
