package me.tr.survival.main;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.travel.TravelManager;
import me.tr.survival.main.other.backpacks.Backpack;
import me.tr.survival.main.other.booster.Boosters;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.*;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Profile {

    public static void openProfile(Player opener, UUID targetUUID) {

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);

        if(!PlayerData.isLoaded(targetUUID)) {

            Chat.sendMessage(opener, "Etsitään pelaajan §a" + target.getName() + " §7tietoja...");

            Autio.async(() -> {

                PlayerData.loadPlayer(targetUUID, (result) -> {

                    if(result) {
                        Autio.task(() -> {

                            if(Settings.get(targetUUID, "privacy") && !Ranks.isStaff(opener.getUniqueId())) {

                                Gui.openGui(opener, "Ei voitu avata", 27, (gui) -> {

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

                                return;
                            } else {
                                openProfile(opener, targetUUID);
                            }
                        });
                    } else {
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
                        Autio.task(() -> {
                            gui.open(opener);
                        });
                    }

                });

            });

            return;
        }

        if(Settings.get(targetUUID, "privacy") && !Ranks.isStaff(opener.getUniqueId())) {
            Gui.openGui(opener, "Ei voitu avata", 27, (gui) -> {

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
            return;
        }

        Gui gui = new Gui("Pelaajan tiedot", 5 * 9);
        HashMap<String, Object> data = PlayerData.getData(targetUUID);

        Timestamp lastTimestamp = new Timestamp(target.getLastSeen());
        LocalDateTime lastSeen = lastTimestamp.toLocalDateTime();

        gui.addItem(1, ItemUtil.makeSkullItem(target.getName(), 1, "§2Profiili", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Nimi: §a" + target.getName(),
                " ",
                " §7Arvo: §r" + Ranks.getDisplayName(Ranks.getRank(targetUUID)),
                " §7Rahatilanne: §e" + Util.formatDecimals(Balance.get(targetUUID)) + "€",
                " ",
                " §7Liittynyt: §a" + data.get("joined"),
                " §7Viimeksi nähty: §a" + lastSeen.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 13);

        double diamond_percentage;
        if(Ores.getDiamonds(targetUUID) >= 1) {
            diamond_percentage = Math.round(((double) Ores.getDiamonds(targetUUID) / (double) Ores.getTotal(targetUUID)) * 100d);
        } else {
            diamond_percentage = 0;
        }

        double gold_percentage;
        if(Ores.getGold(targetUUID) >= 1) {
            gold_percentage =  Math.round(((double) Ores.getGold(targetUUID) / (double) Ores.getTotal(targetUUID)) * 100d);
        } else {
            gold_percentage = 0;
        }

        double iron_percentage;
        if(Ores.getIron(targetUUID) >= 1) {
            iron_percentage = Math.round(((double) Ores.getIron(targetUUID) /(double)  Ores.getTotal(targetUUID)) * 100d);
        } else {
            iron_percentage = 0;
        }

        double coal_percentage;
        if(Ores.getCoal(targetUUID) >= 1) {
            coal_percentage = Math.round(((double)  Ores.getCoal(targetUUID) / (double) Ores.getTotal(targetUUID)) * 100d);
        } else {
            coal_percentage = 0;
        }

        double other_percentage;
        if(Ores.getOther(targetUUID) >= 1) {
            other_percentage = Math.round(((double) Ores.getOther(targetUUID) / (double) Ores.getTotal(targetUUID)) * 100d);
        } else {
            other_percentage = 0;
        }


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
        )), 19);

        gui.addButton(new Button(1, 22, ItemUtil.makeItem(Material.OAK_DOOR, 1, "§2Kodit", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Kodit: §e" + new Homes(target).getHomesAmount(),
                " ",
                " §aKlikkaa nähdäksesi kotisi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Homes.panel(clicker, clicker);
            }
        });

        gui.addButton(new Button(1, 25, ItemUtil.makeItem(Material.LEGACY_REDSTONE_COMPARATOR, 1, "§2Asetukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §aKlikkaa avataksesi asetukset!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.panel(clicker);
            }
        });

        // 39 40 41

        gui.addButton(new Button(1, 38, ItemUtil.makeItem(Material.EMERALD, 1, "§aTehostukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tästä klikkaamalla pääset",
                " §atehostuksien §7valikkoon",
                " §7joilla voit hieman tehostaa",
                " §7pelin kulkua! ;)",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Boosters.panel(clicker);
            }
        });

        gui.addItem(1, ItemUtil.makeSkullItem("MHF_Question", 1, "§6Apua", Arrays.asList(
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
        )), 39);

        gui.addButton(new Button(1, 40, ItemUtil.makeItem(Material.MAP, 1, "§eMatkustaminen", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tästä klikkaamalla, pääset",
                " §7matkustamaan eri §emaailmoihin§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                TravelManager.gui(clicker);
            }
        });



        gui.addButton(new Button(1, 41, ItemUtil.makeItem(Material.PAPER, 1, "§dPosti", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tästä klikkaamalla pääset",
                " §7katsomaan §dpäivittäisiä",
                " §dtoimituksiasi§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Mail.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 42, ItemUtil.makeItem(Material.NETHER_STAR, 1, "§bKosmetiikka", Arrays.asList(
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
                    Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Sinulla täytyy olla vähintään §aPremium§7-arvo tähän toimintoon!");
                } else {
                    Particles.openMainGui(clicker);
                }

            }
        });

        List<String> backpackLore = Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa tarkastellaksesi",
                " §7reppuasi!",
                " ",
                " §7Reppusi taso: §e" + Backpack.getLevelNumber(targetUUID),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        );

        if(Ranks.isStaff(opener.getUniqueId()) && targetUUID != opener.getUniqueId()) {
            backpackLore = Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa tarkastellaksesi",
                    " §7pelaajan §6" + target.getName(),
                    " §7reppua!",
                    " ",
                    " §7Repun taso: §e" + Backpack.getLevelNumber(targetUUID),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            );
        }

        gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.CHEST, 1, "§2Reppu", backpackLore)) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                gui.close(clicker);

                if(Ranks.isStaff(opener.getUniqueId()) && targetUUID != opener.getUniqueId()) {
                    Bukkit.dispatchCommand(opener, "reppu katso " + target.getName());
                } else {
                    Backpack.openBackpack(clicker);
                }

            }
        });

        gui.open(opener);

    }
}
