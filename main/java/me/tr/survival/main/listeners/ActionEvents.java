package me.tr.survival.main.listeners;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.StaffManager;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionEvents implements Listener {


    // Here downwards are just events for disabling unwanted actions from players or staff


    private static final Map<UUID, Long> lastCommand = new HashMap<>();

    @EventHandler
    public void onCommandPreProcessEvent(PlayerCommandPreprocessEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if(System.currentTimeMillis() - Util.joined.getOrDefault(player.getUniqueId(), System.currentTimeMillis()) < 5000L) {
            e.setCancelled(true);
            return;
        }

        if(!lastCommand.containsKey(uuid)) {
            lastCommand.put(uuid, System.currentTimeMillis());
        } else {

            long last = lastCommand.get(uuid);
            long now = System.currentTimeMillis();
            if(now - last < 1000 * 2) {
                if(!Ranks.isStaff(uuid)) {
                    e.setCancelled(true);
                    Chat.sendMessage(player, "Rauhoituthan noiden komentojen kanssa!");
                }
            } else {
                lastCommand.remove(uuid);
            }
        }

        if(Events.deathIsland.contains(player.getUniqueId())) {
            if(player.isOp()) return;
            e.setCancelled(true);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Kun olet odottamassa pääsyä takaisin elävien joukkoon et voi suorittaa mitään komentoja!");
        }

    }

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent e) {
        if(e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if(StaffManager.hasStaffMode(player)) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();

        if(player.getAllowFlight()) {
            if(Util.getRegions(player).size() < 1) {
                if(!StaffManager.hasStaffMode(player)) {
                    if(player.isFlying()) {
                        player.teleport(e.getFrom());
                        player.setFlying(false);
                        int y = player.getWorld().getHighestBlockYAt(player.getLocation().getBlockX(),player.getLocation().getBlockZ());
                        player.teleport(new Location(player.getWorld(), player.getLocation().getX(), (double)y + 1, player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
                        Chat.sendMessage(player, "Lentäminen on sallittua vain §eSpawn§7-alueella!");
                    }
                    player.setAllowFlight(false);
                }
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getReason().equalsIgnoreCase("disconnect.spam")) {
            e.setCancelled(true);
            return;
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getType().equals(EntityType.PHANTOM)) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPortalEnter(PlayerPortalEvent e) {
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL || e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            e.setCancelled(true);
            e.getPlayer().teleport(e.getFrom());
            Chat.sendMessage(e.getPlayer(), Chat.Prefix.ERROR, "Portaalit eivät valitettavasti toimi. Pääset kuitenkin toisiin maailmoihin komennolla §a/matkusta§7!");
        }
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        if(e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            e.setCancelled(true);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Portaalit eivät valitettavasti toimi. Pääset kuitenkin toisiin maailmoihin komennolla §a/matkusta§7!");
        }
    }

    @EventHandler
    public void onTrade(InventoryOpenEvent e) {

        Player player = (Player) e.getPlayer();

        if(System.currentTimeMillis() - Util.joined.getOrDefault(player.getUniqueId(), System.currentTimeMillis()) < 5000L) {
            e.setCancelled(true);
            return;
        }

        if(e.getInventory().getType() == InventoryType.MERCHANT) {
            e.setCancelled(true);
        }

        Sorsa.async(() -> {

            for(int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if(item == null) continue;
                if(item.getType() == Material.AIR) continue;
                if(item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().hasLore()) {
                    final int slot = i;
                    Sorsa.task(() -> player.getInventory().setItem(slot, new ItemStack(Material.AIR)));
                }
            }

        });

    }

}
