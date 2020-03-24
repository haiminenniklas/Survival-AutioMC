package me.tr.survival.main.other.warps;

import me.tr.survival.main.Chat;
import me.tr.survival.main.util.callback.DatabaseCallback;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Warp {

    private String name, displayName, description;
    private int x, y, z;
    private float yaw, pitch;
    private World world;
    private Location loc;

    public Warp(String name, Location loc, String description, String displayName) {

        this.name = name.toLowerCase();
        this.displayName = "§6" + displayName;

        this.loc = loc;

        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();

        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();

        this.world = loc.getWorld();
        this.description = description;
    }

    public void teleport(Player player) {
        Chat.sendMessage(player, "Sinua viedään warpille §a" + this.displayName + "§7...");
        player.teleport(new Location(this.world, this.x, this.y, this.z, this.yaw, this.pitch));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }
}
