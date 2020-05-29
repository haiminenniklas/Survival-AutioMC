package me.tr.survival.main.other.travel;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EndManager implements CommandExecutor {

    // Command
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(args.length < 1) {
                panel(player);
            } else {

                if(args[0].equalsIgnoreCase("kutsu") || args[0].equalsIgnoreCase("luota")  || args[0].equalsIgnoreCase("lisää")) {

                    if(args.length == 2) {

                        Player target = Bukkit.getPlayer(args[1]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                            return true;
                        }

                        invite(player, target);

                    } else {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytä §c/end kutsu <pelaaja>§7!");
                    }

                } else if(args[0].equalsIgnoreCase("poista")) {
                    if(args.length == 2) {

                        Player target = Bukkit.getPlayer(args[1]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                            return true;
                        }

                        remove(player, target);

                    } else {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytä §c/end poista <pelaaja>§7!");
                    }
                } else if(args[0].equalsIgnoreCase("forcestop")) {
                    if(player.isOp()) {
                        end();
                    }
                } else if(args[0].equalsIgnoreCase("forcetp")) {

                    if(args.length < 2) {

                        teleport(player, true);

                    } else {
                        Player target = Bukkit.getPlayer(args[1]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                            return true;
                        }

                        teleport(target, true);
                    }

                } else if(args[0].equalsIgnoreCase("disable")) {

                   if(player.isOp()) {
                       enabled = false;
                       Chat.sendMessage(player, "End on nyt §cpois päältä§7!");
                   }

                } else if(args[0].equalsIgnoreCase("enable")) {

                    if(player.isOp()) {
                        enabled = true;
                        Chat.sendMessage(player, "End on nyt §apäällä§7!");
                    }

                } else if(args[0].equalsIgnoreCase("help")) {
                    if(player.isOp()) {
                        Chat.sendMessage(player, "§c/ääri forcestop");
                        Chat.sendMessage(player, "§c/ääri forcetp [pelaaja]");
                        Chat.sendMessage(player, "§c/ääri (disable / enable)");
                    } else {
                        Bukkit.dispatchCommand(player, "apua matkustaminen");
                    }
                }

            }

        }

        return true;
    }

    // File Management
    private static File endFile;
    private static FileConfiguration endConfig;

    public static void createEndConfig() {
        endFile = new File(Main.getInstance().getDataFolder(), "end.yml");
        if (!endFile.exists()) {
            endFile.getParentFile().mkdirs();
            Main.getInstance().saveResource("end.yml", false);
        }
        endConfig= new YamlConfiguration();
        reloadEndConfig();
    }

    public static void saveEndConfig() {

        if(isOccupied()) {
            getEndConfig().set("running", true);
            getEndConfig().set("holder", holder.toString());
            getEndConfig().set("started", started);
            getEndConfig().set("enabled", enabled);
            List<String> uuids = new ArrayList<>();
            for(UUID uuid : allowedPlayers) {
                uuids.add(uuid.toString());
            }
            getEndConfig().set("allowed", uuids);
        } else {
            getEndConfig().set("running", false);
        }

        try {
            endConfig.save(endFile);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void reloadEndConfig() {
        try {
            endConfig.load(endFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration getEndConfig() {
        return endConfig;
    }

    //SETTINGS
    private static final long durationMillis = 1000 * 60 * 60 * 2;
    private static final long durationMinutes = durationMillis / 1000 / 60;
    private static final int price = 500000;

    private static List<UUID> allowedPlayers = new ArrayList<>();
    private static UUID holder = null;

    private static long started = 0L;
    private static boolean enabled = true;

    private static final int MAX_PLAYERS = 3;

    public static void panel(Player player) {
        Gui.openGui(player, "Matkusta Endiin", 27, (gui) -> {

            List<String> lore = new ArrayList<>();
            lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            lore.add(" §7§oPystyt aktivoimaan itsellesi");
            lore.add( "§7§oja kavereillesi §5§oEndin");
            lore.add(" §7§omuutamaksi tunniksi!");

            lore.add(" ");
            if(isOccupied()) {

                lore.add(" §7Tila: §c§lVARATTU");
                lore.add(" §7Varaaja: §c" + getHolderPlayer().getName());
                lore.add(" §7Loppuu: §c" + getTimeLeft());
                lore.add(" ");

                // Text to show participants

                List<UUID> temp = allowedPlayers;
                temp.remove(getHolderPlayer().getUniqueId());
                if(temp.size() >= 1) {
                    lore.add("Osallistujat: ");
                    lore.add("  §c" + StringUtils.join(temp, "§7,§c "));
                }

            } else {
                lore.add(" §7Tila: §a§lVAPAA");
                lore.add(" §7Hinta: §a§l500 000€");
                lore.add(" ");
                if(Balance.canRemove(player.getUniqueId(), price)) {
                    lore.add(" §aKlikkaa aktivoidaksesi!");
                } else {
                    lore.add(" §cSinulla ei ole varaa");
                }
            }

            lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            ItemStack item = ItemUtil.makeItem(Material.END_PORTAL_FRAME, 1, "§5End", lore);

            if(isOccupied()) item = Util.makeEnchanted(item);

            gui.addButton(new Button(1, 13, item) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    if(!isOccupied()) {
                        if(Balance.canRemove(player.getUniqueId(), price)) {
                            gui.close(clicker);
                            start(clicker);
                        } else {
                            clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        }
                    } else {
                        if(allowedPlayers.contains(clicker.getUniqueId())) {
                            gui.close(clicker);
                            teleport(clicker, false);
                        } else {
                            clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        }
                    }
                }
            });

        });
    }

    public static void loadPreviousData() {
        if(getEndConfig().get("running") != null && getEndConfig().getBoolean("running")) {
            started = getEndConfig().getLong("started");
            holder = UUID.fromString(getEndConfig().getString("holder"));
            allowedPlayers = new ArrayList<>();
            for(String sUuid : getEndConfig().getStringList("allowed")) {
                allowedPlayers.add(UUID.fromString(sUuid));
            }
        }

        if(getEndConfig().get("enabled") != null) {
            enabled = getEndConfig().getBoolean("enabled");
        } else {
            enabled = true;
        }

    }

    public static void start(Player player) {

        if(!enabled) {
            Chat.sendMessage(player, "End ei ole käytettävissä tällä hetkellä... Odotathan, kunnes ylläpito pistää sen takaisin päälle!");
            return;
        }

        if(Balance.canRemove(player.getUniqueId(), price)) {

            Balance.remove(player.getUniqueId(), price);

            started = System.currentTimeMillis();
            holder = player.getUniqueId();

            // Create World

            Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
            Bukkit.broadcastMessage(" §5§lEND AKTIVOITU!");
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(" §7Aktivoija: §a" + player.getName());
            Bukkit.broadcastMessage(" §7Kesto: §a2h");
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(" §cGeneroimme End-maailmaa, joka");
            Bukkit.broadcastMessage(" §csaattaa synnyttää lagia.");
            Bukkit.broadcastMessage(" §cPahoittelemme häiriötä...");
            Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            Util.broadcastSound(Sound.ENTITY_ENDER_DRAGON_DEATH);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv create world_the_end end");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import world_the_end");

            Chat.sendMessage(player, "§7Aktivoit §5Endin§7! Hienoa työtä! Jos haluat jakaa §5Endin §7salaisuudet ystäviesi kanssa, pystyt päästämään " +
                    "heidät komennolla §a/ääri lisää <pelaaja>§7! Hienoja löytöretkiä ja onnea matkaan! Sinulla on §e2 tuntia §7aikaa!");

            allowedPlayers.add(player.getUniqueId());

            //Teleport
            /*new BukkitRunnable() {
                @Override
                public void run() {
                    teleport(player, false);
                }
            }.runTaskLater(Main.getInstance(), 20); */

        }

    }

    public static void invite(Player invitor, Player target) {

        if(isOccupied()) {

            if(!holder.equals(invitor.getUniqueId())) {
                Chat.sendMessage(invitor, Chat.Prefix.ERROR, "End ei ole aktivoitu sinun nimissäsi!");
                return;
            }

            if(allowedPlayers.contains(target.getUniqueId())) {
                Chat.sendMessage(invitor, Chat.Prefix.ERROR, "Tämä pelaaja on jo lisätty sallittujen listalle!");
                return;
            }

            if(allowedPlayers.size() >= MAX_PLAYERS) {
                Chat.sendMessage(invitor, Chat.Prefix.ERROR, "Olet jo kutsunut liian monta pelaajaa Endiin!");
                return;
            }

            allowedPlayers.add(target.getUniqueId());
            Chat.sendMessage(invitor, "Pelaaja §a" + target.getName() + " §7on nyt sallittu kulkemaan §5Endiin§7! Voit poistaa pääsyn komennolla §a/ääri poista " + target.getName() + "§7!");
            Chat.sendMessage(target, "Pääset nyt kulkemaan §5Endiin§7! Pääsyn myönsi pelaaja §a" + invitor.getName() + "§7!");

        } else {
            Chat.sendMessage(invitor, Chat.Prefix.ERROR, "End ei ole aktivoitu");
        }

    }

    public static void remove(Player invitor, Player target) {

        if(isOccupied()) {

            if(!holder.equals(invitor.getUniqueId())) {
                Chat.sendMessage(invitor, Chat.Prefix.ERROR, "End ei ole aktivoitu sinun nimissäsi!");
                return;
            }

            if(!allowedPlayers.contains(target.getUniqueId())) {
                Chat.sendMessage(invitor, Chat.Prefix.ERROR, "Tämä pelaaja ei ole sallittujen listalla!");
                return;
            }

            allowedPlayers.remove(target.getUniqueId());
            Chat.sendMessage(invitor, "Pelaajalta §a" + target.getName() + " §7on nyt evätty pääsy kulkemaan §5Endiin§7! Voit antaa pääsyn uudelleen komennolla §a/ääri luota " + target.getName() + "§7!");
            Chat.sendMessage(target, "Pääsysi §5Endiin §7on evätty! Pääsyn poisti pelaaja §a" + invitor.getName() + "§7!");

        } else {
            Chat.sendMessage(invitor, Chat.Prefix.ERROR, "End ei ole aktivoitu");
        }

    }

    public static boolean isInEnd(Player player) {
        return player.getWorld().getName().equalsIgnoreCase("world_the_end");
    }

    public static void teleport(Player player, boolean force) {

        if(!force && !enabled) {
            Chat.sendMessage(player, "End ei ole käytettävissä tällä hetkellä... Odotathan, kunnes ylläpito pistää sen takaisin päälle!");
            return;
        }

        if(!force && !allowedPlayers.contains(player.getUniqueId())) {
            Chat.sendMessage(player, "Et ole sallittujen pelaajien listalla §5Endiin§7!");
            return;
        }

        Chat.sendMessage(player, "Sinut viedään §5Endiin §c3s §7kuluttua...");
        Autio.after(3, () -> player.teleport(Bukkit.getWorld("world_the_end").getSpawnLocation()));


    }

    public static void startManager() {
        Autio.everyAsync(5, () -> {
            if(!canContinue()) Autio.task(EndManager::end);
        });
    }

    private static boolean canContinue() {
        if(isOccupied()) {
            long shouldEnd = started + durationMillis;
            return System.currentTimeMillis() < shouldEnd;
        }
        return false;
    }

    public static void end() {

        if(!isOccupied()) return;

        OfflinePlayer player = getHolderPlayer();

        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        Bukkit.broadcastMessage(" §5§lEND DEAKTIVOITU!");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §aEnd taas aktivoitavissa!");
        Bukkit.broadcastMessage(" §7Aktivoija: §c" + player.getName());
        Bukkit.broadcastMessage(" §7Kesto: §c2h");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §cPoistamme End-maailmaa, joka");
        Bukkit.broadcastMessage(" §csaattaa synnyttää lagia.");
        Bukkit.broadcastMessage(" §cPahoittelemme häiriötä...");
        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        Util.broadcastSound(Sound.ENTITY_BLAZE_DEATH);

        for(UUID uuid : allowedPlayers) {
            if(Bukkit.getPlayer(uuid) != null) {
                Player o = Bukkit.getPlayer(uuid);
                Autio.teleportToSpawn(o);
            }
        }

        started = 0L;
        holder = null;
        allowedPlayers = new ArrayList<>();
        saveEndConfig();

        World world = Bukkit.getWorld("world_the_end");

        // Kill the dragons
        if(Util.isEntityTypeAlive(world, EntityType.ENDER_DRAGON)) {
            for(Entity e : Util.getEntities(world, EntityType.ENDER_DRAGON)) {
                EnderDragon dragon = (EnderDragon) e;
                dragon.damage(dragon.getHealth() + 20);
            }
        }

        File worldFolder = world.getWorldFolder();
        // Delete End World
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv remove world_the_end");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");

        // Delete world directory
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    FileUtils.deleteDirectory(worldFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.runTaskLater(Main.getInstance(), 20);

    }

    public static OfflinePlayer getHolderPlayer() {
        if(holder == null) return null;
        return Bukkit.getOfflinePlayer(holder);
    }

    public static long getTimeLeftMillis() {
        if(isOccupied()) {
            long shouldStop = started + durationMillis;
            return shouldStop - System.currentTimeMillis();
        }
        return 0L;
    }

    public static String getTimeLeft() {
        if(getTimeLeftMillis() >= 1) {
            long millis = getTimeLeftMillis();
            long minute = (millis / (1000 * 60)) % 60;
            long hour = (millis / (1000 * 60 * 60)) % 24;
            return Util.formatTime((int) hour, (int) minute, true);
        }
        return "§cEi käynnissä";
    }

    public static boolean isOccupied() {
        return holder != null && started >= 1L;
    }



}
