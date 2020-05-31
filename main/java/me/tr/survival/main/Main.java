package me.tr.survival.main;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import me.tr.survival.main.commands.*;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.other.*;
import me.tr.survival.main.other.backpacks.Backpack;
import me.tr.survival.main.other.booster.Boosters;
import me.tr.survival.main.other.recipes.Recipe;
import me.tr.survival.main.other.travel.EndManager;
import me.tr.survival.main.other.travel.TravelManager;
import me.tr.survival.main.other.warps.Warp;
import me.tr.survival.main.other.warps.Warps;
import me.tr.survival.main.trading.TradeManager;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.RTP;
import me.tr.survival.main.util.Times;
import me.tr.survival.main.util.callback.SpigotCallback;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.data.Homes;
import me.tr.survival.main.util.data.Level;
import me.tr.survival.main.util.staff.StaffManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener, PluginMessageListener {

    public static Main instance;
    public static Main getInstance() {
        return Main.instance;
    }

    private static LuckPerms luckPerms;
    public static LuckPerms getLuckPerms() {
        return Main.luckPerms;
    }

    private static PlayerParticlesAPI particlesAPI;
    public static PlayerParticlesAPI getParticlesAPI() {
        return particlesAPI;
    }

    private static Map<UUID, Long> spawnCommandDelay = new HashMap<>();
    //public static final HashMap<Player, Player> messages = new HashMap<>();

    private static long started = 0L;

    @Override
    public void onEnable() {
        // Some setupping


        Main.instance = this;
        Main.luckPerms = LuckPermsProvider.get();


        Autio.logColored("§a---------------------------");
        Autio.logColored(" §aEnabling SorsaSurvival....");

        long start = System.currentTimeMillis();

        Autio.logColored(" ");
        Autio.logColored(" §6IF YOU DON'T WANT LOGS FROM THE PLUGIN, DISABLE IT FROM THE config.yml!");
        Autio.logColored(" ");

        Autio.logColored(" §aSetupping configs and database...");

        saveDefaultConfig();
        EndManager.createEndConfig();
        EndManager.loadPreviousData();
        SQL.setup();


        /*Autio.logColored(" §aSetupping Boosters-system...");

        File boosterJson = new File(getDataFolder() + File.separator + "boosters.json");
        if(!boosterJson.exists()) {
            try {
                if(boosterJson.createNewFile()) {
                    Autio.logColored(" §aCreated file for Boosters!");
                } else {
                    Autio.warn(" Could not create file for booster. Boosters will not be saved!");
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        } */

        Autio.logColored(" §aRegistering plugin event listeners...");

        // Events
        PluginManager pm = getServer().getPluginManager();

        new SpigotCallback(this);

        pm.registerEvents(this, this);
        pm.registerEvents(new Events(), this);
        //pm.registerEvents(new EnderpearlCooldown(), this);
        pm.registerEvents(new Chat(), this);
        pm.registerEvents(new StaffManager(), this);
        pm.registerEvents(new Essentials(), this);
        pm.registerEvents(new TradeManager(), this);
        pm.registerEvents(new TravelManager(), this);
        pm.registerEvents(new MoneyManager(), this);
        pm.registerEvents(new Backpack(), this);
        pm.registerEvents(new Particles(), this);
        pm.registerEvents(new PlayerDeathMessageManager(), this);

        Autio.logColored(" §aRegistering messaging channels for BungeeCord...");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        Autio.logColored(" §aRegistering PlayerParticlesAPI...");

        if (Bukkit.getPluginManager().getPlugin("PlayerParticles") != null) {
            Main.particlesAPI = PlayerParticlesAPI.getInstance();
        } else {
            Autio.log("§cCould not find PlayerParticles plugin, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }


        Autio.logColored(" §aDisabling Announcement of Advancements...");

        // Disable Advancement announcing
        for(World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }

        // Commands

        Autio.logColored(" §aRegistering plugin commands....");

        getCommand("home").setExecutor(new HomeCommand());

        getCommand("tpa").setExecutor(new TpaCommand());
        getCommand("tpaccept").setExecutor(new TpaCommand());
        getCommand("tpahere").setExecutor(new TpaCommand());
        getCommand("tp").setExecutor(new TpaCommand());
        getCommand("tphere").setExecutor(new TpaCommand());
        getCommand("tpdeny").setExecutor(new TpaCommand());

        getCommand("staff").setExecutor(new StaffManager());

        getCommand("apua").setExecutor(new Essentials());
        getCommand("broadcast").setExecutor(new Essentials());
        getCommand("discord").setExecutor(new Essentials());

        getCommand("bal").setExecutor(new MoneyCommand());
        getCommand("pay").setExecutor(new MoneyCommand());

        getCommand("trade").setExecutor(new TradeManager());

        getCommand("world").setExecutor(new Essentials());
        getCommand("clear").setExecutor(new Essentials());
        getCommand("koordinaatit").setExecutor(new Essentials());

        getCommand("baltop").setExecutor(new BaltopCommand());
        getCommand("matkusta").setExecutor(new TravelManager());

        getCommand("valuutta").setExecutor(new MoneyManager());
        getCommand("shekki").setExecutor(new MoneyManager());

        getCommand("stop").setExecutor(new StopCommand());
        getCommand("forcestop").setExecutor(new StopCommand());

        getCommand("reppu").setExecutor(new Backpack());
      //  getCommand("huutokauppa").setExecutor(new AuctionCommands());
        getCommand("invsee").setExecutor(new Essentials());

        getCommand("kosmetiikka").setExecutor(new Particles());
        getCommand("givevip").setExecutor(new VipCommand());
        getCommand("hehku").setExecutor(new PlayerGlowManager());
        getCommand("houkutin").setExecutor(new Houkutin());

        getCommand("ääri").setExecutor(new EndManager());

        // Autosave code...

        Autio.logColored(" §aStarting autosaving for players...");
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {


            if(Autio.getCurrentTPS() >= 18.5) {
                Autio.log("Trying to save the data of " + Bukkit.getOnlinePlayers().size() + " players...");
                int times_saved = 0;
                for(Player player : Bukkit.getOnlinePlayers()) {
                    times_saved += 1;
                    PlayerData.savePlayer(player.getUniqueId());
                    //Chat.sendMessage(player, "Tietosi tallennettiin automaattisesti!");
                }
                Autio.log("Saved the data of " + times_saved + " players!");
            } else {
                Autio.warn("Server TPS too low, not updating players this time...");
            }

            // Fetch Balances...
            Balance.fetchTopBalance();

        }, 20, (20*60) * 5);



        Autio.logColored(" §aStarting AutoBroadcaster...");
        AutoBroadcaster.start();
        Warps.loadWarps((value) -> {
            String output = (value) ? "Loaded warps from the Database!" : "Did not load warps from the database, did an error occur?";
            System.out.println(output);
        });

        Autio.logColored(" §aStarting Managers...");
        Boosters.activateManager();
        Houkutin.activateManager();
        EndManager.startManager();

        Autio.logColored(" §aInitializing ChatManager");
        Chat.init();

        Autio.log(" §aLoading Custom Recipes...");
        Recipe.load();

        Autio.log(" §aIntegrating custom economy into Vault...");
        if(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            Bukkit.getServer().getServicesManager().register(Economy.class, new CustomEconomy(), this, ServicePriority.Highest);
        } else {
            Autio.log(" §cCould not find Vault! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Autio.logColored("§a Enabled SorsaSurvival! (It took " + (System.currentTimeMillis() - start) +
                "ms / " + ((System.currentTimeMillis() - start) / 1000.0f) + "s)");
        Autio.logColored("§a---------------------------");

        started = System.currentTimeMillis();

    }

    @Override
    public void onDisable() {

        Autio.logColored("§a---------------------------");
        Autio.logColored(" §aDisabling SorsaSurvival....");

        Autio.logColored(" ");
        Autio.logColored(" §6IF YOU DON'T WANT LOGS FROM THE PLUGIN, DISABLE IT FROM THE config.yml!");
        Autio.logColored(" ");

        long start = System.currentTimeMillis();

        Autio.logColored(" §aSaving configs...");

        saveConfig();
        EndManager.saveEndConfig();

        Autio.logColored(" §aClosing Database Connection...");

        SQL.source.close();

        Autio.logColored("§a Disabled SorsaSurvival! (It took " + (System.currentTimeMillis() - start) +
                "ms / " + ((System.currentTimeMillis() - start) / 1000.0f) + "s)");

        Autio.logColored("§a---------------------------");

        started = 0L;

    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("")) {

        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

           if(command.getLabel().equalsIgnoreCase("save")) {

                if(player.isOp()) {

                    if(args.length == 0) {
                        PlayerData.savePlayer(player.getUniqueId());
                        Chat.sendMessage(player, "Sinun tietosi tallennettin!");
                    } else if(args.length == 1) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        if(!PlayerData.isLoaded(target.getUniqueId())) {
                            Chat.sendMessage(player, "Pelaajan " + target.getName() + " tietoja ei ole ladattu, joten ne tallennetaan tyhjänä");
                            TextComponent message = new TextComponent( "§a§lHaluatko jatkaa?" );
                            message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/save " + target.getName() + " yes" ) );
                            message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Haluatko varmasti jatkaa?" ).create()));
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

           } else if(command.getLabel().equals("uptime")) {

               long now = System.currentTimeMillis();
               long uptime = now - started;
               Chat.sendMessage(player, String.format("Palvelin on ollut päällä §c%s", DurationFormatUtils.formatDurationWords(uptime, false, true)));

           } else if(command.getLabel().equalsIgnoreCase("profile")) {

                if(args.length == 0) {

                    Profile.openProfile(player, player.getUniqueId());

                } else if(args.length >= 1) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                    Profile.openProfile(player, target.getUniqueId());

                }

            } else if(command.getLabel().equalsIgnoreCase("vip")) {

               Settings.vipPanel(player);

           } else if(command.getLabel().equalsIgnoreCase("spawn")) {

                if(args.length < 1) {

                    if(player.getWorld().getName().equals("world_nether")) {
                        Chat.sendMessage(player, "§7Tämä ei toimi §cNetherissä§7!");
                        return true;
                    }

                    if(spawnCommandDelay.containsKey(uuid)) {

                        long shouldSpawn = spawnCommandDelay.get(uuid);
                        if(System.currentTimeMillis() < shouldSpawn) {
                            long secondsLeft = (shouldSpawn - System.currentTimeMillis()) / 1000;
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pystyt lähtemään spawnille uudestaan §c" + secondsLeft + "s §7jälkeen.");
                            return true;
                        }

                    } else {
                        if(!StaffManager.hasStaffMode(player)) {
                            spawnCommandDelay.put(uuid, System.currentTimeMillis() + (1000 * 60));
                        }
                    }

                    Autio.teleportToSpawn(player);

                } else {
                    if(player.isOp()) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty");
                            return true;
                        }
                        Autio.teleportToSpawn(target);
                        Chat.sendMessage(player, "Pelaaja §a" + target.getName() + " §7vietiin spawnille!");
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
                        Chat.sendMessage(player, "§6/level set <player> <level>");
                        Chat.sendMessage(player, "§6/level add <player> <level>");
                        Chat.sendMessage(player, "§6/level addXP <player> <level>");
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
                            Chat.sendMessage(player, "Pelaajan §a" + target.getName() + "§7 leveli on nyt §6" + value);

                        } else if(args[0].equalsIgnoreCase("add")) {

                            Level.add(target, value);
                            Chat.sendMessage(player, "Lisätty §6" + value + " §7leveliä pelaajalle §6" + target.getName());

                        } else if(args[0].equalsIgnoreCase("addXP")) {

                            if(!target.isOnline()) {
                                Chat.sendMessage(player, "Tämä toimintoo vain jos §6" + target.getName() + " §7on paikalla!");
                                return true;
                            }

                            Level.addXP(target.getPlayer(), value);
                            Chat.sendMessage(player, "Lisätty §6" + value + " §7XP:tä pelaajalle §6" + target.getName());

                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("ping")) {

                if(args.length < 1) {
                    Chat.sendMessage(player, "Viiveesi: §a" + Util.getPing(player) + "ms");
                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                        return true;
                    }
                    Chat.sendMessage(player, "Pelaajan §6" + target.getName() +  " §7viive: §a" + Util.getPing(target) + "ms");

                }

            } else if(command.getLabel().equalsIgnoreCase("speed")) {

                if(player.isOp()) {

                    if(args.length < 1) {

                        Chat.sendMessage(player, "Käytä /speed <nopeus>");
                        Chat.sendMessage(player, "Nopeudet: 1-10 (normaali on 2)");

                    } else {

                        float value;
                        try {
                            value = Float.parseFloat(args[0]);
                        } catch(NumberFormatException ex){
                            Chat.sendMessage(player, "Käytä numeroita");
                            return true;
                        }

                        if(value > 10) {
                            player.sendMessage("§6Nopeus max. 10");
                            return true;
                        }

                        float speed = value / 10;
                        if(!player.isFlying()) {
                            player.setWalkSpeed(speed);
                            Chat.sendMessage(player, "Kävelynopeus nyt " + value + " (" + speed + ")");
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

                    if(!StaffManager.hasStaffMode(player)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pystyt vaihtamaan pelimuotoasi vain §eStaff§7-tila päällä. (Tee §a/staff§7)");
                        return true;
                    }

                    if(args.length < 1) {

                        if(player.getGameMode() != GameMode.SURVIVAL) {
                            player.setGameMode(GameMode.SURVIVAL);
                            Chat.sendMessage(player, "Pelimuoto Survival");
                            Util.heal(player);
                        } else if(player.getGameMode() == GameMode.SURVIVAL) {
                            player.setGameMode(GameMode.CREATIVE);
                            Chat.sendMessage(player, "Pelimuoto Creative");
                        }

                    } else {

                        if(args.length == 1) {
                            if(args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("survival")
                                    || args[0].equalsIgnoreCase("0")) {

                                player.setGameMode(GameMode.SURVIVAL);
                                Chat.sendMessage(player, "Pelimuoto Survival");
                                Util.heal(player);

                            } else if(args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("creative") ||
                                    args[0].equalsIgnoreCase("1")) {

                                player.setGameMode(GameMode.CREATIVE);
                                Chat.sendMessage(player, "Pelimuoto Creative");

                            } else if(args[0].equalsIgnoreCase("adventure") || args[0].equalsIgnoreCase("2")
                                    || args[0].equalsIgnoreCase("a")) {
                                player.setGameMode(GameMode.ADVENTURE);
                                Chat.sendMessage(player, "Pelimuoto Adventure");

                            } else if(args[0].equalsIgnoreCase("spectator") || args[0].equalsIgnoreCase("3")
                                    || args[0].equalsIgnoreCase("sp")) {

                                player.setGameMode(GameMode.SPECTATOR);
                                Chat.sendMessage(player, "Pelimuoto Spectator");
                            }
                        } else {

                            Player target = Bukkit.getPlayer(args[1]);
                            if(target == null) {
                                Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                                return true;
                            }

                            if(args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("survival")
                                    || args[0].equalsIgnoreCase("0")) {

                                target.setGameMode(GameMode.SURVIVAL);
                                Chat.sendMessage(player, "Pelimuoto Survival pelaajalle §a" + target.getName());
                                Util.heal(player);

                            } else if(args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("creative") ||
                                    args[0].equalsIgnoreCase("1")) {

                                target.setGameMode(GameMode.CREATIVE);
                                Chat.sendMessage(player, "Pelimuoto Creative pelaajalle §a" + target.getName());

                            } else if(args[0].equalsIgnoreCase("adventure") || args[0].equalsIgnoreCase("2")
                                    || args[0].equalsIgnoreCase("a")) {

                                target.setGameMode(GameMode.ADVENTURE);
                                Chat.sendMessage(player, "Pelimuoto Adeventure pelaajalle §a" + target.getName());

                            } else if(args[0].equalsIgnoreCase("spectator") || args[0].equalsIgnoreCase("3")
                                    || args[0].equalsIgnoreCase("sp")) {

                                target.setGameMode(GameMode.SPECTATOR);
                                Chat.sendMessage(player, "Pelimuoto Spectator pelaajalle §a" + target.getName());

                            }


                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("sethome")) {

                if(!player.isOp()) {
                    Homes.panel(player, player);
                } else {
                    if(args.length >= 1) {

                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                        if(!PlayerData.isLoaded(target.getUniqueId())) {
                            Chat.sendMessage(player, "Pelaajan §a" + target.getName() + " §7koteja ei ole ladattu. Tee §a/debug load "
                                    + target.getName() + " §7ja kokeile uudestaan!");
                            return true;
                        } else {
                            Homes.panel(player, target);
                        }

                    } else {
                        Homes.panel(player, player);
                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("heal")) {
                if(Ranks.isStaff(player.getUniqueId())) {
                    if(StaffManager.hasStaffMode(player)) {
                        if(args.length < 1) {
                            Util.heal(player);
                            Chat.sendMessage(player, "Paransit itsesi!");
                        } else {
                            if(player.isOp()) {
                                Player target = Bukkit.getPlayer(args[0]);
                                if(target == null) {
                                    Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                                    return true;
                                }
                                Util.heal(target);
                                Chat.sendMessage(player, "Paransit pelaajan §a" + target.getName() + "§7!");
                            }
                        }
                    } else {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pystyt parantaman itsesi ja muut vain §eStaff§7-tila päällä. (Tee §a/staff§7)");
                        return true;
                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("time")) {

                if(player.isOp()) {

                    if(args.length < 1) {

                        Chat.sendMessage(player, "§7Käytä: §a/aika <day|night|noon>");
                        StaffManager.timeGui(player);

                    } else {

                        World world = player.getWorld();

                        switch(args[0]) {
                            case "day":
                                world.setTime(Times.DAY);
                                Chat.sendMessage(player, "Aika asetettiin §apäiväksi§7!");
                                break;
                            case "night":
                                world.setTime(Times.NIGHT);
                                Chat.sendMessage(player, "Aika asetettiin §ayöksi§7!");
                                break;
                            case "noon":
                                world.setTime(Times.NOON);
                                Chat.sendMessage(player, "Aika asetettiin §akeskipäiväksi§7!");
                                break;
                            default:
                                long value;
                                try {
                                    value = Long.parseLong(args[0]);
                                } catch(NumberFormatException ex){
                                    Chat.sendMessage(player, "Käytä numeroita (0-24000) tai 'day', 'night', 'noon'!");
                                    break;
                                }
                                world.setTime(value);
                                Chat.sendMessage(player, "Aika asetettiin: §a" + value + "§7!");
                        }

                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("fly")) {

                if(Ranks.isStaff(uuid)) {

                    if(!StaffManager.hasStaffMode(player)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pystyt lentämään vain §eStaff§7-tila päällä. (Tee §a/staff§7)");
                        return true;
                    }

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
                       if(player.isOp()) {
                           Player target = Bukkit.getPlayer(args[0]);
                           if(target == null) {
                               Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                               return true;
                           }
                           if(!target.getAllowFlight()) {
                               target.setAllowFlight(true);
                               target.setFlying(true);
                               Chat.sendMessage(target, "Lento §apäällä§7!");
                               Chat.sendMessage(player, "Lento §apäällä§7 pelaajalla §a" + target.getName() + "§7!");
                           } else {
                               target.setAllowFlight(false);
                               target.setFlying(false);
                               Chat.sendMessage(target, "Lento §cpois päältä§7!");
                               Chat.sendMessage(player, "Lento §cpois päältä§7 pelaajalla §a" + target.getName() + "§7!");
                           }
                       }
                    }
                } else {
                    if(!Ranks.hasRank(uuid, "sorsa") && !Ranks.isStaff(uuid)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän toimintoon vaaditaan §2§lSORSA§7-arvo! Lisätietoa §a/kauppa§7!");
                    } else {
                        Settings.toggleFlight(player);
                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("roskis")) {

               if(!Ranks.isStaff(player.getUniqueId()) && !Ranks.hasRank(player, "premium")) {
                   Chat.sendMessage(player, "Tähän toimintoon tarvitaan §e§lPremium§7-arvon! Lisätietoa §a/kauppa§7!");
                   return true;
               }

               Inventory inv = Bukkit.createInventory(null, 54, "Heivaa turhakkeet tänne!");
               player.openInventory(inv);

           } else if(command.getLabel().equalsIgnoreCase("enchant")) {

                if(player.isOp()) {

                    if(args.length < 2) {
                        Chat.sendMessage(player, "Käytä: §a/lumoa <lumous> <taso>");
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

                        Enchantment enchantment = Enchantment.getByName(args[0].toUpperCase());
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
                                    Chat.sendMessage(player, "Lisätty §b" + value +  " §7kristallia pelaajalle §a" + target.getName());

                                } else if(args[0].equalsIgnoreCase("set")) {

                                    Crystals.set(target.getUniqueId(), value);
                                    Chat.sendMessage(player, "Pelaajalle §a" + target.getName() + " §7asetettu §b" + value + " §7kristallia!");

                                }

                            } else {

                                if(args[0].equalsIgnoreCase("get")) {
                                    Chat.sendMessage(player, "Pelaajan §a" + target.getName() + "§7 kristallit: §b" + Crystals.get(target.getUniqueId()));
                                }

                            }

                        }

                    }

                } else {
                    Chat.sendMessage(player, "Täältä ei löydy sitä mitä etsit!");
                }

            } else if(command.getLabel().equalsIgnoreCase("rtp")) {
                RTP.teleport(player);
            } else if(command.getLabel().equalsIgnoreCase("autio")) {

                if(player.isOp()) {
                    if(args.length < 1) {
                        Chat.sendMessage(player, "/autio reload");
                        Chat.sendMessage(player, "/autio logging");
                    } else {
                        if(args[0].equalsIgnoreCase("reload")) {

                            Chat.sendMessage(player, Chat.Prefix.DEBUG, "Uudelleenladataan §aconfig.yml§7...");
                            reloadConfig();
                            Chat.sendMessage(player, Chat.Prefix.DEBUG, "§aconfig.yml §7uudelleenladattu!");

                            Chat.sendMessage(player, Chat.Prefix.DEBUG, "§7Ladataan §awarppeja§7...");
                            Warps.loadWarps((value) -> {
                                String output = (value) ? "§aWarpit ladattiin!" : "§cWarppeja §7ei ladattu.. Onkohan consolessa erroria?";
                                Chat.sendMessage(player, Chat.Prefix.DEBUG, output);
                            });

                        } else if(args[0].equalsIgnoreCase("logging")) {
                            boolean current = getConfig().getBoolean("other.logging");
                            getConfig().set("other.logging", !current);

                            String msg = (!current) ? "§apäällä" : "§cpois päältä";
                            Chat.sendMessage(player, "Tietojen tallentaminen lokiin " + msg);

                        } else if(args[0].equalsIgnoreCase("setdeathspawn")) {
                            Autio.setDeathSpawn(player.getLocation());
                            Chat.sendMessage(player, "Kuolemansaaren spawni asetettu!");
                        }
                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("skull")) {
                if(player.isOp()) {
                    if(args.length < 1) {
                        Chat.sendMessage(player, "§7Käytä: §a/skull <player>");
                    } else {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        player.getInventory().addItem(ItemUtil.makeSkullItem(target, 1, "§6" + target.getName()));
                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("debug")) {

                if(player.isOp()) {
                    if(args.length < 1) {
                        Chat.sendMessage(player, "/debug load [player]");
                        Chat.sendMessage(player, "/debug mode");
                        Chat.sendMessage(player, "/debug update [player]");
                        Chat.sendMessage(player, "/debug info");
                        Chat.sendMessage(player, "/debug resetMail");
                        Chat.sendMessage(player, "/debug resetBooster");
                        Chat.sendMessage(player, "/debug run");
                        Chat.sendMessage(player, "/debug autobroadcast");
                        Chat.sendMessage(player, "/debug resetData [player] §cOle varovainen tän kaa!");
                    } else {

                        if(args.length == 1) {

                            if(args[0].equalsIgnoreCase("mode")) {
                                Autio.toggleDebugMode(player);
                            } else if(args[0].equalsIgnoreCase("load")) {

                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Tietojasi haetaan tietokannasta...");
                                Autio.async(() -> {
                                    PlayerData.loadPlayer(player.getUniqueId(), (result) -> {
                                        if(result) {
                                            Chat.sendMessage(player, Chat.Prefix.DEBUG, "Haettu ja ladattu!");
                                        } else {
                                            Chat.sendMessage(player, Chat.Prefix.DEBUG, "Tietoja ei ollut olemassa, ladattiin nollatiedot.");
                                        }
                                    });

                                });

                            } else if(args[0].equalsIgnoreCase("autobroadcast")){
                              AutoBroadcaster.test();
                            } else if(args[0].equalsIgnoreCase("update")) {
                                Autio.updatePlayer(player);
                                Chat.sendMessage(player, "Päivitit asetukset ja tagin!");
                            } else if(args[0].equalsIgnoreCase("info")) {
                                player.sendMessage("§7§m--------------------");
                                player.sendMessage("§7Versio: §6" + Bukkit.getVersion());
                                player.sendMessage("§7Bukkit versio: §6" + Bukkit.getBukkitVersion());
                                player.sendMessage("§7Tämänhetkinen TPS: §6" + Autio.getCurrentTPS());
                                player.sendMessage("§7IP: §6" + getServer().getIp() + ":" + getServer().getPort());
                                player.sendMessage("§7Pelaajia: §6" + Bukkit.getOnlinePlayers().size());
                                player.sendMessage("§7Plugineita: §6" + getServer().getPluginManager().getPlugins().length);
                                player.sendMessage("§7Whitelist: " + (getServer().hasWhitelist() ? "§aPäällä" : "§6Ei päällä"));
                                player.sendMessage("§7Maailmoja: §6" + getServer().getWorlds().size());
                                player.sendMessage("§7§m--------------------");
                            } else if(args[0].equalsIgnoreCase("resetMail")) {
                                Mail.setLastMail(uuid, System.currentTimeMillis() - (1000 * 60 * 60 * 24));
                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Päivittäinen posti tyhjennetty!");
                            } else if(args[0].equalsIgnoreCase("resetBooster")) {
                                Boosters.getActive().clear();
                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Resetoitiin boosterit!");
                            } else if(args[0].equalsIgnoreCase("run")) {
                                Autio.runDebug(player);
                            } else if(args[0].equalsIgnoreCase("resetData")) {
                                Autio.async(() -> {
                                    PlayerData.loadNull(player.getUniqueId(), true);
                                    PlayerData.savePlayer(player.getUniqueId());
                                    Chat.sendMessage(player, "Data tyhjennetty pelaajalta!");
                                });
                            }

                        } else if(args.length == 2) {

                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if(args[0].equalsIgnoreCase("load")) {

                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Pelaajan §a" + target.getName() + " §7tietoja haetaan tietokannasta...");
                                Autio.async(() ->
                                    PlayerData.loadPlayer(target.getUniqueId(), (result) -> {
                                        if (result) Chat.sendMessage(player, Chat.Prefix.DEBUG, "Haettu ja ladattu!");
                                        else Chat.sendMessage(player, Chat.Prefix.DEBUG, "Tietoja ei ollut olemassa, ladattiin nollatiedot.");
                                    }));
                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Haettu ja ladattu!");

                            } else if(args[0].equalsIgnoreCase("update")) {

                                Player newTarget = Bukkit.getPlayer(args[1]);
                                if(newTarget == null) {
                                    Chat.sendMessage(player, "En löytänyt tuota pelaajaa");
                                    return true;
                                }

                                Autio.updatePlayer(newTarget);
                                Chat.sendMessage(player, "Päivitit asetukset ja tagin pelaajalta §a" + target.getName() + "§7!");
                            } else if(args[0].equalsIgnoreCase("resetData")) {
                                Autio.async(() -> {
                                    PlayerData.loadNull(target.getUniqueId(), true);
                                    PlayerData.savePlayer(target.getUniqueId());
                                    Chat.sendMessage(player, "Data tyhjennetty pelaajalta §a" + target.getName() + "§7!");
                                });
                            }
                        }

                    }
                } else {

                    Autio.runDebug(player);

                }

            } else if(command.getLabel().equalsIgnoreCase("posti")) {

                if(args.length < 1) {
                    Mail.panel(player);
                } else {
                    if(player.isOp()) {

                        if(args.length < 2) {
                            Chat.sendMessage(player, "/posti addStreak <player>");
                            Chat.sendMessage(player, "/posti addTickets <player>");
                            Chat.sendMessage(player, "/posti getStreak <player>");
                            Chat.sendMessage(player, "/posti getTickets <player>");
                        } else {

                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if(!PlayerData.isLoaded(target.getUniqueId())) {
                                PlayerData.loadNull(target.getUniqueId(), false);
                            }

                            if(args[0].equalsIgnoreCase("addStreak")) {
                                Mail.addStreak(target);
                                Chat.sendMessage(player, "Lisätty streak pelaajalle §6" + target.getName() + "§7!");
                            } else if(args[0].equalsIgnoreCase("addTickets")) {
                                Mail.addTickets(target.getUniqueId(), 1);
                                Chat.sendMessage(player, "Lisätty §6arpa §7pelaajalle §6" + target.getName() + "§7!");
                            } else if(args[0].equalsIgnoreCase("getStreak")) {
                                Chat.sendMessage(player, "Pelaajan §6" + target.getName() + " §7streak on §6" + Mail.getStreak(target));
                            } else if(args[0].equalsIgnoreCase("getTickets")) {
                                Chat.sendMessage(player, "Pelaajan §6" + target.getName() + " §7arvat: §6" + Mail.getTickets(target));
                            }

                        }

                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("arpa")) {

                Lottery.lot(player);

            } else if(command.getLabel().equalsIgnoreCase("iteminfo")) {

                if(player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    Chat.sendMessage(player, "Sinulla ei ole kädessä mitään!");
                    return true;
                }

                ItemStack item = player.getInventory().getItemInMainHand();
                player.sendMessage("§7§m--------------------");
                player.sendMessage(" §7Materiaali (Spigot): §6" + item.getType().toString());
                player.sendMessage(" §7Määrä: §6" + item.getAmount());
                player.sendMessage(" §7Nimi (Minecraft): §6" + item.getType().getKey());
                player.sendMessage("§7§m--------------------");

            } else if(command.getLabel().equalsIgnoreCase("warp")) {

                if(args.length < 1) {
                    Warps.panel(player);
                } else {
                    if(player.isOp()) {

                        if(args.length < 2) {
                            if(args[0].equalsIgnoreCase("load")) {

                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Ladataan warppeja...");
                                Warps.loadWarps((value) -> {
                                    String output = (value) ? "§7Warpit ladattiin!" : "§7Ei voitu ladata warppeja...";
                                    Chat.sendMessage(player, Chat.Prefix.DEBUG, output);
                                });

                            } else if(args[0].equalsIgnoreCase("help")) {
                                Chat.sendMessage(player, "/warp set <nimi>");
                                Chat.sendMessage(player, "/warp delete <nimi>");
                                Chat.sendMessage(player, "/warp load");
                                Chat.sendMessage(player, "/warp setDisplayname <warp> <uusi_nimi>");
                                Chat.sendMessage(player, "/warp setDescription <warp> <kuvaus>");
                            } else {
                                String name = args[0].toLowerCase();
                                Warp warp = Warps.get(name);
                                if(warp == null) {
                                    Chat.sendMessage(player, "Tuota warppia ei löytynyt...");
                                    return true;
                                }
                                warp.teleport(player);
                            }
                        } else if(args.length < 3) {

                            String name = args[1].toLowerCase();

                            if(args[0].equalsIgnoreCase("delete")) {

                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Poistetaan warp §6" + name + "§7...");
                                Warps.deleteWarp(name, (value) -> {
                                    if(value) {
                                        Chat.sendMessage(player, Chat.Prefix.DEBUG, "Warp poistettiin!");
                                    } else {
                                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei voitu poistaa warppia!");
                                    }
                                });

                            } else if(args[0].equalsIgnoreCase("set")) {

                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Asettaan warp §6" + name + "§7...");
                                Warps.createWarp(player.getLocation(), name.toLowerCase(), (value) -> {
                                    if(value) {
                                        Chat.sendMessage(player, Chat.Prefix.DEBUG, "Warp asetettiin!");
                                    } else {
                                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei voitu asettaa warppia!");
                                    }
                                });
                            }

                        } else {

                            String name = args[1].toLowerCase();

                            Warp warp = Warps.get(name);
                            if(warp == null) {
                                Chat.sendMessage(player, "Warppia ei löytynyt");
                                return true;
                            }

                            StringBuilder sb = new StringBuilder();
                            for(int i = 2; i < args.length; i++) {
                                sb.append(args[i] + " ");
                            }
                            String text = sb.toString().trim();

                            if(args[0].equalsIgnoreCase("setDisplayname")) {

                                warp.setDisplayName(text);

                            } else if(args[0].equalsIgnoreCase("setDescription")) {

                                warp.setDescription(text);

                            }

                            Warps.saveWarp(warp, (value) -> {
                                if(value) {
                                    Chat.sendMessage(player, Chat.Prefix.DEBUG, "Muutokset tehty!");
                                } else {
                                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei voitu tallentaa warppia!");
                                }
                            });

                        }

                    } else {
                        String name = args[0].toLowerCase();
                        Warp warp = Warps.get(name);
                        if(warp == null) {
                            Chat.sendMessage(player, "Tuota warppia ei löytynyt...");
                            return true;
                        }
                        warp.teleport(player);

                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("tehostus")) {

                if(!player.isOp()) {
                    Boosters.panel(player);
                } else {
                    if(args.length >= 1) {

                        if(args.length == 1) {

                            if(args[0].equalsIgnoreCase("clearAll")) {
                                for(Boosters.Booster booster : Boosters.Booster.values()) {

                                    if(Boosters.isActive(booster)) {
                                        Boosters.deactivate(booster);
                                    }

                                }
                                Chat.sendMessage(player, "Kaikki aktiiviset tehostukset lopetettu!");
                            } else if(args[0].equalsIgnoreCase("clearCooldown")) {

                                for(Boosters.Booster booster : Boosters.Booster.values()) {
                                    if(Boosters.isInCooldown(booster)) {
                                        Boosters.getInCooldown().remove(booster.getDisplayName());
                                    }
                                }

                                Chat.sendMessage(player, "Kaikkien tehostuksien jäähy tyhjennetty!");

                            } else if(args[0].equalsIgnoreCase("disable")) {
                                Boosters.ENABLED = false;
                                Chat.sendMessage(player, "Tehostukset ovat nyt §cpois päältä§7!");
                            } else if(args[0].equalsIgnoreCase("enable")) {
                                Boosters.ENABLED = true;
                                Chat.sendMessage(player, "Tehostukset ovat nyt §apäällä§7!");
                            }
                            else {
                                Chat.sendMessage(player, "/tehostus clearAll");
                                Chat.sendMessage(player, "/tehostus clear [tehostus]");
                                Chat.sendMessage(player, "/tehostus clearCooldown");
                                Chat.sendMessage(player, "/tehostus (enable | disable)");
                            }

                        } else {

                            Boosters.Booster booster = Boosters.getBoosterByName(args[1]);

                            if(booster == null) {
                                Chat.sendMessage(player, "Tuota tehostussa ei ole olemassa!");
                                return true;
                            }

                            if(Boosters.isActive(booster)) {
                                Boosters.deactivate(booster);
                                Chat.sendMessage(player, "Tehostus " + booster.getDisplayName() + " §7lopetettu!");
                            } else {
                                Chat.sendMessage(player, "Tuo tehostus ei ole aktiivinen..");
                            }

                        }

                    } else {
                        Boosters.panel(player);
                    }
                }

            } else if(command.getLabel().equalsIgnoreCase("back")) {

               if(!Ranks.isStaff(player.getUniqueId())) {
                   Chat.sendMessage(player, "Ei oikeuksia!");
                   return true;
               }

               if(!StaffManager.hasStaffMode(player)) {
                   Chat.sendMessage(player, "Sallittu vain ylläpitotilassa!");
                   return true;
               }

                if(!Events.lastLocation.containsKey(uuid)) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei ole mitään mihin viedä.");
                } else {
                    Location loc = Events.lastLocation.get(uuid);
                    if(loc != null) {
                        Chat.sendMessage(player, "Viedään äskeiseen sijaintiin...");
                        player.teleport(Events.lastLocation.get(uuid));
                    } else {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei ole mitään mihin viedä.");
                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("enderchest")) {

                if(!Ranks.isStaff(uuid)) {
                    Chat.sendMessage(player, "Ei oikeuksia!");
                    return true;
                }

               if(!StaffManager.hasStaffMode(player)) {
                   Chat.sendMessage(player, "Sallittu vain ylläpitotilassa!");
                   return true;
               }

                if(args.length < 1) {
                    Inventory ec = player.getEnderChest();
                    player.openInventory(ec);
                } else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null) {
                        Chat.sendMessage(player, "En löytänyt tuota pelaajaa");
                        return true;
                    }

                    player.openInventory(player.getEnderChest());

                }


            } else if(command.getLabel().equalsIgnoreCase("weather")) {
                if(Ranks.isStaff(uuid)) {

                    StaffManager.weatherGui(player);

                }
            } else if(command.getLabel().equalsIgnoreCase("vanish")) {
                if(Ranks.isStaff(uuid)) {
                    StaffManager.toggleVanish(player);
                }
            }

        } else {

            //CONSOLE ONLY COMMANDS

            if(command.getLabel().equalsIgnoreCase("spawn")) {
                if(args.length < 1) {
                    sender.sendMessage("§c/spawn <pelaaja>");
                } else {
                    if(sender.isOp()) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            sender.sendMessage("§cPelaajaa ei löydetty...");
                            return true;
                        }
                        Autio.teleportToSpawn(target);
                        sender.sendMessage("§7Pelaaja §a" + target.getName() + " §7vietiin spawnille!");
                    }
                }
            }

        }


        return true;
    }

}
