package me.tr.survival.main;

import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.callback.TypedCallback;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Home {

    private UUID ownerUUID;
    private OfflinePlayer owner;

    private double x, y, z;
    private World world;

    private Float yaw, pitch;

    public Home(UUID owner, double x, double y, double z, String worldName) {

        this.ownerUUID = owner;
        this.owner = Bukkit.getOfflinePlayer(owner);

        this.x = x;
        this.y = y;
        this.z = z;

        this.world = Bukkit.getWorld(worldName);

    }

    public Home(UUID owner, double x, double y, double z, String worldName, float yaw, float pitch) {

        this.ownerUUID = owner;
        this.owner = Bukkit.getOfflinePlayer(owner);

        this.x = x;
        this.y = y;
        this.z = z;

        this.yaw = yaw;
        this.pitch = pitch;

        this.world = Bukkit.getWorld(worldName);
    }

    public Location getLocation() {

        if(this.yaw == null || this.pitch == null) {
            return new Location(this.world, this.x, this.y, this.z);
        } else {
            return new Location(this.world, this.x, this.y, this.z, this.yaw, this.pitch);
        }

    }

    public boolean teleport() {
        Player player = Bukkit.getPlayer(this.ownerUUID);
        if(player != null && player.isOnline()) {
            player.teleport(this.getLocation());
            return true;
        }
        return false;
    }

    public void teleport(Player player, TypedCallback<Boolean> result) {
        if(player.getWorld().getName().equals("world_nether")) {
            Chat.sendMessage(player, "§7Tämä ei toimi §cNetherissä§7!");
            result.execute(false);
            return;
        }
        Autio.afterAsync(3, () -> {
            Util.sendNotification(player, "§7Teleportataan...", true);
            CompletableFuture<Boolean> teleport = player.teleportAsync(this.getLocation());
            if(!teleport.join()) {
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Teleporttaus kotiin epäonnistui... Yritä pian uudestaan!");
                result.execute(false);
            } else {
                result.execute(true);
            }
        });
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
