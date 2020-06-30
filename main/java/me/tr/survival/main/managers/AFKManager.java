package me.tr.survival.main.managers;

import me.tr.survival.main.Main;
import me.tr.survival.main.other.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AFKManager implements CommandExecutor, Listener {

    private final Map<UUID, Long> lastInteraction = new HashMap<>();
    private final List<UUID> afk = new ArrayList<>();

    public void enableChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for(Map.Entry<UUID, Long> entry : lastInteraction.entrySet()) {

                    final UUID uuid = entry.getKey();
                    final Long last = entry.getValue();
                    final long now = System.currentTimeMillis();

                    long timePassed = now - last;

                    // If more than 5 min has passed from the last interaction player is AFK
                    if(timePassed >= 1000 * 60 * 5) enableAFK(uuid);

                }

            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20, 20 * 60);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            final Player player = (Player) sender;
            final UUID uuid = player.getUniqueId();

            if(toggleAFK(uuid)) Chat.sendMessage(player, "Olet nyt AFK!");
            else Chat.sendMessage(player, "Et ole enää AFK!");

        }

        return true;
    }

    // Returns true, if afk was toggled ON, otherwise false
    public boolean toggleAFK(final UUID uuid) {
        if(!afk.contains(uuid)) {
            enableAFK(uuid);
            return true;
        } else {
            disableAFK(uuid);
            return false;
        }
    }

    private void enableAFK(UUID uuid) {
        afk.add(uuid);
        final Player player = Bukkit.getPlayer(uuid);
        if(player != null) changeTabName(player);
    }

    private void disableAFK(UUID uuid)  {
        afk.remove(uuid);
        final Player player = Bukkit.getPlayer(uuid);
        if(player != null) changeTabName(player);
    }

    private void changeTabName(final Player player) {
        if(afk.contains(player.getUniqueId())) {
            String tabDisplayName = Ranks.getRankColor(Ranks.getRank(player.getUniqueId())) + player.getName();
            player.setPlayerListName(tabDisplayName + " §8[AFK]");
        } else player.setPlayerListName(player.getName());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        // Update the last interaction for the player
        lastInteraction.put(player.getUniqueId(), System.currentTimeMillis());
        // Disable AFK for the player
        disableAFK(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        final Player player = e.getPlayer();
        lastInteraction.put(player.getUniqueId(), System.currentTimeMillis());
        // Disable AFK for the player
        disableAFK(player.getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        final Player player = e.getPlayer();
        lastInteraction.put(player.getUniqueId(), System.currentTimeMillis());
        // Disable AFK for the player
        disableAFK(player.getUniqueId());
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        final Player player = e.getPlayer();
        lastInteraction.put(player.getUniqueId(), System.currentTimeMillis());
        // Disable AFK for the player
        disableAFK(player.getUniqueId());
    }

}
