package me.tr.survival.main.managers.villages;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Profile;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.callback.SpigotCallback;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
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

    private boolean ENABLED = true;

    public final int VILLAGE_PURCHASE_PRICE = 35000;

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
        villageConfig.set("villages." + village.getUniqueId().toString(), "");
        saveVillageConfig();
        loadVillagesFromFile();
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

                final List<UUID> invited = new ArrayList<>();
                for(String invitedPlayer : config.getStringList("villages." + uuid + ".invited")) {
                    invited.add(UUID.fromString(invitedPlayer));
                }

                final List<UUID> requested = new ArrayList<>();
                for(String playerWhoRequested : config.getStringList("villages." + uuid + ".requested")) {
                    requested.add(UUID.fromString(playerWhoRequested));
                }

                long created = config.getLong("villages." + uuid + ".created");

                final List<String> tags = config.getStringList("villages." + uuid + ".tags");

                return new PlayerVillage(UUID.fromString(uuid), title, leader, coLeaders, citizens, taxRate, spawn,
                        maxPlayers, closed, tags, balance, totalMoneyGathered, invited, requested, created);

            } catch (Exception ex) {

                ex.printStackTrace();
                Sorsa.logColored("§c[PlayerVillages] Error occurred whilst loading village with id '" + uuid + "'. Error above!");
                return null;
            }
        }
        return null;
    }

    public PlayerVillage findVillage(UUID uuid) { return findVillage(uuid.toString()); }

    public void openVillageCreationConfirmationMenu(Player player) {

        final Gui gui = new Gui("Kylän luonnin varmistus", 27);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa kyläsi luominen!",
                " §7Tämä maksaa §e" + Util.formatDecimals(VILLAGE_PURCHASE_PRICE) + "€§7!",
                " ",
                " §aKlikkaa asettaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                PlayerVillage village = createVillage(clicker.getUniqueId());
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                openPersonalVillage(clicker, village);
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
                mainGui(clicker);
            }
        });

        gui.open(player);
    }

    public PlayerVillage createVillage(UUID creatorUUID) {

        OfflinePlayer creator = Bukkit.getOfflinePlayer(creatorUUID);
        Balance.remove(creatorUUID, VILLAGE_PURCHASE_PRICE);
        if(this.hasJoinedVillage(creator.getUniqueId())) return null;

        final List<String> tags = new ArrayList<>();
        final List<UUID> citizens = new ArrayList<>();

        citizens.add(creator.getUniqueId());

        final PlayerVillage village = new PlayerVillage(
                UUID.randomUUID(),
                creator.getName() + ":n uusi kylä!",
                creator.getUniqueId(),
                new ArrayList<>(),
                citizens,
                0,
                Sorsa.getSpawn(),
                8,
                true,
                tags,
                0,
                0,
                new ArrayList<>(),
                new ArrayList<>(),
                System.currentTimeMillis()
        );

        Main.getVillageManager().addVillageToList(village);

        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §a§lUUSI KYLÄ!");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" §7Pelaaja §e" + creator.getName() + " §7loi uuden");
        Bukkit.broadcastMessage(" §7pelaajakylän! ");
        Bukkit.broadcastMessage(" ");

        for(Player player : Bukkit.getOnlinePlayers()) {
            TextComponent openMsg = new TextComponent(TextComponent.fromLegacyText(" §a§lNäytä kylä!"));
            openMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klikkaa avataksesi kylän tiedot!").create()));
            SpigotCallback.createCommand(
                    openMsg, opener -> Main.getVillageManager().openVillageView(opener, village, true)
            );
            player.spigot().sendMessage(openMsg);
        }

        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        Util.broadcastSound(Sound.ENTITY_ENDER_DRAGON_GROWL);
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
            config.set("villages." + uuid + ".leader", village.getLeader().toString());
            config.set("villages." + uuid + ".tax-rate", village.getTaxRate());
            config.set("villages." + uuid + ".spawn", Util.locationToText(village.getSpawn()));
            config.set("villages." + uuid + ".max-players", village.getMaxPlayers());
            config.set("villages." + uuid + ".closed", village.isClosed());
            config.set("villages." + uuid + ".balance", village.getBalance());
            config.set("villages." + uuid + ".total-money-gathered", village.getTotalMoneyGathered());

            config.set("villages." + uuid + ".co-leaders", Util.toStringList(village.getCoLeaders()));
            config.set("villages." + uuid + ".citizens", Util.toStringList(village.getCitizens()));
            config.set("villages." + uuid + ".invited", Util.toStringList(village.getInvited()));
            config.set("villages." + uuid + ".requested", Util.toStringList(village.getRequested()));

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
                    if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("apua")) {

                        if(player.isOp()) {
                            Chat.sendMessage(player, "/kylä (enable | disable)");
                        } else {
                            player.performCommand("/apua kylä");
                        }

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

                    } else if(args[0].equalsIgnoreCase("kutsu") || args[0].equalsIgnoreCase("invite")) {

                        if(args.length >= 2) {

                            if(ownsVillage(uuid)) {

                                PlayerVillage village = findVillageByLeader(uuid);
                                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                                village.invite(target.getUniqueId());
                                Chat.sendMessage(player, "Kutsuit pelaajan §a" + target.getName() + " §7liittymään kylääsi!");

                            } else {
                                Chat.sendMessage(player, "Et omista kylää!");
                            }

                        }

                    } else if(args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("kaikki")) {
                        openListAllVillagesMenu(player);
                    } else if(args[0].equalsIgnoreCase("top")) {
                        openTopBalanceMenu(player);
                    } else if(args[0].equalsIgnoreCase("verota")) {

                       if(player.isOp()) {
                           if(ownsVillage(uuid)) {
                               PlayerVillage village = findVillageByLeader(uuid);
                               if(village != null) {
                                   village.taxPlayers();
                               }
                           }
                       }

                    } else if(args[0].equalsIgnoreCase("nosta")) {

                       if(hasJoinedVillage(uuid)) {

                           PlayerVillage village = findVillageByPlayer(uuid);
                           if(village.canModify(uuid)) {
                               double amount;
                               try { amount = Double.parseDouble(args[1]);
                               } catch(NumberFormatException ex) {
                                   Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytäthän numeroita!");
                                   return true;
                               }

                               if(amount < 1) {
                                   Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei negatiivisia numeroita, tai nolla!");
                                   return true;
                               }

                               if(village.getBalance() >= amount) {

                                   this.openVillageWithdrawalConfirmation(player, village, amount);

                               } else {
                                   Chat.sendMessage(player, "Kylällä ei ole tarpeeksi varoja tähän! Suosittelemme, että kylän kaikkia rahoja ei nostettaisi.");
                               }

                           } else {
                               Chat.sendMessage(player, "Hei! Sinulla ei ole oikeuksia tehdä tätä sinun kylässäsi! Ole yhteydessä kyläsi luottamushenkilöön tai pormestariin!");
                           }

                       } else {
                           Chat.sendMessage(player, "Höpsö! Et ole kylässä!");
                       }

                    } else if(args[0].equalsIgnoreCase("poistu")) {
                        this.leaveCurrentVillage(player);
                    } else if(args[0].equalsIgnoreCase("nimi")) {

                        if(args.length >= 2) {

                            if(this.hasJoinedVillage(uuid)) {

                                PlayerVillage village = findVillageByPlayer(uuid);
                                if(village != null) {

                                    if(village.canModify(uuid)) {

                                        StringBuilder sb = new StringBuilder();
                                        for(int i = 1; i < args.length; i++) {
                                            sb.append(args[i] + " ");
                                        }

                                        String newTitle = sb.toString().trim();

                                        String[] blacklistedWords = {
                                                "paska",
                                                "neekeri",
                                                "huora",
                                                "vittu",
                                                "saatana",
                                                "top",
                                                "enable",
                                                "poistu",

                                        };

                                        boolean cannotCreate = false;

                                        for(String blacklistedWord : blacklistedWords) {
                                            if(newTitle.toLowerCase().contains(blacklistedWord)) {
                                                cannotCreate = true;
                                            }
                                        }

                                        if(cannotCreate) {
                                            Chat.sendMessage(player, "Tuohan on nyt varsin tuhma nimi... Kokeile jotain toista");
                                        } else {
                                            openChangeNameConfirmation(player, village, newTitle);
                                        }

                                    } else Chat.sendMessage(player, "Ei oikeuksia! Sinun tulee olla luottamushenkilö, niin voit muokata kylän nimeä!");

                                } else Chat.sendMessage(player, "Jotain meni vikaan.. Yritäppä uudelleen!");

                            } else Chat.sendMessage(player, "Et ole kylässä....");

                        } else Chat.sendMessage(player, "Käytä: §a/kylä nimi <uusi nimi>");

                    }
                    else searchForVillage(player, args);
                }
            }
        }
        return true;
    }

    private void openVillageWithdrawalConfirmation(Player player, PlayerVillage village, double amount) {

        final Gui gui = new Gui("Kylän rahanoston varmistus", 27);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Olet nostamassa kyläsi pankki-",
                " §7tililtä §e" + Util.formatDecimals(amount) + "€§7.",
                " §7Oletko varma tästä?",
                " ",
                " §7Kylän tilille jää: §e" + Util.formatDecimals((village.getBalance() - amount)) + "€",
                " ",
                " §aSuosittelemme§7, että kaikkia kylän rahoja",
                " §7ei nostettaisi ja tilille jätettäisiin",
                " §7hieman rahaa, esim. §averoja §7varten.",
                " ",
                " §aKlikkaa nostaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);

                village.removeBalance(amount);
                Balance.add(player.getUniqueId(), amount);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                Chat.sendMessage(player, "§a§lOnnittelut! §7Nostit juuri §e" + Util.formatDecimals(amount) + "€ §7kyläsi pankkitililtä!");

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
            }
        });

        gui.open(player);
    }

    public void leaveCurrentVillage(Player player) {
        if(hasJoinedVillage(player.getUniqueId())) {

            PlayerVillage village = findVillageByPlayer(player.getUniqueId());
            if(village != null) {
                if(village.isLeader(player.getUniqueId())) {
                    openVillageRemovalConifrmationMenu(player, village);
                } else {
                    village.removePlayer(player.getUniqueId());
                    Chat.sendMessage(player, "Poistuit kylästäsi... Jos teit tämän vahingossa, ole vain yhteydessä kylän §epormestariin§7!");
                }
            } else {
                Chat.sendMessage(player, "Kävi virhe tuota tehdessä... Yritäppä uudelleen...?");
            }

        } else {
            Chat.sendMessage(player, "Et ole kylässä.. Miten kuvittelet poistuvasi sieltä?");
        }
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

    private void searchForVillage(final Player player, String[] args) {
        Sorsa.async(() -> {

            // First, search by Player
            OfflinePlayer searchedPlayer = Bukkit.getOfflinePlayer(args[0]);

            PlayerVillage villageSearchedByPlayer = findVillageByPlayer(searchedPlayer.getUniqueId());
            if(villageSearchedByPlayer == null) {

                // Join the arguments in to a string
                final String query = String.join(" ", args).toLowerCase().trim();

                for(PlayerVillage village : villages) {
                    if(village == null) continue;

                    // Change to lowercase and remove whitespace
                    String title = village.getTitle().toLowerCase().trim();

                    // First check if the query and title are equal
                    if(title.equalsIgnoreCase(query)) {
                        searchVillageMode.remove(player.getUniqueId()); // If village found and player was using search mode, deactivate it
                        Sorsa.task(() -> openVillageView(player, village, true));
                        break;
                    } else {

                        // If not, check if the title contains the query...
                        if(title.contains(query)) {
                            searchVillageMode.remove(player.getUniqueId()); // If village found and player was using search mode, deactivate it
                            Sorsa.task(() -> openVillageView(player, village, true));
                            break;
                        } else {
                            // Let's give up, no villages were found
                            Sorsa.task(() -> Chat.sendMessage(player, "Kyliä ei löydetty hakutermillä §c" + query +
                                    "§7... Mikäli et halua enää etsiä kyliä, kirjoita chattiin §alopeta§7!"));
                        }
                    }
                }
            } else {
                // Open the village view
                searchVillageMode.remove(player.getUniqueId()); // If village found and player was using search mode, deactivate it
                Sorsa.task(() -> openVillageView(player, villageSearchedByPlayer, true));
            }

        });
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

    private void searchForVillage(final Player player, String searchQuery) { this.searchForVillage(player, searchQuery.split(" ")); }

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

    private void openChangeNameConfirmation(Player player, PlayerVillage village, String title) {

        final Gui gui = new Gui("Nimen vaihdon varmistus", 27);

        int price = (village.hasDefaultName()) ? 0 : 500;

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Varmista kyläsi nimen vaihdos",
                " §7nimeen: §b" + title + "§7!",
                " ",
                " §7Hinta vaihdokseen on: §e" + Util.formatDecimals(price) +  "€§7!",
                " §7Hinta veloitetaan kylän maksutililtä!",
                " ",
                " §aKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                if(!village.hasDefaultName()) {
                    if(village.getBalance() >= price) {
                        village.setTitle(title);
                        village.removeBalance(price);
                        openPersonalVillage(player, village);
                    } else {
                        Chat.sendMessage(clicker, "Kylälläsi ei ole rahaa nimen vaihtamiseen...");
                    }
                } else {
                    village.setTitle(title);
                    openPersonalVillage(player, village);
                }
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
            }
        });

        gui.open(player);

    }

    public void openVillageView(final Player player, PlayerVillage village, boolean other) {

        if(!ENABLED && !player.isOp()) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto on toistaiseksi poissa käytöstä!");
            return;
        }

        searchVillageMode.remove(player.getUniqueId());

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
            villageLore.add(" §7Luottamushenkilöt: ");
            for(final UUID coUUID : village.getCoLeaders()) {
                final OfflinePlayer coLeader = Bukkit.getOfflinePlayer(coUUID);
                villageLore.add("§7  - §a§o" + coLeader.getName());
            }
        }
        villageLore.add(" ");

        villageLore.add(" §7Luotu: §a" + village.getCreationDate());
        villageLore.add(" §7Seuraava verotuspäivä: §a" + Util.formatDate(this.getNextTaxationDate()));

        villageLore.add(" ");
        villageLore.add(" §7Jäsenet: §a" + village.getCitizens().size() + "/" + village.getMaxPlayers());
        villageLore.add(" §7Rahatilanne: §e" + Util.formatDecimals(village.getBalance()) + " €");
        villageLore.add(" §7Yhteensä kerätty raha: §e" + Util.formatDecimals(village.getTotalMoneyGathered()) + " €");

        villageLore.add(" ");
        if(village.isInvited(player.getUniqueId()) && !village.isMember(player.getUniqueId())) {
            villageLore.add(" ");
            villageLore.add(" §aLiity kylään!");
        } else {
            if(!village.isClosed() && !village.isFull() && !village.isMember(player.getUniqueId())) {
                villageLore.add(" ");
                villageLore.add(" §aPyydä liittymistä!");
            } else if(village.isClosed() && !village.isFull() && !village.isMember(player.getUniqueId())) {
                villageLore.add(" ");
                villageLore.add(" §aPyydä liittymistä!");
            }
        }
        villageLore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.IRON_DOOR, 1, "§2" + village.getTitle(), villageLore)) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                if(village.isInvited(player.getUniqueId()) && !village.isMember(player.getUniqueId())) {
                    gui.close(clicker);
                    village.join(player);
                } else {
                    if(!village.isClosed() && !village.isFull() && !village.isMember(player.getUniqueId())) {
                        gui.close(clicker);
                        village.requestToJoin(clicker);
                    } else if(village.isClosed() && !village.isFull() && !village.isMember(player.getUniqueId())) {
                        gui.close(clicker);
                        village.requestToJoin(clicker);
                    }
                }
            }
        });

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

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eRikkaimmat kylät", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Katso miten kylä sijoittuu",
                " §erikkaimpien kylien §7joukossa!",
                " ",
                " §aKlikkaa minua!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Main.getVillageManager().openTopBalanceMenu(clicker);
            }
        });

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.PAPER, 1, "§2Kaikki kylät", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Näytä palvelimen kaikki",
                " §7pelaajakylät!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openListAllVillagesMenu(clicker);
            }
        });

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
        if(!village.getCoLeaders().isEmpty()) {
            villageLore.add(" §7Luottamushenkilöt: ");
            for(final UUID coUUID : village.getCoLeaders()) {
                final OfflinePlayer coLeader = Bukkit.getOfflinePlayer(coUUID);
                villageLore.add("§7  - §a§o" + coLeader.getName());
            }
        }
        villageLore.add(" ");
        villageLore.add(" §7Luotu: §a" + village.getCreationDate());
        villageLore.add(" ");
        villageLore.add(" §7Jäsenet: §a" + village.getCitizens().size() + "/" + village.getMaxPlayers());
        if(village.getTaxRate() > 0) {
            villageLore.add(" §7Seuraava verotuspäivä: §a" + Util.formatDate(this.getNextTaxationDate()));
        }
        villageLore.add(" §7Rahatilanne: §e" + Util.formatDecimals(village.getBalance()) + " €");
        villageLore.add(" §7Yhteensä kerätty raha: §e" + Util.formatDecimals(village.getTotalMoneyGathered()) + " €");
        villageLore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        gui.addItem(1, ItemUtil.makeItem(Material.IRON_DOOR, 1, "§2" + village.getTitle(), villageLore), 11);


        final Location spawnLoc = village.getSpawn();
        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.MAP, 1, "§2Vieraile", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Teleporttaa sinun kylääsi",
                " §7klikkaamalla minua!",
                " ",
                " §7Nykyinen sijainti: §e" + spawnLoc.getBlockX() + "§7, §e" + spawnLoc.getBlockY() + "§7, §e" + spawnLoc.getBlockZ(),
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
                if(!village.canModify(clicker.getUniqueId(), true)) clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                else {
                    gui.close(clicker);
                    openVillageSettings(clicker, village);
                }
            }
        });

        if(village.isLeader(player.getUniqueId())) {
            gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.PAPER, 1, "§aLiittymispyynnöt", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Hallinnoi kylän liittymis-",
                    " §7pyyntöjä klikkaamalla §aminua§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    if(village.isLeader(clicker.getUniqueId())) {
                        gui.close(clicker);
                        openRequestedPlayersMenu(clicker, village);
                    }
                }
            });
        }

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eRikkaimmat kylät", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Katso miten kylä sijoittuu",
                " §erikkaimpien kylien §7joukossa!",
                " ",
                " §aKlikkaa minua!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Main.getVillageManager().openTopBalanceMenu(clicker);
            }
        });

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.PRISMARINE_SHARD, 1, "§cPoistu kylästä", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa minua §cpoistuaksesi",
                " §7tästä kylästä! Tätä ei voi perua!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                leaveCurrentVillage(clicker);
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

        if(!village.canModify(uuid, true)) return;

        // If isn't co leader, but is staff
        if(!village.getCoLeaders().contains(player.getUniqueId()) && Ranks.isStaff(player.getUniqueId())) {
            if(!Main.getStaffManager().hasStaffMode(player)) {
                Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                return;
            }
        }

        int size = 36;
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
                " §7Nykyinen sijainti: §e" + spawnLoc.getBlockX() + "§7, §e" + spawnLoc.getBlockY() + "§7, §e" + spawnLoc.getBlockZ(),
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
                " §7- " + (village.getTaxRate() == 8000 ? "§a§l8 000,00€" : "§e8 000,00€"),
                " §7- " + (village.getTaxRate() == 3500 ? "§a§l3 500,00€" : "§e3 500,00€"),
                " §7- " + (village.getTaxRate() == 1250 ? "§a§l1 250,00€" : "§e1 250,00€"),
                " §7- " + (village.getTaxRate() == 500 ? "§a§l500,00€" : "§e500,00€"),
                " §7- " + (village.getTaxRate() == 100 ? "§a§l100€,00" : "§e100,00€"),
                " §7- " + (village.getTaxRate() == 0 ? "§a§l0,00€ §7§o(Ei verotusta)" : "§e0,00€ §7§o(Ei verotusta)"),
                " ",
                " §7Verotus tapahtuu §aviikon §7välein!",
                " ",
                " §aKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                int taxRate = village.getTaxRate();
                if(taxRate == 8000) taxRate = 3500;
                else if(taxRate == 3500) taxRate = 1250;
                else if(taxRate == 1250) taxRate = 500;
                else if(taxRate == 500) taxRate = 100;
                else if(taxRate == 100) taxRate = 0;
                else if(taxRate == 0) taxRate = 8000;
                village.setTaxRate(taxRate);
                openVillageSettings(clicker, village);
            }
        });

        String addCoLeaderText = (village.ownsVillage(uuid) ? " §aKlikkaa muokkaaksesi!" : " §cEi oikeuksia!");

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.IRON_INGOT, 1, "§2Luottamushenkilöt", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Voit asettaa luottamushenkilöitä",
                " §7kylääsi. Näillä henkilöillä on korkeat oikeudet",
                " §7muokkaamaan kylää. Luottamushenkilöt",
                " §7näkyvät kylän pääsivulla.",
                " ",
                " §7Huomioithan, että kylä on velvollinen",
                " §7maksamaan §e5% §7joka verotussummasta",
                " §7kaikille luottamushenkilöille!",
                " ",
                " §7Tämän hetkinen luottamushenkilön",
                " §7palkka: §e" + Util.formatDecimals((village.getTaxRate() * 0.05)) + " €",
                " ",
                " " + addCoLeaderText,
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

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.COBWEB, 1, "§2Potki pelaaja", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Voit potkia huonosti käyttäytyvät",
                " §7pelaajat pois tästä kylästä!",
                " §7Muista, että luottamushenkilöitä",
                " §cei voi poistaa §7kylästä!",
                " ",
                (village.ownsVillage(uuid) ? " §aKlikkaa potkiaksesi!" : " §cEi oikeuksia!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                if(village.ownsVillage(uuid)) {
                    gui.close(clicker);
                    openVillagePlayerKickMenu(player, village);
                }
            }
        });

        gui.addButton(new Button(1, 20, ItemUtil.makeItem(Material.PLAYER_HEAD, 1, "§2Kutsu pelaaja", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Kutsu pelaaja komennolla",
                " §a/kylä kutsu <pelaaja>",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) { }
        });

        gui.addButton(new Button(1, 21, ItemUtil.makeItem(Material.OAK_SIGN, 1, "§2Vaihda nimi", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Vaihda kylän nimi komennolla",
                " §a/kylä nimi <uusi nimi>§7!",
                " ",
                " §7Muista! Nimen vaihto maksaa",
                " §e500€§7! Vain ensimmäinen §7kerta",
                " §7on §ailmainen§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) { }
        });

        gui.addButton(new Button(1, 22, ItemUtil.makeItem(Material.BOOK, 1, "§2Pelaajamäärä", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Voit määrittää, kuinka monta pelaajaa",
                " §7kylääsi mahtuu!",
                "",
                " §7Saatavilla olevat pelaajamäärät:",
                " §7- " + (village.getMaxPlayers() == 32 ? "§a§l32 pelaajaa" : "§e32 pelaajaa"),
                " §7- " + (village.getMaxPlayers() == 24 ? "§a§l24 pelaajaa" : "§e24 pelaajaa"),
                " §7- " + (village.getMaxPlayers() == 16 ? "§a§l16 pelaajaa" : "§e16 pelaajaa"),
                " §7- " + (village.getMaxPlayers() == 8 ? "§a§l8 pelaajaa" : "§e8 pelaajaa"),
                " §7- " + (village.getMaxPlayers() == 4 ? "§a§l4 pelaajaa" : "§e4 pelaajaa"),
                " §7- " + (village.getMaxPlayers() == 2 ? "§a§l2 pelaajaa" : "§e2 pelaajaa"),
                " ",
                " " + (village.getLeader().equals(player.getUniqueId()) ? "§aKlikkaa vaihtaaksesi!" : "§cEi oikeuksia..."),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                if(village.ownsVillage(clicker.getUniqueId())) {
                    gui.close(clicker);

                    int currentMaxPlayers = village.getMaxPlayers();
                    int nextMaxPlayers = 2;

                    if(currentMaxPlayers == 32) nextMaxPlayers = 2;
                    else if(currentMaxPlayers == 24) nextMaxPlayers = 32;
                    else if(currentMaxPlayers == 16) nextMaxPlayers = 24;
                    else if(currentMaxPlayers == 8) nextMaxPlayers = 16;
                    else if(currentMaxPlayers == 4) nextMaxPlayers = 8;
                    else if(currentMaxPlayers == 2) nextMaxPlayers = 4;

                    village.setMaxPlayers(nextMaxPlayers);

                    openVillageSettings(clicker, village);
                }
            }
        });

        double returnTaxAmount = (village.getTaxRate() > 0) ?  Util.round((village.getBalance() / village.getCitizens().size()) * .2, 2) : 0;

        gui.addButton(new Button(1, 23, ItemUtil.makeItem(Material.GOLDEN_CARROT, 1, "§2Veronpalautus", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Kylän asukkaille maksetaan",
                " §7verotuksen yhteydessä §averonpalautusta§7!",
                " §7Veron palautuksen suuruus määräytyy sekä",
                " §7sen hetkisen kylän §easukasluvun§7, että",
                " §erahatilanteen §7mukaan. Tämän hetkisen",
                " §7tilanteen perusteella maksaisit",
                " §7veronpalautusta jokaiselle asukkaalle:",
                " ",
                "     §a§l" + Util.formatDecimals(returnTaxAmount) + " €",
                " ",
                " §cHuom! §7Jos asukas ei maksa täyttä",
                " §7verotusta, hän ei saa palautustakaan!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) { }
        });

        gui.addButton(new Button(1, 24, ItemUtil.makeItem(Material.WRITABLE_BOOK, 1, "§2Kutsumispyynnöt", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Hallinnoi niitä liittymiskutsuja",
                " §7mitä olet eri pelaajille lähettänyt",
                " ",
                " §aKlikkaa minua avataksesi",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                if(village.canModify(clicker.getUniqueId())) {
                    gui.close(clicker);
                    openInvitationMenu(player, village);
                }
            }
        });

        gui.addButton(new Button(1, 27, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openPersonalVillage(player, village);
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

        gui.addButton(new Button(1, 35, ItemUtil.makeItem(Material.GOLD_NUGGET, 1, "§2Nosta rahaa", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Hyvä muistaa, että voit",
                " §enostaa rahaa §7kylän pankkitililtä",
                " §7komennolla §a/kylä nosta <määrä>§7!",
                " ",
                " §cHuom! §7Muista nostaa rahaa ",
                " §7rehellisesti ja kun se on",
                " §7yhdessä päätetty asukkaiden",
                " §7kanssa!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
            }
        });

        final int[] glassSlots = { 10,19,16,25 };
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

            Gui gui = new Gui("Mitä etsit?", 27);

            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§cTulossa pian...", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Pelaajakylät eivät ole vielä",
                    " §7käytössä, mutta tulevat toimintaan",
                    " §7pikimmiten! Pahoittelut häiriöstä!",
                    " ",
                    " §7Pistäkää se §cTeamRaiderz §7takaisin",
                    " §7töihin!!!",
                    " ",
                    " §7Lisätietoa: §9/discord",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 13);

            gui.open(player);
            return;
        }

        final UUID uuid = player.getUniqueId();

        if(this.hasJoinedVillage(uuid)) {
            final PlayerVillage village = this.findVillageByPlayer(uuid);
            this.openPersonalVillage(player, village);
        } else {

            final int size = 27;

            final Gui gui = new Gui("Pelaajakylät", size);
            gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAPER, 1, "§2Et ole kylässä", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Et ole tällä hetkellä liittynyt",
                    " §7mihinkään kylään! Voit luoda uuden",
                    " §ePelaajakylän §7painamalla minua!",
                    " ",
                    " §7Kylän luominen maksaa §a" + Util.formatDecimals(VILLAGE_PURCHASE_PRICE) + "€§7!",
                    " ",
                    (Balance.get(player.getUniqueId()) >= VILLAGE_PURCHASE_PRICE) ? " §aKlikkaa luodaksesi!" : " §cEi varaa...",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    if(Balance.get(clicker.getUniqueId()) >= VILLAGE_PURCHASE_PRICE) {
                        gui.close(clicker);
                        openVillageCreationConfirmationMenu(clicker);
                    } else {
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    }
                }
            });

            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.MAP, 1, "§2Etsi kyliä", Arrays.asList(
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

            gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.PAPER, 1, "§2Kaikki kylät", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Näytä palvelimen §ekaikki",
                    " §7pelaajakylät!",
                    " ",
                    " §aKlikkaa minua!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    openListAllVillagesMenu(clicker);
                }
            });

            gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eRikkaimmat kylät", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Katso miten kylät sijouittuvat",
                    " §erikkaimpien §7joukossa!",
                    " ",
                    " §aKlikkaa minua!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Main.getVillageManager().openTopBalanceMenu(clicker);
                }
            });

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Profile.openProfile(clicker, clicker.getUniqueId());
                }
            });


            final int[] glassSlots = { 10, 12, 14 };
            for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.RED_STAINED_GLASS_PANE, 1), slot); }

            for(int i = 0; i < size; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
            }

            gui.open(player);

        }

    }

    private void openRequestedPlayersMenu(Player player, final PlayerVillage village) {

        int size = 54;
        final Gui gui = new Gui("Kylän Liittymispyynnöt", size);

        int slotToAddHead = 9;
        List<UUID> requested = village.getRequested();
        if(village.getCitizens().size() >= village.getMaxPlayers()) {
            // Max players
            gui.addItem(1, ItemUtil.makeItem(Material.BARRIER, 1, "§cKylä täynnä", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Kyläsi on täynnä pelaajista!",
                    " §7Nosta pelaajarajoitustasi tai",
                    " §7potki pelaajia ulos!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 22);
        } else {

            int added = 0;
            for(final UUID rUUID : requested) {

                if(slotToAddHead >= 45) break;

                if(village.getLeader().equals(rUUID)) continue;
                if(village.getCitizens().contains(rUUID)) continue;

                OfflinePlayer rPlayer = Bukkit.getOfflinePlayer(rUUID);

                gui.addButton(new Button(1, slotToAddHead, ItemUtil.makeSkullItem(rPlayer, 1, "§a" + rPlayer.getName(), Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §aVasen-klikkaa §7hyväksyäksesi!",
                        " §cOikea-klikkaa §7kieltäytyäksesi!",
                        " ",
                        " §7Jos klikkaat väärin, ja päästät pelaajan",
                        " §7vahingossa kylään, niin voit potkia hänet",
                        " §7heti ulos! ",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        village.getRequested().remove(rUUID);
                        if(clickType == ClickType.LEFT) {
                            village.join(rPlayer);
                        }
                        openRequestedPlayersMenu(clicker, village);
                    }
                });

                added += 1;
                slotToAddHead += 1;

            }


            if(added == 36 && requested.size() >= 36) {
                int leftOver = requested.size() - 35; // 35, because we're removing the last item (pos 44)...
                gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§eLisää löytyy...", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Hyväksyttäviä kutsuja on",
                        " §7vielä §a" + leftOver + " §7jäljellä...",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                )), 44);
            }

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

    public void openListAllVillagesMenu(Player player, final int pageToView) {

        if(pageToView < 1) {
            openListAllVillagesMenu(player, 1);
            return;
        }

        Gui gui = new Gui("Pelaajakylät (Sivu " + pageToView + ")", 54);

        final Map<Integer, List<PlayerVillage>> paginated = this.paginateVillages();
        // If has page...
        if(!paginated.isEmpty() && paginated.containsKey(pageToView)) {

            final List<PlayerVillage> currentVillages = paginated.get(pageToView);

            int added = 0, slotToAdd = 9;

            for (PlayerVillage village : currentVillages) {

                if(village == null) continue;

                if(added >= 36) break;

                OfflinePlayer leader = Bukkit.getOfflinePlayer(village.getLeader());

                gui.addButton(new Button(1, slotToAdd, ItemUtil.makeSkullItem(leader, 1, "§a" +village.getTitle(), Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Rahatilanne: §e" + Util.formatDecimals(village.getBalance()) + "€",
                        " §7Yhteensä kerätty raha: §e" + Util.formatDecimals(village.getTotalMoneyGathered()) + "€",
                        " §7Pormestari: §e" + leader.getName(),
                        " ",
                        " §aKlikkaa nähdäksesi lisätietoja!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        openVillageView(clicker, village, true);
                    }
                });

                added += 1;
                slotToAdd += 1;

            }

        } else if(paginated.isEmpty()) {
            this.mainGui(player);
            return;
        }

        // 45, 53

        // Should add "previous page" button
        if(paginated.containsKey(pageToView - 1)) {
            gui.addButton(new Button(pageToView, 45, ItemUtil.makeItem(Material.ARROW, 1, "§aTaaksepäin", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Mene sivu taaksepäin",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    openListAllVillagesMenu(player, pageToView - 1);
                }
            });
        }

        // Should add "next page" button
        if(paginated.containsKey(pageToView + 1)) {
            gui.addButton(new Button(pageToView, 45, ItemUtil.makeItem(Material.ARROW, 1, "§aEteenpäin", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Mene sivu eteenpäin",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    openListAllVillagesMenu(player, pageToView - 1);
                }
            });
        }

        gui.addButton(new Button(pageToView, 49, ItemUtil.makeItem(Material.BARRIER, 1, "§7Sulje")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
            }
        });

        gui.addButton(new Button(pageToView, 48, ItemUtil.makeItem(Material.OAK_DOOR, 1, "§2Sinun kyläsi", Arrays.asList(

                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Vuokraa ja hallinnoi",
                " §7pelaajakylääsi!",
                " ",
                " §aKlikkaa minua!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Main.getVillageManager().mainGui(clicker);
            }
        });

        gui.addButton(new Button(pageToView, 50, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eRikkaimmat kylät", Arrays.asList(

                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Katso miten eri pelaajakylät",
                " §7sijoittuvat listalle rahallisesti",
                " ",
                " §aKlikkaa minua!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Main.getVillageManager().openTopBalanceMenu(clicker);
            }
        });

        for(int i = 0; i < 9; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(pageToView, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
        }

        for(int i = 45; i < 54; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(pageToView, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), i);
        }

        gui.open(player);

    }

    public void openListAllVillagesMenu(Player player) {
        this.openListAllVillagesMenu(player, 1);
    }

    private Map<Integer, List<PlayerVillage>> paginateVillages() {
        final Map<Integer, List<PlayerVillage>> paginated = new HashMap<>();
        int currentPageToParse = 1;

        List<PlayerVillage> currentVillages = new ArrayList<>();

        // Villages per page = 36

        for(int index = 0; index < this.villages.size(); index++) {
            final PlayerVillage village = this.villages.get(index);
            if(village != null) {

                // If is divisible by 36, move to next page
                if(index % 36 == 0) {

                    // Add previous page to map
                    paginated.put(currentPageToParse, currentVillages);

                    // Move to next page, clear current added villages
                    currentPageToParse += 1;
                    currentVillages.clear();
                }

                currentVillages.add(village);

            }
        }

        return paginated;
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
            if(village.getCoLeaders().contains(member.getUniqueId()) && village.getCoLeaders().size() < village.getMaxCoLeaders()) {

                gui.addButton(new Button(1, slotToAddHead, ItemUtil.makeSkullItem(member, 1, "§a" + member.getName(), Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Klikkaa minua lisätäksesi",
                        " §7henkilön §e" + member.getName(),
                        " §7luottamushenkilöksi!",
                        " ",
                        " " + (village.getCoLeaders().size() >= village.getMaxCoLeaders() ? "§cEi voida lisätä..." : " §aKlikkaa lisätäksesi!"),
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(village.getCoLeaders().size() < village.getMaxCoLeaders()) {
                            gui.close(player);
                            village.addCoLeader(memberUUID);
                            openChangeCoLeaderMenu(player, village);
                        }
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
                openVillageSettings(player, village);
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

    private void openInvitationMenu(Player player, final PlayerVillage village) {
        int size = 54;
        final Gui gui = new Gui("Kylän Liittymispyynnöt", size);

        int slotToAddHead = 9;
        List<UUID> requested = village.getInvited();
        int added = 0;
        for(final UUID rUUID : requested) {

            if(slotToAddHead >= 45) break;

            if(village.getLeader().equals(rUUID)) continue;
            if(village.getCitizens().contains(rUUID)) continue;

            OfflinePlayer rPlayer = Bukkit.getOfflinePlayer(rUUID);

            gui.addButton(new Button(1, slotToAddHead, ItemUtil.makeSkullItem(rPlayer, 1, "§a" + rPlayer.getName(), Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §cKlikkaa §7poistaaksesi pyynnön!",
                    " ",
                    " §7Poistä tämä pyyntö ja pelaaja ei voi",
                    " §7enää liittyä tähän kylään.",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    village.getInvited().remove(rPlayer.getUniqueId());
                    openInvitationMenu(clicker, village);
                }
            });

            added += 1;
            slotToAddHead += 1;

        }

        if(added == 36 && requested.size() >= 36) {
            int leftOver = requested.size() - 35; // 35, because we're removing the last item (pos 44)...
            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§eLisää löytyy...", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Poistettavia kutsuja on",
                    " §7vielä §a" + leftOver + " §7jäljellä...",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 44);
        }

        gui.addButton(new Button(1, 49, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openVillageSettings(player, village);
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

    private void openVillagePlayerKickMenu(Player player, final PlayerVillage village) {

        int size = 54;
        final Gui gui = new Gui("Potki pelaajia", size);

        int slotToAddHead = 9;
        List<UUID> members = village.getCitizens();
        for(final UUID memberUUID : members) {

            if(slotToAddHead >= 45) break;

            if(village.getLeader().equals(memberUUID)) continue;

            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUUID);
            if(village.canModify(memberUUID) && village.getCoLeaders().contains(memberUUID)) {
                continue;
            } else {
                gui.addButton(new Button(1, slotToAddHead, ItemUtil.makeSkullItem(member, 1, "§c" + member.getName(), Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Klikkaa minua potkiaksesi",
                        " §7henkilön §e" + member.getName(),
                        " §7täsä kylästä!",
                        " ",
                        " §cKlikkaa potkiaksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        openPlayerKickConfirmationMenu(player, village, memberUUID);
                    }
                });
            }

            slotToAddHead += 1;

        }

        gui.addButton(new Button(1, 49, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openVillageSettings(player, village);
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

    private void openPlayerKickConfirmationMenu(Player player, PlayerVillage village, UUID playerToKick) {

        OfflinePlayer kicked = Bukkit.getOfflinePlayer(playerToKick);

        final Gui gui = new Gui("Pelaajan potkimisen vahvistus", 27);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa vahvistaaksesi sen",
                " §7että potkit pelaajan ",
                " §c" + kicked.getName() + "",
                " §7kylästäsi...",
                " ",
                " §aKlikkaa poistaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                village.removePlayer(playerToKick);
                openVillagePlayerKickMenu(clicker, village);
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
                openVillagePlayerKickMenu(player, village);
            }
        });

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

    public void openTopBalanceMenu(Player player) {

        if(!ENABLED && !player.isOp()) {
            final Gui gui = new Gui("Rikkaimmat kylät", 27);

            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§cMitäs oikein etsit?", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Sinuakin kiinnostaa mitä täältä",
                    " §7löytyy, eikö? Ei hätää! Pelaajakylät",
                    " §7aukeavat tuota pikaa...",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 13);

            gui.open(player);
            return;
        }

       Sorsa.async(() -> {
           final Gui gui = new Gui("Rikkaimmat kylät", 54);

           gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mikä tämä on?", Arrays.asList(
                   "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                   " §7Tässä valikossa näkyy",
                   " §7Survival-palvelimen",
                   " §erikkaimmat pelaajakylät!",
                   " ",
                   " §7Lista päivittyy muutaman",
                   " §7minuutin välein, joten",
                   " §7listalla olevat rahamäärät",
                   " §7voivat olla hieman väärässä",
                   " §7todellisesta rahamäärästä!",
                   "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
           )), 0);

           gui.addButton(new Button(1, 4, ItemUtil.makeItem(Material.PAPER, 1, "§2Kaikki kylät", Arrays.asList(
                   "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                   " §7Näytä palvelimen kaikki",
                   " §7pelaajakylät!",
                   " ",
                   " §aKlikkaa tästä!",
                   "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
           ))) {
               @Override
               public void onClick(Player clicker, ClickType clickType) {
                   gui.close(clicker);
                   openListAllVillagesMenu(clicker);
               }
           });

           List<PlayerVillage> topVillages = villages;
           topVillages.sort(Comparator.comparing(PlayerVillage::getBalance, Comparator.reverseOrder()));

           final int[] yellowGlassSlots = new int[]{20, 29, 38, 22, 31, 40, 24, 33, 42};
           final int[] villageSlots = new int[]{19, 28, 37, 21, 30, 39, 23, 32, 41, 25, 34, 43};

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

           for (int slot : yellowGlassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE), slot); }
           for (int j = 0; j < 54; j++) {
               if (gui.getItem(j) != null) continue;
               if (gui.getButton(j) != null) continue;
               gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), j);
           }

           int i= 0;
           for(PlayerVillage village : topVillages) {

               OfflinePlayer leader = Bukkit.getOfflinePlayer(village.getLeader());


               int slot = villageSlots[i];

               gui.addButton(new Button(1, slot, ItemUtil.makeSkullItem(leader, 1, "§a" +village.getTitle(), Arrays.asList(
                       "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                       " §7Rahatilanne: §e" + Util.formatDecimals(village.getBalance()) + "€",
                       " §7Yhteensä kerätty raha: §e" + Util.formatDecimals(village.getTotalMoneyGathered()) + "€",
                       " ",
                       " §aKlikkaa nähdäksesi lisätietoja!",
                       "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
               ))) {
                   @Override
                   public void onClick(Player clicker, ClickType clickType) {
                       gui.close(clicker);
                       openVillageView(clicker, village, true);
                   }
               });

               i += 1;

           }

            Sorsa.task(() -> gui.open(player));

       });
    }

}
