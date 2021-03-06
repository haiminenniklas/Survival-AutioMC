package me.tr.survival.main.other;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.callback.TypedCallback;
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
        if(this.yaw == null || this.pitch == null) return new Location(this.world, this.x, this.y, this.z);
        else return new Location(this.world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public boolean teleport() {
        Player player = Bukkit.getPlayer(this.ownerUUID);
        if(player != null && player.isOnline()) {
            this.teleport(player, r -> {});
            return true;
        }
        return false;
    }

    public void teleport(Player player, TypedCallback<Boolean> result) {
        if(player.getWorld().getEnvironment() == World.Environment.NETHER) {
            Chat.sendMessage(player, "§7Tämä ei toimi §cNetherissä§7!");
            result.execute(false);
            return;
        }

        final Location loc = this.getLocation();

        if(loc.getWorld().getEnvironment() == World.Environment.THE_END ||
                loc.getWorld().getEnvironment() == World.Environment.NETHER) {
            Chat.sendMessage(player, "Kodin sijainti ei ole tavallisessa maailmassa, joten sinne ei pysty teleporttaamaan. Ole hyvä ja poista tämä koti.");
            result.execute(false);
            return;
        }

        Sorsa.after(3, () -> {
            Util.sendNotification(player, "§7Teleportataan...", true);
            Sorsa.logColored("§6[Homes] Player " + player.getName() + " (" + player.getUniqueId() + ") teleported to their home at " + Util.formatLocation(loc) + "!");
            Util.teleportHorse(player, loc);
            player.teleport(loc);
        });
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
