package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Profile;
import me.tr.survival.main.Settings;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Times;
import me.tr.survival.main.util.Weathers;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class PlayerWeather {

    public static void panel(Player player) {

        Gui gui = new Gui("Sää ja aika", 27);

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.BOOK, 1, "§6Selkeä", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Aseta sääksi §6Selkeä§7!",
                " ",
                " §c§lHUOM! §7Tämä on vain näkyvä",
                " §7ominaisuus!",
                " ",
                (!Ranks.hasRank(player.getUniqueId(), "premiumplus", "sorsa") && !Ranks.isStaff(player.getUniqueId()) ? "§cVaatii §6§lPremium§f+§7!" : "§aKlikkaa vaihtaaksesi!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                if(Ranks.hasRank(clicker.getUniqueId(), "premiumplus", "sorsa") || Ranks.isStaff(clicker.getUniqueId())) {
                    gui.close(clicker);
                    Chat.sendMessage(clicker, "Sää asetttu selkeäksi!");
                    player.setPlayerWeather(Weathers.SUNNY);
                    panel(player);
                } else {
                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS,1, 1);
                }
            }
        });

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.BOOK, 1, "§9Sateinen", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Aseta sääksi §9Sateiseksi§7!",
                " ",
                " §c§lHUOM! §7Tämä on vain näkyvä",
                " §7ominaisuus!",
                " ",
                (!Ranks.hasRank(player.getUniqueId(), "premiumplus", "sorsa") && !Ranks.isStaff(player.getUniqueId()) ? "§cVaatii §6§lPremium§f+§7!" : "§aKlikkaa vaihtaaksesi!"),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                if(Ranks.hasRank(clicker.getUniqueId(), "premiumplus") || Ranks.isStaff(clicker.getUniqueId())) {
                    gui.close(clicker);
                    Chat.sendMessage(clicker, "Sää asetttu sateiseksi!");
                    player.setPlayerWeather(Weathers.RAINY);
                    panel(player);
                } else {
                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS,1, 1);

                }

            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.BARRIER, 1, "§cTyhjennä")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.resetPlayerTime();
                clicker.resetPlayerWeather();
                Chat.sendMessage(player, "Sää ja aika tyhjennetty!");
                panel(player);
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.BOOK, 1, "§ePäivä", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Aseta ajaksi §ePäivä§7!",
                " ",
                " §c§lHUOM! §7Tämä on vain näkyvä",
                " §7ominaisuus, eikä esim. ala",
                " §7sytyttämään mobeja tuleen!",
                " ",
                " §aKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.sendMessage(player, "Aika asetettu päiväksi!");
                clicker.setPlayerTime(Times.DAY, false);
                panel(player);
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.BOOK, 1, "§9Yö", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Aseta ajaksi §9Yö§7!",
                " ",
                " §c§lHUOM! §7Tämä on vain näkyvä",
                " §7ominaisuus, eikä esim. ala",
                " §7synnyttämään mobeja!",
                " ",
                " §aKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.sendMessage(player, "Aika asetettu yöksi!");
                clicker.setPlayerTime(Times.NIGHT, false);
                panel(player);
            }
        });

        gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Settings.vipPanel(player);
            }
        });

        int[] glassSlots = new int[] {10,16};
        for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.ORANGE_STAINED_GLASS_PANE), slot); }

        for(int i = 0; i < 27; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }

        gui.open(player);

    }

}
