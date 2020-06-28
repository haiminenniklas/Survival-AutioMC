package me.tr.survival.main;

import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import me.tr.survival.main.commands.*;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.listeners.*;
import me.tr.survival.main.managers.*;
import me.tr.survival.main.managers.features.Backpack;
import me.tr.survival.main.managers.features.Boosters;
import me.tr.survival.main.managers.features.Houkutin;
import me.tr.survival.main.managers.features.Lottery;
import me.tr.survival.main.managers.other.AutoBroadcaster;
import me.tr.survival.main.managers.other.WeatherVote;
import me.tr.survival.main.managers.perks.Particles;
import me.tr.survival.main.managers.perks.PlayerDeathMessageManager;
import me.tr.survival.main.managers.perks.PlayerGlowManager;
import me.tr.survival.main.managers.travel.EndManager;
import me.tr.survival.main.managers.travel.TravelManager;
import me.tr.survival.main.managers.villages.VillageManager;
import me.tr.survival.main.other.*;
import me.tr.survival.main.managers.warps.Warp;
import me.tr.survival.main.managers.warps.Warps;
import me.tr.survival.main.managers.trading.TradeManager;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.managers.RTP;
import me.tr.survival.main.util.Times;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.callback.SpigotCallback;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.database.data.Crystals;
import me.tr.survival.main.database.data.Level;
import me.tr.survival.main.managers.StaffManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;
    public static Main getInstance() {
        return Main.instance;
    }
    private static LuckPerms luckPerms;
    static LuckPerms getLuckPerms() {
        return Main.luckPerms;
    }
    private static PlayerParticlesAPI particlesAPI;
    static PlayerParticlesAPI getParticlesAPI() {
        return particlesAPI;
    }
    private static Map<UUID, Long> spawnCommandDelay = new HashMap<>();

    private long started = 0L;

    // Instances for Managers
    private static Backpack backpack ;
    public static Backpack getBackpack() { return backpack; }

    private static MoneyManager moneyManager;
    public static MoneyManager getMoneyManager() { return moneyManager; }

    private static TravelManager travelManager;
    public static TravelManager getTravelManager() { return travelManager; }

    private static StaffManager staffManager;
    public static StaffManager getStaffManager() { return staffManager; }

    private static TradeManager tradeManager;
    public static TradeManager getTradeManager() { return tradeManager; }

    private static EndManager endManager;
    public static EndManager getEndManager() { return endManager; }

    private static PlayerGlowManager playerGlowManager;
    public static PlayerGlowManager getPlayerGlowManager() { return playerGlowManager; }

    private static PlayerDeathMessageManager playerDeathMessageManager;
    public static PlayerDeathMessageManager getPlayerDeathMessageManager() { return playerDeathMessageManager; }

    private static Particles particles;
    public static Particles getParticles() { return particles; }

    private static Houkutin houkutin;
    public static Houkutin getHoukutin() { return houkutin; }

    private static AFKManager afkmanager;
    public static AFKManager getAFKManager() { return afkmanager; }

    private static VillageManager villageManager;
    public static VillageManager getVillageManager() { return villageManager; }

    // Other instances
    private static TpaCommand tpaCommand;
    private static Essentials essentials;
    private static MoneyCommand moneyCommand;
    private static StopCommand stopCommand;
    private static VipCommand vipCommand;
    private static AntiAFKFishing antiAFKFishing;
    private static WeatherVote weatherVote;

    // Listener instances
    private static Events events;
    public static Events getEventsListener() { return events; }

    private static ActionEvents actionEvents;
    private static GuiEvents guiEvents;
    private static ConnectionEvents connectionEvents;

    @Override
    public void onEnable() {
        // Setup instances
        Main.instance = this;
        Main.luckPerms = LuckPermsProvider.get();

        // Managers
        Main.backpack = new Backpack();
        Main.moneyManager = new MoneyManager();
        Main.travelManager = new TravelManager();
        Main.staffManager = new StaffManager();
        Main.tradeManager = new TradeManager();
        Main.endManager = new EndManager();
        Main.playerGlowManager = new PlayerGlowManager();
        Main.playerDeathMessageManager = new PlayerDeathMessageManager();
        Main.particles = new Particles();
        Main.houkutin = new Houkutin();
        Main.afkmanager = new AFKManager();
        Main.villageManager = new VillageManager();

        // Commands
        Main.tpaCommand = new TpaCommand();
        Main.essentials = new Essentials();
        Main.moneyCommand = new MoneyCommand();
        Main.stopCommand = new StopCommand();
        Main.vipCommand = new VipCommand();

        // Listeners
        Main.events = new Events();
        Main.actionEvents = new ActionEvents();
        Main.guiEvents = new GuiEvents();
        Main.connectionEvents = new ConnectionEvents();

        // Other
        Main.antiAFKFishing = new AntiAFKFishing();
        Main.weatherVote = new WeatherVote();

        new SpigotCallback(this);

        Sorsa.logColored("§a---------------------------");
        Sorsa.logColored(" §aEnabling SorsaSurvival...");

        final long start = System.currentTimeMillis();

        Sorsa.logColored(" ");
        Sorsa.logColored(" §6IF YOU DON'T WANT LOGS FROM THE PLUGIN, DISABLE IT FROM THE config.yml!");
        Sorsa.logColored(" ");
        Sorsa.logColored(" §aSetupping configs and database...");

        saveDefaultConfig();
        endManager.createEndConfig();
        endManager.loadPreviousData();
        villageManager.createVillageConfig();
        villageManager.loadVillagesFromFile();
        SQL.setup();

        Sorsa.logColored(" §aRegistering plugin event listeners...");

        // Events
        PluginManager pm = getServer().getPluginManager();

        // Listeners
        pm.registerEvents(this, this);
        pm.registerEvents(events, this);
        pm.registerEvents(actionEvents, this);
        pm.registerEvents(guiEvents, this);
        pm.registerEvents(connectionEvents, this);

        // Managers
        pm.registerEvents(new Chat(), this);
        pm.registerEvents(new AntiCheat(), this);

        pm.registerEvents(staffManager, this);
        pm.registerEvents(essentials, this);
        pm.registerEvents(tradeManager, this);
        pm.registerEvents(travelManager, this);
        pm.registerEvents(moneyManager, this);
        pm.registerEvents(backpack, this);
        pm.registerEvents(playerDeathMessageManager, this);
        pm.registerEvents(particles, this);
        pm.registerEvents(weatherVote, this);
        pm.registerEvents(afkmanager, this);
        pm.registerEvents(villageManager, this);

        // Other
        pm.registerEvents(antiAFKFishing, this);

        Sorsa.logColored(" §aRegistering messaging channels for BungeeCord...");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeEvents());

        Sorsa.logColored(" §aRegistering PlayerParticlesAPI...");

        if (Bukkit.getPluginManager().getPlugin("PlayerParticles") != null) Main.particlesAPI = PlayerParticlesAPI.getInstance();
        else {
            Sorsa.log("§cCould not find PlayerParticles plugin, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Sorsa.logColored(" §aDisabling Announcement of Advancements...");

        // Disable Advancement announcing
        for(World world : Bukkit.getWorlds()) { world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false); }

        // Commands
        Sorsa.logColored(" §aRegistering commands....");

        getCommand("tpa").setExecutor(tpaCommand);
        getCommand("tpaccept").setExecutor(tpaCommand);
        getCommand("tpahere").setExecutor(tpaCommand);
        getCommand("tp").setExecutor(tpaCommand);
        getCommand("tphere").setExecutor(tpaCommand);
        getCommand("tpdeny").setExecutor(tpaCommand);

        getCommand("reppu").setExecutor(backpack);

        getCommand("afk").setExecutor(afkmanager);

        getCommand("village").setExecutor(villageManager);

        getCommand("broadcast").setExecutor(essentials);
        getCommand("world").setExecutor(essentials);
        getCommand("clear").setExecutor(essentials);
        getCommand("invsee").setExecutor(essentials);
        getCommand("sää-äänestys").setExecutor(weatherVote);

        getCommand("bal").setExecutor(moneyCommand);
        getCommand("pay").setExecutor(moneyCommand);

        getCommand("valuutta").setExecutor(moneyManager);
        getCommand("shekki").setExecutor(moneyManager);

        getCommand("stop").setExecutor(stopCommand);
        getCommand("forcestop").setExecutor(stopCommand);

        getCommand("trade").setExecutor(tradeManager);
        getCommand("staff").setExecutor(staffManager);
        getCommand("ääri").setExecutor(endManager);
        getCommand("hehku").setExecutor(playerGlowManager);
        getCommand("matkusta").setExecutor(travelManager);
        getCommand("givevip").setExecutor(vipCommand);
        getCommand("kosmetiikka").setExecutor(particles);
        getCommand("houkutin").setExecutor(houkutin);

        getCommand("home").setExecutor(new HomeCommand());
        getCommand("baltop").setExecutor(new BaltopCommand());
        getCommand("apua").setExecutor(new HelpCommand());

        // Autosave code...

        Sorsa.logColored(" §aStarting autosaving for players...");
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {

            Sorsa.logColored("§a[Database] Trying to save the data of " + Bukkit.getOnlinePlayers().size() + " players...");
            int times_saved = 0;
            for(Player player : Bukkit.getOnlinePlayers()) {
                times_saved += 1;
                PlayerData.savePlayer(player.getUniqueId());
            }
            Sorsa.logColored("§a[Database] Saved the data of " + times_saved + " players!");

            // Fetch Balances...
            new Balance().fetchTopBalance();

        }, 20, (20*60) * 5);


        getServer().getScheduler().runTaskTimer(Main.getInstance(), (task -> {

            // This runnable is responsible for restarting the server every morning
            final Calendar rightNow = Calendar.getInstance();
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);
            // Check if it's atleast 5 o'clok (in the morning). This runnable should
            // check the time every minute, so it should start the restart at least
            // a minute after 5 o'clock
            int minute = rightNow.get(Calendar.MINUTE);
            if(hour == 5 && minute <= 5) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");

        }), 20, (long) 20 * 60);


        Sorsa.logColored(" §aStarting AutoBroadcaster...");
        AutoBroadcaster.start();

        Sorsa.logColored(" §aStarting Managers...");
        Boosters.activateManager();
        Main.getHoukutin().activateManager();
        Main.getEndManager().startManager();
        Main.getAFKManager().enableChecker();

        Sorsa.logColored(" §aInitializing ChatManager");
        Chat.init();

        Sorsa.log(" §aLoading Custom Recipes...");

        Sorsa.log(" §aIntegrating custom economy into Vault...");
        if(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) Bukkit.getServer().getServicesManager().register(Economy.class, new CustomEconomy(), this, ServicePriority.Highest);
        else {
            Sorsa.log(" §cCould not find Vault! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Warps.loadWarps(r -> {
            if(r) Sorsa.logColored("§a[Warps] All warps loaded successfully!");
            else Sorsa.logColored("§c[Warps] Warps were loaded unsuccessfully! Did an error occur?");
        });
        Bukkit.getServer().setWhitelist(getConfig().getBoolean("whitelist"));

        Sorsa.logColored("§a Enabled SorsaSurvival! (It took " + (System.currentTimeMillis() - start) +
                "ms / " + ((System.currentTimeMillis() - start) / 1000.0f) + "s)");
        Sorsa.logColored("§a---------------------------");

        started = System.currentTimeMillis();

    }

    @Override
    public void onDisable() {

        Sorsa.logColored("§a---------------------------");
        Sorsa.logColored(" §aDisabling SorsaSurvival....");
        Sorsa.logColored(" ");
        Sorsa.logColored(" §6IF YOU DON'T WANT LOGS FROM THE PLUGIN, DISABLE IT FROM THE config.yml!");
        Sorsa.logColored(" ");
        long start = System.currentTimeMillis();
        Sorsa.logColored(" §aSaving configs...");
        saveConfig();

        for(UUID uuid : PlayerData.getPlayerData().keySet()) {
            PlayerData.savePlayer(uuid);
        }

        Main.getEndManager().saveEndConfig();
        Sorsa.logColored(" §aClosing Database Connection...");
        SQL.source.close();
        Sorsa.logColored("§a Disabled SorsaSurvival! (It took " + (System.currentTimeMillis() - start) +
                "ms / " + ((System.currentTimeMillis() - start) / 1000.0f) + "s)");
        Sorsa.logColored("§a---------------------------");
        started = 0L;

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
                if(args.length < 1) Profile.openProfile(player, player.getUniqueId());
                else {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                    Profile.openOther(player, target);
                }
            }
            else if(command.getLabel().equalsIgnoreCase("vote")) Chat.sendMessage(player, "Äänestä meitä: §ahttps://minecraft-mp.com/server-s259722 §7! Äänestämällä saat itsellesä §e1kpl arpoja§7!");
            else if(command.getLabel().equalsIgnoreCase("vip")) Settings.vipPanel(player);
            else if(command.getLabel().equalsIgnoreCase("spawn")) {
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
                    }

                    if(!Main.getStaffManager().hasStaffMode(player)) {
                        spawnCommandDelay.put(uuid, System.currentTimeMillis() + (1000 * 60));
                        Chat.sendMessage(player, "Sinut viedään spawnille §c5s §7päästä!");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Sorsa.teleportToSpawn(player);
                                cancel();
                            }
                        }.runTaskLater(Main.getInstance(), 20 * 5);
                        return true;
                    }

                    Chat.sendMessage(player, "Sinut viedään nyt spawnille!");
                    Sorsa.teleportToSpawn(player);


                } else {
                    if(player.isOp()) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty");
                            return true;
                        }
                        Sorsa.teleportToSpawn(target);
                        Chat.sendMessage(player, "Pelaaja §a" + target.getName() + " §7vietiin spawnille!");
                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("setspawn")) {
                if(player.isOp()) {
                    Sorsa.setSpawn(player.getLocation());
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
                        if(!PlayerData.isLoaded(target.getUniqueId())) PlayerData.loadNull(target.getUniqueId(), false);
                        int value;
                        try { value = Integer.parseInt(args[2]);
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
            } else if(command.getLabel().equalsIgnoreCase("speed")) {
                if(player.isOp()) {
                    if(args.length < 1) {
                        Chat.sendMessage(player, "Käytä /speed <nopeus>");
                        Chat.sendMessage(player, "Nopeudet: 1-10 (normaali on 2)");
                    } else {
                        float value;
                        try { value = Float.parseFloat(args[0]);
                        } catch(NumberFormatException ex){
                            Chat.sendMessage(player, "Käytä numeroita");
                            return true;
                        }

                        if(value < 0) {
                            player.sendMessage("§6Nopeus min. 0");
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
            }
            else if(command.getLabel().equalsIgnoreCase("Settings")) Settings.panel(player);
            else if(command.getLabel().equalsIgnoreCase("gamemode")) {
                if(Ranks.isStaff(player.getUniqueId())) {
                    if(!Main.getStaffManager().hasStaffMode(player)) {
                        Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                        return true;
                    }
                    if(args.length < 1) {
                        if(player.isOp()) {
                            if(player.getGameMode() != GameMode.SURVIVAL) {
                                player.setGameMode(GameMode.SURVIVAL);
                                Chat.sendMessage(player, "Pelimuoto Survival");
                                Util.heal(player);
                            } else if(player.getGameMode() == GameMode.SURVIVAL) {
                                player.setGameMode(GameMode.CREATIVE);
                                Chat.sendMessage(player, "Pelimuoto Creative");
                            }
                        } else {
                            if(player.getGameMode() == GameMode.SPECTATOR) {
                                player.setGameMode(GameMode.SURVIVAL);
                                Chat.sendMessage(player, "Pelimuoto Survival");
                                Util.heal(player);
                            } else if(player.getGameMode() == GameMode.SURVIVAL) {
                                player.setGameMode(GameMode.SPECTATOR);
                                Chat.sendMessage(player, "Pelimuoto Spectator");
                            }
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
                                if(player.isOp()) {
                                    player.setGameMode(GameMode.CREATIVE);
                                    Chat.sendMessage(player, "Pelimuoto Creative");
                                }
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
                            if(player.isOp()) {
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
                }
            } else if(command.getLabel().equalsIgnoreCase("heal")) {
                if(Ranks.isStaff(player.getUniqueId())) {
                    if(Main.getStaffManager().hasStaffMode(player)) {
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
                        Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                        return true;
                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("time")) {
                if(player.isOp()) {
                    if(args.length < 1) {
                        Chat.sendMessage(player, "§7Käytä: §a/aika <day|night|noon>");
                        Main.getStaffManager().timeGui(player);
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
                                try { value = Long.parseLong(args[0]);
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

                    if(args.length < 1) {
                        Settings.toggleFlight(player);
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
                    if(!Ranks.hasRank(uuid, "sorsa") && !Ranks.isStaff(uuid)) Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän toimintoon vaaditaan §2§lSORSA§7-arvo! Lisätietoa §a/kauppa§7!");
                    else Settings.toggleFlight(player);
                }
            } else if(command.getLabel().equalsIgnoreCase("roskis")) {
               if(!Ranks.isVIP(player.getUniqueId())) {
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
                        if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                            Chat.sendMessage(player, "Sinulla pitää olla esine kädessä!");
                            return true;
                        }
                        int level;
                        try { level = Integer.parseInt(args[1]);
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
                            if(!PlayerData.isLoaded(target.getUniqueId())) PlayerData.loadNull(target.getUniqueId(), false);
                            if(args.length >= 3) {
                                int value;
                                try { value = Integer.parseInt(args[2]);
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
                            } else if(args[0].equalsIgnoreCase("get")) Chat.sendMessage(player, "Pelaajan §a" + target.getName() + "§7 kristallit: §b" + Crystals.get(target.getUniqueId()));
                        }
                    }
                } else Chat.sendMessage(player, "Täältä ei löydy sitä mitä etsit!");
            } else if(command.getLabel().equalsIgnoreCase("rtp")) {
               RTP.teleport(player);
            } else if(command.getLabel().equalsIgnoreCase("sijainti")) {
                Location loc = player.getLocation();
                Chat.sendMessage(player, "Sinun sijaintisi: §a" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            } else if(command.getLabel().equalsIgnoreCase("autio")) {
                if(player.isOp()) {
                    if(args.length < 1) {
                        Chat.sendMessage(player, "/sorsa reload");
                        Chat.sendMessage(player, "/sorsa logging");
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
                            Sorsa.setDeathSpawn(player.getLocation());
                            Chat.sendMessage(player, "Kuolemansaaren spawni asetettu!");
                        }
                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("skull")) {
                if(player.isOp()) {
                    if(args.length < 1) Chat.sendMessage(player, "§7Käytä: §a/skull <player>");
                    else {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        player.getInventory().addItem(ItemUtil.makeSkullItem(target, 1, "§6" + target.getName()));
                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("debug")) {
                if(player.isOp()) {
                    if(args.length < 1) {
                        Chat.sendMessage(player, "/debug load [player]");
                        Chat.sendMessage(player, "/debug mode");
                        Chat.sendMessage(player, "/debug info");
                        Chat.sendMessage(player, "/debug resetMail");
                        Chat.sendMessage(player, "/debug resetBooster");
                        Chat.sendMessage(player, "/debug run");
                        Chat.sendMessage(player, "/debug autobroadcast");
                        Chat.sendMessage(player, "/debug resetData [player] §cOle varovainen tän kaa!");
                        Chat.sendMessage(player, "/debug setSlots <amount>");
                    } else {
                        if(args.length >= 1) {
                            if(args[0].equalsIgnoreCase("mode")) {
                                Sorsa.toggleDebugMode(player);
                            } else if(args[0].equalsIgnoreCase("load")) {
                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Tietojasi haetaan tietokannasta...");
                                Sorsa.async(() ->
                                    PlayerData.loadPlayer(player.getUniqueId(), (result) -> {
                                        if(result) Chat.sendMessage(player, Chat.Prefix.DEBUG, "Haettu ja ladattu!");
                                        else Chat.sendMessage(player, Chat.Prefix.DEBUG, "Tietoja ei ollut olemassa, ladattiin nollatiedot.");
                                    }));
                            } else if(args[0].equalsIgnoreCase("autobroadcast")){
                              AutoBroadcaster.test();
                            }else if(args[0].equalsIgnoreCase("info")) {
                                player.sendMessage("§7§m--------------------");
                                player.sendMessage("§7Versio: §6" + Bukkit.getVersion());
                                player.sendMessage("§7Bukkit versio: §6" + Bukkit.getBukkitVersion());
                                player.sendMessage("§7Tämänhetkinen TPS: §6" + Sorsa.getCurrentTPS());
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
                                Sorsa.runDebug(player);
                            } else if(args[0].equalsIgnoreCase("resetData")) {
                                Sorsa.async(() -> {
                                    PlayerData.loadNull(player.getUniqueId(), true);
                                    PlayerData.savePlayer(player.getUniqueId());
                                    Chat.sendMessage(player, "Data tyhjennetty pelaajalta!");
                                });
                            } else if(args[0].equalsIgnoreCase("setSlots")) {

                                if(args.length >= 2) {

                                    int num;
                                    try {
                                        num = Integer.parseInt(args[1]);
                                    } catch(NumberFormatException ex) {
                                        Chat.sendMessage(player, "Käytä perhana oikeita numeroita!");
                                        return true;
                                    }
                                    try {
                                        Util.changeSlots(num);
                                        Chat.sendMessage(player, "Pelaajamäärä muutettu §a" + num + "§7!");
                                    } catch (ReflectiveOperationException e) {
                                        getLogger().log(java.util.logging.Level.WARNING, "An error occurred while updating max players", e);
                                    }

                                }

                            }
                        } else if(args.length == 2) {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                            if(args[0].equalsIgnoreCase("load")) {
                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Pelaajan §a" + target.getName() + " §7tietoja haetaan tietokannasta...");
                                Sorsa.async(() ->
                                    PlayerData.loadPlayer(target.getUniqueId(), (result) -> {
                                        if (result) Chat.sendMessage(player, Chat.Prefix.DEBUG, "Haettu ja ladattu!");
                                        else Chat.sendMessage(player, Chat.Prefix.DEBUG, "Tietoja ei ollut olemassa, ladattiin nollatiedot.");
                                    }));
                                Chat.sendMessage(player, Chat.Prefix.DEBUG, "Haettu ja ladattu!");
                            } else if(args[0].equalsIgnoreCase("resetData")) {
                                Sorsa.async(() -> {
                                    PlayerData.loadNull(target.getUniqueId(), true);
                                    PlayerData.savePlayer(target.getUniqueId());
                                    Chat.sendMessage(player, "Data tyhjennetty pelaajalta §a" + target.getName() + "§7!");
                                });
                            }
                        }
                    }
                } else Sorsa.runDebug(player);
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
                            if(!PlayerData.isLoaded(target.getUniqueId())) PlayerData.loadNull(target.getUniqueId(), false);
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
                if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    Chat.sendMessage(player, "Sinulla ei ole kädessäsi mitään!");
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
                            if(args[0].equalsIgnoreCase("setDisplayname")) warp.setDisplayName(text);
                            else if(args[0].equalsIgnoreCase("setDescription")) warp.setDescription(text);
                            Warps.saveWarp(warp, (value) -> {
                                if(value) Chat.sendMessage(player, Chat.Prefix.DEBUG, "Muutokset tehty!");
                                else Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei voitu tallentaa warppia!");
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
                                    if(Boosters.isActive(booster)) Boosters.deactivate(booster);
                                }
                                Chat.sendMessage(player, "Kaikki aktiiviset tehostukset lopetettu!");
                            } else if(args[0].equalsIgnoreCase("clearCooldown")) {
                                for(Boosters.Booster booster : Boosters.Booster.values()) {
                                    if(Boosters.isInCooldown(booster)) Boosters.getInCooldown().remove(booster.getDisplayName());
                                }
                                Chat.sendMessage(player, "Kaikkien tehostuksien jäähy tyhjennetty!");
                            } else if(args[0].equalsIgnoreCase("disable")) {
                                Boosters.ENABLED = false;
                                Chat.sendMessage(player, "Tehostukset ovat nyt §cpois päältä§7!");
                            } else if(args[0].equalsIgnoreCase("enable")) {
                                Boosters.ENABLED = true;
                                Chat.sendMessage(player, "Tehostukset ovat nyt §apäällä§7!");
                            } else {
                                Chat.sendMessage(player, "/tehostus clearAll");
                                Chat.sendMessage(player, "/tehostus clear [tehostus]");
                                Chat.sendMessage(player, "/tehostus clearCooldown");
                                Chat.sendMessage(player, "/tehostus (enable | disable)");
                            }
                        } else {
                            Boosters.Booster booster = Boosters.Booster.valueOf(args[0].toUpperCase());
                            if(booster == null) {
                                Chat.sendMessage(player, "Tuota tehostussa ei ole olemassa! Käytettävissä olevat Tehostukset: §a" + StringUtils.join(Boosters.Booster.values(), ", "));
                                return true;
                            }

                            if(Boosters.isActive(booster)) {
                                Boosters.deactivate(booster);
                                Chat.sendMessage(player, "Tehostus " + booster.getDisplayName() + " §7lopetettu!");
                            } else Chat.sendMessage(player, "Tuo tehostus ei ole aktiivinen..");
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
               if(!Main.getStaffManager().hasStaffMode(player)) {
                   Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                   return true;
               }
                if(!Main.getEventsListener().lastLocation.containsKey(uuid)) {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei ole mitään mihin viedä.");
                } else {
                    Location loc = Main.getEventsListener().lastLocation.get(uuid);
                    if(loc != null) {
                        Chat.sendMessage(player, "Viedään äskeiseen sijaintiin...");
                        player.teleport(Main.getEventsListener().lastLocation.get(uuid));
                    } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei ole mitään mihin viedä.");
                }
            } else if(command.getLabel().equalsIgnoreCase("enderchest")) {
                if(!Ranks.isStaff(uuid)) {
                    Chat.sendMessage(player, "Ei oikeuksia!");
                    return true;
                }
               if(!Main.getStaffManager().hasStaffMode(player)) {
                   Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                   return true;
               }
                if(args.length < 1) {
                    Inventory ec = player.getEnderChest();
                    player.openInventory(ec);
                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, "En löytänyt tuota pelaajaa");
                        return true;
                    }
                    player.openInventory(player.getEnderChest());
                }
            } else if(command.getLabel().equalsIgnoreCase("weather")) {
               if(!Main.getStaffManager().hasStaffMode(player)) {
                   Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                   return true;
               }
                if(Ranks.isStaff(uuid)) Main.getStaffManager().weatherGui(player);
            } else if(command.getLabel().equalsIgnoreCase("vanish")) {
               if(!Main.getStaffManager().hasStaffMode(player)) {
                   Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                   return true;
               }
                if(Ranks.isStaff(uuid)) Main.getStaffManager().toggleVanish(player);
            } else if(command.getLabel().equalsIgnoreCase("staffmode")) {
               if(Ranks.isStaff(uuid)) Main.getStaffManager().toggleStaffMode(player);
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
                        Sorsa.teleportToSpawn(target);
                        sender.sendMessage("§7Pelaaja §a" + target.getName() + " §7vietiin spawnille!");
                    }
                }
            } else if(command.getLabel().equals("uptime")) {
                long now = System.currentTimeMillis();
                long uptime = now - started;
                sender.sendMessage(String.format("§aPalvelin on ollut päällä §c%s", DurationFormatUtils.formatDurationWords(uptime, false, true)));
            }
        }
        return true;
    }
}
