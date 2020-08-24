package me.tr.survival.main.listeners;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.StaffManager;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionEvents implements Listener {


    // Here downwards are just events for disabling unwanted actions from players or staff

    private final Map<UUID, Long> lastCommand = new HashMap<>();

    @EventHandler
    public void onCommandPreProcessEvent(PlayerCommandPreprocessEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if(System.currentTimeMillis() - Util.joined.getOrDefault(player.getUniqueId(), System.currentTimeMillis()) < 5000L) {
            e.setCancelled(true);
            return;
        }

        if(!lastCommand.containsKey(uuid)) lastCommand.put(uuid, System.currentTimeMillis());
        else {
            long last = lastCommand.get(uuid);
            long now = System.currentTimeMillis();
            if(now - last < 1000 * 2) {
                if(!Ranks.isStaff(uuid)) {
                    e.setCancelled(true);
                    Chat.sendMessage(player, "Rauhoituthan noiden komentojen kanssa!");
                }
            } else lastCommand.remove(uuid);
        }

        if(Main.getEventsListener().deathIsland.contains(player.getUniqueId())) {
            if(player.isOp()) return;
            e.setCancelled(true);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Kun olet odottamassa pääsyä takaisin elävien joukkoon et voi suorittaa mitään komentoja!");
        }

    }

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent e) {
        if(e.getEntity() instanceof Player) {
            final Player player = (Player) e.getEntity();
            Util.checkForIllegalItems(player);
            if(Main.getStaffManager().hidden.contains(player.getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        final Player player = e.getPlayer();
        final World world = e.getTo().getWorld();
        if(world.getEnvironment() == World.Environment.NETHER && e.getTo().getBlockY() > 130) {
            e.setCancelled(true);
           // Sorsa.teleportToNether(player);
            Chat.sendMessage(player, "Netheriä voit tutkia vain Netherin sisällä! Soo soo!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        // SHOP ELEVATOR
        if(world.getName().equalsIgnoreCase("world")) {

            Location downSpawn = new Location(world, -6.5, 46.5, -27.5, -178f, 0.0f);
            Location upSpawn = new Location(world, -6.5, 56.5, -27.5, -180f, 0.0f);

            Location current = player.getLocation();
            if(Util.isNumberInRange(current.getBlockX(), -8, -7) && current.getBlockZ() == -24) {
                if(Util.isNumberInRange(current.getBlockY(), 46, 48)) {
                    player.teleport(upSpawn);
                    Util.sendNotification(player, "§a§lKAUPPA! §7Matkustit yläkertaan!");
                } else if(Util.isNumberInRange(current.getBlockY(), 56, 58)) {
                    player.teleport(downSpawn);
                    Util.sendNotification(player, "§a§lKAUPPA! §7Matkustit alakertaan!");
                }
            }
        }

        if(player.getAllowFlight()) {
            if(Util.getRegions(player).size() < 1 || Util.isInRegion(player, "pvp-kuoppa")) {
                if(!Main.getStaffManager().hasStaffMode(player)) {
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
    public void onTeleport(PlayerTeleportEvent e) {

        Player player = e.getPlayer();
        if(e.getTo().getWorld().getEnvironment() == World.Environment.NETHER) {
            if(!Main.getStaffManager().hasStaffMode(player)) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getReason().equalsIgnoreCase("disconnect.spam")) e.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        // if (event.getEntity().getType().equals(EntityType.PHANTOM)) event.setCancelled(true);

        final CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

        if(spawnReason == CreatureSpawnEvent.SpawnReason.DEFAULT || spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {

            final EntityType spawnedEntityType = event.getEntityType();
            final Entity spawnedEntity = event.getEntity();

            final EntityType[] whitelist = new EntityType[] {
                    EntityType.IRON_GOLEM,
                    EntityType.VILLAGER,
                    EntityType.PLAYER,
                    EntityType.ARMOR_STAND
            };

            boolean cannotSpawn = true;

            for(EntityType whitelisted : whitelist) {
                if(spawnedEntityType == whitelisted) cannotSpawn = false;
            }

            if(cannotSpawn) {
                double tps = Sorsa.getCurrentTPS();
                if(tps < 12) {
                    event.setCancelled(true);
                    spawnedEntity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onPortalEnter(PlayerPortalEvent e) {
        final Player player = e.getPlayer();
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            e.setCancelled(true);
            player.teleport(e.getFrom());
            Chat.sendMessage(e.getPlayer(), Chat.Prefix.ERROR, "Portaalit eivät valitettavasti toimi. Pääset kuitenkin toisiin maailmoihin komennolla §a/matkusta§7!");
            Sorsa.logColored("§6[TravelManager] The player " + player.getName() + " was tried to enter a portal, but was prohibited!");
        } else {
            e.setCancelled(true);
            if(e.getFrom().getWorld().getEnvironment() == World.Environment.NORMAL) {
                Sorsa.teleportToNether(player);
                Chat.sendMessage(player, "Suosittelemme, että käytät §a/matkusta §7komentoa matkustaaksesi §cNetheriin§7!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        if(e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            e.setCancelled(true);
            if(e.getReason() != PortalCreateEvent.CreateReason.FIRE) {
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Portaalit eivät valitettavasti toimi. Pääset kuitenkin toisiin maailmoihin komennolla §a/matkusta§7!");
                Sorsa.logColored("§6[TravelManager] The player " + player.getName() + " was tried to create a new portal, but was stopped!");
                return;
            }
            if(player.getWorld().getEnvironment() != World.Environment.NORMAL) {
                e.setCancelled(true);
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Nether portaalin luonti, toimii vain " +
                        "tavallisessa maailmassa. Suosittelemme muutenkin, että käytät komentoa §a/matkusta§7, päästäksesi muihin maailmoihin!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                Sorsa.logColored("§6[TravelManager] The player " + player.getName() + " was tried to create a new portal, but was stopped!");
                return;
            } else {
                e.setCancelled(false);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {

      //  final Player player = (Player) event.getWhoClicked();
        final Inventory inv = event.getClickedInventory();
        if(inv != null) {

            final ItemStack item = event.getCurrentItem();
            if(item != null && item.hasItemMeta()) {

               if(Main.getClaimBlockCouponsManager().isCoupon(item) || Main.getMoneyManager().isCheque(item)) {

                    final InventoryType invType = inv.getType();
                    if(invType == InventoryType.ANVIL || invType == InventoryType.GRINDSTONE || invType == InventoryType.WORKBENCH) {
                        event.setCancelled(true);
                        event.setResult(Event.Result.DENY);
                    }
               }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Util.checkForIllegalItems(player);
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
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Kyläläisten kanssa ei voi toistaiseksi tehdä vaihtokauppaa. Toiminto todennäköisesti tulossa myöhemmin käyttöön. Lisätietoa §9/discord§7!");
        }
        Util.checkForIllegalItems(player);
    }

}
