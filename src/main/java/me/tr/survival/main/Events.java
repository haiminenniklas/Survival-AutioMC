package me.tr.survival.main;

import me.tr.survival.main.database.PlayerAliases;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.events.LevelUpEvent;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

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
            Main.teleportToSpawn(e.getPlayer());
        }

        player.setPlayerListHeaderFooter(
                ChatColor.translateAlternateColorCodes('&', config.getString("tablist.header")),
                ChatColor.translateAlternateColorCodes('&', config.getString("tablist.footer"))
        );

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

        if(Ranks.getRank(uuid) == "default") {
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
    public void onInvClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null) return;
        if(e.getCurrentItem() == null) return;

        if(e.getView().getTitle().contains("§r")) e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        if(!player.isOp()) e.setCancelled(true);

        if(Gui.getGui(player) != null) {
            System.out.println(Gui.getGui(player).getTitle());
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
            player.getWorld().playSound(e.getTo(),
                    Sound.valueOf(config.getString("effects.teleport.sound")), 1, 1);
            player.getWorld().playSound(e.getFrom(),
                    Sound.valueOf(config.getString("effects.teleport.sound")), 1, 1);
        }

    }
}
