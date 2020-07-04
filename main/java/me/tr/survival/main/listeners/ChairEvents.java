package me.tr.survival.main.listeners;

import me.tr.survival.main.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityDismountEvent;

public class ChairEvents implements Listener {

    @EventHandler
    public void onBlockClick(PlayerInteractEvent e) {

        final Player player = e.getPlayer();
        final Block block = e.getClickedBlock();
        if(block != null && block.getType() != Material.AIR) {

            if(Util.isBlockStair(block)) {

                final Location loc = block.getLocation();
                Arrow arrow = (Arrow) loc.getWorld().spawnEntity(loc, EntityType.ARROW);
                arrow.setInvulnerable(true);
                arrow.setGravity(false);
                arrow.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false), true);
                arrow.addPassenger(player);

            }

        }

    }

    @EventHandler
    public void chairExitEvent(EntityDismountEvent e) {
        if(e.getEntity() instanceof Player) {
            final Entity dismounted = e.getDismounted();
            if(dismounted instanceof Arrow && dismounted.getType() == EntityType.ARROW) dismounted.remove();
        }
    }

}
