package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.data.Homes;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.Arrays;

public class MoneyManager implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("valuutta")) {
                main(player);
            } else if(command.getLabel().equalsIgnoreCase("vaihda")) {

                if(args.length < 1) {
                    Chat.sendMessage(player, "Käytä §6/vaihda <halutut kristallit>");
                    return true;
                } else {

                    int value;
                    try {
                        value = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ex) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytä oikeita numeroita!");
                        return true;
                    }

                    if(value < 1) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei negatiivisia numeroita, tai nolla!");
                        return true;
                    }

                    if(Balance.canRemove(player.getUniqueId(), getPriceForCrystals(value))) {

                        changeMoneyToCrystals(player, value);

                    } else {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän! Yhden kristallin arvo on §a" + getPriceForCrystals(1) + "€§7!");
                    }

                }

            } else if(command.getLabel().equalsIgnoreCase("shekki")) {

                if(args.length < 1) {
                    Chat.sendMessage(player, "Käytä §6/shekki <haluttu määrä>");
                    return true;
                } else {

                    int value;
                    try {
                        value = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ex) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytä oikeita numeroita!");
                        return true;
                    }

                    if(value < 1) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei negatiivisia numeroita, tai nolla!");
                        return true;
                    }

                    if(Balance.canRemove(player.getUniqueId(), value)) {

                        writeCheque(player, value);

                    } else {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän!");
                    }

                }

            }

        }

        return true;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        if(e.getItem() != null) {

            if(e.getItem().hasItemMeta()) {
                confirmChequeWithdrawal(player, e.getItem());
            }

        }

    }
    public static void main(Player player) {

        // 10 13 16

        Gui.openGui(player, "Finanssivalvonta", 27, (gui) -> {

            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.DIAMOND, 1, "§bVaihda rahaa", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Onko sinulla liian paljon",
                    " §arahaa§7, mitä et voi käyttää?",
                    " §7Voit vaihtaa rahasi §bkristalleihin§7,",
                    " §7joilla voit ostaa §derikoisuuksia",
                    " §7palvelimella.",
                    "",
                    " §6Klikkaa päästäksesi",
                    " §6vaihtamaan rahaa!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    moneyExchange(clicker);
                }
            });

            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§aRahatilanne", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Saldo: §a" + Balance.get(player.getUniqueId()) + "€",
                    " §7Kristallit: §b" + Crystals.get(player.getUniqueId()),
                    " ",
                    " §7Lisätietoa valuutoista",
                    " §6/apua valuutta",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 13);

            gui.addButton(new Button(1, 16, ItemUtil.makeItem(Material.PAPER,1, "Shekki", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
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
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
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
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Vaadittu raha: §a" + getMoneyString(player, 1) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    changeMoneyToCrystals(clicker, 1);
                }
            });

            gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.DIAMOND, 1, "§b10 kristallia", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Vaadittu raha: §a" + getMoneyString(player, 10) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    changeMoneyToCrystals(clicker, 10);
                }
            });

            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.DIAMOND, 1, "§b50 kristallia", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Vaadittu raha: §a" + getMoneyString(player, 50) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    changeMoneyToCrystals(clicker, 50);
                }
            });

            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.DIAMOND, 1, "§b100 kristallia", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Vaadittu raha: §a" + getMoneyString(player, 100) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    changeMoneyToCrystals(clicker, 100);
                }
            });

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.DIAMOND, 1, "§b1250 kristallia", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Vaadittu raha: §a" + getMoneyString(player, 250) + "€",
                    " ",
                    " §6Klikkaa vaihtaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    changeMoneyToCrystals(clicker, 250);
                }
            });

            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Muu määrä?", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Jos haluat muuntaa jonkin",
                    " §7toisen määrän, se onnistuu",
                    " §7komennolla",
                    " §6/vaihda <määrä kristalleja>§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 16);

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    main(clicker);
                }
            });

        });

    }

    public static void cheques(Player player) {

        Gui.openGui(player, "Kirjoita shekkejä", 27, (gui) -> {
            gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.PAPER, 1, "§a50€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §6Klikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 50);
                }
            });

            gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAPER, 1, "§a100€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §6Klikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 100);
                }
            });

            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.PAPER, 1, "§a250€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §6Klikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 250);
                }
            });

            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§a1000€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §6Klikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 1000);

                }
            });

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.PAPER, 1, "§a2500€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §6Klikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 2500);
                }
            });

            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Muu määrä?", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Jos haluat kirjoittaa jonkin",
                    " §7toisen määrän, se onnistuu",
                    " §7komennolla",
                    " §6/shekki <haluttu rahamäärä>§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 16);


            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    main(clicker);
                }
            });

        });

    }

    private static String getMoneyString(Player player, int crystalsWanted) {

        int price = getPriceForCrystals(crystalsWanted);

        return Balance.canRemove(player.getUniqueId(), price) ? "§a" + price : "§c" + price;

    }

    public static void changeMoneyToCrystals(Player player, int crystalsWanted) {

        int price = 15000 * crystalsWanted;

        if(Balance.canRemove(player.getUniqueId(), price)) {

            Balance.remove(player.getUniqueId(), price);
            Crystals.add(player.getUniqueId(), crystalsWanted);

        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän...");
        }


    }

    public static void writeCheque(Player player, int amount) {

        if(!Balance.canRemove(player.getUniqueId(), amount)) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän...");
            return;
        }

        ItemStack item = ItemUtil.makeItem(Material.PAPER, 1, "§a§lShekki", Arrays.asList(
                " §7Tämä shekki sisältää ",
                " §a" + amount + "€§7!",
                " §7Kun klikkaat tätä itemiä",
                " §7saat pankkitilillesi rahat!",
                " §7Voit antaa tämän myös kaverillesi",
                " §7pienenä §dlahjoituksena§7!"
        ));

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "cheque-amount");
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getCustomTagContainer().setCustomTag(key, ItemTagType.INTEGER, amount);
        item.setItemMeta(itemMeta);

        player.getInventory().addItem(Util.makeEnchanted(item));
        Chat.sendMessage(player, "Kirjoitit shekin, joka sisältää §a" + amount + "€§7!");

    }

    public static void withdrawCheque(Player player, ItemStack cheque) {

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "cheque-amount");
        ItemMeta itemMeta = cheque.getItemMeta();
        CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();

        if(tagContainer.hasCustomTag(key , ItemTagType.INTEGER)) {
            int foundValue = tagContainer.getCustomTag(key, ItemTagType.INTEGER);
            Balance.add(player.getUniqueId(), foundValue);


            cheque.setAmount(cheque.getAmount() - 1);
            if(cheque.getAmount() < 1) player.getInventory().remove(cheque);

            player.updateInventory();

            Chat.sendMessage(player, "Nostit shekin, joka sisälsi §a" + foundValue + "€§7! Shekkejä voit kirjoittaa komennolla §6/valuutta§7!");

        }

    }

    public static void confirmChequeWithdrawal(Player player, ItemStack cheque) {

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "cheque-amount");
        ItemMeta itemMeta = cheque.getItemMeta();
        CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();

        // Confirm that the item is an actual cheque


        if (tagContainer.hasCustomTag(key, ItemTagType.INTEGER)) {
            int foundValue = tagContainer.getCustomTag(key, ItemTagType.INTEGER);

            Gui.openGui(player, "Varmista Shekin nosto (" + foundValue + "€)", 27, (gui) -> {

                gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahivsta", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Klikkaa vahvistaaksesi noston!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {

                        withdrawCheque(player, cheque);
                        gui.close(player);

                    }
                });

                gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§7 Klikkaa peruuttaaksesi noston!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {

                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Shekin nostaminen peruutettiin");

                    }
                });

            });

        }
    }

    public static int getPriceForCrystals(int crystalsWanted) {
        return 15000 * crystalsWanted;
    }

}
