package me.tr.survival.main.managers.travel;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.Main;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Settings;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
            if(args.length < 1) panel(player);
            else {
                if(args[0].equalsIgnoreCase("kutsu") || args[0].equalsIgnoreCase("luota")  || args[0].equalsIgnoreCase("lisää")  || args[0].equalsIgnoreCase("invite")) {
                    if(args.length == 2) {

                        Player target = Bukkit.getPlayer(args[1]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                            return true;
                        }

                        if(player.getName().equals(target.getName())) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Älä nyt hullu ole ja itseäsi uudelleen kutsu endiin!");
                            return true;
                        }

                        invite(player, target);

                    } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytä §c/end kutsu <pelaaja>§7!");

                } else if(args[0].equalsIgnoreCase("poista")) {
                    if(args.length == 2) {

                        Player target = Bukkit.getPlayer(args[1]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                            return true;
                        }

                        if(player.getName().equals(target.getName())) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Älä nyt hullu ole ja itseäsi poista Endin pääsylistalta!");
                            return true;
                        }

                        remove(player, target);

                    } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytä §c/end poista <pelaaja>§7!");

                } else if(args[0].equalsIgnoreCase("forcestop")) {
                    if(player.isOp()) end();
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
                       Chat.sendMessage(player, "End on nyt §cpois päältä§7! End suljetaan ja siellä olevat pelaajat teleportataan pois.");
                       end();

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
                    } else Bukkit.dispatchCommand(player, "apua matkustaminen");
                }
            }
        }
        return true;
    }

    // File Management
    private File endFile;
    private FileConfiguration endConfig;

    public void createEndConfig() {
        endFile = new File(Main.getInstance().getDataFolder(), "end.yml");
        if (!endFile.exists()) {
            endFile.getParentFile().mkdirs();
            Main.getInstance().saveResource("end.yml", false);
        }
        endConfig= new YamlConfiguration();
        reloadEndConfig();
    }

    public void saveEndConfig() {

        if(isOccupied()) {
            getEndConfig().set("running", true);
            getEndConfig().set("holder", holder.toString());
            getEndConfig().set("started", started);
            getEndConfig().set("enabled", enabled);
            List<String> uuids = new ArrayList<>();
            for(UUID uuid : allowedPlayers) { uuids.add(uuid.toString()); }
            getEndConfig().set("allowed", uuids);
        } else getEndConfig().set("running", false);

        try { endConfig.save(endFile);
        } catch(IOException e) { e.printStackTrace(); }
    }

    private void reloadEndConfig() {
        try { endConfig.load(endFile);
        } catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
    }

    private FileConfiguration getEndConfig() {
        return endConfig;
    }

    //SETTINGS
    private final long durationMillis = 1000 * 60 * 60 * 3;
    private final long durationMinutes = durationMillis / 1000 / 60;
    private final int price = 175000;

    private List<UUID> allowedPlayers = new ArrayList<>();
    private UUID holder = null;

    private long started = 0L;
    private boolean enabled = true;

    private final int MAX_PLAYERS = 3;

    public void panel(final Player player) {
        Gui.openGui(player, "Matkusta Endiin", 27, (gui) -> {

            int[] purpleGlass = new int[] { 11,12, 14,15  };

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
                List<String> temp = new ArrayList<>();
                for(UUID uuid : allowedPlayers) {
                    temp.add(Bukkit.getOfflinePlayer(uuid).getName());
                }
                if(temp.size() >= 1) {
                    lore.add(" §7Osallistujat: ");
                    lore.add("  §c" + StringUtils.join(temp, "§7,§c "));
                }
                if(allowedPlayers.contains(player.getUniqueId())) {
                    lore.add(" ");
                    lore.add(" §aKlikkaa teleportataksesi!");
                }

            } else {
                lore.add(" §7Tila: §a§lVAPAA");
                lore.add(" §7Hinta: §a§l" + Util.formatDecimals(price) + "€");
                lore.add(" §7Kesto: §a§l3h");
                lore.add(" ");
                if(Balance.canRemove(player.getUniqueId(), price)) lore.add(" §aKlikkaa aktivoidaksesi!");
                else lore.add(" §cSinulla ei ole varaa");

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

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Main.getTravelManager().gui(clicker);
                }
            });

            for(int slot : purpleGlass) { gui.addItem(1, ItemUtil.makeItem(Material.PURPLE_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }

        });
    }

    public void loadPreviousData() {
        if(getEndConfig().get("running") != null && getEndConfig().getBoolean("running")) {
            started = getEndConfig().getLong("started");
            holder = UUID.fromString(getEndConfig().getString("holder"));
            allowedPlayers = new ArrayList<>();
            for(String sUuid : getEndConfig().getStringList("allowed")) { allowedPlayers.add(UUID.fromString(sUuid)); }
        }

        if(getEndConfig().get("enabled") != null) enabled = getEndConfig().getBoolean("enabled");
        else enabled = true;


    }

    public void start(Player player) {

        if(!enabled) {
            Chat.sendMessage(player, "End ei ole käytettävissä tällä hetkellä... Odotathan, kunnes ylläpito pistää sen takaisin päälle!");
            return;
        }

        if(Balance.canRemove(player.getUniqueId(), price)) {
            allowedPlayers.clear();

            Balance.remove(player.getUniqueId(), price);

            started = System.currentTimeMillis();
            holder = player.getUniqueId();

            Sorsa.logColored("§6[EndManager] End was activated by " + player.getName() + " (" + player.getUniqueId() + ")!");

            // Create World

            Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
            Bukkit.broadcastMessage(" §5§lEND AKTIVOITU!");
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(" §7Aktivoija: §a" + player.getName());
            Bukkit.broadcastMessage(" §7Kesto: §a3h");
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(" §cGeneroimme End-maailmaa, joka");
            Bukkit.broadcastMessage(" §csaattaa synnyttää lagia.");
            Bukkit.broadcastMessage(" §cPahoittelemme häiriötä...");
            Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            Util.broadcastSound(Sound.ENTITY_ENDER_DRAGON_DEATH);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv create world_the_end end");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import world_the_end");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv gamerule announceAdvancements false world_the_end");

            Chat.sendMessage(player, "§7Aktivoit §5Endin§7! Hienoa työtä! Jos haluat jakaa §5Endin §7salaisuudet ystäviesi kanssa, pystyt päästämään " +
                    "heidät komennolla §a/ääri lisää <pelaaja>§7! Hienoja löytöretkiä ja onnea matkaan! Sinulla on §e3 tuntia §7aikaa!");

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

    private void invite(Player invitor, Player target) {

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

            Sorsa.logColored("§6[EndManager] Player " + invitor.getName() + " (" + invitor.getUniqueId() + ") invited the player " + target.getName() + " (" + target.getUniqueId() + ") to visit the End!");

        } else Chat.sendMessage(invitor, Chat.Prefix.ERROR, "End ei ole aktivoitu");

    }

    public void remove(Player invitor, Player target) {

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

        } else Chat.sendMessage(invitor, Chat.Prefix.ERROR, "End ei ole aktivoitu");

    }

    public boolean isInEnd(Player player) { return player.getWorld().getName().equalsIgnoreCase("world_the_end"); }

    public void teleport(Player player, boolean force) {

        if(!force && !enabled) {
            Chat.sendMessage(player, "End ei ole käytettävissä tällä hetkellä... Odotathan, kunnes ylläpito pistää sen takaisin päälle!");
            return;
        }

        if(!force && !allowedPlayers.contains(player.getUniqueId())) {
            Chat.sendMessage(player, "Et ole sallittujen pelaajien listalla §5Endiin§7!");
            return;
        }

        Chat.sendMessage(player, "Sinut viedään §5Endiin §c3s §7kuluttua...");
        Sorsa.logColored("§6[EndManager] The player " + player.getName() + " (" + player.getUniqueId() + ") teleported to the end!");
        Sorsa.after(3, () -> player.teleport(Bukkit.getWorld("world_the_end").getSpawnLocation()));

    }

    public void startManager() {
        Sorsa.everyAsync(5, () -> { if(!canContinue()) Sorsa.task(this::end); });
    }

    private boolean canContinue() {

        if(!enabled) return false;

        if(isOccupied()) {
            long shouldEnd = started + durationMillis;
            return System.currentTimeMillis() < shouldEnd;
        }
        return false;
    }

    private void returnFees() {
        if(isOccupied()) {
            // If end hadn't been up for more than 20min, give the money back
            if(System.currentTimeMillis() - started < 1000 * 60 * 20) {
                if(holder != null) {
                    Balance.add(holder, price);
                    Player h = Bukkit.getPlayer(holder);
                    if(h != null && h.isOnline()) {
                        if(!Settings.get(holder, "chat_mentions")) h.playSound(h.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        Chat.sendMessage(h, "Sinun aktivoima §5End §7oli päällä alle §a20min§7, ennen kuin se suljettiin, niin saat täyden summan takaisin itsellesi (§e" +  Util.formatDecimals(price) + "€§7)! ");
                    }
                }
            }
        }
    }

    public void end() {

        if(!isOccupied()) return;

        OfflinePlayer player = getHolderPlayer();

        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        Bukkit.broadcastMessage(" §5§lEND DEAKTIVOITU!");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §aEnd taas aktivoitavissa!");
        Bukkit.broadcastMessage(" §7Aktivoija: §c" + player.getName());
        Bukkit.broadcastMessage(" §7Kesto: §c3h");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §cPoistamme End-maailmaa, joka");
        Bukkit.broadcastMessage(" §csaattaa synnyttää lagia.");
        Bukkit.broadcastMessage(" §cPahoittelemme häiriötä...");
        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        Util.broadcastSound(Sound.ENTITY_BLAZE_DEATH);
        Sorsa.logColored("§6[EndManager] The end was deactivated!");

        for(UUID uuid : allowedPlayers) {
            if(Bukkit.getPlayer(uuid) != null) {
                Player o = Bukkit.getPlayer(uuid);
                Sorsa.teleportToSpawn(o);
            }
        }

        returnFees();

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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv remove world_the_end");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");

        // Delete world directory
        new BukkitRunnable() {
            @Override
            public void run() {
                try { FileUtils.deleteDirectory(worldFolder);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }.runTaskLater(Main.getInstance(), 20);

    }

    private OfflinePlayer getHolderPlayer() {
        if(holder == null) return null;
        return Bukkit.getOfflinePlayer(holder);
    }

    private long getTimeLeftMillis() {
        if(isOccupied()) {
            long shouldStop = started + durationMillis;
            return shouldStop - System.currentTimeMillis();
        }
        return 0L;
    }

    private String getTimeLeft() {
        if(getTimeLeftMillis() >= 1) {
            long millis = getTimeLeftMillis();
            long second = (millis / (1000)) % 60;
            long minute = (millis / (1000 * 60)) % 60;
            long hour = (millis / (1000 * 60 * 60)) % 24;
            return Util.formatTime((int) hour, (int) minute, (int) second, true);
        }
        return "§cEi käynnissä";
    }

    private boolean isOccupied() {
        return holder != null && started != 0L;
    }



}
