package me.tr.survival.main.other;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiAFKFishing implements Listener {

    private final Map<UUID, Long> fishing = new HashMap<>();
    private final Map<UUID, Location> fishingPos = new HashMap<>();

    @EventHandler
    public void onFish(PlayerFishEvent e) {

        Player player = e.getPlayer();
        if(hasFishedBefore(player)) {

            FileConfiguration config = Main.getInstance().getConfig();
            final int minBlocksToMove = config.getInt("anti-afk-fishing.min-blocks-to-move");
            final int minTimeInSeconds = config.getInt("anti-afk-fishing.min-time-in-seconds");

            final long lastFished = getLastFished(player);
            final long now = System.currentTimeMillis();

            // If not enough time has passed
            if(now - lastFished > minTimeInSeconds * 1000) {

                final Location lastPos = getLastFishingPosition(player);
                final Location nowPos = player.getLocation();

                // If hasn't moved enough
                if(lastPos.distanceSquared(nowPos) <= (Math.pow(minBlocksToMove, 2))) {
                    e.setCancelled(true);
                    e.setExpToDrop(0);
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinun pitää liikkua vähäsen kalastelun aikana!");
                    Sorsa.logColored("§6[AntiAFKFishing] Player " + player.getName() + " is filed to be AFK-Fishing!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                }

            }

        } else {
            fishing.put(player.getUniqueId(), System.currentTimeMillis());
            fishingPos.put(player.getUniqueId(), player.getLocation());
        }

    }

    private boolean hasFishedBefore(Player player) {
        return getLastFished(player) != -1;
    }

    private long getLastFished(Player player) {
        return fishing.getOrDefault(player.getUniqueId(), -1L);
    }

    private Location getLastFishingPosition(Player player) {
        return fishingPos.getOrDefault(player.getUniqueId(), null);
    }

}
