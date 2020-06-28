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

    public PlayerVillage(UUID uuid, String title, UUID leader, List<UUID> coLeaders, List<UUID> citizens, int taxRate, Location spawn) {

        this.uuid = uuid;
        this.title = title;
        this.leader = leader;
        this.coLeaders = coLeaders;

        this.spawn = spawn;

        this.taxRate = taxRate;
        this.maxPlayers = 8;

    }

}
