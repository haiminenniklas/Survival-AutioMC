package me.tr.survival.main.listeners;

import me.tr.survival.main.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class ChairEvents implements Listener {

    @EventHandler
    public void onBlockClick(PlayerInteractEvent e) {

        final Player player = e.getPlayer();
        final Block block = e.getClickedBlock();
        if(block != null && block.getType() != Material.AIR) {

            if(Util.isBlockStair(block)) {

                final Action action = e.getAction();
                if(action == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
                    final Location loc = block.getLocation();
                    ArmorStand seat = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(-0.5d, 1.7d, -0.5d), EntityType.ARMOR_STAND);
                    seat.setInvulnerable(true);
                    seat.setGravity(false);
                    seat.setVisible(false);
                    player.setSneaking(false);
                    seat.addPassenger(player);
                }
            }
        }
    }

    @EventHandler
    public void chairExitEvent(EntityDismountEvent e) {
        if(e.getEntity() instanceof Player) {
            final Entity dismounted = e.getDismounted();
            if(dismounted instanceof ArmorStand && dismounted.getType() == EntityType.ARMOR_STAND) dismounted.remove();
        }
    }
}
