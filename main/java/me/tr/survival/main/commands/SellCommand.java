package me.tr.survival.main.commands;

import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class SellCommand implements CommandExecutor {

    private boolean ENABLED = true;

    private EnumMap<Material, Double> prices;

    public SellCommand() {
        this.prices = new EnumMap<>(Material.class);

        // Prices...

        this.prices.put(Material.DIAMOND, 35d);
        this.prices.put(Material.IRON_INGOT, 1d);
        this.prices.put(Material.EMERALD, 2d);
        this.prices.put(Material.GOLD_INGOT, 8d);
        this.prices.put(Material.COAL, 0.1d);
        this.prices.put(Material.PRISMARINE_SHARD, 0.25d);
        this.prices.put(Material.PRISMARINE_CRYSTALS, 0.25d);

        this.prices.put(Material.ENDER_PEARL, 5d);
        this.prices.put(Material.GHAST_TEAR, 25d);
        this.prices.put(Material.MAGMA_CREAM, 5d);
        this.prices.put(Material.WITHER_SKELETON_SKULL, 600d);
        this.prices.put(Material.NAME_TAG, 80d);
        this.prices.put(Material.GUNPOWDER, 1d);
        this.prices.put(Material.SPONGE, 25d);

        this.prices.put(Material.ROTTEN_FLESH, 0.1d);
        this.prices.put(Material.BONE, 0.1d);
        this.prices.put(Material.SPIDER_EYE, 0.1d);
        this.prices.put(Material.BLAZE_ROD, 1d);
        this.prices.put(Material.GLISTERING_MELON_SLICE, 4d);
        this.prices.put(Material.GOLDEN_CARROT, 4d);
        this.prices.put(Material.STRING, 0.2d);

    }

    public String getDisplayNameForMaterial(Material mat) {

        switch( mat) {

            case DIAMOND:
                return "§bTimantti";
            case IRON_INGOT:
                return "§fRauta";
            case EMERALD:
                return "§aSmaragdi";
            case GOLD_INGOT:
                return "§6Kulta";
            case COAL:
                return "§8Hiili";
            case PRISMARINE_SHARD:
                return "§bPrismariinisirpaleet";
            case PRISMARINE_CRYSTALS:
                return "§bPrismariini";
            case ENDER_PEARL:
                return "§5Äärihelmi";
            case GHAST_TEAR:
                return "§fGhastin kyynel";
            case MAGMA_CREAM:
                return "§6Magmavoide";
            case WITHER_SKELETON_SKULL:
                return "§8Wither-luurangon kallo";
            case NAME_TAG:
                return "§eNimilappu";
            case GUNPOWDER:
                return "§7Ruuti";
            case SPONGE:
                return "§ePesusieni";
            case ROTTEN_FLESH:
                return "§cMädäntynyt liha";
            case BONE:
                return "§fLuu";
            case SPIDER_EYE:
                return "§cHämähäkin silmä";
            case BLAZE_ROD:
                return "§cRoihusauva";
            case GLISTERING_MELON_SLICE:
                return "§eKimalteleva meloonin siivu";
            case GOLDEN_CARROT:
                return "§6Kultainen porkkana";
            case STRING:
                return "§fLanka";
            default:
                return "§2" + Util.firstLetterCapital(mat.name());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(!ENABLED && !player.isOp()) {
                Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä, yritä myöhemmin uudelleen!");
                return true;
            }

            this.openGui(player);
        }

        return true;
    }

    public Map<Material, Double> getPrices() {
        return prices;
    }

    public double getPriceForMaterial(Material mat) {
        return this.prices.getOrDefault(mat, 0d);
    }

    private void openGui(Player player) {

        Gui gui = new Gui("Myy tavaraasi!", 54);

        int itemPos = 10;

        for(Map.Entry<Material, Double> entry : this.prices.entrySet()) {

            final Material mat = entry.getKey();
            final double price = entry.getValue();

            final double stackPrice = price * mat.getMaxStackSize();
            final int playerAmount = Util.getAmountOfMaterialInInventory(player.getInventory(), mat);

            // Side pieces
            if(itemPos == 17 || itemPos == 26 || itemPos == 35) {
                itemPos += 2;
            } else if(itemPos == 44) break;

            final String displayName = this.getDisplayNameForMaterial(mat);

            List<String> lore = new ArrayList<>();

            lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
            lore.add(" §7Klikkaa, jos haluat myydä");
            lore.add(" §7tavaraa " + displayName + "§7!");
            lore.add(" ");
            lore.add(" §7Yksikköhinta: §e" + Util.formatDecimals(price) + "€");
            lore.add(" §7Hinta " + mat.getMaxStackSize() + "kpl: §e" + Util.formatDecimals(stackPrice) + "€");
            lore.add(" ");
            if(playerAmount >= 1) {
                lore.add(" §aKlikkaa myydäksesi!");
            } else {
                lore.add(" §cSinulla ei ole tätä!");
            }
            lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            gui.addButton(new Button(1, itemPos, ItemUtil.makeItem(mat, 1, displayName, lore)) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    if(playerAmount >= 1) {

                        gui.close(clicker);
                        openSellConfirmationMenu(player, mat, price,1);

                    } else {
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    }
                }
            });

            itemPos++;

        }

        gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Haluatko ostaa tavaraa?", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Haluatko mielummin §aostaa",
                " §7tavaraa itsellesi?",
                " §7Kirjoita chattiin §e/sorsastore§7,",
                " §7niin voit siirtyä palvelimemme",
                " §aKauppaan§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 49);

        for(int i = 0; i < 54; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }


        gui.open(player);

    }

    private void openSellConfirmationMenu(Player player, final Material mat, final double price, int amount) {

        Gui gui = new Gui("Myy tavaraa", 45);

        final double stackPrice = price * mat.getMaxStackSize();
        final double finalPrice = (amount * price);

        final int playerAmount = Util.getAmountOfMaterialInInventory(player.getInventory(), mat);

        if(amount > playerAmount) {
            openSellConfirmationMenu(player, mat, price, playerAmount);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        if(amount < 1) {
            openSellConfirmationMenu(player, mat, price, 1);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.PAPER, 1,"§a+1", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Lisää §a+1 §7tavaraa",
                " §7myytäväksi!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openSellConfirmationMenu(player, mat, price, amount + 1);
            }
        });

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAPER, 1,"§a+5", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Lisää §a+5 §7tavaraa",
                " §7myytäväksi!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openSellConfirmationMenu(player, mat, price, amount + 5);
            }
        });

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.PAPER, 1,"§a+10", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Lisää §a+10 §7tavaraa",
                " §7myytäväksi!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openSellConfirmationMenu(player, mat, price, amount + 10);
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.PAPER, 1,"§c-1", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Poista §c-1 §7tavaraa",
                " §7myytäväksi!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openSellConfirmationMenu(player, mat, price, amount - 1);
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.PAPER, 1,"§c-5", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Poista §c-5 §7tavaraa",
                " §7myytäväksi!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openSellConfirmationMenu(player, mat, price, amount - 5);
            }
        });

        gui.addButton(new Button(1, 16, ItemUtil.makeItem(Material.PAPER, 1,"§c-10", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Poista §c-10 §7tavaraa",
                " §7myytäväksi!",
                " ",
                " §aKlikkaa tästä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openSellConfirmationMenu(player, mat, price, amount - 10);
            }
        });


        gui.addButton(new Button(1, 29, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1,"§aHyväksy", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa myydäksesi tavaraa",
                " " +this.getDisplayNameForMaterial(mat),
                " §7" + amount + " kpl hintaan §e" + Util.formatDecimals(finalPrice) + "€§7!",
                " ",
                " §aKlikkaa hyväksyäksesi!" ,
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                clicker.closeInventory();

                clicker.playSound(clicker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                Inventory inv = clicker.getInventory();
                Util.removeItems(inv, mat, amount);
                clicker.updateInventory();

                Balance.add(clicker.getUniqueId(), finalPrice);

                Chat.sendMessage(player, "Myit juuri §a" + amount + "kpl §7tavaraa " + getDisplayNameForMaterial(mat) + " §7hintaan §e" + Util.formatDecimals(finalPrice) + "€§7!");
            }
        });

        gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Info", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Olet myymässä tavaraa:",
                " " + getDisplayNameForMaterial(mat),
                " ",
                " §7Yksikköhinta: §e" + Util.formatDecimals(price) + "€",
                " §7Hinta " + mat.getMaxStackSize() + "kpl: §e" + Util.formatDecimals(stackPrice) + "€",
                " §7Hinta " + amount + " kpl: §a" + Util.formatDecimals(price * amount) + "€",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 31);

        gui.addButton(new Button(1, 33, ItemUtil.makeItem(Material.RED_CONCRETE, 1,"§cPeruuta", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa peruuttaaksesi toiminnon!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openGui(player);
            }
        });

        int[] yellowGlassSlots = { 13 };
        for (int slot : yellowGlassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE), slot); }

        int[] greenGlassSlots = { 30, 32 };
        for (int slot : greenGlassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

        for(int i = 0; i < 45; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }

        gui.open(player);
    }

    private void sell(Material mat, int quantity) {

    }

}
