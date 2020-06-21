package me.tr.survival.main.managers;

import com.songoda.ultimatetimber.UltimateTimber;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.Main;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.perks.Particles;
import me.tr.survival.main.managers.perks.PlayerDeathMessageManager;
import me.tr.survival.main.managers.perks.PlayerGlowManager;
import me.tr.survival.main.managers.perks.PlayerWeather;
import me.tr.survival.main.other.*;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Settings {

    public static void panel(Player player) {

        Gui gui = new Gui("Asetukset", 27);
        UUID uuid = player.getUniqueId();

        int[] glassSlots = new int[] { 10, 16 };
        for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE), slot); }

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAINTING, 1, "§2Scoreboard", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Tila: " + settingText(Settings.get(uuid, "scoreboard")),
                " ",
                "§7§oKun tämä asetus on päällä,",
                "§7§osivulle tulee näkyville",
                "§7§oikkuna, jossa on näkyvillä",
                "§7§ohyödyllistä informaatiota",
                "",
                "§aKlikkaa vaihtaaksesi asetusta!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1 );
                Settings.toggle(uuid, "scoreboard");
                Settings.scoreboard(clicker);
                panel(clicker);
            }
        });


        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.ANVIL, 1, "§2Yksityinen tila", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Tila: " + settingText(Settings.get(uuid, "privacy")),
                " ",
                "§7§oKun tämä asetus on päällä",
                "§7§omuut pelaajat eivät voi",
                "§7§onähdä profiiliasi ja tietojasi!",
                "",
                "§aKlikkaa vaihtaaksesi asetusta!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1 );
                Settings.toggle(uuid, "privacy");
                panel(clicker);
            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§2Chat", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Tila: " + (Settings.get(uuid, "chat") ? "§a§lGlobaali" : "§e§lPaikallinen"),
                " ",
                "§7§oPalvelimella on käytössä",
                "§aGlobaali §7§oja §ePaikallinen §7§ochat",
                "§7§oja pystyt vaihtelemaan näiden välillä!",
                "",
                " §7Lisätietoa: §a/apua chat",
                "",
                "§aKlikkaa vaihtaaksesi",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1 );
                Settings.toggle(uuid, "chat");
                panel(clicker);
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.OAK_SAPLING, 1, "§2Puun kaato", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Tila: " + settingText(UltimateTimber.getInstance().getChoppingManager().isChopping(player)),
                " ",
                "§7§oKun tämä asetus on päällä,",
                "§7§opystyt kaatamaan koko puun",
                "§7§okun kaadat ensimmäisen palikan",
                "§7§osiitä!",
                "",
                "§aKlikkaa vaihtaaksesi asetusta!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1 );
                UltimateTimber.getInstance().getChoppingManager().togglePlayer(clicker);
                panel(clicker);
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.WRITABLE_BOOK, 1, "§2Älä häiritse", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Tila: " + settingText(Settings.get(uuid, "chat_mentions")),
                " ",
                "§7§oKun tämä asetus on päällä,",
                "§7§oet saa ylimääräisiä ilmoituksia",
                "§7§oja ääniä palvelimelta.",
                "",
                "§aKlikkaa vaihtaaksesi asetusta!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1 );
                Settings.toggle(uuid, "chat_mentions");
                panel(clicker);
            }
        });

        gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.GOLDEN_CARROT, 1, "§6VIP-Asetukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa päästäksesi",
                " §6VIP§7-asetuksiin!",
                " ",
                ((Ranks.isVIP(player.getUniqueId())) ? "§aKlikkaa avataksesi" : "§cVaatii §e§lPremium§c-arvon!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                if(!Ranks.isVIP(player.getUniqueId())) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto vaatii vähintään §e§lPremium§7-arvon! Lisätietoa §a/kauppa§7!");
                    return;
                } else {
                    gui.close(clicker);
                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1,1 );
                    vipPanel(clicker);
                }
            }
        });

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1,1 );
                Profile.openProfile(clicker, clicker.getUniqueId());
            }
        });

        gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§2Virheenkorjaus", Arrays.asList(
                "§7Jos asetuksissa ilmenee virheitä,",
                "§7tai jokin ei toimi, niin kokeile",
                "§7komentoa §a/debug §7tai",
                "§7poistu palvelimelta ja liity",
                "§7tänne uudestaan!"
        )), 26);

        for(int i = 0; i < 27; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }
        gui.open(player);
    }

    public static void vipPanel(Player player) {

        if(!Ranks.isVIP(player.getUniqueId())) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto vaatii vähintään §e§lPremium§7-arvon! Lisätietoa §a/kauppa§7!");
            return;
        }

        UUID uuid = player.getUniqueId();
        Gui.openGui(player, "VIP-asetukset", 27, (gui) -> {

            int[] glassSlots = new int[] {10, 16};
            for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE), slot); }

            gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.CLOCK, 1, "§2Sää ja aika", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7§oPäivitä henkilökohtaista",
                    "§7§osäätäsi ja aikaasi",
                    "§7§opalvelimella!",
                    " ",
                    ((!Ranks.isVIP(player.getUniqueId()) && !Ranks.isStaff(player.getUniqueId())) ? "§cVaatii §e§lPremium§c-arvon!" : "§aKlikkaa vaihtaaksesi!"),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {

                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    if(!Ranks.isVIP(clicker.getUniqueId()) && !Ranks.isStaff(clicker.getUniqueId())) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    else {
                        gui.close(clicker);
                        PlayerWeather.panel(clicker);
                    }
                }
            });

            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.PHANTOM_MEMBRANE, 1, "§2Hehkumimnen", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7Tila: " + settingText(Settings.get(uuid, "glow_effect")),
                    " ",
                    "§7§oKun tämä asetus on päällä,",
                    "§7§osaat käyttöösi hehkuefektin",
                    "§7§oja pelaajat voivat nähdä sinut",
                    "§7§opalikoiden läpi!",
                    "",
                    ((!Ranks.isStaff(player.getUniqueId()) && !Ranks.hasRank(player, "sorsa")) ? "§cVaatii §2§lSORSA§c-arvon!" : "§aKlikkaa vaihtaaksesi asetusta!"),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1 );
                    Main.getPlayerGlowManager().toggle(player);
                    vipPanel(clicker);
                }
            });
            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.SKELETON_SKULL, 1, "§2Kuolemaviestit", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7§oPäivitä henkilökohtaista",
                    "§7§okuolemaviestiäsi!",
                    " ",
                    ((!Ranks.hasRank(player.getUniqueId(), "premiumplus") && !Ranks.isStaff(player.getUniqueId())) ? "§cVaatii §6§lPremium§f+§c-arvon!" : "§aKlikkaa vaihtaaksesi!"),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    if(!Ranks.hasRank(clicker.getUniqueId(), "premiumplus", "sorsa") && !Ranks.isStaff(clicker.getUniqueId())) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    else {
                        gui.close(clicker);
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1,1 );
                        Main.getPlayerDeathMessageManager().deathMessagePanel(clicker);
                    }
                }
            });

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.IRON_SWORD, 1, "§2Tappoviestit", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7§oPäivitä henkilökohtaista",
                    "§7§otappoviestiäsi!",
                    " ",
                    ((!Ranks.hasRank(player.getUniqueId(), "premiumplus") && !Ranks.isStaff(player.getUniqueId())) ? "§cVaatii §6§lPremium§f+§c-arvon!" : "§aKlikkaa vaihtaaksesi!"),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    if(!Ranks.hasRank(clicker.getUniqueId(), "premiumplus", "sorsa") && !Ranks.isStaff(clicker.getUniqueId())) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    else {
                        gui.close(clicker);
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1,1 );
                        Main.getPlayerDeathMessageManager().killMessagePanel(clicker);
                    }
                }
            });

            gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.FEATHER, 1, "§2Lento", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7Tila: " + settingText(player.getAllowFlight()),
                    " ",
                    "§7§oTämän avulla pystyt",
                    "§7lentämään spawnilla!",
                    " ",
                    ((Ranks.hasRank(uuid, "sorsa") || Ranks.isStaff(uuid)) ? "§aKlikkaa vaihtaaksesi asetusta!" : "§cVaatii §2§lSORSA§c-arvon!"),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    if(!Ranks.hasRank(uuid, "sorsa") && !Ranks.isStaff(uuid)) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    else {
                        gui.close(clicker);
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1 );
                        toggleFlight(clicker);
                        vipPanel(clicker);
                    }

                }
            });

            gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.NETHER_STAR, 1, "§bKosmetiikka", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tästä klikkaamalla pääset",
                    " §7katsomaan §bkosmetiisia efektejä",
                    " §7ja §bominaisuuksia §7jotka ovat",
                    " §7sinulle avoinna!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    if(!Ranks.isVIP(clicker.getUniqueId())) Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Sinulla täytyy olla vähintään §e§lPremium§7-arvo tähän toimintoon!");
                    else Main.getParticles().openMainGui(clicker);
                }
            });
            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1,1 );
                    Settings.panel(clicker);
                }
            });
            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§2Virheenkorjaus", Arrays.asList(
                    "§7Jos asetuksissa ilmenee virheitä,",
                    "§7tai jokin ei toimi, niin kokeile",
                    "§7komentoa §a/debug §7tai",
                    "§7poistu palvelimelta ja liity",
                    "§7tänne uudestaan!"
            )), 26);
            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }
        });
    }

    public static void toggleFlight(Player player) {
        if(!player.getAllowFlight()) {
            if(Util.getRegions(player).size() >= 1) {
                player.setAllowFlight(true);
                player.setFlying(true);
                Chat.sendMessage(player, "Lentotila §apäällä§7!");
            } else {
                if(Ranks.isStaff(player.getUniqueId())) {
                    if(Main.getStaffManager().hasStaffMode(player)) {
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        Chat.sendMessage(player, "Lentotila §apäällä§7!");
                    } else Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                    return;
                } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto toimii vain spawnilla!");
            }
        } else {
            player.setAllowFlight(false);
            player.setFlying(false);
            Chat.sendMessage(player, "Lentotila §cpois päältä§7!");
        }
    }

    public static boolean get(UUID uuid, String setting) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (boolean) PlayerData.getValue(uuid, setting);
    }

    public static void set(UUID uuid, String setting, boolean value) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.set(uuid, setting, value);
    }

    private static void toggle(UUID uuid, String setting) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        Settings.set(uuid, setting, !Settings.get(uuid, setting));
    }

    private static String settingText(boolean value) {
        return value ? "§a§lPÄÄLLÄ" : "§c§lEI PÄÄLLÄ";
    }

    public static final Map<UUID, BukkitTask> scoreboardRunnables = new HashMap<>();

    public static void scoreboard(Player player) {

        if(scoreboardRunnables.containsKey(player.getUniqueId())) scoreboardRunnables.get(player.getUniqueId()).cancel();

        if(!Settings.get(player.getUniqueId(), "scoreboard")){
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            return;
        }
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("Sorsa", "dummy", "  §2§lSorsaMC §8| §7Survival  ", RenderType.INTEGER);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.getScore("§7 §a §o").setScore(14);
        obj.getScore("§7» Rahatilanne").setScore(13);

        Team moneyCounter = board.registerNewTeam("bal");
        moneyCounter.addEntry(ChatColor.BLUE + "" + ChatColor.RED + "" + ChatColor.RED);
        moneyCounter.setPrefix("§a" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€");
        obj.getScore(ChatColor.BLUE + "" + ChatColor.RED + "" + ChatColor.RED).setScore(12);
        obj.getScore("§7 §9 §l").setScore(11);
        obj.getScore("§7» Arvo").setScore(10);

        Team rank = board.registerNewTeam("rank");
        rank.addEntry(ChatColor.GREEN + "" + ChatColor.BLUE + "" + ChatColor.RED);
        rank.setPrefix(Ranks.getDisplayName(Ranks.getRank(player.getUniqueId())));
        obj.getScore(ChatColor.GREEN + "" + ChatColor.BLUE + "" + ChatColor.RED).setScore(9);
        obj.getScore("§7 §6 §k").setScore(8);
        obj.getScore("§7» Pelaajat").setScore(7);

        Team players = board.registerNewTeam("players");
        players.addEntry(ChatColor.LIGHT_PURPLE + "" + ChatColor.GREEN + "" + ChatColor.RED);
        players.setPrefix("§a" + Bukkit.getOnlinePlayers().size());
        obj.getScore(ChatColor.LIGHT_PURPLE + "" + ChatColor.GREEN + "" + ChatColor.RED).setScore(6);

        obj.getScore("§7 §1 §k").setScore(5);
        obj.getScore("       §2sorsamc.fi").setScore(4);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if(player == null) cancel();
                if(board == null) cancel();

                board.getTeam("bal").setPrefix("§a" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€");
                board.getTeam("rank").setPrefix(Ranks.getDisplayName(Ranks.getRank(player.getUniqueId())));
                board.getTeam("players").setPrefix("§a" + Sorsa.getOnlinePlayers().size());
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20, 20 * 3);
        scoreboardRunnables.put(player.getUniqueId(), task);

        player.setScoreboard(board);
    }

}
