package me.tr.survival.main.managers;

import me.tr.survival.main.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class AntiCheat implements Listener {

    public static HashMap<Player, Integer> PreVL = new HashMap<>();
    public static ArrayList<Player> Bypass = new ArrayList<>();


    @EventHandler
    public void onToggle(PlayerToggleFlightEvent e) {
        if (!Bypass.contains(e.getPlayer())) {
            Bypass.add(e.getPlayer());
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bypass.remove(e.getPlayer());
                }
            }.runTaskLater(Main.getInstance(), 20);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        
        final Player player = e.getPlayer();
        
        if(!player.getLocation().getChunk().isLoaded()) {
            double newLocX = player.getLocation().getX() - 250;
            double currentY = player.getLocation().getY();
            double newLocZ = player.getLocation().getZ() - 250;
            player.teleport(new Location(Bukkit.getWorld("world"), newLocX, currentY, newLocZ));
            player.sendMessage("§c§lVAROITUS §7» Lohkot lataavat hitaasti, sinua on siirretty taaksepäin!");
        }

        float threshold = player.isOnGround() ? 0.41f : 0.65f;
        float deltaXZ = (float) Math.sqrt(Math.pow(e.getTo().getX() - e.getFrom().getX(), 2) + Math.pow(e.getTo().getZ() - e.getFrom().getZ(), 2)), deltaY = (float) (e.getTo().getY() - e.getFrom().getY());

        if(player.getGameMode().equals(GameMode.SPECTATOR)) threshold+= 2.9f;

        if(player.isRiptiding()) return;
        if(player.isGliding()) return;
        if(player.isInsideVehicle()) return;

        if(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) return;

        if (player.isFlying()) {
            threshold+= player.getFlySpeed() > 0.1 ? player.getFlySpeed() * 10 : player.getFlySpeed() * 1;
        } else {
            threshold+= player.getWalkSpeed() > 0.2 ? player.getWalkSpeed() * 1.1 : player.getWalkSpeed() * 0.2;
        }
        if(!PreVL.containsKey(player)) {
            PreVL.put(player, 0);
        }
        if(Bypass.contains(player)) {
            PreVL.put(player, 0);
            return;
        }
        if(deltaXZ > threshold) {
            int current = PreVL.get(player);
            PreVL.replace(player, current + 1);
            if(PreVL.get(player) > 2) {
                PreVL.replace(player, 0);
                player.sendMessage("§c§lVAROITUS §7» Liikut liian nopeasti!");
                e.setCancelled(true);
                double increase = Math.round(((deltaXZ - threshold) / threshold) * 100);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.hasPermission("antigg.staff")) {
                        TextComponent message = new TextComponent("§8[§e§l⚡§8] §e" + player.getName() + " §8- §fSpeed A §8[§a" + increase + "%§8]");
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName()));
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("* deltaXZ=§a" + deltaXZ + "\n* maxSpeed=§a" + threshold).create()));
                        online.spigot().sendMessage(message);
                    }
                }
            }
        } else {
            if(PreVL.containsKey(player)) {
                int current = PreVL.get(player);
                if (current > 0) {
                    PreVL.replace(player, current - 1);
                }
            }
        }
    }
}