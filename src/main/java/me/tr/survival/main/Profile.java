package me.tr.survival.main;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.data.Homes;
import me.tr.survival.main.util.data.Level;
import me.tr.survival.main.util.data.Ores;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Profile {

    public static void openProfile(Player opener, UUID targetUUID) {

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        Gui gui = new Gui("Pelaajan tiedot", 4 * 9);

        HashMap<String, Object> data = PlayerData.getData(targetUUID);

        gui.addItem(1, ItemUtil.makeSkullItem(target.getName(), 1, "§cProfiili", Arrays.asList(
                "§7§m--------------------",
                "§7Nimi: §c" + target.getName(),
                " ",
                "§7Arvo: §c" + Ranks.getDisplayName(Ranks.getRank(targetUUID)),
                "§7Kristallit: §c" + Crystals.get(targetUUID),
                " ",
                "§7Liittynyt: §c" + data.get("joined"),
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

        gui.addItem(1, ItemUtil.makeItem(Material.IRON_PICKAXE, 1, "§cTuhotut blockit", Arrays.asList(
                "§7§m--------------------",
                "§7Yhteensä: §c" + Ores.getTotal(targetUUID),
                " ",
                "§7Timantti: §b" + Ores.getDiamonds(targetUUID) + " §7§o(" + diamond_percentage +  "%)",
                "§7Kulta: §6" + Ores.getGold(targetUUID) + " §7§o(" + gold_percentage +  "%)",
                "§7Rauta: §f" + Ores.getIron(targetUUID) + " §7§o(" + iron_percentage +  "%)",
                "§7Hiili: §8" + Ores.getCoal(targetUUID)  +" §7§o(" + coal_percentage +  "%)",
                "§7Muu: §c" + Ores.getOther(targetUUID) + " §7§o(" + other_percentage +  "%)",
                "§7§m--------------------"
        )), 19);

        gui.addButton(new Button(1, 22, ItemUtil.makeItem(Material.OAK_DOOR, 1, "§cKodit", Arrays.asList(
                "§7§m--------------------",
                "§7Kodit: §c" + new Homes(target).get().size(),
                " ",
                "§cKlikkaa näkeäksesi kotisi!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Homes.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 25, ItemUtil.makeItem(Material.LEGACY_REDSTONE_COMPARATOR, 1, "§cAsetukset", Arrays.asList(
                "§7§m--------------------",
                "§cKlikkaa avataksesi asetukset!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.panel(clicker);
            }
        });

        gui.open(opener);

    }

}
