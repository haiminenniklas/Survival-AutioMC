package me.tr.survival.main;

import me.tr.survival.main.commands.HomeCommand;
import me.tr.survival.main.commands.RankCommand;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.other.AutoBroadcaster;
import me.tr.survival.main.other.EnderpearlCooldown;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Level;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;
    public static Main getInstance() {
        return Main.instance;
    }

    @Override
    public void onEnable() {

        // Some setupping

        Main.instance = this;
        saveDefaultConfig();
        SQL.setup();

        // Events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new Events(), this);
        pm.registerEvents(new EnderpearlCooldown(), this);

        // Commands

        getCommand("home").setExecutor(new HomeCommand());
        getCommand("rank").setExecutor(new RankCommand());

        // Autosave code...

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {

            System.out.println("Trying to save the data of " + Bukkit.getOnlinePlayers().size() + " players...");
            int times_saved = 0;
            for(Player player : Bukkit.getOnlinePlayers()) {
                times_saved += 1;
                PlayerData.savePlayer(player.getUniqueId());
                player.sendMessage("§c§lAutio §7» Tietosi tallennettiin automaattisesti!");
            }
            System.out.println("Saved the data of " + times_saved + " players!");

        }, 20, (20*60) * 5);

        AutoBroadcaster.start();

    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("bal")) {
                if(args.length == 0){
                    player.sendMessage("§c§lAutio §7» Rahatilanne: §c" + Balance.get(player) + "€");
                } else if(args.length > 0) {
                    if(!player.isOp()){
                        player.sendMessage("§c§lAutio §7» Rahatilanne: §c" + Balance.get(player) + "€");
                    } else {

                        if(args.length == 1 && args[0].equalsIgnoreCase("help")) {
                            player.sendMessage("§c/bal add <player> <amount>");
                            player.sendMessage("§c/bal remove <player> <amount>");
                            player.sendMessage("§c/bal get <player>");
                        } else if(args.length >= 2) {

                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if(!PlayerData.isLoaded(target.getUniqueId())) {
                                player.sendMessage("§c(Pelaajan " + target.getName() + " tietoja ei ole ladattu)");
                            }

                            if(args.length >= 3) {

                                int value;
                                try {
                                    value = Integer.parseInt(args[2]);
                                } catch(NumberFormatException ex) {
                                    player.sendMessage("§cKäytä numeroita!");
                                    return false;
                                }

                                if(args[0].equalsIgnoreCase("add")) {
                                    PlayerData.add(target.getUniqueId(), "money", value);
                                    player.sendMessage("§cPelaajalle annettu §4" + value + "€§c! Hänen rahatilanteensa: " + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                } else if(args[0].equalsIgnoreCase("remove")) {
                                    PlayerData.add(target.getUniqueId(), "money", -value);
                                    player.sendMessage("§cPelaajalta poistettu §4" + value + "€§c! Hänen rahatilanteensa: " + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                }

                                player.sendMessage("§cTallenna pelaajan tiedot komennolla §c/save " + target.getName());

                            } else {
                                if(args[0].equalsIgnoreCase("get")) {
                                    player.sendMessage("§cPelaajan §4" + target.getName() + " rahatilanne: " + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                }
                            }

                        }

                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("save")) {

                if(player.isOp()) {

                    if(args.length == 0) {
                        PlayerData.savePlayer(player.getUniqueId());
                        player.sendMessage("§cSinun tietosi tallennettin!");
                    } else if(args.length == 1) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        if(!PlayerData.isLoaded(target.getUniqueId())) {
                            player.sendMessage("§cPelaajan " + target.getName() + " tietoja ei ole ladattu, joten ne tallennetaan tyhjänä");
                            TextComponent message = new TextComponent( "§c§lHaluatko jatkaa?" );
                            message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/save " + target.getName() + " yes" ) );
                            message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Haluatko varmasti jatkaa?" ).create() ) );
                            player.spigot().sendMessage(message);
                        } else {
                            PlayerData.savePlayer(target.getUniqueId());
                            player.sendMessage("§cPelaajan " + target.getName() + " tiedot tallennettin!");
                        }

                    } else if(args.length == 2) {

                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                        if(args[1].equalsIgnoreCase("yes")) {
                            PlayerData.savePlayer(target.getUniqueId());
                            player.sendMessage("§cPelaajan " + target.getName() + " tiedot tallennettin tyhjänä!");
                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("profile")) {

                if(args.length == 0) {

                    Profile.openProfile(player, player.getUniqueId());

                } else if(args.length >= 1) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                    Profile.openProfile(player, target.getUniqueId());

                }

            } else if(command.getLabel().equalsIgnoreCase("spawn")) {

                player.sendMessage("§c§lAutio §7» Sinua viedään spawnille...");
                Main.teleportToSpawn(player);

            } else if(command.getLabel().equalsIgnoreCase("setspawn")) {

                if(player.isOp()) {
                    Main.setSpawn(player.getLocation());
                    player.sendMessage("§cSpawni asetettu sijaintiisi");
                }

            } else if(command.getLabel().equalsIgnoreCase("level")) {

                if(player.isOp()) {

                    if(args.length < 3) {
                        player.sendMessage("§c/level set <player> <level>");
                        player.sendMessage("§c/level add <player> <level>");
                        player.sendMessage("§c/level addXP <player> <level>");
                    } if(args.length >= 3) {

                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                        if(!PlayerData.isLoaded(target.getUniqueId()))
                            PlayerData.loadNull(target.getUniqueId(), false);

                        int value;
                        try {
                            value = Integer.parseInt(args[2]);
                        } catch(NumberFormatException ex){
                            player.sendMessage("§cKäytä numeroita");
                            return false;
                        }

                        if(args[0].equalsIgnoreCase("set")) {

                            Level.set(target, value);
                            player.sendMessage("§cPelaajan " + target.getName() + " leveli on nyt " + value);

                        } else if(args[0].equalsIgnoreCase("add")) {

                            Level.add(target, value);
                            player.sendMessage("§cLisätty " + value + " leveliä pelaajalle " + target.getName());

                        } else if(args[0].equalsIgnoreCase("addXP")) {

                            if(!target.isOnline()) {
                                player.sendMessage("§cTämä toimintoo vain jos " + target.getName() + " on paikalla!");
                                return false;
                            }

                            Level.addXP(target.getPlayer(), value);
                            player.sendMessage("§cLisätty " + value + " XP:tä pelaajalle " + target.getName());

                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("ping")) {
                player.sendMessage("§c§lAutio §7» Viive: §c" + getPing(player) + "ms");
            } else if(command.getLabel().equalsIgnoreCase("speed")) {

                if(player.isOp()) {

                    if(args.length == 0) {

                        player.sendMessage("§c/speed <amount>");

                    } else if(args.length >= 1) {

                        float value;
                        try {
                            value = Float.parseFloat(args[0]);
                        } catch(NumberFormatException ex){
                            player.sendMessage("§cKäytä numeroita");
                            return false;
                        }

                        if(value > 10) {
                            player.sendMessage("§cNopeus max. 10");
                            return false;
                        }

                        float speed = (float) value / 10;
                        if(!player.isFlying()) {
                            player.setWalkSpeed(speed);
                            player.sendMessage("§cKävelynopeys nyt " + value + "(" + speed + ")");
                        } else {
                            player.setFlySpeed(speed);
                            player.sendMessage("§cLentonopeus nyt " + value + "(" + speed + ")");
                        }

                    }

                }

            }

        }

        return false;
    }

    public static int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            int ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
            return ping;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void updateTag(Player player) {



    }

    public static void teleportToSpawn(Player player) {
        player.teleport(Main.getSpawn());
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

}
