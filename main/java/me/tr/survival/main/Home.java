package me.tr.survival.main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Home {

    private UUID ownerUUID;
    private OfflinePlayer owner;

    private double x, y, z;
    private World world;

    public Home(UUID owner, double x, double y, double z, String worldName) {

        this.ownerUUID = owner;
        this.owner = Bukkit.getOfflinePlayer(owner);

        this.x = x;
        this.y = y;
        this.z = z;

        this.world = Bukkit.getWorld(worldName);
        //sss

    }

    public Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z);
    }

    public boolean teleport() {
        Player player = Bukkit.getPlayer(this.ownerUUID);
        if(player != null && player.isOnline()) {
            player.teleport(this.getLocation());
            return true;
        }
        return false;
    }

    public void teleport(Player player) {
        player.teleport(this.getLocation());
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }
}
