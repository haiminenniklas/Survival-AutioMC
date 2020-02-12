package me.tr.survival.main.util;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.other.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class RTP {

    public static HashMap<UUID, Long> cooldown = new HashMap<>();

    public static boolean teleport(Player player) {

        if(cooldown.containsKey(player.getUniqueId()) && System.currentTimeMillis() < cooldown.get(player.getUniqueId()) ) {
            Chat.sendMessage(player, "Sinun pitää odottaa hetki, jotta voit tehdä teleportata uudestaan..");
            return false;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
        Util.sendNotification(player, "§7Etsitään sopivaa sijaintia...");

        Location loc = randomLocation(player.getWorld());

        // Maybe fix inside block teleport
        if(!loc.isChunkLoaded()) {
            loc.getChunk().load();
        }

        player.teleportAsync(loc);
        Util.sendNotification(player, "§7Sinut vietiin §aErämaahan§7!");

        if(!player.isOp()) {
            cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (3 * 60 * 1000));
        }

        return true;
    }

    public static Location randomLocation(World world) {
        Random r = new Random();
        int range = Main.getInstance().getConfig().getInt("random-tp.range");
        int newX = r.nextInt(range), newZ = r.nextInt(range);

        if(newX >= 9000) {

            newX = 9000;

        } else if(newX <= -9000) {
            newX = -9000;
        }

        if(newZ >= 9000) {
            newZ = 9000;
        } else if(newZ <= -9000) {
            newZ = -9000;
        }

        int newY = world.getHighestBlockYAt(newX, newZ);
        Location loc = new Location(world, newX, newY, newZ);

        Block block = loc.clone().add(0d, -1d, 0d).getBlock();
        if(block.isLiquid()) {
            return randomLocation(world);
        }
        return loc;
    }

}
