package me.tr.survival.main;

import me.tr.survival.main.other.PlayerWeather;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class Settings {

    public static void panel(Player player) {

        Gui gui = new Gui("Asetukset", 54);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.OAK_SIGN, 1, "§cScoreboard", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: §c§lEI PÄÄLLÄ",
                " ",
                "§7§oKun tämä asetus on päällä",
                "§7§o, sivulle tulee näkyville",
                "§7§oikkuna, jossa on näkyvillä",
                "§7§o hyödyllistä informaatiota",
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

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.WRITABLE_BOOK, 1, "§cYksityinen tila", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: §c§lEI PÄÄLLÄ",
                " ",
                "§7§oKun tämä asetus on päällä,",
                "§7§o et näe enää yksityisviestejä",
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

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.PAPER, 1, "§cChat", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: §a§lPÄÄLLÄ",
                " ",
                "§7§oKun tämä asetus on pois päältä,",
                "§7§oet näe enää chat-viestejä"  ,
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

        gui.addButton(new Button(1, 22, ItemUtil.makeItem(Material.PAPER, 1, "§cPuun kaato", Arrays.asList(
                "§7§m--------------------",
                "§7Tila: §a§lPÄÄLLÄ",
                " ",
                "§7§oKun tämä asetus on päällä,",
                "§7§opystyt kaatamaan koko puun",
                "§7§okun kaadat ensimmäisen palikan",
                "§7§osiitä!",
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

        gui.addButton(new Button(1, 40, ItemUtil.makeItem(Material.PAPER, 1, "§cSää ja aika", Arrays.asList(
                "§7§m--------------------",
                "§cKlikkaa vaihtaakesi sinun säätä ja aikaa",
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

        gui.addButton(new Button(1, 45, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Profile.openProfile(player, player.getUniqueId());
            }
        });


        gui.open(player);

    }

}
