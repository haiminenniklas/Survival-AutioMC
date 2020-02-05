package me.tr.survival.main;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.booster.Boosters;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.*;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Profile {

    public static void openProfile(Player opener, UUID targetUUID) {

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);

        if(!PlayerData.isLoaded(targetUUID)) {

            Chat.sendMessage(opener, "Etsitään pelaajan §6" + target.getName() + " §7tietoja...");

            Autio.async(() -> {

                if(!PlayerData.loadPlayer(targetUUID)) {

                    Gui gui = new Gui("Ei löydetty", 27);
                    gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§c§lEI LÖYDETTY", Arrays.asList(
                            "§7§m--------------------",
                            " §7Pelaajan §6" + target.getName() + " §7tietoja",
                            " §7ei löydetty. Ehkei hän ole ikinä",
                            " §7liittynyt, tai hänen tietojaan ei",
                            " §7olla vielä ladattu. Yritä myöhemmin",
                            " §7uudestaan!",
                            "",
                            "§7 Omat tiedot saat §6/tiedot§7!",
                            "§7§m--------------------"
                    )), 13);
                    Autio.task(() -> {
                        gui.open(opener);
                    });
                } else {
                    Autio.task(() -> {
                        openProfile(opener, targetUUID);
                    });
                }


            });

            return;
        }

        Gui gui = new Gui("Pelaajan tiedot", 5 * 9);
        HashMap<String, Object> data = PlayerData.getData(targetUUID);

        gui.addItem(1, ItemUtil.makeSkullItem(target.getName(), 1, "§6Profiili", Arrays.asList(
                "§7§m--------------------",
                "§7Nimi: §6" + target.getName(),
                " ",
                "§7Arvo: §6" + Ranks.getDisplayName(Ranks.getRank(targetUUID)),
                "§7Kristallit: §b" + Crystals.get(targetUUID),
                "§7Rahatilanne: §a" + Balance.get(targetUUID),
                " ",
                "§7Liittynyt: §6" + data.get("joined"),
                "§7§m--------------------"
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

        gui.addItem(1, ItemUtil.makeItem(Material.IRON_PICKAXE, 1, "§6Tuhotut blockit", Arrays.asList(
                "§7§m--------------------",
                "§7Yhteensä: §e" + Ores.getTotal(targetUUID),
                " ",
                "§7Timantti: §b" + Ores.getDiamonds(targetUUID) + " §7§o(" + diamond_percentage +  "%)",
                "§7Kulta: §6" + Ores.getGold(targetUUID) + " §7§o(" + gold_percentage +  "%)",
                "§7Rauta: §f" + Ores.getIron(targetUUID) + " §7§o(" + iron_percentage +  "%)",
                "§7Hiili: §8" + Ores.getCoal(targetUUID)  +" §7§o(" + coal_percentage +  "%)",
                "§7Muu: §e" + Ores.getOther(targetUUID) + " §7§o(" + other_percentage +  "%)",
                "§7§m--------------------"
        )), 19);

        gui.addButton(new Button(1, 22, ItemUtil.makeItem(Material.OAK_DOOR, 1, "§6Kodit", Arrays.asList(
                "§7§m--------------------",
                "§7Kodit: §e" + new Homes(target).getHomesAmount(),
                " ",
                "§6Klikkaa näkeäksesi kotisi!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Homes.panel(clicker, clicker);
            }
        });

        gui.addButton(new Button(1, 25, ItemUtil.makeItem(Material.LEGACY_REDSTONE_COMPARATOR, 1, "§6Asetukset", Arrays.asList(
                "§7§m--------------------",
                "§6Klikkaa avataksesi asetukset!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.panel(clicker);
            }
        });

        // 39 40 41

        gui.addButton(new Button(1, 38, ItemUtil.makeItem(Material.EMERALD, 1, "§aTehostukset", Arrays.asList(
                "§7§m--------------------",
                " §7Tästä klikkaamalla pääset",
                " §atehostuksien §7valikkoon",
                " §7joilla voit hieman tehostaa",
                " §7pelin kulkua! ;)",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Boosters.panel(clicker);
            }
        });

        gui.addItem(1, ItemUtil.makeSkullItem("MHF_Question", 1, "§6Apua", Arrays.asList(
                "§7§m--------------------",
                " §7Hyödylliset komennot:",
                "  §6/profiili §7tämä valikko",
                "  §6/rtp §7vie sinut arämaahan",
                "  §6/msg §7yksityisviestit",
                "  §6/tpa §7teleporttauspyyntö",
                "  §6/warp §7palvelimen warpit",
                "  §9/discord §7Discord-yhteisö",
                "  §6/vaihda §7vaihtokauppa",
                "  §a/osta §7verkkokauppa",
                "§7§m--------------------"
        )), 39);


        gui.addButton(new Button(1, 41, ItemUtil.makeItem(Material.PAPER, 1, "§dPosti", Arrays.asList(
                "§7§m--------------------",
                " §7Tästä klikkaamalla pääset",
                " §7katsomaan §dpäivittäisiä",
                " §dtoimituksiasi§7!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Mail.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 42, ItemUtil.makeItem(Material.NETHER_STAR, 1, "§bPartikkelit", Arrays.asList(
                "§7§m--------------------",
                " §7Tästä klikkaamalla pääset",
                " §7katsomaan §bpartikkeliefektejä",
                " §7jotka ovat sinulle avoinna!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);

                if(!Ranks.isVIP(clicker.getUniqueId())) {
                    Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Sinulla täytyy olla vähintään §6§lPremium§7-arvo tähän toimintoon!");
                } else {
                    Chat.sendMessage(clicker, "Tulossa myöhemmin! Stay tuned ;)");
                }

            }
        });
        gui.open(opener);

    }
}
