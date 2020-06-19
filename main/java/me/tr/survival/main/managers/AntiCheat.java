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
        if(!e.getPlayer().getLocation().getChunk().isLoaded()) {
            double newLocX = e.getPlayer().getLocation().getX() - 250;
            double currentY = e.getPlayer().getLocation().getY();
            double newLocZ = e.getPlayer().getLocation().getZ() - 250;
            e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), newLocX, currentY, newLocZ));
            e.getPlayer().sendMessage("§c§lVAROITUS §7» Lohkot lataavat hitaasti, sinua on siirretty taaksepäin!");
        }

        float threshold = e.getPlayer().isOnGround() ? 0.41f : 0.65f;
        float deltaXZ = (float) Math.sqrt(Math.pow(e.getTo().getX() - e.getFrom().getX(), 2) + Math.pow(e.getTo().getZ() - e.getFrom().getZ(), 2)), deltaY = (float) (e.getTo().getY() - e.getFrom().getY());

        if(e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            threshold+= 2.9f;
        }

        if(e.getPlayer().isRiptiding()) return;
        if(e.getPlayer().isGliding()) return;

        if(e.getPlayer().getVehicle() != null) {
            final Entity vehicle = e.getPlayer().getVehicle();
            if(vehicle instanceof Boat) return;
        }

        if (e.getPlayer().isFlying()) {
            threshold+= e.getPlayer().getFlySpeed() > 0.1 ? e.getPlayer().getFlySpeed() * 10 : e.getPlayer().getFlySpeed() * 1;
        } else {
            threshold+= e.getPlayer().getWalkSpeed() > 0.2 ? e.getPlayer().getWalkSpeed() * 1.1 : e.getPlayer().getWalkSpeed() * 0.2;
        }
        if(!PreVL.containsKey(e.getPlayer())) {
            PreVL.put(e.getPlayer(), 0);
        }
        if(Bypass.contains(e.getPlayer())) {
            PreVL.put(e.getPlayer(), 0);
            return;
        }
        if(deltaXZ > threshold) {
            int current = PreVL.get(e.getPlayer());
            PreVL.replace(e.getPlayer(), current + 1);
            if(PreVL.get(e.getPlayer()) > 2) {
                PreVL.replace(e.getPlayer(), 0);
                e.getPlayer().sendMessage("§c§lVAROITUS §7» Liikut liian nopeasti!");
                e.setCancelled(true);
                double increase = Math.round(((deltaXZ - threshold) / threshold) * 100);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.hasPermission("antigg.staff")) {
                        TextComponent message = new TextComponent("§8[§e§l⚡§8] §e" + e.getPlayer().getName() + " §8- §fSpeed A §8[§a" + increase + "%§8]");
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + e.getPlayer().getName()));
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("* deltaXZ=§a" + deltaXZ + "\n* maxSpeed=§a" + threshold).create()));
                        online.spigot().sendMessage(message);
                    }
                }
            }
        } else {
            if(PreVL.containsKey(e.getPlayer())) {
                int current = PreVL.get(e.getPlayer());
                if (current > 0) {
                    PreVL.replace(e.getPlayer(), current - 1);
                }
            }
        }
    }
}