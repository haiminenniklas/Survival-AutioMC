package me.tr.survival.main;

import com.songoda.ultimatetimber.UltimateTimber;
import me.tr.survival.main.database.PlayerAliases;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Events implements Listener {

    @EventHandler
    public void onLevelUp(LevelUpEvent e){
        Player player = e.getPlayer();

        Util.sendNotification(player, "§a§lTASO! §7Nousit tasolle §c" + e.getLevel() + "§7!");
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
        player.sendMessage("§7Tervetuloa §c§lAutioMC§7-palvelimelle!");
        player.sendMessage(" ");
        player.sendMessage("§7Pelaajia paikalla: §c" + Bukkit.getOnlinePlayers().size());
        player.sendMessage("§7Apua: §c/apua");
        player.sendMessage(" ");
        player.sendMessage("§chttp://autiomc.eu");
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

        Main.getInstance().getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {

            PlayerAliases.load(player);
            PlayerAliases.add(player, player.getAddress().getHostName());

        });



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

        for(Player r : e.getRecipients()) {
            if(!Settings.get(r.getUniqueId(), "chat")) {
                e.getRecipients().remove(r);
            }
        }

        if(Ranks.getRank(uuid).equalsIgnoreCase("default")) {
            e.setFormat("§70 " + player.getName() + ":§r " + e.getMessage());
        } else {
            e.setFormat("§70 " + Ranks.getPrefix(Ranks.getRank(uuid)) + " §7" + player.getName() + ":§r " + e.getMessage());
        }

        if(e.getMessage().startsWith("#") && Ranks.isStaff(uuid)) {
            e.setCancelled(true);
            for(Player online : Bukkit.getOnlinePlayers()) {
                if(Ranks.isStaff(online.getUniqueId())) {
                    online.sendMessage("§7§l(§c§lYLLÄPITO§7§l) §c" + player.getName() + " §7» §f" + e.getMessage().substring(1));
                }
            }
        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        Location loc = e.getTo();

        // RTP Portal
        if(loc.getBlockZ() == 29 && (loc.getBlockX() <= 15 && loc.getBlockX() >= 11) &&
                e.getTo().getWorld().getName().equalsIgnoreCase(Autio.getSpawn().getWorld().getName())) {
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
    public void onTeleport(PlayerTeleportEvent e) {

        Player player = e.getPlayer();
        FileConfiguration config = Main.getInstance().getConfig();

        if(config.getBoolean("effects.teleport.enabled")) {
            player.getWorld().playSound(e.getFrom(),
                    Sound.valueOf(config.getString("effects.teleport.sound")), 1, 1);
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();
        UUID uuid = player.getUniqueId();

        PlayerData.add(player.getUniqueId(), "total", 1);
        int random = new Random().nextInt(100);

        if(block.getType() == Material.DIAMOND_ORE) {
            PlayerData.add(uuid, "diamond", 1);
            if(random <= 10) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §c" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.GOLD_ORE) {
            PlayerData.add(uuid, "gold", 1);
            if(random <= 5) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §c" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.IRON_ORE) {
            PlayerData.add(uuid, "iron", 1);
            if(random <= 3) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §c" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.COAL_ORE) {
            PlayerData.add(uuid, "coal", 1);
            if(random <= 1) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §c" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.EMERALD_ORE) {
            if(random <= 18) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §c" + add  + " §7kristallia!");
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
