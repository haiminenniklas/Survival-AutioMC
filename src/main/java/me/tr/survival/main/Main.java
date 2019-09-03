package me.tr.survival.main;

import me.tr.survival.main.commands.HomeCommand;
import me.tr.survival.main.commands.RankCommand;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.other.AutoBroadcaster;
import me.tr.survival.main.other.EnderpearlCooldown;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.Times;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Homes;
import me.tr.survival.main.util.data.Level;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;
    public static Main getInstance() {
        return Main.instance;
    }

    public static final HashMap<Player, Player> messages = new HashMap<>();

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
                Chat.sendMessage(player, "Tietosi tallennettiin automaattisesti!");
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
            UUID uuid = player.getUniqueId();

            if(command.getLabel().equalsIgnoreCase("bal")) {
                if(args.length == 0){
                    Chat.sendMessage(player, "Rahatilanne: §c" + Balance.get(player) + "€");
                } else if(args.length > 0) {
                    if(!player.isOp()){
                        Chat.sendMessage(player, "Rahatilanne: §c" + Balance.get(player) + "€");
                    } else {

                        if(args.length == 1 && args[0].equalsIgnoreCase("help")) {
                            Chat.sendMessage(player, "/bal add <player> <amount>");
                            Chat.sendMessage(player, "/bal remove <player> <amount>");
                            Chat.sendMessage(player, "/bal get <player>");
                        } else if(args.length >= 2) {

                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if(!PlayerData.isLoaded(target.getUniqueId())) {
                                Chat.sendMessage(player, "(Pelaajan " + target.getName() + " tietoja ei ole ladattu)");
                            }

                            if(args.length >= 3) {

                                int value;
                                try {
                                    value = Integer.parseInt(args[2]);
                                } catch(NumberFormatException ex) {
                                    Chat.sendMessage(player, "Käytä numeroita!");
                                    return false;
                                }

                                if(args[0].equalsIgnoreCase("add")) {
                                    PlayerData.add(target.getUniqueId(), "money", value);
                                    Chat.sendMessage(player, "Pelaajalle annettu §c" + value + "€§7! Hänen rahatilanteensa: §c" + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                } else if(args[0].equalsIgnoreCase("remove")) {
                                    PlayerData.add(target.getUniqueId(), "money", -value);
                                    Chat.sendMessage(player, "Pelaajalta poistettu §c" + value + "€! §7Hänen rahatilanteensa: §c" + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                }

                                Chat.sendMessage(player, "Tallenna pelaajan tiedot komennolla §c/save " + target.getName());

                            } else {
                                if(args[0].equalsIgnoreCase("get")) {
                                    Chat.sendMessage(player, "Pelaajan §4" + target.getName() + " rahatilanne: " + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                }
                            }

                        }

                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("save")) {

                if(player.isOp()) {

                    if(args.length == 0) {
                        PlayerData.savePlayer(player.getUniqueId());
                        Chat.sendMessage(player, "Sinun tietosi tallennettin!");
                    } else if(args.length == 1) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        if(!PlayerData.isLoaded(target.getUniqueId())) {
                            Chat.sendMessage(player, "Pelaajan " + target.getName() + " tietoja ei ole ladattu, joten ne tallennetaan tyhjänä");
                            TextComponent message = new TextComponent( "§c§lHaluatko jatkaa?" );
                            message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/save " + target.getName() + " yes" ) );
                            message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Haluatko varmasti jatkaa?" ).create() ) );
                            player.spigot().sendMessage(message);
                        } else {
                            PlayerData.savePlayer(target.getUniqueId());
                            Chat.sendMessage(player, "Pelaajan " + target.getName() + " tiedot tallennettin!");
                        }

                    } else if(args.length == 2) {

                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                        if(args[1].equalsIgnoreCase("yes")) {
                            PlayerData.savePlayer(target.getUniqueId());
                            Chat.sendMessage(player, "Pelaajan " + target.getName() + " tiedot tallennettin tyhjänä!");
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

                if(args.length < 1) {
                    player.sendMessage("§c§lAutio §7» Sinua viedään spawnille...");
                    Main.teleportToSpawn(player);
                } else {
                    if(player.isOp()) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty");
                            return false;
                        }
                        Main.teleportToSpawn(target);
                        Chat.sendMessage(player, "Pelaaja §c" + target.getName() + " §7vietiin spawnille!");
                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("setspawn")) {

                if(player.isOp()) {
                    Main.setSpawn(player.getLocation());
                    Chat.sendMessage(player, "Spawn asetettu sijaintiisi");
                }

            } else if(command.getLabel().equalsIgnoreCase("level")) {

                if(player.isOp()) {

                    if(args.length < 3) {
                        Chat.sendMessage(player, "§c/level set <player> <level>");
                        Chat.sendMessage(player, "§c/level add <player> <level>");
                        Chat.sendMessage(player, "§c/level addXP <player> <level>");
                    } if(args.length >= 3) {

                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                        if(!PlayerData.isLoaded(target.getUniqueId()))
                            PlayerData.loadNull(target.getUniqueId(), false);

                        int value;
                        try {
                            value = Integer.parseInt(args[2]);
                        } catch(NumberFormatException ex){
                            Chat.sendMessage(player, "Käytä numeroita");
                            return false;
                        }

                        if(args[0].equalsIgnoreCase("set")) {

                            Level.set(target, value);
                            Chat.sendMessage(player, "Pelaajan §c" + target.getName() + "§7 leveli on nyt §c" + value);

                        } else if(args[0].equalsIgnoreCase("add")) {

                            Level.add(target, value);
                            Chat.sendMessage(player, "Lisätty §c" + value + " §7leveliä pelaajalle §c" + target.getName());

                        } else if(args[0].equalsIgnoreCase("addXP")) {

                            if(!target.isOnline()) {
                                Chat.sendMessage(player, "Tämä toimintoo vain jos §c" + target.getName() + " §7on paikalla!");
                                return false;
                            }

                            Level.addXP(target.getPlayer(), value);
                            Chat.sendMessage(player, "Lisätty §c" + value + " §7XP:tä pelaajalle §c" + target.getName());

                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("ping")) {

                if(args.length < 1) {
                    Chat.sendMessage(player, "Viiveesi: §c" + getPing(player) + "ms");
                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                        return false;
                    }
                    Chat.sendMessage(player, "Pelaajan §c" + target.getName() +  " §7viive: §c" + getPing(target) + "ms");

                }

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

                        float speed = value / 10;
                        if(!player.isFlying()) {
                            player.setWalkSpeed(speed);
                            player.sendMessage("§cKävelynopeys nyt " + value + " (" + speed + ")");
                        } else {
                            player.setFlySpeed(speed);
                            player.sendMessage("§cLentonopeus nyt " + value + " (" + speed + ")");
                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("Settings")) {
                Settings.panel(player);
            } else if(command.getLabel().equalsIgnoreCase("gamemode")) {

                if(player.isOp()) {
                    if(args.length < 1) {

                        if(player.getGameMode() != GameMode.SURVIVAL) {
                            player.setGameMode(GameMode.SURVIVAL);
                            player.sendMessage("§cPelimuoto Survival");
                            Util.heal(player);
                        } else if(player.getGameMode() == GameMode.SURVIVAL) {
                            player.setGameMode(GameMode.CREATIVE);
                            player.sendMessage("§cPelimuoto Creative");
                        }

                    } else if(args.length >= 1) {

                        if(args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("survival")
                            || args[0].equalsIgnoreCase("0")) {

                            player.setGameMode(GameMode.SURVIVAL);
                            player.sendMessage("§cPelimuoto Survival");
                            Util.heal(player);

                        } else if(args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("creative") ||
                            args[0].equalsIgnoreCase("1")) {

                            player.setGameMode(GameMode.CREATIVE);
                            player.sendMessage("§cPelimuoto Creative");

                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("msg")) {

                if(args.length < 2) {

                    Chat.sendMessage(player, "Käytä §c/msg <pelaaja> <viesti>");

                } else if(args.length >= 2) {

                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, "Pelaajaa ei löydetty");
                        return false;
                    }

                    StringBuilder sb = new StringBuilder();
                    for(int i = 1; i < args.length; i++) {
                        sb.append(args[i] + " ");
                    }

                    messages.put(player, target);
                    messages.put(target, player);
                    target.sendMessage("§c" + player.getName() + " §7-> §c Sinä §7» §f" + sb.toString().trim());
                    player.sendMessage("§c Sinä §7-> §c" + target.getName() + " §7» §f" + sb.toString().trim());


                }

            } else if(command.getLabel().equalsIgnoreCase("r")) {

                if(args.length < 1) {

                    Chat.sendMessage(player, "Käytä §c/r <viesti>");

                } else if(args.length >= 1) {

                    if(!messages.containsKey(player)){
                        Chat.sendMessage(player, "Ei ketään kenelle lähettää");
                        return false;
                    }

                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < args.length; i++) {
                        sb.append(args[i] + " ");
                    }

                    Player target = messages.get(player);

                    if(target == null) {
                        Chat.sendMessage(player, "Pelaajaa ei löydetty");
                        return false;
                    }

                    messages.put(player, target);
                    messages.put(target, player);

                    target.sendMessage("§c" + player.getName() + " §7-> §c Sinä §7» §f" + sb.toString().trim());
                    player.sendMessage("§c Sinä §7-> §c" + target.getName() + " §7» §f" + sb.toString().trim());


                }

            } else if(command.getLabel().equalsIgnoreCase("sethome")) {

                if(!PlayerData.isLoaded(uuid)) {
                    PlayerData.loadNull(uuid, false);
                }

                Homes homes = new Homes(player);
                for(int i = 0; i < homes.get().size(); i++) {
                    Home home = homes.get().get(i);
                    int pos = i+1;

                    if(home == null) {
                        switch(pos) {
                            case 1:
                                homes.createHome("first_home", player.getLocation());
                            case 2:
                                homes.createHome("second_home", player.getLocation());
                            case 3:
                                homes.createHome("third_home", player.getLocation());
                            default:
                        }
                        Chat.sendMessage(player, "Koti §c#" + pos + " §7asetettu!");
                        Chat.sendMessage(player, "Pääset hallitsemaan koteja komennolla §c/koti§7!");
                        break;
                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("heal")) {
                if(player.isOp()) {
                    if(args.length < 1) {
                        Util.heal(player);
                        Chat.sendMessage(player, "Paransit itsesi!");
                    } else {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                            return false;
                        }
                        Util.heal(target);
                        Chat.sendMessage(player, "Paransit pelaajan §c" + target.getName() + "§7!");
                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("time")) {

                if(player.isOp()) {

                    if(args.length < 1) {

                        Chat.sendMessage(player, "§7Käytä: §c/aika <day|night|noon>");

                    } else {

                        World world = player.getWorld();

                        switch(args[0]) {
                            case "day":
                                world.setTime(Times.DAY);
                                Chat.sendMessage(player, "Aika asetettiin §cpäiväksi§7!");
                                break;
                            case "night":
                                world.setTime(Times.NIGHT);
                                Chat.sendMessage(player, "Aika asetettiin §cyöksi§7!");
                                break;
                            case "noon":
                                world.setTime(Times.NOON);
                                Chat.sendMessage(player, "Aika asetettiin §ckeskipäiväksi§7!");
                                break;
                            default:
                                long value;
                                try {
                                    value = Long.parseLong(args[0]);
                                } catch(NumberFormatException ex){
                                    Chat.sendMessage(player, "Käytä numeroita!");
                                    break;
                                }
                                world.setTime(value);
                                Chat.sendMessage(player, "Aika asetettiin: §c" + value + "§7!");
                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("fly")) {

                if(player.isOp()) {
                    if(args.length < 1) {
                        if(!player.getAllowFlight()) {
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            Chat.sendMessage(player, "Lento §apäällä§7!");
                        } else {
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            Chat.sendMessage(player, "Lento §cpois päältä§7!");
                        }
                    } else {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                            return false;
                        }
                        if(!target.getAllowFlight()) {
                            target.setAllowFlight(true);
                            target.setFlying(true);
                            Chat.sendMessage(target, "Lento §apäällä§7!");
                            Chat.sendMessage(player, "Lento §apäällä§7 pelaajalla §c" + target.getName() + "§7!");
                        } else {
                            target.setAllowFlight(false);
                            target.setFlying(false);
                            Chat.sendMessage(target, "Lento §cpois päältä§7!");
                            Chat.sendMessage(player, "Lento §cpois päältä§7 pelaajalla §c" + target.getName() + "§7!");
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
