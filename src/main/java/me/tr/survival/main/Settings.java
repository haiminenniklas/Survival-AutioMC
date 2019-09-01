package me.tr.survival.main;

import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class Settings {

    public static void panel(Player player) {

        Gui gui = new Gui("Asetukset", 27);

        gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.OAK_SIGN, 1, "§cScoreboard", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: §c§lEI PÄÄLLÄ",
                " ",
                "§7§oKun tämä asetus on päällä, sivulle tulee näkyville",
                "§7§oikkuna, jossa on näkyvillä hyödyllistä informaatiota",
                "",
                "§cKlikkaa vaihtaaksesi asetusta!",
                "§7§m--------------------"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.sendMessage("§cEi vielä toimi");
            }
        });

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.WRITABLE_BOOK, 1, "§cYksityinen tila", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: §c§lEI PÄÄLLÄ",
                " ",
                "§7§oKun tämä asetus on päällä, et näe enää yksityisviestejä",
                "§7§omuilta pelaajilta",
                "",
                "§cKlikkaa vaihtaaksesi asetusta!",
                "§7§m--------------------"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.sendMessage("§cEi vielä toimi");
            }
        });

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.PAPER, 1, "§cChat", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: §c§lEI PÄÄLLÄ",
                " ",
                "§7§oKun tämä asetus on päällä, et näe enää chat-viestejä",
                "§7§omuilta pelaajilta",
                "",
                "§cKlikkaa vaihtaaksesi asetusta!",
                "§7§m--------------------"
        ))) {

            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                clicker.sendMessage("§cEi vielä toimi");
            }
        });

        gui.open(player);

    }

}
