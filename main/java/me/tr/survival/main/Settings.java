package me.tr.survival.main;

import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.manager.ChoppingManager;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.*;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.data.Ores;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scoreboard.*;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.UUID;

public class Settings {

    public static void panel(Player player) {

        Gui gui = new Gui("Asetukset", 27);
        UUID uuid = player.getUniqueId();

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
                Settings.toggle(uuid, "scoreboard");
                Settings.scoreboard(clicker);
                gui.refresh(clicker);
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
                Settings.toggle(uuid, "privacy");
                gui.refresh(clicker);
            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§2Chat", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Tila: " + settingText(Settings.get(uuid, "chat")),
                " ",
                "§7§oKun tämä asetus on pois päältä,",
                "§7§oet näe enää chat-viestejä"  ,
                "§7§omuilta pelaajilta",
                "",
                "§aKlikkaa vaihtaaksesi asetusta!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                Settings.toggle(uuid, "chat");
                gui.refresh(clicker);
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
                UltimateTimber.getInstance().getChoppingManager().togglePlayer(clicker);
                gui.refresh(clicker);
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
                Settings.toggle(uuid, "chat_mentions");
                gui.refresh(clicker);
            }
        });

        String areVipSettingsApplicable = (Ranks.isVIP(player.getUniqueId())) ? "§aKlikkaa avataksesi" : "§cVaatii §aPremium§c-arvon!";

        gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.GOLDEN_CARROT, 1, "§6VIP-Asetukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa päästäksesi",
                " §6VIP§7-asetuksiin!",
                " ",
                areVipSettingsApplicable,
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                vipPanel(clicker);
            }
        });

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
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

        gui.open(player);

    }

    public static void vipPanel(Player player) {

        if(!Ranks.isVIP(player.getUniqueId())) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto vaatii vähintään §aPremium§7-arvon! Lisätietoa §a/kauppa§7!");
            return;
        }

        UUID uuid = player.getUniqueId();

        Gui.openGui(player, "VIP-asetukset", 27, (gui) -> {

            String isWeatherApplicable = (!Ranks.isVIP(player.getUniqueId()) && !Ranks.isStaff(player.getUniqueId())) ? "§cVaatii §aPremium§c-arvon!" : "§aKlikkaa vaihtaaksesi!";

            gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.CLOCK, 1, "§2Sää ja aika", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7§oPäivitä henkilökohtaista",
                    "§7§osäätäsi ja aikaasi",
                    "§7§opalvelimella!",
                    " ",
                    isWeatherApplicable,
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {

                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    if(!Ranks.isVIP(clicker.getUniqueId()) && !Ranks.isStaff(clicker.getUniqueId())) {
                        Chat.sendMessage(clicker, "§7Tähän toimintoon tarvitset vähintään §aPremium§7-arvon!");
                    } else {
                        PlayerWeather.panel(clicker);
                    }
                }
            });

            String isGlowApplicable = (!Ranks.isStaff(player.getUniqueId()) && !Ranks.hasRank(player, "sorsa")) ? "§cVaatii §2§lSORSA§c-arvon!" : "§aKlikkaa vaihtaaksesi asetusta!";

            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.PHANTOM_MEMBRANE, 1, "§2Hehkumimnen", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7Tila: " + settingText(Settings.get(uuid, "glow_effect")),
                    " ",
                    "§7§oKun tämä asetus on päällä,",
                    "§7§osaat käyttöösi hehkuefektin",
                    "§7§oja pelaajat voivat nähdä sinut",
                    "§7§opalikoiden läpi!",
                    "",
                    isGlowApplicable,
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {

                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    PlayerGlowManager.toggle(player);
                    Settings.vipPanel(clicker);
                    gui.refresh(clicker);
                }
            });

            String areDeathMessagesApplicable = (!Ranks.hasRank(player.getUniqueId(), "premiumplus") && !Ranks.isStaff(player.getUniqueId())) ? "§cVaatii §aPremium§f+§c-arvon!" : "§aKlikkaa vaihtaaksesi!";

            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.SKELETON_SKULL, 1, "§2Kuolemaviestit", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7§oPäivitä henkilökohtaista",
                    "§7§okuolemaviestiäsi!",
                    " ",
                    areDeathMessagesApplicable,
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {

                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    if(!Ranks.hasRank(clicker.getUniqueId(), "premiumplus") && !Ranks.isStaff(clicker.getUniqueId())) {
                        Chat.sendMessage(clicker, "§7Tähän toimintoon tarvitset vähintään §aPremium§f+§7-arvon!");
                    } else {
                        PlayerDeathMessageManager.deathMessagePanel(clicker);
                    }
                }
            });

            String areKillMessagesApplicable = (!Ranks.hasRank(player.getUniqueId(), "premiumplus") && !Ranks.isStaff(player.getUniqueId())) ? "§cVaatii §aPremium§f+§c-arvon!" : "§aKlikkaa vaihtaaksesi!";

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.IRON_SWORD, 1, "§2Tappoviestit", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7§oPäivitä henkilökohtaista",
                    "§7§otappoviestiäsi!",
                    " ",
                    areKillMessagesApplicable,
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {

                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    if(!Ranks.hasRank(clicker.getUniqueId(), "premiumplus") && !Ranks.isStaff(clicker.getUniqueId())) {
                        Chat.sendMessage(clicker, "§7Tähän toimintoon tarvitset vähintään §aPremium§f+§7-arvon!");
                    } else {
                        PlayerDeathMessageManager.killMessagePanel(clicker);
                    }
                }
            });

            String isFlighApplicable = (Ranks.hasRank(uuid, "sorsa") || Ranks.isStaff(uuid)) ? "§aKlikkaa vaihtaaksesi asetusta!" : "§cVaatii §2§lSORSA§c-arvon!";

            gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.FEATHER, 1, "§2Lento", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7Tila: " + settingText(player.getAllowFlight()),
                    " ",
                    "§7§oTämän avulla pystyt",
                    "§7lentämään spawnilla!",
                    " ",
                    isFlighApplicable,
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {

                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    if(!Ranks.hasRank(uuid, "sorsa") && !Ranks.isStaff(uuid)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän toimintoon vaaditaan §2§lSORSA§7-arvo! Lisätietoa §a/kauppa§7!");
                    } else {
                        toggleFlight(clicker);
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

                    if(!Ranks.isVIP(clicker.getUniqueId())) {
                        Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Sinulla täytyy olla vähintään §a§lPremium§7-arvo tähän toimintoon!");
                    } else {
                        Particles.openMainGui(clicker);
                    }

                }
            });

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
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

        });

    }

    public static void toggleFlight(Player player) {
        if(!player.getAllowFlight()) {

            if(player.isOp()) {
                player.setAllowFlight(true);
                player.setFlying(true);
                Chat.sendMessage(player, "Lentotila §apäällä§7!");
                return;
            }

            if(Util.isInRegion(player, "spawn") && !Ranks.isStaff(player.getUniqueId())) {
                player.setAllowFlight(true);
                player.setFlying(true);
                Chat.sendMessage(player, "Lentotila §apäällä§7!");
            } else {
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Tämä toiminto toimii vain spawnilla!");
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
        obj.setDisplayName("  §2§lSorsaMC §8| §7Survival  ");


        obj.getScore("§7 §6 §k").setScore(15);
        /*obj.getScore("§7» Kristallit").setScore(14);

        Team crystals = board.registerNewTeam("crystals");
        crystals.addEntry(ChatColor.RED + "" + ChatColor.WHITE + "" + ChatColor.RED);
        crystals.setPrefix("§b" + Crystals.get(player.getUniqueId()));
        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE + "" + ChatColor.RED).setScore(13); */

        obj.getScore("§7 §a §o").setScore(14);
        obj.getScore("§7» Rahatilanne").setScore(13);

        Team moneyCounter = board.registerNewTeam("blocks");
        moneyCounter.addEntry(ChatColor.BLUE + "" + ChatColor.RED + "" + ChatColor.RED);
        moneyCounter.setPrefix("§e" + Balance.get(player.getUniqueId()) + "€");
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

        Main.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {

            //board.getTeam("crystals").setPrefix("§b" + Crystals.get(player.getUniqueId()));
            board.getTeam("blocks").setPrefix("§a" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€");
            board.getTeam("rank").setPrefix(Ranks.getDisplayName(Ranks.getRank(player.getUniqueId())));
            board.getTeam("players").setPrefix("§a" + Autio.getOnlinePlayers().size());

        }, 0, 20 * 2);

        player.setScoreboard(board);

    }

}
