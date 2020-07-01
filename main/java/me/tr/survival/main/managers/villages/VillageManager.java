package me.tr.survival.main.managers.villages;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VillageManager implements Listener, CommandExecutor {

    private final List<PlayerVillage> villages = new ArrayList<>();

    // File Management
    private File villageFile;
    private FileConfiguration villageConfig;

    public synchronized void loadVillagesFromFile() {

        villages.clear();
        final FileConfiguration config = this.getVillageConfig();
        if(config == null) return;

        final ConfigurationSection villagesSection = config.getConfigurationSection("villages");
        if(villagesSection == null) return;

        Sorsa.logColored("§a[PlayerVillages] Loading PlayerVillages...");

        int loaded = 0;

        for(final String rawUuid : villagesSection.getKeys(false)) {
            final PlayerVillage village = findVillage(rawUuid);
            if(village != null) {
                villages.add(village);
                loaded += 1;
            }
        }
        Sorsa.logColored("§a[PlayerVillages] Loaded " + loaded + " PlayerVillages!");
    }


    public PlayerVillage findVillageByLeader(UUID uuid) {
        for(PlayerVillage village : villages) {
            if(village.getLeader().equals(uuid)) return village;
        }

        // Return null, if no villages were found
        return null;
    }

    public boolean hasJoinedVillage(UUID uuid) { return findVillageByPlayer(uuid) != null; }
    public boolean ownsVillage(UUID uuid) { return findVillageByPlayer(uuid) != null; }

    /**
     *
     * Searches all villages and tries to find any village
     * which has the given player (by UUID)
     *
     * @param uuid The UUID of the player
     * @return The village the given player (by UUID) is in
     */
    public PlayerVillage findVillageByPlayer(UUID uuid) {

        for(PlayerVillage village : villages) {

            // We can check for the leader and co-leaders first,
            // because it may make the process faster!
            if(village.getLeader().equals(uuid)) return village;

            for(UUID coLeader : village.getCoLeaders()) {
                if(coLeader.equals(uuid)) return village;
            }


            // If the given player wasn't a co-leader nor the leader
            // he/she must be a citizen. So let's loop them
            for(UUID citizen : village.getCitizens()) {
                if(citizen.equals(uuid)) return village;
            }

        }

        // Return null, if no villages were found
        return null;
    }

    public PlayerVillage findVillage(final String uuid) {

        final FileConfiguration config = this.getVillageConfig();
        final ConfigurationSection villagesSection = config.getConfigurationSection("villages");

        if(villagesSection.get(uuid) != null) {

            try {
                final String title = villagesSection.getString(uuid + ".title");
                final UUID leader = UUID.fromString(villagesSection.getString(uuid + ".leader"));
                final int taxRate = villagesSection.getInt(uuid + ".tax-rate");
                final int maxPlayers = villagesSection.getInt(uuid + ".max-players");
                final Location spawn = Util.textToLocation(villagesSection.getString(uuid + ".spawn"));

                final List<UUID> citizens = new ArrayList<>();
                for(String citizen : villagesSection.getStringList(uuid + ".citizens")) {
                    citizens.add(UUID.fromString(citizen));
                }

                final List<UUID> coLeaders = new ArrayList<>();
                for(String coLeader : villagesSection.getStringList(uuid + ".co-leaders")) {
                    coLeaders.add(UUID.fromString(coLeader));
                }

                final PlayerVillage village = new PlayerVillage
                        (UUID.fromString(uuid), title, leader, coLeaders, citizens, taxRate, spawn, maxPlayers);

                return village;

            } catch (Exception ex) {

                ex.printStackTrace();
                Sorsa.logColored("§c[PlayerVillages] Error occurred whilst loading village with id '" + uuid + "'. Error above!");
                return null;
            }
        }
        return null;
    }

    public PlayerVillage findVillage(UUID uuid) { return findVillage(uuid.toString()); }

    public synchronized void savePlayerVillages() {

        Sorsa.logColored("§a[PlayerVillages] Saving " + villages.size() + " villages...");

        final FileConfiguration config = this.getVillageConfig();
        final ConfigurationSection villagesSection = config.getConfigurationSection("villages");

        int amountSaved = 0;

        for(final PlayerVillage village : villages) {

            final UUID uuid = village.getUniqueId();

            villagesSection.set(uuid + ".id", uuid.toString());
            villagesSection.set(uuid + ".title", village.getTitle());
            villagesSection.set(uuid + ".leader", village.getUniqueId().toString());
            villagesSection.set(uuid + ".tax-rate", village.getTaxRate());
            villagesSection.set(uuid + ".spawn", Util.locationToText(village.getSpawn()));
            villagesSection.set(uuid + ".max-players", village.getMaxPlayers());

            villagesSection.set(uuid + ".co-leaders", Util.toStringList(village.getCoLeaders()));
            villagesSection.set(uuid + ".co-leaders", Util.toStringList(village.getCitizens()));

            amountSaved += 1;

        }

        Sorsa.logColored("§a[PlayerVillages] Saved " + amountSaved + "/" + villages.size() + " villages!" +
                " If errors occurred, they'll be listed above!");

        saveVillageConfig();

    }

    public void createVillageConfig() {
        villageFile = new File(Main.getInstance().getDataFolder(), "villages.yml");
        if (!villageFile.exists()) {
            villageFile.getParentFile().mkdirs();
            Main.getInstance().saveResource("villages.yml", false);
        }
        villageConfig= new YamlConfiguration();
        reloadVillageConfig();
    }


    private void saveVillageConfig() {
        try { villageConfig.save(villageFile);
        } catch(IOException e) { e.printStackTrace(); }
    }

    private void reloadVillageConfig() {
        try { villageConfig.load(villageFile);
        } catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
    }

    private FileConfiguration getVillageConfig() {
        return villageConfig;
    }

    // EVENTS



    // COMMAND
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            final Player player = (Player) sender;
            final UUID uuid = player.getUniqueId();

            if(args.length < 1) {
                this.mainGui(player);
            } else {



            }
        }
        return true;
    }

    // OTHER

    private void mainGui(final Player player) {

        int size = 27;
        final Gui gui = new Gui("Pelaajakylät", size);
        final UUID uuid = player.getUniqueId();

        if(this.hasJoinedVillage(uuid)) {

            size = 36;

        } else {
            size = 27;
            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§2Et ole kylässä", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Et ole tällä hetkellä liittynyt",
                    " §7mihinkään kylään! Voit luoda uuden",
                    " §ePelaajakylän §7painamalla minua!",
                    " ",
                    " §aKlikkaa luodaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                }
            });
        }

        for(int i = 0; i < size; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
        }

        gui.setSize(size);
        gui.open(player);

    }

}
