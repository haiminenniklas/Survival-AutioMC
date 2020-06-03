package me.tr.survival.main.util;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.other.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RTP {

    public static HashMap<UUID, Long> cooldown = new HashMap<>();

    public static boolean teleport(final Player player) {

        if(!player.getWorld().getName().equals("world")) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "RTP toimii vain tavallisessa maailmassa!");
        }

        if(cooldown.containsKey(player.getUniqueId()) && System.currentTimeMillis() < cooldown.get(player.getUniqueId()) ) {
            long timeLeftRaw = (cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;

            long minutes = (int) timeLeftRaw / 60;
            long seconds = timeLeftRaw - (60 * minutes);

            String timeLeft = Util.formatTime((int) minutes, (int) seconds, true);

            Chat.sendMessage(player, "Odota vielä §c" + timeLeft + " §7jotta voit käyttää tätä uudestaan!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return false;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
        Util.sendNotification(player, "§7Etsitään sopivaa sijaintia...");

        // Do the RTP in async for better performance
        Autio.async(() -> {
            World world = Bukkit.getWorld("world");
            Location loc = randomLocation(player.getWorld());

            if(world != null && loc != null){
                Autio.afterAsync(1, () -> {

                    double y = world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
                    final Location newLoc = new Location(world, loc.getX(), y + 1, loc.getZ());

                    Autio.task(() -> player.teleport(newLoc));
                    Util.sendNotification(player, "§7Sinut vietiin §aErämaahan§7!");

                    if(!player.isOp()) {
                        cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (3 * 60 * 1000));
                    }

                });
            } else {
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Teleporttaus epäonnistui, yritä pian uudestaan!");
            }

        });

        return true;
    }

    public static Location randomLocation(World world) {
        Random r = new Random();
        int range = Main.getInstance().getConfig().getInt("random-tp.range");
        int newX = r.nextInt(range), newZ = r.nextInt(range);

        if(newX >= 14000) {

            newX = 14000;

        } else if(newX <= -14000) {
            newX = -14000;
        }

        if(newZ >= 14000) {
            newZ = 14000;
        } else if(newZ <= -14000) {
            newZ = -14000;
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
