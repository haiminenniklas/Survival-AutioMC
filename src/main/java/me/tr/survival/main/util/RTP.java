package me.tr.survival.main.util;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.other.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class RTP {

    public static HashMap<UUID, Long> cooldown = new HashMap<>();

    public static void teleport(Player player) {

        if(cooldown.containsKey(player.getUniqueId()) && System.currentTimeMillis() < cooldown.get(player.getUniqueId()) ) {
            Chat.sendMessage(player, "Sinun pitää odottaa hetki, jotta voit tehdä teleportata uudestaan..");
            return;
        }

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

        int newY = player.getWorld().getHighestBlockYAt(newX, newZ);

        player.teleport(new Location(player.getWorld(), newX, newZ, newY));
        Util.sendNotification(player, "§7Sinut vietiin §aErämaahan§7!");

        cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (3 * 60 * 1000));

    }

}
