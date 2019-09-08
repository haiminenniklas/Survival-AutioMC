package me.tr.survival.main;

import me.tr.survival.main.commands.HomeCommand;
import me.tr.survival.main.commands.RankCommand;
import me.tr.survival.main.database.PlayerAliases;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.other.*;
import me.tr.survival.main.util.RTP;
import me.tr.survival.main.util.Times;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
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
                    //Chat.sendMessage(player, "Rahatilanne: §c" + Balance.get(player) + "€");
                    Chat.sendMessage(player, "Kristallit: §c" + Crystals.get(player.getUniqueId()));
                } else if(args.length > 0) {
                    if(!player.isOp()){
                      //Chat.sendMessage(player, "Rahatilanne: §c" + Balance.get(player) + "€");
                        Chat.sendMessage(player, "Kristallit: §c" + Crystals.get(player.getUniqueId()));
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
                                    return true;
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
                    Autio.teleportToSpawn(player);
                } else {
                    if(player.isOp()) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty");
                            return true;
                        }
                        Autio.teleportToSpawn(target);
                        Chat.sendMessage(player, "Pelaaja §c" + target.getName() + " §7vietiin spawnille!");
                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("setspawn")) {

                if(player.isOp()) {
                    Autio.setSpawn(player.getLocation());
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
                            return true;
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
                                return true;
                            }

                            Level.addXP(target.getPlayer(), value);
                            Chat.sendMessage(player, "Lisätty §c" + value + " §7XP:tä pelaajalle §c" + target.getName());

                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("ping")) {

                if(args.length < 1) {
                    Chat.sendMessage(player, "Viiveesi: §c" + Util.getPing(player) + "ms");
                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                        return true;
                    }
                    Chat.sendMessage(player, "Pelaajan §c" + target.getName() +  " §7viive: §c" + Util.getPing(target) + "ms");

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
                            Chat.sendMessage(player, "Käytä numeroita");
                            return true;
                        }

                        if(value > 10) {
                            player.sendMessage("§cNopeus max. 10");
                            return true;
                        }

                        float speed = value / 10;
                        if(!player.isFlying()) {
                            player.setWalkSpeed(speed);
                            Chat.sendMessage(player, "Kävelynopeys nyt " + value + " (" + speed + ")");
                        } else {
                            player.setFlySpeed(speed);
                            Chat.sendMessage(player, "Lentonopeus nyt " + value + " (" + speed + ")");
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
                            Chat.sendMessage(player, "Pelimuoto Survival");
                            Util.heal(player);
                        } else if(player.getGameMode() == GameMode.SURVIVAL) {
                            player.setGameMode(GameMode.CREATIVE);
                            Chat.sendMessage(player, "Pelimuoto Creative");
                        }

                    } else if(args.length >= 1) {

                        if(args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("survival")
                            || args[0].equalsIgnoreCase("0")) {

                            player.setGameMode(GameMode.SURVIVAL);
                            Chat.sendMessage(player, "Pelimuoto Survival");
                            Util.heal(player);

                        } else if(args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("creative") ||
                            args[0].equalsIgnoreCase("1")) {

                            player.setGameMode(GameMode.CREATIVE);
                            Chat.sendMessage(player, "Pelimuoto Creative");

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
                        return true;
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
                        return true;
                    }

                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < args.length; i++) {
                        sb.append(args[i] + " ");
                    }

                    Player target = messages.get(player);

                    if(target == null) {
                        Chat.sendMessage(player, "Pelaajaa ei löydetty");
                        return true;
                    }

                    messages.put(player, target);
                    messages.put(target, player);

                    target.sendMessage("§c" + player.getName() + " §7-> §c Sinä §7» §f" + sb.toString().trim());
                    player.sendMessage("§c Sinä §7-> §c" + target.getName() + " §7» §f" + sb.toString().trim());


                }

            } else if(command.getLabel().equalsIgnoreCase("sethome")) {

                Homes.panel(player);

               /* if(!PlayerData.isLoaded(uuid)) {
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

                } */

            } else if(command.getLabel().equalsIgnoreCase("heal")) {
                if(player.isOp()) {
                    if(args.length < 1) {
                        Util.heal(player);
                        Chat.sendMessage(player, "Paransit itsesi!");
                    } else {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                            return true;
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
                            return true;
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

            } else if(command.getLabel().equalsIgnoreCase("enchant")) {

                if(player.isOp()) {

                    if(args.length < 2) {
                        Chat.sendMessage(player, "Käytä: §c/lumoa <lumous> <taso>");
                    } else {

                        if(player.getInventory().getItemInMainHand() == null) {
                            Chat.sendMessage(player, "Sinulla pitää olla esine kädessä!");
                            return true;
                        }

                        int level;
                        try {
                            level = Integer.parseInt(args[1]);
                        } catch(NumberFormatException ex) {
                            Chat.sendMessage(player, "Käytä numeroita levelissä!");
                            return true;
                        }

                        Enchantment enchantment = Enchantment.getByName(args[0]);
                        if(enchantment == null) {
                            Chat.sendMessage(player, "Lumousta ei löytynyt tuolla nimellä...");
                            return true;
                        }

                        Enchant enchant = new Enchant(enchantment, level);
                        ItemStack item = player.getInventory().getItemInMainHand();

                        ItemStack newItem = Util.makeEnchanted(item, enchant);
                        player.getInventory().remove(item);
                        player.getInventory().addItem(newItem);

                        Chat.sendMessage(player, "Esine lumottu!");

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("crystal")) {

                if(player.isOp()) {

                    if(args.length < 1) {

                        Chat.sendMessage(player, "/crystals add <player> <value>");
                        Chat.sendMessage(player, "/crystals get <player>");
                        Chat.sendMessage(player, "/crystals set <player> <value>");

                    } else {

                        if(args.length >= 2) {

                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                            if(!PlayerData.isLoaded(target.getUniqueId())) {
                                PlayerData.loadNull(target.getUniqueId(), false);
                            }

                            if(args.length >= 3) {

                                int value;
                                try {
                                    value = Integer.parseInt(args[2]);
                                } catch (NumberFormatException ex){
                                    Chat.sendMessage(player, "Käytä numeroita!");
                                    return true;
                                }

                                if(args[0].equalsIgnoreCase("add")) {

                                    Crystals.add(target.getUniqueId(), value);
                                    Chat.sendMessage(player, "Lisätty §c" + value +  " §7kristallia pelaajalle §c" + target.getName());

                                } else if(args[0].equalsIgnoreCase("set")) {

                                    Crystals.set(target.getUniqueId(), value);
                                    Chat.sendMessage(player, "Pelaajalle §c" + target.getName() + " §7asetettu §c" + value + " §7kristallia!");

                                }

                            } else {

                                if(args[0].equalsIgnoreCase("get")) {
                                    Chat.sendMessage(player, "Pelaajan §c" + target.getName() + "§7 kristallit: §c" + Crystals.get(target.getUniqueId()));
                                }

                            }

                        }

                    }

                } else {
                    Chat.sendMessage(player, "Kristallit: §c" + Crystals.get(player.getUniqueId()));
                }

            } else if(command.getLabel().equalsIgnoreCase("rtp")) {
                RTP.teleport(player);
            } else if(command.getLabel().equalsIgnoreCase("autio")) {

                if(player.isOp()) {
                    if(args.length < 1) {
                        Chat.sendMessage(player, "/autio reload");
                    } else {
                        if(args[0].equalsIgnoreCase("reload")) {
                            reloadConfig();
                            Chat.sendMessage(player, "Config uudelleenladattu!");
                        }
                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("seen")) {

                if(player.isOp()) {

                    if(args.length < 1) {
                        Chat.sendMessage(player, "/seen <player>");
                    } else {

                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        getServer().getScheduler().runTaskAsynchronously(this, () -> {
                            String[] addresses = PlayerAliases.load(target);

                            if(addresses != null) {
                                StringBuilder sb = new StringBuilder();
                                for(int i = 0; i < addresses.length; i++) {
                                    if(i + 1 < addresses.length) {
                                        sb.append("§c" + addresses[i] + ", ");
                                    } else if(i + 1 >= addresses.length) {
                                        sb.append("§c" + addresses[i]);
                                    }
                                }

                                player.sendMessage("§7--------------------------");
                                player.sendMessage("§7Pelaajan §c" + target.getName() + "§7 IP-osoitteet:");
                                player.sendMessage(sb.toString());
                                player.sendMessage("§7--------------------------");
                            } else {
                                Chat.sendMessage(player, "Pelaajalla ei ole koskaan liittynyt palvelimelle..");
                            }

                        });

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("piiloudu")) {

                if (Disguise.changeSkin(player)) {
                    Chat.sendMessage(player, "Skini vaihdettu!");
                } else {
                    Chat.sendMessage(player, "Skiniä ei voitu vaihtaa. Olethan yhteydessä ylläpitoon!");
                }

            }

        }

        return true;
    }


}
