package me.tr.survival.main.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VoteListener;
import com.vexsoftware.votifier.model.VotifierEvent;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.Main;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Mail;
import me.tr.survival.main.managers.StaffManager;
import me.tr.survival.main.other.Ranks;
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
    public final HashMap<UUID, Location> deathLocation = new HashMap<>();

    @EventHandler
    public void onVote(VotifierEvent e) {
        Vote vote = e.getVote();
        String userName = vote.getUsername();
        OfflinePlayer op = Bukkit.getOfflinePlayer(userName);
        Sorsa.logColored("§a[Vote] The vote of " + userName + " was registered!");
        Mail.addTickets(op.getUniqueId(), 1);
        Player player = op.getPlayer();
        if(player != null && op.isOnline()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            Chat.sendMessage(player, "Äänestit ja sait arvan! Lunasta arpa komennolla §a/posti§7!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        lastLocation.put(player.getUniqueId(), player.getLocation());
        deathLocation.put(player.getUniqueId(), player.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {
        final Player player = e.getPlayer();
        final FileConfiguration config = Main.getInstance().getConfig();

        if(deathIsland.contains(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        final Location destination = e.getTo();
        final Chunk destChunk = destination.getChunk();
        if(!destChunk.isLoaded()) {
            e.setCancelled(true);
            if(!destChunk.load(true)) {
                Sorsa.logColored("§e[Teleport] Could not load the chunk at " + Util.formatLocation(destination) + " in '" + destChunk.getWorld().getName() + "'!");
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Jotain meni pieleen viedessä sinua kyseiseen sijaintiin! Yritä pian uudestaan!");
            } else {
                player.teleport(destination);
            }
        }

        if(config.getBoolean("effects.teleport.enabled")) {
            if(e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) return;
            if(e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) return;
            if(!Main.getStaffManager().hasStaffMode(player)) {
                player.getWorld().playSound(e.getFrom(),
                        Sound.valueOf(config.getString("effects.teleport.sound")), 1, 1);
            }
        }
        if(!Boosters.isActive(Boosters.Booster.EXTRA_HEARTS)) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);

        // Fix so that player's cannot teleport each others to the PVP-zone
        if(Util.isInRegion(destination, "pvp-kuoppa")) {
            e.setCancelled(true);
            Chat.sendMessage(player, "§7Sinua yritettiin viedä §cPvP-alueelle§7, mutta me estimme sen tapahtumasta. PvP-alue löytyy §eSpawnin §7lähettyviltä.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
            player.teleport(e.getFrom());
            return;
        }

        // Add resistance effect so the player would not take that much
        // damage from possible suffocation
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) Sorsa.getCurrentTPS() * 15, 1));

        Sorsa.logColored("§a[Teleport] The player " + player.getName() + " (" + player.getUniqueId() + ") was sent from '" + Util.formatLocation(e.getFrom()) + "' to '" + Util.formatLocation(e.getTo()) + "'!");
        lastLocation.put(player.getUniqueId(), e.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {

        if(e.getEntity() instanceof Player) {

            Player player = (Player) e.getEntity();
            int foodLevelToChange = e.getFoodLevel();
            int currentFoodLevel = player.getFoodLevel();

            // Food level is decreasing
            if(foodLevelToChange < currentFoodLevel) {
                if(Boosters.isActive(Boosters.Booster.NO_HUNGER)) {
                    e.setCancelled(true);
                }
            }

        }

    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {

        Player player = e.getPlayer();
        if(!Ranks.isStaff(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        if(!Main.getStaffManager().hasStaffMode(player)) {
            e.setCancelled(true);
            Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
            return;
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityKill(EntityDeathEvent e) {

        if(e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            Location loc = player.getLocation();
            Sorsa.logColored("§6[DeathLog] Player " + player.getName() + " (" + player.getUniqueId() + ")" +
                    " at X: " + Util.formatLocation(loc) + " in '" + loc.getWorld().getName() + "'! Last damage cause: " +
                    (player.getLastDamageCause() != null ? player.getLastDamageCause().getCause() : "Not determinable"));
        }

        if(Boosters.isActive(Boosters.Booster.DOUBLE_XP) && e.getEntity().getKiller() != null) {
            e.setDroppedExp(e.getDroppedExp() * 2);
            if(e.getDroppedExp() >= 1) Util.sendNotification(e.getEntity().getKiller(), "§a§lTEHOSTUS §7Tupla XP!", false);
        }

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

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        final Player player = e.getPlayer();
        final Block block = e.getBlock();
        final UUID uuid = player.getUniqueId();
        final Material mat = block.getType();
        final ItemStack tool = player.getInventory().getItemInMainHand();

        PlayerData.add(player.getUniqueId(), "total", 1);

        if(!Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {
            if(Boosters.isActive(Boosters.Booster.MORE_ORES)) {

                if(mat == Material.EMERALD_ORE || mat == Material.DIAMOND_ORE || mat == Material.LAPIS_ORE) {
                    // Add one of every drop (2x)
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), (task -> {
                        for(final ItemStack item : block.getDrops(tool, player)) {
                            if(item == null) continue;
                            if(item.getType() == Material.AIR) continue;
                            block.getWorld().dropItem(block.getLocation(), item.clone());
                        }
                        task.cancel();
                    }), 2);
                    Util.sendNotification(player, "§a§lTEHOSTUS §72x oret!");
                }
            }
        } else if(Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {

            int dropAmount = 1;
            if(tool != null && tool.getType() != Material.AIR) dropAmount = block.getDrops(tool, player).size();

            if(mat == Material.IRON_ORE || mat == Material.GOLD_ORE) {

                Util.sendNotification(player, "§a§lTEHOSTUS §7Välittömät oret!");
                if(Boosters.isActive(Boosters.Booster.MORE_ORES)) dropAmount = dropAmount * 2;

                ItemStack item;
                if(mat == Material.IRON_ORE) item = new ItemStack(Material.IRON_INGOT, dropAmount);
                else item = new ItemStack(Material.GOLD_INGOT, dropAmount);

                player.getInventory().addItem(item);
                player.updateInventory();

                block.setType(Material.AIR);

            }
        }

        // Add data to block breaks
        if(mat == Material.IRON_ORE) PlayerData.add(uuid, "iron", 1);
        else if(mat == Material.GOLD_ORE) PlayerData.add(uuid, "gold", 1);
        else if(mat == Material.COAL_ORE) PlayerData.add(uuid, "coal", 1);
        else if(mat == Material.DIAMOND_ORE) PlayerData.add(uuid, "diamond", 1);

    }
}
