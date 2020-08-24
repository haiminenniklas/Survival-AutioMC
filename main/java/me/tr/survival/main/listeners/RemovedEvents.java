package me.tr.survival.main.listeners;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.features.Boosters;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RemovedEvents implements Listener {

   /* @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {

        Player player = e.getPlayer();

        if(!Boosters.isActive(Boosters.Booster.EXTRA_HEARTS))
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);

        if(!player.hasPermission("deathisland.bypass") && !deathIsland.contains(player.getUniqueId())) {
            Sorsa.teleportToSpawn(player);
           Bukkit.getScheduler().runTaskLater(Main.getInstance(), (task -> {
                Location deathSpawn = Sorsa.getDeathSpawn();
                player.teleport(deathSpawn);
                Util.heal(player);
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) Sorsa.getCurrentTPS() * 30, 999, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) Sorsa.getCurrentTPS() * 30, 1, true, false));
                player.playSound(player.getLocation(), Sound.MUSIC_DISC_13, 1, 1);
                deathIsland.add(player.getUniqueId());
                Sorsa.logColored("§6[DeathIsland] The player " + player.getUniqueId() + " was sent to the Death Island!");
            }), 5);

            new BukkitRunnable() {

                int timer = 30;

                @Override
                public void run() {
                    if(!deathIsland.contains(player.getUniqueId())) {
                        cancel();
                        return;
                    }

                    if(timer >= 0) {
                        player.sendTitle("§c§lKUOLIT", "§7Pääset §c" + timer + "s §7päästä takaisin!", 15, 20, 15);
                        timer -= 1;
                    }

                    if(timer <= 0) {
                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), (task -> {
                            Util.heal(player);
                            player.sendTitle("§a§lTAKAISIN", "§7Olet taas elossa!", 15, 20, 15);
                            deathIsland.remove(player.getUniqueId());
                            player.stopSound(Sound.MUSIC_DISC_13);
                            Sorsa.teleportToSpawn(player);
                            Sorsa.logColored("§6[DeathIsland] The player " + player.getUniqueId() + " got away from the Death Island was teleported to the spawn!");
                            task.cancel();
                        }), 5);
                        cancel();
                    }

                }
            }.runTaskTimerAsynchronously(Main.getInstance(), 25, 20);

        } else Sorsa.teleportToSpawn(player);

    } */

}
