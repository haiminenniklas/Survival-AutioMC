package me.tr.survival.main.managers;

import me.tr.survival.main.Main;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.database.data.Crystals;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import java.util.HashMap;
import java.util.Map;

public class MoneyManager implements CommandExecutor, Listener {

    public static boolean ENABLED = true;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("valuutta")) {
                main(player);
            } else if(command.getLabel().equalsIgnoreCase("shekki")) {

                if(args.length < 1) {
                    Chat.sendMessage(player, "Käytä §6/shekki <haluttu määrä>");
                    cheques(player);
                    return true;
                } else {

                    if(player.isOp()) {
                        if(args[0].equalsIgnoreCase("help")) {
                            sender.sendMessage("§7/shekki (enable | disable)");
                        } else if(args[0].equalsIgnoreCase("disable")) {
                            ENABLED = false;
                            sender.sendMessage("§7Shekit on nyt §cpois päältä§7!");
                            return true;
                        } else if(args[0].equalsIgnoreCase("enable")) {
                            ENABLED = true;
                            sender.sendMessage("§77Shekit on nyt §apäällä§7!");
                            return true;
                        }
                    }

                    if(!player.isOp() && !ENABLED) {
                        Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä. Yritähän myöhemmin uudelleen.");
                        return true;
                    }

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

            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER,1, "Shekki", Arrays.asList(
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
                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1,1 );
                    cheques(clicker);
                }
            });

            int[] glass = new int[] { 11,12, 14,15 };
            for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.ORANGE_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }

        });

    }

    @Deprecated
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
            gui.addButton(new Button(1, 11, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a50€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää §a50€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 50);
                }
            });

            gui.addButton(new Button(1, 12, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a100€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää §a100€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 100);
                }
            });

            gui.addButton(new Button(1, 13, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a250€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää §a250€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 250);
                }
            });

            gui.addButton(new Button(1, 14, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a1 000€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää",
                    " §a1 000€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 1000);

                }
            });

            gui.addButton(new Button(1, 15, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a2 500€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää",
                    " §a2 500€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 2500);
                }
            });

            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Muu määrä?", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Jos haluat kirjoittaa jonkin",
                    " §7toisen määrän, se onnistuu",
                    " §7komennolla:",
                    " §a/shekki <haluttu rahamäärä>§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 8);


            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    main(clicker);
                }
            });

            int[] glass = new int[] { 10, 16 };
            for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }

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

        if(!player.isOp() && !ENABLED) {
            Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä. Yritähän myöhemmin uudelleen.");
            return;
        }

        if(!Balance.canRemove(player.getUniqueId(), amount)) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän...");
            return;
        }

        Balance.remove(player.getUniqueId(), amount);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        forceWriteCheque(player, amount);
        Chat.sendMessage(player, "Kirjoitit shekin, joka sisältää §a" + amount + "€§7!");

    }

    public static void forceWriteCheque(Player player, int amount) {

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

        HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(Util.makeEnchanted(item));
        for(Map.Entry<Integer, ItemStack> entry : unadded.entrySet()) {
            player.getWorld().dropItemNaturally(player.getLocation(), entry.getValue());
        }
    }

    public static void withdrawCheque(Player player, ItemStack cheque) {

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "cheque-amount");
        ItemMeta itemMeta = cheque.getItemMeta();
        CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();

        if(tagContainer.hasCustomTag(key , ItemTagType.INTEGER)) {
            int foundValue = tagContainer.getCustomTag(key, ItemTagType.INTEGER);
            Balance.add(player.getUniqueId(), foundValue);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
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

                gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Shekki katoaa inventorystäsi",
                        " §7ja tilillesi laitetaan shekin",
                        " §7sisältämä summa (§e" + foundValue + "€§7)",
                        " ",
                        " §aKlikkaa nostaaksesi!",
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
                        " §7Shekki jää inventoryysi",
                        " §7ja mitään ei tapahdu. Pystyt",
                        " §7silti nostamaan shekin rahan",
                        " §7tilillesi myöhemmin!",
                        " ",
                        " §cKlikkaa peruuttaaksesi!",
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
