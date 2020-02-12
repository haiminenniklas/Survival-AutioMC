package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class MoneyManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("valuutta")) {
                main(player);
            }

        }

        return true;
    }

    public static void main(Player player) {

        // 10 13 16

        Gui.openGui(player, "Finanssivalvonta", 27, (gui) -> {

            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.DIAMOND, 1, "§bVaihda rahaa", Arrays.asList(
                    "§7§m--------------------",
                    " §7Onko sinulla liian paljon",
                    " §arahaa§7, mitä et voi käyttää?",
                    " §7Voit vaihtaa rahasi §bkristalleihin§7,",
                    " §7joilla voit ostaa §derikoisuuksia",
                    " §7palvelimella.",
                    "",
                    " §6Klikkaa päästäksesi",
                    " §6vaihtamaan rahaa!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    moneyExchange(clicker);
                }
            });

            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§aRahatilanne", Arrays.asList(
                    "§7§m--------------------",
                    " §7Saldo: §a" + Balance.get(player.getUniqueId()) + "€",
                    " §7Kristallit: §b" + Crystals.get(player.getUniqueId()),
                    " ",
                    " §7Lisätietoa valuutoista",
                    " §6/apua valuutta",
                    "§7§m--------------------"
            )), 13);

            gui.addButton(new Button(1, 16, ItemUtil.makeItem(Material.PAPER,1, "Shekki", Arrays.asList(
                    "§7§m--------------------",
                    " §7Haluatko pitää siirtää tai",
                    " §7säilyttää rahaa hieman",
                    " §eperinteisemmällä §7tavalla?",
                    " §7Voit kirjoittaa shekkejä,",
                    " §7joihin voit tallettaa",
                    " §7haluamasi rahamäärän. Myöhemmin",
                    " §7kun klikkaa tätä shekkiä,",
                    " §7saat kyseisen rahamäärän",
                    " §atilillesi!",
                    " ",
                    " §6Klikkaa kirjoittaaksesi",
                    " §6shekkejä!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    cheques(clicker);
                }
            });

        });

    }

    public static void moneyExchange(Player player) {

        Gui.openGui(player, "Vaihda valuuttaa", 27, (gui) -> {

            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.DIAMOND, 1, "§b1 kristalli", Arrays.asList(
                    "§7§m--------------------",
                    " §7Vaadittu raha: §a" + getPriceForCrystals(1) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    changeMoneyToCrystals(clicker, 1);
                }
            });

            gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.DIAMOND, 1, "§b10 kristallia", Arrays.asList(
                    "§7§m--------------------",
                    " §7Vaadittu raha: §a" + getPriceForCrystals(10) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    changeMoneyToCrystals(clicker, 10);
                }
            });

            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.DIAMOND, 1, "§b50 kristallia", Arrays.asList(
                    "§7§m--------------------",
                    " §7Vaadittu raha: §a" + getPriceForCrystals(50) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    changeMoneyToCrystals(clicker, 50);
                }
            });

            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.DIAMOND, 1, "§b100 kristallia", Arrays.asList(
                    "§7§m--------------------",
                    " §7Vaadittu raha: §a" + getPriceForCrystals(100) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    changeMoneyToCrystals(clicker, 100);
                }
            });

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.DIAMOND, 1, "§b1250 kristallia", Arrays.asList(
                    "§7§m--------------------",
                    " §7Vaadittu raha: §a" + getPriceForCrystals(250) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    changeMoneyToCrystals(clicker, 250);
                }
            });

            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Muu määrä?", Arrays.asList(
                    "§7§m--------------------",
                    " §7Jos haluat muuntaa jonkin",
                    " §7toisen määrän, se onnistuu",
                    " §7komennolla",
                    " §6/vaihda <määrä kristalleja>§7!",
                    "§7§m--------------------"
            )), 16);

        });

    }

    public static void cheques(Player player) {

        Gui.openGui(player, "Kirjoita shekkejä", 27, (gui) -> {
            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.PAPER, 1, "§a50€", Arrays.asList(
                    "§7§m--------------------",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 50);
                }
            });

            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.PAPER, 1, "§a100€", Arrays.asList(
                    "§7§m--------------------",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 100);
                }
            });

            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.PAPER, 1, "§a250€", Arrays.asList(
                    "§7§m--------------------",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 250);
                }
            });

            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.PAPER, 1, "§a1000€", Arrays.asList(
                    "§7§m--------------------",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 1000);

                }
            });

            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.PAPER, 1, "§a2500€", Arrays.asList(
                    "§7§m--------------------",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m--------------------"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 2500);
                }
            });

            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Muu määrä?", Arrays.asList(
                    "§7§m--------------------",
                    " §7Jos haluat kirjoittaa jonkin",
                    " §7toisen määrän, se onnistuu",
                    " §7komennolla",
                    " §6/shekki <haluttu rahamäärä>§7!",
                    "§7§m--------------------"
            )), 16);
        });

    }

    public static void changeMoneyToCrystals(Player player, int crystalsWanted) {

        int price = 5000 * crystalsWanted;

        if(Balance.canRemove(player.getUniqueId(), price)) {

            Balance.remove(player.getUniqueId(), price);
            Crystals.add(player.getUniqueId(), crystalsWanted);

        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän...");
        }


    }

    public static void writeCheque(Player player, int amount) {

    }

    public static int getPriceForCrystals(int crystalsWanted) {
        return 15000 * crystalsWanted;
    }

}
