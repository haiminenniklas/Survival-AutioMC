package me.tr.survival.main;

import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.manager.ChoppingManager;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.PlayerWeather;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.data.Ores;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scoreboard.*;

import java.util.Arrays;
import java.util.UUID;

public class Settings {

    public static void panel(Player player) {

        Gui gui = new Gui("Asetukset", 27);
        UUID uuid = player.getUniqueId();

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.OAK_SIGN, 1, "§6Scoreboard", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: " + settingText(Settings.get(uuid, "scoreboard")),
                " ",
                "§7§oKun tämä asetus on päällä,",
                "§7§osivulle tulee näkyville",
                "§7§oikkuna, jossa on näkyvillä",
                "§7§ohyödyllistä informaatiota",
                "",
                "§6Klikkaa vaihtaaksesi asetusta!",
                "§7§m--------------------"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.toggle(uuid, "scoreboard");
                Settings.panel(clicker);
                Settings.scoreboard(clicker);
            }
        });

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.WRITABLE_BOOK, 1, "§6Yksityinen tila", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: " + settingText(Settings.get(uuid, "privacy")),
                " ",
                "§7§oKun tämä asetus on päällä,",
                "§7§oet näe enää yksityisviestejä",
                "§7§omuilta pelaajilta",
                "",
                "§6Klikkaa vaihtaaksesi asetusta!",
                "§7§m--------------------"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.toggle(uuid, "privacy");
                Settings.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§6Chat", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: " + settingText(Settings.get(uuid, "chat")),
                " ",
                "§7§oKun tämä asetus on pois päältä,",
                "§7§oet näe enää chat-viestejä"  ,
                "§7§omuilta pelaajilta",
                "",
                "§6Klikkaa vaihtaaksesi asetusta!",
                "§7§m--------------------"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.toggle(uuid, "chat");
                Settings.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.OAK_SAPLING, 1, "§6Puun kaato", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: " + settingText(UltimateTimber.getInstance().getChoppingManager().isChopping(player)),
                " ",
                "§7§oKun tämä asetus on päällä,",
                "§7§opystyt kaatamaan koko puun",
                "§7§okun kaadat ensimmäisen palikan",
                "§7§osiitä!",
                "",
                "§6Klikkaa vaihtaaksesi asetusta!",
                "§7§m--------------------"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                UltimateTimber.getInstance().getChoppingManager().togglePlayer(clicker);
                Settings.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.CLOCK, 1, "§6Sää ja aika", Arrays.asList(
                "§7§m--------------------",
                "§6Klikkaa vaihtaakesi sinun säätä ja aikaa",
                "§7§m--------------------"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                if(!Ranks.isVIP(clicker.getUniqueId()) || !Ranks.isStaff(clicker.getUniqueId())) {
                    Chat.sendMessage(clicker, "§7Tähän toimintoon tarvitset vähintään §6§lPremium§7-arvon!");
                } else {
                    PlayerWeather.panel(clicker);
                }
            }
        });

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Profile.openProfile(player, player.getUniqueId());
            }
        });

        gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§6Virheenkorjaus", Arrays.asList(
                "§7Jos asetuksissa ilmenee virheitä,",
                "§7tai jokin ei toimi, niin kokeile",
                "§7komentoa §6/debug §7tai",
                "§7poistu palvelimelta ja liity",
                "§7tänne uudestaan!"
        )), 26);

        gui.open(player);

    }

    public static boolean get(UUID uuid, String setting) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return (boolean) PlayerData.getValue(uuid, setting);
    }

    public static void set(UUID uuid, String setting, boolean value) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.set(uuid, setting, value);
    }

    public static void toggle(UUID uuid, String setting) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        Settings.set(uuid, setting, !Settings.get(uuid, setting));
    }

    private static String settingText(boolean value) {
        return value ? "§a§lPÄÄLLÄ" : "§c§lEI PÄÄLLÄ";
    }

    public static void scoreboard(Player player) {

        if(!Settings.get(player.getUniqueId(), "scoreboard")){
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            return;
        }

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("AutioMC", "dummy", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("  §6§lNIMETÖN §8| §7Survival  ");

        obj.getScore("§7 §6 §k").setScore(15);
        obj.getScore("§7» Kristallit").setScore(14);

        Team crystals = board.registerNewTeam("crystals");
        crystals.addEntry(ChatColor.RED + "" + ChatColor.WHITE + "" + ChatColor.RED);
        crystals.setPrefix("§6" + Crystals.get(player.getUniqueId()));
        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE + "" + ChatColor.RED).setScore(13);

        obj.getScore("§7 §a §o").setScore(12);
        obj.getScore("§7» Rahatilanne").setScore(11);

        Team moneyCounter = board.registerNewTeam("blocks");
        moneyCounter.addEntry(ChatColor.BLUE + "" + ChatColor.RED + "" + ChatColor.RED);
        moneyCounter.setPrefix("§6" + Balance.get(player.getUniqueId()) + "€");
        obj.getScore(ChatColor.BLUE + "" + ChatColor.RED + "" + ChatColor.RED).setScore(10);

        obj.getScore("§7 §9 §l").setScore(9);
        obj.getScore("§7» Arvo").setScore(8);

        Team rank = board.registerNewTeam("rank");
        rank.addEntry(ChatColor.GREEN + "" + ChatColor.BLUE + "" + ChatColor.RED);
        rank.setPrefix(Ranks.getDisplayName(Ranks.getRank(player.getUniqueId())));
        obj.getScore(ChatColor.GREEN + "" + ChatColor.BLUE + "" + ChatColor.RED).setScore(7);

        obj.getScore("§7 §6 §k").setScore(6);
        obj.getScore("§7» Pelaajat").setScore(5);

        Team players = board.registerNewTeam("players");
        players.addEntry(ChatColor.LIGHT_PURPLE + "" + ChatColor.GREEN + "" + ChatColor.RED);
        players.setPrefix("§6" + Bukkit.getOnlinePlayers().size());
        obj.getScore(ChatColor.LIGHT_PURPLE + "" + ChatColor.GREEN + "" + ChatColor.RED).setScore(4);

        obj.getScore("§7 §1 §k").setScore(3);
        obj.getScore("       §6nimeton.fi").setScore(2);

        Main.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {

            board.getTeam("crystals").setPrefix("§6" + Crystals.get(player.getUniqueId()));
            board.getTeam("blocks").setPrefix("§6" + Balance.get(player.getUniqueId()) + "€");
            board.getTeam("rank").setPrefix(Ranks.getDisplayName(Ranks.getRank(player.getUniqueId())));
            board.getTeam("players").setPrefix("§6" + Bukkit.getOnlinePlayers().size());

        }, 0, 20 * 2);

        player.setScoreboard(board);

    }

}
