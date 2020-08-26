package me.tr.survival.main.managers.villages;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Profile;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VillageManager implements Listener, CommandExecutor {

    private boolean ENABLED = false;

    private final List<PlayerVillage> villages = new ArrayList<>();
    private final int[] taxationDates = { 1, 7, 14, 21, 28 };

    // File Management
    private File villageFile;
    private FileConfiguration villageConfig;

    private final List<UUID> searchVillageMode = new ArrayList<>();

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

    public void removeVillage(PlayerVillage village) {
        villages.remove(village);
        villageConfig.set(village.getUniqueId().toString(), "");
        saveVillageConfig();
    }

    public boolean hasJoinedVillage(UUID uuid) { return findVillageByPlayer(uuid) != null; }
    public boolean ownsVillage(UUID uuid) { return findVillageByLeader(uuid) != null; }

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

        if(config.contains("villages." + uuid)) {

            try {
                final String title = config.getString("villages." + uuid + ".title");
                final UUID leader = UUID.fromString(config.getString("villages." + uuid + ".leader"));
                final int taxRate = config.getInt("villages." + uuid + ".tax-rate");
                final int maxPlayers = config.getInt("villages." + uuid + ".max-players");
                final Location spawn = Util.textToLocation(config.getString("villages." + uuid + ".spawn"));
                final boolean closed = config.getBoolean("villages." + uuid + ".closed");
                final double balance = config.getDouble("villages." + uuid + ".balance");
                final double totalMoneyGathered = config.getDouble("villages." + uuid + ".total-money-gathered");

                final List<UUID> citizens = new ArrayList<>();
                for(String citizen : config.getStringList("villages." + uuid + ".citizens")) {
                    citizens.add(UUID.fromString(citizen));
                }

                final List<UUID> coLeaders = new ArrayList<>();
                for(String coLeader : config.getStringList("villages." + uuid + ".co-leaders")) {
                    coLeaders.add(UUID.fromString(coLeader));
                }

                final List<String> tags = config.getStringList("villages." + uuid + ".tags");

                return new PlayerVillage(UUID.fromString(uuid), title, leader, coLeaders, citizens, taxRate, spawn, maxPlayers, closed, tags, balance, totalMoneyGathered);

            } catch (Exception ex) {

                ex.printStackTrace();
                Sorsa.logColored("§c[PlayerVillages] Error occurred whilst loading village with id '" + uuid + "'. Error above!");
                return null;
            }
        }
        return null;
    }

    public PlayerVillage findVillage(UUID uuid) { return findVillage(uuid.toString()); }

    public PlayerVillage createVillage(UUID creatorUUID) {

        OfflinePlayer creator = Bukkit.getOfflinePlayer(creatorUUID);
        if(this.hasJoinedVillage(creator.getUniqueId())) return null;

        final List<String> tags = new ArrayList<>();
        final List<UUID> citizens = new ArrayList<>();

        citizens.add(creator.getUniqueId());

        final PlayerVillage village = new PlayerVillage(
                UUID.randomUUID(),
                creator.getName()+ ":n uusi kylä!",
                creator.getUniqueId(),
                new ArrayList<>(),
                citizens,
                0,
                Sorsa.getSpawn(),
                8,
                true,
                tags,
                0,
                0
        );

        Main.getVillageManager().addVillageToList(village);
        return village;
    }

    public void savePlayerVillages() {

        Sorsa.logColored("§a[PlayerVillages] Saving " + villages.size() + " villages...");

        final FileConfiguration config = this.getVillageConfig();
        ConfigurationSection villagesSection = config.getConfigurationSection("villages");
        if(villagesSection == null) villagesSection = config.createSection("villages");


        int amountSaved = 0;

        for(final PlayerVillage village : villages) {

            final UUID uuid = village.getUniqueId();

            config.set("villages." + uuid + ".id", uuid.toString());
            config.set("villages." + uuid + ".title", village.getTitle());
            config.set("villages." + uuid + ".leader", village.getUniqueId().toString());
            config.set("villages." + uuid + ".tax-rate", village.getTaxRate());
            config.set("villages." + uuid + ".spawn", Util.locationToText(village.getSpawn()));
            config.set("villages." + uuid + ".max-players", village.getMaxPlayers());
            config.set("villages." + uuid + ".closed", village.isClosed());
            config.set("villages." + uuid + ".balance", village.getBalance());
            config.set("villages." + uuid + ".total-money-gathered", village.getTotalMoneyGathered());

            config.set("villages." + uuid + ".co-leaders", Util.toStringList(village.getCoLeaders()));
            config.set("villages." + uuid + ".citizens", Util.toStringList(village.getCitizens()));

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

    public void addVillageToList(PlayerVillage village) {
        villages.add(village);
        savePlayerVillages();
    }

    // EVENTS

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent e) {

        final Player player = e.getPlayer();
        if(searchVillageMode.contains(player.getUniqueId())) {

            e.setCancelled(true);

            final String input = e.getMessage();
            if(input.equalsIgnoreCase("lopeta")) {
                searchVillageMode.remove(player.getUniqueId());
                Chat.sendMessage(player, "Lopetit kylähaun!");
                this.mainGui(player);
            } else this.searchForVillage(player, input);

        }

    }

    // COMMAND
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {

            final Player player = (Player) sender;
            final UUID uuid = player.getUniqueId();

            if(!ENABLED && !player.isOp()) {
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto on toistaiseksi poissa käytöstä!");
                return true;
            }

            if(args.length < 1) this.mainGui(player);
            else {
                if(!Ranks.isStaff(uuid)) this.searchForVillage(player, args);
                else {
                    if(args[0].equalsIgnoreCase("help")) {

                        Chat.sendMessage(player, "/kylä (enable | disable)");

                    } else if(args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {

                        if(player.isOp()) {
                            if(args[0].equalsIgnoreCase("enable")) {
                                ENABLED = true;
                                Chat.sendMessage(player, "Pelaajakylät ovat nyt päällä!");
                                return true;
                            } else {
                                ENABLED = false;
                                Chat.sendMessage(player, "Pelaajakylät ovat nyt poissa päältä!");
                                return true;
                            }
                        }

                    }
                    else searchForVillage(player, args);
                }
            }
        }
        return true;
    }

    public boolean isTaxationDay() {
        final Calendar calendar = Calendar.getInstance(new Locale("fi", "FI"));
        boolean isTaxationDate = false;
        for(int taxationDate : taxationDates) {
            if(taxationDate == calendar.get(Calendar.DAY_OF_MONTH)) isTaxationDate = true;
        }
        return isTaxationDate;
    }

    public void initTaxForVillages() {
        for(PlayerVillage village : villages) {
            village.taxPlayers();
        }
    }

    private boolean searchForVillage(final Player player, String[] args) {
        String searchQuery = "";
        for(int i = 0; i < args.length; i++) { searchQuery = searchQuery.concat(args[i] + " "); }
        OfflinePlayer searchedLeader = Bukkit.getOfflinePlayer(searchQuery);
        PlayerVillage foundVillage = this.findVillageByLeader(searchedLeader.getUniqueId());
        boolean found = false;
        // First, we try to check if the user wrote
        if(foundVillage == null) {
            for(final PlayerVillage village : villages) {
                if(village == null) continue;
                if(village.getTitle().toLowerCase().trim().equalsIgnoreCase(searchQuery.toLowerCase().trim())) {
                    openVillageView(player, village, true);
                    found = true;
                    break;
                }
            }
            if(!found) Chat.sendMessage(player, Chat.Prefix.ERROR, "Emme löytäneet yhtään kylää hakusanalla: §c" + searchQuery.toLowerCase() + "§7... Yritäthän pian uudestaan uudella hakutermillä!");

        } else openVillageView(player, foundVillage, true);

        return found;

    }

    public Calendar getNextTaxationDate() {

        final Calendar calendar = Calendar.getInstance(new Locale("fi", "FI"));
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        final int monthMaxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        if(dayOfMonth >= 28) {
            calendar.add(Calendar.DAY_OF_YEAR, (monthMaxDays - dayOfMonth) + 1);
            return calendar;
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            return calendar;
        }

    }

    private boolean searchForVillage(final Player player, String searchQuery) { return this.searchForVillage(player, searchQuery.split(" ")); }

    public boolean isVillageAvailable(String query) {
        boolean found = false;
        for(final PlayerVillage village : villages) {
            if(village == null) continue;
            if(village.getTitle().toLowerCase().trim().equalsIgnoreCase(query.toLowerCase().trim())) {
                found = true;
                break;
            }
        }
        return !found;
    }

    // OTHER

    public void openVillageView(final Player player, PlayerVillage village, boolean other) {

        if(!ENABLED && !player.isOp()) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto on toistaiseksi poissa käytöstä!");
            return;
        }

        if(!other) {
            openPersonalVillage(player, village);
            return;
        }

        int size = 27;
        final OfflinePlayer leader = Bukkit.getOfflinePlayer(village.getLeader());

        final Gui gui = new Gui("Pelaajakylä (" + village.getTitle() + ")", size);

        List<String> villageLore = new ArrayList<>();
        villageLore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        villageLore.add(" §7Pormestari: §a" + leader.getName());
        if(village.getCoLeaders().size() >= 1) {
            villageLore.add(" §7Varapormestarit: ");
            for(final UUID coUUID : village.getCoLeaders()) {
                final OfflinePlayer coLeader = Bukkit.getOfflinePlayer(coUUID);
                villageLore.add("§7  - §a§o" + coLeader.getName());
            }
        }
        villageLore.add(" ");
        villageLore.add(" §7Jäsenet: §a" + village.getCitizens().size() + "/" + village.getMaxPlayers());
        villageLore.add(" §7Rahatilanne: §e" + village.getBalance());
        villageLore.add(" §7Yhteensä kerätty raha: §e" + village.getTotalMoneyGathered());

        villageLore.add(" ");
        if(!village.isClosed() && !village.isFull() && !village.isMember(player.getUniqueId())) {
            villageLore.add(" ");
            villageLore.add(" §aKlikkaa liittyäksesi!");
        }
        villageLore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        gui.addItem(1, ItemUtil.makeItem(Material.IRON_DOOR, 1, "§2" + village.getTitle(), villageLore), 12);

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.MAP, 1, "§2Vieraile", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Pystyt käymään vierailemaan",
                " §7kylän tiluksilla klikkaamalla",
                " §7minua! Kylillä monesti saattaa",
                " §7olla §eKauppoja §7tai muuta jännää!",
                " ",
                " " + (village.isClosed() ? "§cKylä on suljettu vierailijoilta..." : "§aKlikkaa teleportataksesi!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                if(village.isClosed()) clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                else {

                    final Location spawn = village.getSpawn();
                    if(spawn == null) {
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Näyttäisi siltä, että kylälle ei olla asetettu vierailumahdollisuutta, eli kylän §eSpawni §7puuttuu! Kannattaa kysästä kylän omistajilta asiasta!");
                    } else {
                        gui.close(clicker);
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        village.teleport(clicker);
                    }
                }
            }
        });

        if(this.hasJoinedVillage(player.getUniqueId())) {
            gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.SPRUCE_DOOR, 1, "§2Sinun kyläsi", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa päästäksesi katsomaan",
                    " §7ja hallinnoimaan omaa kylääsi!",
                    " ",
                    " §aKlikkaa avataksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    mainGui(clicker);
                }
            });
        }

        final int[] glassSlots = { 11,13,15 };
        for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE, 1), slot); }

        for(int i = 0; i < size; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
        }

        gui.open(player);

    }

    private void openPersonalVillage(final Player player, final PlayerVillage village) {

        if(!ENABLED && !player.isOp()) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto on toistaiseksi poissa käytöstä!");
            return;
        }

        int size = 27;
        final OfflinePlayer leader = Bukkit.getOfflinePlayer(village.getLeader());

        final Gui gui = new Gui("Pelaajakylä", size);

        List<String> villageLore = new ArrayList<>();
        villageLore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        villageLore.add(" §7Nimi: §e" + village.getTitle());
        villageLore.add(" §7Pormestari: §a" + leader.getName());
        if(village.getCoLeaders().size() >= 1) {
            villageLore.add(" §7Varapormestarit: ");
            for(final UUID coUUID : village.getCoLeaders()) {
                final OfflinePlayer coLeader = Bukkit.getOfflinePlayer(coUUID);
                villageLore.add("§7  - §a§o" + coLeader.getName());
            }
        }
        villageLore.add(" ");
        villageLore.add(" §7Jäsenet: §a" + village.getCitizens().size() + "/" + village.getMaxPlayers());
        if(village.getTaxRate() > 0) {
            villageLore.add(" §7Seuraava verotuspäivä: §a" + Util.formatDate(this.getNextTaxationDate()));
        }
        villageLore.add(" §7Rahatilanne: §e" + village.getBalance());
        villageLore.add(" §7Yhteensä kerätty raha: §e" + village.getTotalMoneyGathered());
        villageLore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        gui.addItem(1, ItemUtil.makeItem(Material.IRON_DOOR, 1, "§2" + village.getTitle(), villageLore), 11);

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.MAP, 1, "§2Vieraile", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Teleporttaa sinun kylääsi",
                " §7klikkaamalla minua!",
                " ",
                " " + (village.getSpawn() == null ? "§cSpawnia ei ole asetettu..." : "§aKlikkaa teleportataksesi!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                if(village.getSpawn() == null) clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                else {
                    gui.close(clicker);
                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    village.teleport(player);
                }
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.COMPARATOR, 1, "§2Asetukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Muokkaa kylääsi ja sen",
                " §7asetuksia painamalla minua!",
                " ",
                " " + (village.canModify(player.getUniqueId()) ? "§aKlikkaa avataksesi!" : "§cEi oikeuksia!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                if(!village.canModify(clicker.getUniqueId())) clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                else {
                    gui.close(clicker);
                    openVillageSettings(clicker, village);
                }
            }
        });

        final int[] glassSlots = { 10,12,14,16 };
        for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE, 1), slot); }

        for(int i = 0; i < size; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
        }

        gui.open(player);

    }

    private void openVillageSettings(final Player player, final PlayerVillage village) {

        if(!ENABLED && !player.isOp()) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto on toistaiseksi poissa käytöstä!");
            return;
        }

        final UUID uuid = player.getUniqueId();

        int size = 27;
        final Gui gui = new Gui("Hallitse kylää", size);

        // 11,12,13,14,15

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAPER, 1, "§2Yksityisyys", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Muokkaa kyläsi yksityisyyttä.",
                " §7Jos kylä on yksityinen, muut pelaajat",
                " §7eivät voi liittyä tänne.",
                " ",
                " §7Nykyinen tila: " + (village.isClosed() ? "§cYksityinen" : "§aJulkinen"),
                " ",
                " §aKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                village.setClosed(!village.isClosed());
                openVillageSettings(player, village);
            }
        });

        final Location spawnLoc = village.getSpawn();

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.RED_BED, 1, "§2Kylän koti", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Aseta kyläsi koti, painamalla minua!",
                " §7Tämä on se sijainti, missä muut pelaajat",
                " §7ja kyläsi jäsenet voivat vierailla. Tämä",
                " §7on kyläsi sijainti.",
                " ",
                " §7Nykyinen sijainti: §e" + spawnLoc.getBlockX() + "§7,§e" + spawnLoc.getBlockY() + "§7,§e" + spawnLoc.getBlockZ(),
                " ",
                " §aKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openSpawnSetConfirmalMenu(clicker, village);
            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§2Verotusmäärä", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Voit määritellä, kuinka paljon",
                " §7haluat verottaa asukkailtasi.",
                " §7Otathan huomioon, että sinä olet myös",
                " §7itse verovelvollinen asukas!",
                "",
                " §7Saatavilla olevat verotusmäärät:",
                " §7- " + (village.getTaxRate() == 1250 ? "§a§l1250€" : "§e1250€"),
                " §7- " + (village.getTaxRate() == 600 ? "§a§l600€" : "§e600€"),
                " §7- " + (village.getTaxRate() == 250 ? "§a§l250€" : "§e250€"),
                " §7- " + (village.getTaxRate() == 0 ? "§a§l0€" : "§e0€"),
                " ",
                " §7Verotus tapahtuu kuun §e1.§7, §e7.§7,",
                " §e14.§7, §e21.§7 ja §e28. §7päivä!",
                " ",
                " §aKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                int taxRate = village.getTaxRate();
                if(taxRate == 1250) taxRate = 600;
                else if(taxRate == 600) taxRate = 250;
                else if(taxRate == 250) taxRate = 0;
                else if(taxRate == 0) taxRate = 1250;
                village.setTaxRate(taxRate);
                openVillageSettings(clicker, village);
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.IRON_INGOT, 1, "§2Luottamushenkilöt", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Voit asettaa luottamushenkilöitä",
                " §7kylääsi. Näillä henkilöillä on korkeat oikeudet",
                " §7muokkaamaan kylää. Luottamushenkilöt",
                " §7näkyvät kylän pääsivulla.",
                " ",
                " §7Huomioithan, että kylä on velvollinen",
                " §7maksamaan §e5% §7joka verotussummasta kaikille",
                " §7luottamushenkilöille!",
                " ",
                (village.ownsVillage(uuid) ? " §aKlikkaa muokkaaksesi!" : " §cEi oikeuksia!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                if(village.ownsVillage(uuid)) {
                    gui.close(clicker);
                    openChangeCoLeaderMenu(player, village);
                }
            }
        });


        if(village.ownsVillage(uuid)) {
            gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.BARRIER, 1, "§cPoista kylä", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §aKlikkaa minua§7, jos haluat poistaa",
                    " §7kyläsi!",
                    " ",
                    " §cPäätös on lopullinen!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    openVillageRemovalConifrmationMenu(clicker, village);
                }
            });
        }

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openPersonalVillage(player, village);
            }
        });


        final int[] glassSlots = { 10,16 };
        for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE, 1), slot); }

        for(int i = 0; i < size; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
        }

        gui.open(player);

    }


    public void mainGui(final Player player) {

        if(!ENABLED && !player.isOp()) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto on toistaiseksi poissa käytöstä!");
            return;
        }

        final UUID uuid = player.getUniqueId();

        if(this.hasJoinedVillage(uuid)) {
            final PlayerVillage village = this.findVillageByPlayer(uuid);
            this.openPersonalVillage(player, village);
        } else {

            final int size = 27;

            final Gui gui = new Gui("Pelaajakylät", size);
            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.PAPER, 1, "§2Et ole kylässä", Arrays.asList(
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
                    PlayerVillage village = createVillage(player.getUniqueId());
                    openVillageView(clicker, village, false);
                }
            });

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.MAP, 1, "§2Etsi kyliä", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Etkö halua luoda omaa kylääsi?",
                    " §7Oletko kiinnostunut tekemään",
                    " §7töitä ja tienaamaan muiden kanssa",
                    " §7yhdessä §erahaa§7? Painamalla",
                    " §7minua pystyt hakemaan kyliä",
                    " §7joihin voit liittyä!",
                    " ",
                    " §aKlikkaa hakeaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Chat.sendMessage(player, "§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");
                    Chat.sendMessage(player, " ");
                    Chat.sendCenteredMessage(player, " §a§lEtsi pelaajakylä!");
                    Chat.sendMessage(player, " ");
                    Chat.sendCenteredMessage(player, " §7Kirjoita chattiin hakutermi, jolla haluat etsiä kyliä!");
                    Chat.sendCenteredMessage(player, " §7Jos haluat lopettaa tämän toiminnon, kirjoita chattiin §alopeta§7!");
                    player.sendMessage(" ");
                    Chat.sendMessage(player, "§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");
                    searchVillageMode.add(clicker.getUniqueId());
                }
            });

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Profile.openProfile(clicker, clicker.getUniqueId());
                }
            });


            final int[] glassSlots = { 11, 13, 15 };
            for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.RED_STAINED_GLASS_PANE, 1), slot); }

            for(int i = 0; i < size; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
            }

            gui.open(player);

        }

    }

    private void openChangeCoLeaderMenu(Player player, final PlayerVillage village) {

        int size = 54;
        final Gui gui = new Gui("Lisää luottohenkilöitä", size);

        int slotToAddHead = 9;
        List<UUID> members = village.getCitizens();
        for(final UUID memberUUID : members) {

            if(slotToAddHead >= 45) break;

            if(village.getLeader().equals(memberUUID)) continue;

            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUUID);
            if(village.canModify(memberUUID)) {
                gui.addButton(new Button(1, slotToAddHead, ItemUtil.makeSkullItem(member, 1, "§a" + member.getName(), Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Klikkaa minua lisätäksesi",
                        " §7henkilön §e" + member.getName(),
                        " §7luottamushenkilöksi!",
                        " ",
                        " §aKlikkaa lisätäksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        village.addCoLeader(memberUUID);
                        openChangeCoLeaderMenu(player, village);
                    }
                });
            } else {
                gui.addButton(new Button(1, slotToAddHead, ItemUtil.makeSkullItem(member, 1, "§c" + member.getName(), Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Klikkaa minua poistaaksesi",
                        " §7henkilön §e" + member.getName(),
                        " §7luottamusasemasta!",
                        " ",
                        " §cKlikkaa poistaaksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        village.removeCoLeader(memberUUID);
                        openChangeCoLeaderMenu(player, village);
                    }
                });
            }

            slotToAddHead += 1;

        }

        gui.addButton(new Button(1, 49, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openVillageView(player, village, false);
            }
        });

        for(int i = 0; i < 9; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
        }

        for(int i = 45; i < 54; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
        }

        gui.open(player);

    }

    private void openVillageRemovalConifrmationMenu(Player player, final PlayerVillage village) {

        final Gui gui = new Gui("Kylän poiston varmistus", 27);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa vahvistaaksei kyläsi",
                " §cpoiston§7! Muista, että tämä",
                " §7toiminto on §c§llopullinen§7!",
                " §7eikä sitä voida peruuttaa!",
                " ",
                " §aKlikkaa poistaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                removeVillage(village);
                Chat.sendMessage(player, "Poistit kyläsi... Harmillista... ");
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa peruuttaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openVillageSettings(player, village);
            }
        });

        gui.open(player);

    }

    private void openSpawnSetConfirmalMenu(Player player, PlayerVillage village) {
        final Gui gui = new Gui("Kylän kodin vahvistus", 27);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa vahvistaaksesi",
                " §7kyläsi uuden kodin sijainnin",
                " §7asettamisesi! Muista, että tätä",
                " §7toimintoa ei voi peruuttaa!",
                " ",
                " §aKlikkaa asettaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                village.setSpawn(clicker.getLocation());
                openVillageSettings(player, village);
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa peruuttaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openVillageSettings(player, village);
            }
        });

        gui.open(player);
    }


}
