package me.tr.survival.main.managers;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.commands.BaltopCommand;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.features.Boosters;
import me.tr.survival.main.managers.features.Homes;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.database.data.*;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Profile {

    public static void openProfile(Player opener, UUID targetUUID) {

        if(!opener.getUniqueId().equals(targetUUID)) {
            openOther(opener, Bukkit.getOfflinePlayer(targetUUID));
            return;
        }

        Gui gui = new Gui("Sinun profiili", 5 * 9);
        HashMap<String, Object> data = PlayerData.getData(opener.getUniqueId());

        Timestamp lastTimestamp = new Timestamp(opener.getLastPlayed());
        LocalDateTime lastSeen = lastTimestamp.toLocalDateTime();

        gui.addItem(1, ItemUtil.makeSkullItem(opener, 1, "§2Profiili", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Nimi: §a" + opener.getName(),
                " ",
                " §7Arvo: §r" + Ranks.getDisplayName(Ranks.getRank(targetUUID)),
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(targetUUID)) + "€",
                " ",
                " §7Liityit: §a" + data.get("joined"),
                " §7Viimeksi nähty: §a" + lastSeen.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 0);

        gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eRikkaimmat", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Katso miten sijoitut",
                " §7palvelimen §erikkaimpien",
                " §7joukossa!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                BaltopCommand.openGui(clicker);
            }
        });

        double diamond_percentage;
        if(Ores.getDiamonds(targetUUID) >= 1) diamond_percentage = Math.round(((double) Ores.getDiamonds(targetUUID) / (double) Ores.getTotal(targetUUID)) * 100d);
        else diamond_percentage = 0;


        double gold_percentage;
        if(Ores.getGold(targetUUID) >= 1) gold_percentage =  Math.round(((double) Ores.getGold(targetUUID) / (double) Ores.getTotal(targetUUID)) * 100d);
        else gold_percentage = 0;


        double iron_percentage;
        if(Ores.getIron(targetUUID) >= 1) iron_percentage = Math.round(((double) Ores.getIron(targetUUID) /(double)  Ores.getTotal(targetUUID)) * 100d);
        else iron_percentage = 0;


        double coal_percentage;
        if(Ores.getCoal(targetUUID) >= 1) coal_percentage = Math.round(((double)  Ores.getCoal(targetUUID) / (double) Ores.getTotal(targetUUID)) * 100d);
        else coal_percentage = 0;


        double other_percentage;
        if(Ores.getOther(targetUUID) >= 1) other_percentage = Math.round(((double) Ores.getOther(targetUUID) / (double) Ores.getTotal(targetUUID)) * 100d);
        else other_percentage = 0;


        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.OAK_DOOR, 1, "§2Pelaajakylät", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Vuokraa ja hallinnoi",
                " §7pelaajakyliäsi!",
                " ",
                " §eTulossa pian...",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Main.getVillageManager().mainGui(clicker);
            }
        });

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.RED_BED, 1, "§2Kodit", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tallenna kotisi ja muiden",
                " §7tärkeiden paikkojen sijainnit",
                " §7helposti ja kätevästi!",
                " ",
                " §7Kodit: §e" + new Homes(opener).getHomesAmount(),
                " ",
                " §aKlikkaa avataksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Homes.panel(clicker, clicker);
            }
        });

        gui.addItem(1, ItemUtil.makeItem(Material.IRON_PICKAXE, 1, "§2Tuhotut blockit", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Yhteensä: §e" + Ores.getTotal(targetUUID),
                " ",
                " §7Timantti: §b" + Ores.getDiamonds(targetUUID) + " §7§o(" + diamond_percentage +  "%)",
                " §7Kulta: §6" + Ores.getGold(targetUUID) + " §7§o(" + gold_percentage +  "%)",
                " §7Rauta: §f" + Ores.getIron(targetUUID) + " §7§o(" + iron_percentage +  "%)",
                " §7Hiili: §8" + Ores.getCoal(targetUUID)  +" §7§o(" + coal_percentage +  "%)",
                " §7Muu: §e" + Ores.getOther(targetUUID) + " §7§o(" + other_percentage +  "%)",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 13);

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.LEGACY_REDSTONE_COMPARATOR, 1, "§2Asetukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Mukauta pelikokemustasi",
                " §7juuri sinulle sopivaksi",
                " §7tarjoamillamme asetuksilla!",
                " ",
                " §aKlikkaa avataksesi",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.CHEST, 1, "§2Reppu", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Säilytä arvotavaroitasi",
                " §7turvallisesti repullasi!",
                " ",
                " §7Reppusi taso: §e" + Main.getBackpack().getLevelNumber(targetUUID),
                " ",
                " §aKlikkaa avataksesi",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                gui.close(clicker);
                Main.getBackpack().openBackpack(clicker);

            }
        });

        gui.addButton(new Button(1, 29, ItemUtil.makeItem(Material.EMERALD, 1, "§aTehostukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tästä klikkaamalla pääset",
                " §atehostuksien §7valikkoon",
                " §7joilla voit hieman tehostaa",
                " §7pelin kulkua! ;)",
                " ",
                " §aKlikkaa avataksesi",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Boosters.panel(clicker);
            }
        });

        gui.addItem(1, ItemUtil.makeSkullItem(Bukkit.getOfflinePlayer("MHF_Question"), 1, "§6Apua", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Hyödylliset komennot:",
                "  §6/profiili §7tämä valikko",
                "  §6/rtp §7vie sinut erämaahan",
                "  §6/matkusta §7matkusatminen",
                "  §6/msg §7yksityisviestit",
                "  §6/tpa §7teleporttauspyyntö",
                "  §a/vaihda §7vaihtokauppa",
                "  §9/discord §7Discord-yhteisö",
                "  §e/osta §7verkkokauppa",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 30);

        gui.addButton(new Button(1, 31, ItemUtil.makeItem(Material.MAP, 1, "§eMatkustaminen", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tästä klikkaamalla, pääset",
                " §7matkustamaan eri §emaailmoihin§7!",
                " ",
                " §aKlikkaa avataksesi",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Main.getTravelManager().gui(clicker);
            }
        });

        gui.addButton(new Button(1, 32, ItemUtil.makeItem(Material.PAPER, 1, "§dPosti", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tästä klikkaamalla pääset",
                " §7katsomaan §dpäivittäisiä",
                " §dtoimituksiasi§7!",
                " ",
                " §aKlikkaa avataksesi",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Mail.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 33, ItemUtil.makeItem(Material.NETHER_STAR, 1, "§bKosmetiikka", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tästä klikkaamalla pääset",
                " §7katsomaan §bkosmetiisia efektejä",
                " §7ja §bominaisuuksia §7jotka ovat",
                " §7sinulle avoinna!",
                " ",
                (Ranks.isVIP(opener.getUniqueId()) ? " §aKlikkaa avataksesi" : " §cVaatii §e§lPremium§c-arvon!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                if(!Ranks.isVIP(clicker.getUniqueId())) clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                else {
                    gui.close(clicker);
                    Main.getParticles().openMainGui(clicker);
                }
            }
        });

        int[] glassSlots = new int[] { 10,19,28, 16,25,34 };
        for(int slot : glassSlots) { gui.addItem(1, new ItemStack(Material.BLUE_STAINED_GLASS_PANE), slot); }

        for(int i = 0; i < 5*9; i++) {
            if (gui.getItem(i) != null) continue;
            if (gui.getButton(i) != null) continue;
            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }

        gui.open(opener);

    }

    public static void openOther(Player opener, OfflinePlayer target) {

        final UUID targetUUID = target.getUniqueId();

        if(!PlayerData.isLoaded(targetUUID)) {
            Chat.sendMessage(opener, "Etsitään pelaajan §a" + target.getName() + " §7tietoja...");
            Sorsa.async(() ->
                    PlayerData.loadPlayer(targetUUID, (result) -> {
                        if(result)
                            Sorsa.task(() -> {
                                if(Settings.get(targetUUID, "privacy") && !Ranks.isStaff(opener.getUniqueId())) openPrivateDenyGui(opener);
                                else openOther(opener, target);
                            });
                        else {
                            Gui gui = new Gui("Ei löydetty", 27);
                            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§c§lEI LÖYDETTY", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Pelaajan §a" + target.getName() + " §7tietoja",
                                    " §7ei löydetty. Ehkei hän ole ikinä",
                                    " §7liittynyt, tai hänen tietojaan ei",
                                    " §7olla vielä ladattu. Yritä myöhemmin",
                                    " §7uudestaan!",
                                    "",
                                    "§7 Omat tiedot saat §a/profiili§7!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);
                            Sorsa.task(() -> gui.open(opener));
                        }
                    }));
            return;
        }

        if(Settings.get(targetUUID, "privacy") && !Ranks.isStaff(opener.getUniqueId())) {
            openPrivateDenyGui(opener);
            return;
        }

        Gui gui = new Gui("Pelaajan " + target.getName() + " tiedot", 27);

        Timestamp lastTimestamp = new Timestamp(target.getLastPlayed());
        LocalDateTime lastSeen = lastTimestamp.toLocalDateTime();
        HashMap<String, Object> data = PlayerData.getData(targetUUID);

        gui.addItem(1, ItemUtil.makeSkullItem(target, 1, "§2Profiili", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Nimi: §a" + target.getName(),
                " ",
                " §7Arvo: §r" + Ranks.getDisplayName(Ranks.getRank(targetUUID)),
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(targetUUID)) + "€",
                " ",
                " §7Liittynyt: §a" + data.get("joined"),
                " §7Viimeksi nähty: §a" + lastSeen.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                " ",
                " §7Blockeja rikottu: §e" + Ores.getTotal(targetUUID),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 13);

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.BOOK, 1, "§aOma profiili", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Voit päästä katsomaan",
                " §7omaa §aprofiiliasi",
                " §7klikkaamalla §etästä§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openProfile(clicker, clicker.getUniqueId());
            }
        });

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eRikkaimmat", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Voit myös katsoa miten",
                " §7tämä kyseinen pelaaja",
                " §7saattaa sijoittua",
                " §erikkaimpien §7pelaajien",
                " §7joukossa!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                BaltopCommand.openGui(clicker);
            }
        });

        if(Ranks.isStaff(opener.getUniqueId())) {
            gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.WRITABLE_BOOK, 1, "§6Pelaajan tiedot", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Vahdi ja katso pelaajan",
                    " §7tietoja!",
                    " ",
                    " §aKlikkaa tästä!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Bukkit.dispatchCommand(opener, "staff " + target.getName());
                }
            });
        }

        int[] glassSlots = new int[]{11, 12, 14, 15};
        for (int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE), slot); }

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) != null) continue;
            if (gui.getButton(i) != null) continue;
            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }

        gui.open(opener);

    }

    private static void openPrivateDenyGui(Player player) {
        Gui.openGui(player, "Ei voitu avata", 27, (gui) -> {

            gui.addItem(1, ItemUtil.makeItem(Material.BARRIER, 1, "§cEi voitu avata", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Emme voineet avata tämän",
                    " §7pelaajan §aprofiilia§7,",
                    " §7sillä tällä pelaajalla",
                    " §7on §cyksityinen tila",
                    " §7päällä!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )),13);

            gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.BOOK, 1, "§aOma profiili", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Voit kuitenkin päästä",
                    " §7katsomaan omaa §aprofiiliasi",
                    " §7klikkaamalla §etästä§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    openProfile(clicker, clicker.getUniqueId());
                }
            });
        });
    }
}
