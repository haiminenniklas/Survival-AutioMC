package me.tr.survival.main;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.data.Homes;
import me.tr.survival.main.util.data.Level;
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
        Gui gui = new Gui("Pelaajan tiedot", 27);

        HashMap<String, Object> data = PlayerData.getData(targetUUID);

        gui.addItem(1, ItemUtil.makeSkullItem(target.getName(), 1, "§cProfiili", Arrays.asList(
                "§7§m--------------------",
                "§7Nimi: §c" + target.getName(),
                " ",
                "§7Arvo: §c" + Ranks.getDisplayName(Ranks.getRank(targetUUID)),
                "§7Raha: §c" + data.get("money") + "€",
                "§7Kristallit: §c" + Crystals.get(targetUUID),
                " ",
                "§7Liittynyt: §c" + data.get("joined"),
                "§7§m--------------------"
        )), 10);

        gui.addItem(1, ItemUtil.makeItem(Material.WRITABLE_BOOK, 1, "§cTehtävät", Arrays.asList(
                "§7§m--------------------",
                "§7Suoritettu: §c0§7/100 §o(0%)",
                "§7§m--------------------"
        )), 12);

        gui.addItem(1, ItemUtil.makeItem(Material.IRON_PICKAXE, 1, "§cTuhotut blockit", Arrays.asList(
                "§7§m--------------------",
                "§7Yhteensä: §c0",
                " ",
                "§7Timantti: §b0 §7§o(0%)",
                "§7Kulta: §60 §7§o(0%)",
                "§7Rauta: §f0 §7§o(0%)",
                "§7Hiili: §80 §7§o(0%)",
                "§7Muu: §c0 §7§o(0%)",
                "§7§m--------------------"
        )), 13);

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.OAK_DOOR, 1, "§cKodit", Arrays.asList(
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

        int level = Level.get(targetUUID);
        int xp = Level.getXP(targetUUID);
        float xpToNext = Level.getXPToNextLevel(targetUUID);
        float percent = Math.round((xp / xpToNext) * 100);

        gui.addItem(1, ItemUtil.makeItem(Material.NETHER_STAR, 1, "§cTaso", Arrays.asList(
                "§7§m--------------------",
                "§7Taso: §c" + level,
                " ",
                "§7Seuraava taso (§c" + xp + "xp§7/" + xpToNext + "xp):",
                "§7" + Level.getProgressText(targetUUID, 30, ChatColor.RED) + " §o(" + percent + "%)",
                "§7§m--------------------"
        )), 15);

        gui.addButton(new Button(1, 16, ItemUtil.makeItem(Material.LEGACY_REDSTONE_COMPARATOR, 1, "§cAsetukset", Arrays.asList(
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
