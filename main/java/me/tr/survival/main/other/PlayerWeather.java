package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Profile;
import me.tr.survival.main.Settings;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Times;
import me.tr.survival.main.util.Weathers;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class PlayerWeather {

    public static void panel(Player player) {

        Gui gui = new Gui("Sää ja aika", 36);

        // 12, 13, 14
        // 21, 22, 23

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.BOOK, 1, "§6§lSÄÄ: §7Selkeä")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                if(Ranks.hasRank(clicker.getUniqueId(), "premiumplus") || Ranks.isStaff(clicker.getUniqueId())) {
                    Chat.sendMessage(clicker, "Sää asetttu selkeäksi!");
                    player.setPlayerWeather(Weathers.SUNNY);
                } else {
                    Chat.sendMessage(player, "Tämä toiminto vaatii arvon §aPremium§6+§7!");
                }
            }
        });

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.BOOK, 1, "§6§lSÄÄ: §7Sade")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);

                if(Ranks.hasRank(clicker.getUniqueId(), "premiumplus") || Ranks.isStaff(clicker.getUniqueId())) {
                    Chat.sendMessage(clicker, "Sää asetttu sateiseksi!");
                    player.setPlayerWeather(Weathers.RAINY);
                } else {
                    Chat.sendMessage(player, "Tämä toiminto vaatii arvon §aPremium§6+§7!");
                }

            }
        });

        gui.addButton(new Button(1, 22, ItemUtil.makeItem(Material.BARRIER, 1, "§6Tyhjennä", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§cKlikkaa resetoidaksesi",
                "§csään ja ajan!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.resetPlayerTime();
                clicker.resetPlayerWeather();
                Chat.sendMessage(player, "Sää ja aika tyhjennetty!");
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.BOOK, 1, "§6§lAika: §7Päivä")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.sendMessage(player, "Aika asetettu päiväksi!");
                clicker.setPlayerTime(Times.DAY, false);
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.BOOK, 1, "§6§lAIKA: §7Yö")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.sendMessage(player, "Aika asetettu yöksi!");
                clicker.setPlayerTime(Times.NIGHT, false);
            }
        });

        gui.addButton(new Button(1, 27, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.panel(player);
            }
        });

        gui.open(player);

    }

}
