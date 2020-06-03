package me.tr.survival.main.listeners;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.Main;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.managers.features.Boosters;
import me.tr.survival.main.other.events.LevelUpEvent;
import me.tr.survival.main.database.data.Balance;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Events implements Listener {

    public final HashMap<UUID, Boolean> adminMode = new HashMap<>();
    public final HashMap<UUID, Location> lastLocation = new HashMap<>();
    final ArrayList<UUID> deathIsland = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        lastLocation.put(player.getUniqueId(), player.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent e) {
        if(Boosters.isActive(Boosters.Booster.DOUBLE_XP) && e.getEntity().getKiller() != null) {
            e.setDroppedExp(e.getDroppedExp() * 2);
            if(e.getDroppedExp() >= 1) Util.sendNotification(e.getEntity().getKiller(), "§a§lTEHOSTUS §7Tupla XP!", false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        FileConfiguration config = Main.getInstance().getConfig();
        if(deathIsland.contains(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if(config.getBoolean("effects.teleport.enabled")) {
            if(e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) return;
            if(e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) return;
            player.getWorld().playSound(e.getFrom(),
                    Sound.valueOf(config.getString("effects.teleport.sound")), 1, 1);
        }
        if(!Boosters.isActive(Boosters.Booster.EXTRA_HEARTS)) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);
        }
        lastLocation.put(player.getUniqueId(), e.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if(Boosters.isActive(Boosters.Booster.NO_HUNGER)) e.setCancelled(true);
        else e.setCancelled(false);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent e) {
        if(e.getEntity() instanceof Villager) {
            Villager villager = (Villager) e.getEntity();
            if(villager.getKiller() != null) {
                Player killer = villager.getKiller();
                int amount = new Random().nextInt(25) + 1;
                if(amount >= 1 && (Math.random() <= 0.5)) {
                    Balance.add(killer.getUniqueId(), (double) amount);
                    Chat.sendMessage(killer, "Tapoit raa'asti kyläläisen, mutta hän sinun onneksesi kantoi mukanaan §e" + amount + "€§7!");
                    killer.playSound(killer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {

        Player player = e.getPlayer();

        if(!Boosters.isActive(Boosters.Booster.EXTRA_HEARTS))
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);

        if(!player.hasPermission("deathisland.bypass") && !deathIsland.contains(player.getUniqueId())) {

            Sorsa.after(1, () -> {
                Location deathSpawn = Sorsa.getDeathSpawn();
                player.teleport(deathSpawn);
                Util.heal(player);

                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 30, 999, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 30, 1, true, false));
                player.playSound(player.getLocation(), Sound.MUSIC_DISC_13, 1, 1);
                deathIsland.add(player.getUniqueId());
            });

            new BukkitRunnable() {

                int timer = 30;

                @Override
                public void run() {
                    if(!deathIsland.contains(player.getUniqueId())) {
                        cancel();
                        return;
                    }

                    if(timer > 0) {
                        player.sendTitle("§c§lKUOLIT", "§7Pääset §c" + timer + "s §7päästä takaisin!", 15, 20, 15);
                        timer--;
                    } else {

                        Sorsa.task(() -> {
                            Util.heal(player);
                            player.sendTitle("§a§lTAKAISIN", "§7Olet taas elossa!", 15, 20, 15);
                            deathIsland.remove(player.getUniqueId());
                            player.stopSound(Sound.MUSIC_DISC_13);
                            Sorsa.teleportToSpawn(player);
                        });
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(Main.getInstance(), 25, 20);

        } else {
            Sorsa.teleportToSpawn(player);
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();
        UUID uuid = player.getUniqueId();
        Material mat = block.getType();

        PlayerData.add(player.getUniqueId(), "total", 1);

        if(Boosters.isActive(Boosters.Booster.MORE_ORES)) {
            Collection<ItemStack> drops = block.getDrops();
            if(mat == Material.EMERALD_ORE || mat == Material.DIAMOND_ORE || mat == Material.LAPIS_ORE) {
                // Add one of every drop (2x)
                for(ItemStack item : drops) {
                    e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), item.clone());
                }
                Util.sendNotification(player, "§a§lTEHOSTUS §72x oret!");
            }
        }
        if(Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {

            if(mat == Material.IRON_ORE || mat == Material.GOLD_ORE) {
                block.setType(Material.AIR);
                ItemStack item;
                if(mat == Material.IRON_ORE) {
                    item = new ItemStack(Material.IRON_INGOT);
                    PlayerData.add(uuid, "iron", 1);
                } else {
                    item = new ItemStack(Material.GOLD_INGOT);
                    PlayerData.add(uuid, "gold", 1);
                }
                Util.sendNotification(player, "§a§lTEHOSTUS §7Välittömät oret!");
                e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), item.clone());

            }

        }

    }


}
