package me.tr.survival.main;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sk89q.worldguard.WorldGuard;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.listeners.Events;
import me.tr.survival.main.managers.*;
import me.tr.survival.main.managers.features.Boosters;
import me.tr.survival.main.managers.perks.Particles;
import me.tr.survival.main.managers.perks.PlayerGlowManager;
import me.tr.survival.main.util.Util;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;

import static dev.esophose.playerparticles.particles.spawning.reflective.ReflectionUtils.PackageType.getServerVersion;

public class Sorsa {

    private static final Map<UUID, Long> debugs = new HashMap<>();

    public static void teleportToSpawn(Player player) {
        Location loc = getSpawn();
        player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        Sorsa.logColored("§e[TeleportManager] Player " + player.getName() + " teleported to spawn!");
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

    static void warn(String message) {
        Bukkit.getLogger().log(Level.WARNING, message);
    }

    public static void task(Runnable task) {
        Bukkit.getScheduler().runTask(Main.getInstance(), task);
    }

    public static void async(Runnable task) { Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), task); }

    public static void teleportToNether(Player player) {
        player.teleport(new Location(Bukkit.getWorld("world_nether"), -2.5, 55, 15.5, 179, -2));
    }

    public static void every(int seconds, Runnable task, boolean async) {
        if(async) Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
        else Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds);
    }

    public static void after(int seconds, Runnable task) { Bukkit.getScheduler().runTaskLater(Main.getInstance(), task, 20 * seconds); }

    public static void after(int seconds, Runnable task, boolean async) {
        if(async) Sorsa.after(seconds, task);
        else Sorsa.afterAsync(seconds, task);
    }

    public static void afterAsync(int seconds, Runnable task) { Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), task, (long) getCurrentTPS() * seconds); }

    public static void every(int seconds, Runnable task) { Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds); }

    public static void everyAsync(int seconds, Runnable task) { Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, 20, (long) getCurrentTPS() * seconds); }

    public static double getCurrentTPS() {
        double average;
        try {
            Class<?> craftServer = Class.forName ( "org.bukkit.craftbukkit." + getServerVersion() + ".CraftServer" );
            Method getServer = craftServer.getMethod ( "getServer" );
            Object nmsServer = getServer.invoke ( Bukkit.getServer ( ) );
            double[] recentTps = (double[]) nmsServer.getClass().getField("recentTps").get(nmsServer);
            average = (recentTps[0] + recentTps[1] + recentTps[2]) / 3;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0d;
        }
        return Util.round(average);
    }

    static void setSpawn(Location loc) {
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

    static void setDeathSpawn(Location loc) {

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

    public static FileConfiguration getConfig() {
        return Main.getInstance().getConfig();
    }

    static void runDebug(Player player) {

        UUID uuid = player.getUniqueId();
        if(!player.isOp()) {
            if(!debugs.containsKey(uuid)) debugs.put(uuid, System.currentTimeMillis());
            else {
                long lastRun = debugs.get(uuid);
                long now = System.currentTimeMillis();
                if(now - lastRun < 1000 * 60 * 5) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Palvelimen suorituskyvyn suojaamiseksi, pystyt käynnistämään virheenkorjauksen §c5 minuutin §7välein!");
                    return;
                } else debugs.remove(uuid);
            }
        }

        Chat.sendMessage(player, Chat.Prefix.DEBUG, "Korjataan yleiset virheet ja bugit...");
        Boosters.debug();
        Main.getParticles().reloadParticles(player);
        Main.getPlayerGlowManager().disableGlow(player);
        Chat.sendMessage(player, Chat.Prefix.DEBUG, "Korjattu! Jos mikään ei muuttunut yritä poistua ja liittyä palvelimelle uudestaan!" +
                " Jos muutosta ei vieläkään näy, laita ilmoitus ylläpidollemme Discordissa kanavalla §9#ilmoita-bugeista§7!");
    }

    public static World getEndWorld() {
        return Bukkit.getWorld("world_the_end");
    }

    public static World getNetherWorld() {
        return Bukkit.getWorld("world_nether");
    }

    private static ProtocolManager getProtocolManager(){
        return ProtocolLibrary.getProtocolManager();
    }

    public static void sendTablist(Player player) {
        PacketContainer pc = getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        pc.getChatComponents().write(0, WrappedChatComponent.fromText("\n  §2§lSorsaMC§7 - §aSurvival  \n")).write(1, WrappedChatComponent.fromText("\n  §7Paikalla:  \n  §8⇻§a" + getOnlinePlayers().size() + "§8⇺  \n"));
        try { getProtocolManager().sendServerPacket(player, pc); }
        catch (Exception ex) { ex.printStackTrace(); }
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
        if (user == null) return "§7Pelaaja";
        CachedMetaData data = user.getCachedData().getMetaData(queryOptions);
        String prefix = data.getPrefix();
        getLuckPerms().getUserManager().cleanupUser(user);
        if(prefix == null) return "";
        return prefix;
    }

    static void toggleDebugMode(Player player) {
        UUID uuid = player.getUniqueId();
        if(!Main.getEventsListener().adminMode.containsKey(uuid)) {
            Main.getEventsListener().adminMode.put(uuid, true);
            Chat.sendMessage(player, Chat.Prefix.DEBUG, "Virheenkorjaustila päällä!");

           new BukkitRunnable() {
               @Override
               public void run() {
                   if(!Main.getEventsListener().adminMode.containsKey(uuid)) {
                        cancel();
                       return;
                   }
                   Util.sendNotification(player, "§7TPS: §e" + new DecimalFormat("#.##").format(Sorsa.getCurrentTPS()) +
                           " §8|§7 RAM: §a" + ((int)Util.getUsedMemory()) + "Mb/" + ((int)Util.getMaxMemory()) + "Mb" +
                           " §8|§7 CPU: §b" + Util.getProcessCPULoad() + "%", false);
               }
           }.runTaskTimerAsynchronously(Main.getInstance(), 20, 20);
        } else {
            Main.getEventsListener().adminMode.remove(uuid);
            Chat.sendMessage(player, Chat.Prefix.DEBUG, "Virheenkorjaustila pois päältä!");
        }
    }

    public static Set<Player> getOnlinePlayers() {
        Set<Player> online = new HashSet<>();
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(Main.getStaffManager().hasStaffMode(player)) continue;
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
