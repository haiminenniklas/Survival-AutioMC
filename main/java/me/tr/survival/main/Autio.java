package me.tr.survival.main;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.PlayerGlowManager;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.booster.Boosters;
import me.tr.survival.main.util.staff.StaffManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Autio {

    private static Map<UUID, Long> debugs = new HashMap<>();

    public static void teleportToSpawn(Player player) {
        CompletableFuture<Boolean> result = player.teleportAsync(Autio.getSpawn());
        if(!result.join()) {
            player.teleport(Autio.getSpawn());
        }
        if(Boosters.isActive(Boosters.Booster.EXTRA_HEARTS)) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(24d);
            player.setHealth(24d);
        } else {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);
            player.setHealth(20d);
        }
    }

    public static void log(String msg) {
        if(!getConfig().getBoolean("other.logging")) return;
        Bukkit.getLogger().log(Level.INFO, msg);
    }

    public static void err(String msg) {
        Bukkit.getLogger().log(Level.WARNING, msg);
    }

    public static void logColored(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static void warn(String message) {
        Bukkit.getLogger().log(Level.WARNING, message);
    }

    public static void task(Runnable task) {
        Bukkit.getScheduler().runTask(Main.getInstance(), task);
    }

    public static void async(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), task);
    }

    public static void teleportToNether(Player player) {
        player.teleportAsync(new Location(Bukkit.getWorld("world_nether"), 0.5, 52, 0.5));
    }

    public static void every(int seconds, Runnable task, boolean async) {
        if(async) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
        } else {
            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
        }
    }

    public static void after(int seconds, Runnable task) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), task, 20 * seconds);
    }

    public static void after(int seconds, Runnable task, boolean async) {
        if(async) {
            Autio.after(seconds, task);
        } else {
            Autio.afterAsync(seconds, task);
        }
    }

    public static void afterAsync(int seconds, Runnable task) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), task, (long) getCurrentTPS() * seconds);
    }

    public static void every(int seconds, Runnable task) {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
    }

    public static void everyAsync(int seconds, Runnable task) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
    }

    public static double getCurrentTPS() {
        return Bukkit.getTPS()[0];
    }

    public static void setSpawn(Location loc) {

        FileConfiguration config = Main.getInstance().getConfig();
        config.set("spawn.x", loc.getX());
        config.set("spawn.y", loc.getY());
        config.set("spawn.z", loc.getZ());
        config.set("spawn.yaw", String.valueOf(loc.getYaw()));
        config.set("spawn.pitch", String.valueOf(loc.getPitch()));
        config.set("spawn.world", loc.getWorld().getName());

        Main.getInstance().saveConfig();
    }

    public static Location getSpawn() {

        FileConfiguration config = Main.getInstance().getConfig();

        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");

        float yaw = Float.parseFloat(config.getString("spawn.yaw"));
        float pitch = Float.parseFloat(config.getString("spawn.pitch"));

        World world = Bukkit.getWorld(config.getString("spawn.world"));

        return new Location(world, x, y, z, yaw, pitch);

    }


    public static void setDeathSpawn(Location loc) {

        FileConfiguration config = Main.getInstance().getConfig();
        config.set("deathspawn.x", loc.getX());
        config.set("deathspawn.y", loc.getY());
        config.set("deathspawn.z", loc.getZ());
        config.set("deathspawn.yaw", String.valueOf(loc.getYaw()));
        config.set("deathspawn.pitch", String.valueOf(loc.getPitch()));
        config.set("deathspawn.world", loc.getWorld().getName());

        Main.getInstance().saveConfig();
    }

    public static Location getDeathSpawn() {

        FileConfiguration config = Main.getInstance().getConfig();

        double x = config.getDouble("deathspawn.x");
        double y = config.getDouble("deathspawn.y");
        double z = config.getDouble("deathspawn.z");

        float yaw = Float.parseFloat(config.getString("deathspawn.yaw"));
        float pitch = Float.parseFloat(config.getString("deathspawn.pitch"));

        World world = Bukkit.getWorld(config.getString("deathspawn.world"));

        return new Location(world, x, y, z, yaw, pitch);

    }

    public static void updateTag(Player player) {

        String color = "&" + Ranks.getRankColor(Ranks.getRank(player.getUniqueId())).getChar();
       // Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nte player " + player.getName() + " prefix " + color);

    }

    public static FileConfiguration getConfig() {
        return Main.getInstance().getConfig();
    }

    public static void savePlayer(Player player) {
        Autio.async(() -> {
            PlayerData.savePlayer(player.getUniqueId());
        });
    }

    public static void loadPlayer(Player player) {
        Autio.async(() -> {
           PlayerData.loadPlayer(player.getUniqueId());
        });
    }

    public static void updatePlayer(Player player) {

        Autio.updateTag(player);
        loadPlayer(player);
        Settings.scoreboard(player);

    }

    public static Server getServer() {
        return Bukkit.getServer();
    }

    public static Main getPlugin() {
        return Main.getInstance();
    }

    public static void runDebug(Player player) {

        UUID uuid = player.getUniqueId();

        if(!player.isOp()) {

            if(!debugs.containsKey(uuid)) {
                debugs.put(uuid, System.currentTimeMillis());
            } else {

                long lastRun = debugs.get(uuid);
                long now = System.currentTimeMillis();

                if(now - lastRun < 1000 * 60 * 5) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Palvelimen suorituskyvyn suojaamiseksi, pystyt käynnistämään virheenkorjauksen §c5 minuutin §7välein!");
                    return;
                } else {
                    debugs.remove(uuid);
                }

            }

        }

        Chat.sendMessage(player, Chat.Prefix.DEBUG, "Korjataan yleiset virheet ja bugit...");
        Autio.updatePlayer(player);
        Boosters.debug();
        Particles.reloadParticles(player);
        PlayerGlowManager.disableGlow(player);
        Chat.sendMessage(player, Chat.Prefix.DEBUG, "Korjattu! Jos mikään ei muuttunut yritä poistua ja liittyä palvelimelle uudestaan!" +
                " Jos muutosta ei vieläkään näy, laita ilmoitus ylläpidollemme Discordissa kanavalla §9#ilmoita-bugeista§7!");
    }

    public static World getEndWorld() {
        return Bukkit.getWorld("world_the_end");
    }

    public static World getNetherWorld() {
        return Bukkit.getWorld("world_nether");
    }

    public static ProtocolManager getProtocolManager(){
        return ProtocolLibrary.getProtocolManager();
    }

    public static void sendTablist(Player player) {
        PacketContainer pc = getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

        pc.getChatComponents().write(0, WrappedChatComponent.fromText("\n  §2§lSorsaMC§7 - §aSurvival  \n"))
                .write(1, WrappedChatComponent.fromText("\n  §7Paikalla:  \n  §8⇻§a" + getOnlinePlayers().size() + "§8⇺  \n"));
        try
        {
            getProtocolManager().sendServerPacket(player, pc);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static LuckPerms getLuckPerms() {
        return Main.getLuckPerms();
    }

    public static PlayerParticlesAPI getParticlesAPI() {
        return Main.getParticlesAPI();
    }

    public static String getPrefix(Player player) {
        User user = getLuckPerms().getUserManager().getUser(player.getUniqueId());
        QueryOptions queryOptions = getLuckPerms().getContextManager().getQueryOptions(user).orElse(getLuckPerms().getContextManager().getStaticQueryOptions());
        if (user == null) {
            // user not loaded, even after requesting data from storage
            return "";
        }
        CachedMetaData data = user.getCachedData().getMetaData(queryOptions);
        String prefix = data.getPrefix();
        getLuckPerms().getUserManager().cleanupUser(user);
        if(prefix == null) return "";
        return prefix;

    }

    public static void toggleDebugMode(Player player) {
        UUID uuid = player.getUniqueId();
        if(!Events.adminMode.containsKey(uuid)) {
            Events.adminMode.put(uuid, true);
            Chat.sendMessage(player, Chat.Prefix.DEBUG, "Virheenkorjaustila päällä!");

           new BukkitRunnable() {
               @Override
               public void run() {
                   if(!Events.adminMode.containsKey(uuid)) {
                        cancel();
                       return;
                   }

                   Util.sendNotification(player, "§7TPS: §e" + Util.round(Autio.getCurrentTPS()) +
                           " §7| RAM: §a" + Util.getFreeMemory() + "Mb/" + Util.getMaxMemory() + "Mb" +
                           " §7| CPU: §b" + Util.getProcessCPULoad() + "%/" + Util.getSystemCPULoad() + "%");
               }
           }.runTaskTimerAsynchronously(Main.getInstance(), 20, 20);
        } else {
            Events.adminMode.remove(uuid);
            Chat.sendMessage(player, Chat.Prefix.DEBUG, "Virheenkorjaustila pois päältä!");
        }
    }

    public static void stopServer() {

       new BukkitRunnable() {

           int timer = 300;

           @Override
           public void run() {

                if(timer < 0) {

                    for(Player player : Bukkit.getOnlinePlayers()) {
                        //player.kickPlayer("§cPalvelin sammui \n §7Palvelin käynnistyy uudelleen §anoin minuutin §7kuluttua! Nähdään taas pian!");
                        sendBungeeMessage(player, "Connect", "lobby");
                        sendBungeeMessage(player, "Message", player.getName(), Chat.getPrefix() + " Palvelin, jossa aikaisemmin olit suljettiin ja sinut vietiin aulaamme. Odotathan noin §aminuutin§7, jotta palvelin saadaan uudelleen toimintaan!");
                    }

                    Bukkit.shutdown();
                    cancel();
                    return;
                }

                if(timer == 300) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l5 minuutin §7kuluttua");
                } else if(timer == 180) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l3 minuutin §7kuluttua");
                } else if(timer == 120) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l2 minuutin §7kuluttua");
                } else if(timer == 60) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l1 minuutin §7kuluttua");
                } else if(timer == 30) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l30 sekunnin §7kuluttua");
                } else if(timer == 10) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l10 sekunnin §7kuluttua");
                } else if(timer == 5) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l5...");
                } else if(timer == 4) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l4...");
                } else if(timer == 3) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l3...");
                } else if(timer == 2) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l2...");
                } else if(timer == 1) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu §c§l1...");
                } else if(timer == 0) {
                    Bukkit.broadcastMessage("§c§l! §7Palvelin sammuu nyt!");
                }

                timer -= 1;

           }
       }.runTaskTimer(Main.getInstance(), 0, 20);

    }

    public static List<Player> getOnlinePlayers() {

        List<Player> online = new ArrayList<>();

        for(Player player : Bukkit.getOnlinePlayers()) {
            if(StaffManager.hidden.contains(player.getUniqueId())) continue;
            online.add(player);
        }

        return online;
    }

    public static void sendBungeeMessage(Player player, String subchannel, String... arguments) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subchannel);
        for(String arg : arguments) {
            out.writeUTF(arg);
        }
        player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void sendBungeeMessage(String subchannel, String... arguments) {
        sendBungeeMessage(Iterables.getFirst(Bukkit.getOnlinePlayers(), null), subchannel, arguments);
    }

    public static WorldGuard getWorldGuard() {
        return WorldGuard.getInstance();
    }

}
