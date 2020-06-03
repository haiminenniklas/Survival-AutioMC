package me.tr.survival.main.other;

import java.util.HashMap;
import java.util.UUID;

import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


@Deprecated
public class EnderpearlCooldown implements Listener {

    public static HashMap<UUID, Integer> cooldown = new HashMap<UUID, Integer>();

    @EventHandler
    public void onProjectileLaunch(PlayerInteractEvent e) {

        if(e.isCancelled()) return;

        Player player = e.getPlayer();

        UUID uuid = player.getUniqueId();

        if(e.getItem() != null && e.getItem().getType() != Material.AIR){

            ItemStack item = e.getItem();

            if(item.getType() == Material.ENDER_PEARL){
                if (cooldown.containsKey(uuid)) {
                    e.setCancelled(true);
                    Chat.sendMessage(player, "Odota vielä §c" + cooldown.get(uuid) + "s §7tehdäksesi tuon uudestaan!!");
                    return;
                } else {

                    cooldown.put(uuid, Main.getInstance().getConfig().getInt("enderpearl-cooldown"));

                    new BukkitRunnable() {

                        @Override
                        public void run() {

                            if (cooldown.containsKey(uuid)) {

                                cooldown.put(uuid, cooldown.get(uuid) - 1);

                                if (cooldown.get(uuid) <= 0) {
                                    cancel();
                                    cooldown.remove(uuid);
                                }

                            } else {
                                cancel();
                            }

                        }

                    }.runTaskTimerAsynchronously(Main.getInstance(), 20, 20);

                }
            }

        }

    }

}
