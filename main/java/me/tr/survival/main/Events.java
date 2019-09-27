package me.tr.survival.main;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import me.tr.survival.main.database.PlayerAliases;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.booster.Boosters;
import me.tr.survival.main.other.events.LevelUpEvent;
import me.tr.survival.main.util.RTP;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Events implements Listener {

    public static final HashMap<UUID, Boolean> adminMode = new HashMap<>();
    public static final HashMap<UUID, Location> lastLocation = new HashMap<>();

    @EventHandler
    public void onException(ServerExceptionEvent e) {

        for(Player player : Bukkit.getOnlinePlayers()) {

            if(player.isOp() && adminMode.containsKey(player.getUniqueId())) {
                if(adminMode.get(player.getUniqueId())) {

                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Virhe: §6" + e.getException().getMessage());

                }
            }

        }

    }

    @EventHandler
    public void onLevelUp(LevelUpEvent e){
        Player player = e.getPlayer();

        Util.sendNotification(player, "§a§lTASO! §7Nousit tasolle §6" + e.getLevel() + "§7!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

    }

    @EventHandler
    public void onAsyncLogin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        PlayerData.loadPlayer(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        FileConfiguration config = Main.getInstance().getConfig();

        player.sendMessage("§7§m--------------------------");
        player.sendMessage("§7Tervetuloa §6§lNimettömälle §7 palvelimelle!");
        player.sendMessage(" ");
        player.sendMessage("§7Pelaajia paikalla: §6" + Bukkit.getOnlinePlayers().size());
        player.sendMessage("§7Apua: §6/apua");
        player.sendMessage(" ");
        player.sendMessage("§6http://autiomc.eu");
        player.sendMessage("§7§m--------------------------");

        if(Ranks.isVIP(player.getUniqueId()) || Ranks.isStaff(player.getUniqueId())) {
            e.setJoinMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            Main.getInstance().getConfig().getString("messages.join").replaceAll("%player%", player.getName())));
        } else {
            e.setJoinMessage(null);
        }

        if(!player.hasPlayedBefore()) {
            Autio.teleportToSpawn(e.getPlayer());
        }

        // DISABLED FOR NOW
        /*player.setPlayerListHeaderFooter(
                ChatColor.translateAlternateColorCodes('&', config.getString("tablist.header")),
                ChatColor.translateAlternateColorCodes('&', config.getString("tablist.footer"))
        ); */


        Main.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {

            PlayerAliases.load(player);
            PlayerAliases.add(player, player.getAddress().getHostName());
            PlayerData.loadPlayer(player.getUniqueId());

        }, 20 * 2);

        Autio.updatePlayer(player);

   }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        Player player = e.getPlayer();

        // Save player's data
        Main.getInstance().getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> PlayerData.savePlayer(player.getUniqueId()));

        if(Ranks.isVIP(player.getUniqueId()) || Ranks.isStaff(player.getUniqueId())) {
            e.setQuitMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            Main.getInstance().getConfig().getString("messages.leave").replaceAll("%player%", player.getName())));
        } else {
            e.setQuitMessage(null);
        }

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if(!Settings.get(player.getUniqueId(), "chat")) {
            Chat.sendMessage(player, "Sinulla on chat poissa päältä!");
        }

        for(Player r : e.getRecipients()) {
            if(!Settings.get(r.getUniqueId(), "chat") && !r.getName().equalsIgnoreCase(player.getName())) {
                e.getRecipients().remove(r);
            }
        }

        e.setFormat(Chat.getFormat(player, e.getMessage()));

        if(e.getMessage().startsWith("#") && Ranks.isStaff(uuid)) {
            e.setCancelled(true);
            for(Player online : Bukkit.getOnlinePlayers()) {
                if(Ranks.isStaff(online.getUniqueId())) {
                    online.sendMessage("§7§l(§6§lYLLÄPITO§7§l) §6" + player.getName() + " §7» §f" + e.getMessage().substring(1));
                }
            }
        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        Location loc = e.getTo();

        // RTP Portal
        if(loc.getBlockZ() == -39 && (loc.getBlockX() <= 42 && loc.getBlockX() >= 38) &&
                e.getTo().getWorld().getName().equalsIgnoreCase(Autio.getSpawn().getWorld().getName())
                && loc.getY() <= 136 && loc.getY() >= 133) {
            if(!RTP.teleport(player)) {
                // Bounce player back
                Util.bounceBack(player, e.getFrom(), e.getTo());
            }
        }

    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null) return;
        if(e.getCurrentItem() == null) return;

        if(e.getView().getTitle().contains("§r")) e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        if(!player.isOp()) e.setCancelled(true);

        if(Gui.getGui(player) != null) {
            Gui gui = Gui.getGui(player);
            if(e.getCurrentItem() != null) {
                for(Button b : gui.getButtons()) {
                    if(b.item.clone().equals(e.getCurrentItem())) {
                        b.onClick(player, e.getClick());
                    }
                }
            }

        }

    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {

        Player player = (Player) e.getPlayer();
        if(e.getView().getTitle().contains("§r") && Gui.getGui(player) != null) {
            Gui.getGui(player).close(player);
        }

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        Player player = e.getEntity();

        lastLocation.put(player.getUniqueId(), player.getLocation());

    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {

        Player player = e.getPlayer();
        FileConfiguration config = Main.getInstance().getConfig();

        if(config.getBoolean("effects.teleport.enabled")) {
            player.getWorld().playSound(e.getFrom(),
                    Sound.valueOf(config.getString("effects.teleport.sound")), 1, 1);
        }

        lastLocation.put(player.getUniqueId(), e.getFrom());

    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if(Boosters.isActive(Boosters.Booster.NO_HUNGER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        if(Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {

            Block block = player.getTargetBlock(5);

            if(block != null && block.getType() != Material.AIR) {
                if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if(Util.isMineralOre(block)) {
                        block.breakNaturally();
                    }
                }
            }

        }

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {

        Player player = e.getPlayer();

        if(Boosters.isActive(Boosters.Booster.EXTRA_HEARTS)) {
            Util.heal(player);
            player.setHealth(22d);
        }

        Autio.teleportToSpawn(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();
        UUID uuid = player.getUniqueId();

        PlayerData.add(player.getUniqueId(), "total", 1);

        if(Boosters.isActive(Boosters.Booster.MORE_ORES)) {

            // Add one of every drop (2x)
            for(ItemStack item : block.getDrops()) {
                block.getDrops().add(item.clone());
            }

        }

        double random = new Random().nextDouble();

        /*

        Chances:

        Emerald: 10% (0.1)
        Diamond: 7% (0.07)
        Gold: 3% (0.03)
        Iron: 2% (0.02)
        Coal 0.5% (0.005)

         */

        if(block.getType() == Material.DIAMOND_ORE) {
            PlayerData.add(uuid, "diamond", 1);
            if(random <= 0.07) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §6" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.GOLD_ORE) {
            PlayerData.add(uuid, "gold", 1);
            if(random <= 0.03) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §6" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.IRON_ORE) {
            PlayerData.add(uuid, "iron", 1);
            if(random <= 0.02) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §6" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.COAL_ORE) {
            PlayerData.add(uuid, "coal", 1);
            if(random <= 0.005) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §6" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.EMERALD_ORE) {
            if(random <= 0.10) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §6" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }


    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getType().equals(EntityType.PHANTOM)) {
            event.setCancelled(true);
        }

    }

}
