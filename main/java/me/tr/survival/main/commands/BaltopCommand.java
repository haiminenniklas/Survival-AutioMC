package me.tr.survival.main.commands;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.managers.Profile;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.callback.TypedCallback;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BaltopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(command.getLabel().equalsIgnoreCase("baltop")) {

                Chat.sendMessage(player, "Haetaan rahatietoja, odota hetki...");
                openGui(player);
            }

        }

        return true;
    }

    private static void loadGui(final Player player, TypedCallback<Gui> cb) {
        //final long start = System.currentTimeMillis();
        //System.out.println("[/Baltop] Opening BalanceTop GUI... ");
        // System.out.println("[/Baltop] Getting balance Map... ");
        Balance.getBalances((rawBalanceMap) -> {
            // System.out.println("[/Baltop] Sorting balance Map... ");
            Util.sortByValue(rawBalanceMap, (balanceMap) -> {
                if (balanceMap.size() >= 1) {

                    final Gui gui = new Gui("TOP 10 - Rikkaimmat", 54);

                    final int[] yellowGlassSlots = new int[]{20, 29, 38, 22, 31, 40, 24, 33, 42};
                    final int[] playerSlots = new int[]{19, 28, 37, 21, 30, 39, 23, 32, 41, 25, 34, 43};

                    int i = 0;
                    //System.out.println("[/Baltop] Starting looping Map Entries... ");
                    //final long loopStart = System.currentTimeMillis();


                    for (final Map.Entry<UUID, Double> e : balanceMap.entrySet()) {
                        if (i >= 12) break;
                        if(e == null) continue;
                        if(e.getKey() == null) continue;
                        final OfflinePlayer target = Bukkit.getOfflinePlayer(e.getKey());
                        double balance = e.getValue();
                        int placement = i + 1;
                        int slot = playerSlots[i];
                        if (player.getUniqueId().equals(target.getUniqueId())) {

                            ItemUtil.makeSkullItem(target, 1, "§e#" + placement + " §6§lSinä", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Rahatilanne: §a" + Util.formatDecimals(balance) + "€",
                                    " ",
                                    " §aKlikkaa nähdäksesi lisätietoja",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ), (item) ->
                                gui.addButton(new Button(1, slot, item) {
                                    @Override
                                    public void onClick(Player clicker, ClickType clickType) {
                                        gui.close(clicker);
                                        Profile.openProfile(clicker, clicker.getUniqueId());
                                    }
                                }));

                        } else {
                            if(target.getName() == null) continue;
                            ItemUtil.makeSkullItem(target, 1, "§e#" + placement + " §7" + target.getName(), Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Rahatilanne: §a" + Util.formatDecimals(balance) + "€",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ), (item) -> gui.addItem(1, item, slot));
                        }
                        i += 1;
                    }
                    //System.out.println("[/Baltop] Map Entry Loop took " + (System.currentTimeMillis() - loopStart) + "ms");

                    gui.addButton(new Button(1, 0, ItemUtil.makeItem(Material.BOOK, 1, "§2Mikä tämä on?", Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            " §7Tässä valikossa näkyy",
                            " §7Survival-palvelimen",
                            " §erikkaimmat pelaajat!",
                            " ",
                            " §7Lista päivittyy muutaman",
                            " §7minuutin välein, joten",
                            " §7listalla olevat rahamäärät",
                            " §7voivat olla hieman väärässä!",
                            " ",
                            " §7Lisätietoa: §a/apua raha",
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            gui.close(clicker);
                            Bukkit.dispatchCommand(clicker, "apua raha");
                        }
                    });

                    gui.addButton(new Button(1, 4, ItemUtil.makeSkullItem(player, 1, "§a" + player.getName(), Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            " §7Rahatilanteesi: §e" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€",
                            " §7Sijoituksesi: §e#" + (new ArrayList<>(balanceMap.keySet()).indexOf(player.getUniqueId()) + 1),
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            gui.close(clicker);
                            Profile.openProfile(clicker, clicker.getUniqueId());
                        }
                    });

                    //System.out.println("[/Baltop] Adding Glass Panes... ");
                    for (int slot : yellowGlassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE), slot); }
                    for (int j = 0; j < 54; j++) {
                        if (gui.getItem(j) != null) continue;
                        if (gui.getButton(j) != null) continue;
                        gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), j);
                    }

                    cb.execute(gui);

                } else {
                    Sorsa.task(() -> {

                        Gui gui = new Gui("TOP 10 - Rikkaimmat", 27);
                        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§cEi tietoja", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §7Pelaajien rahatietoja ei",
                                " §7löydetty... Yritä",
                                " §7myöhemmin uudestaan!",
                                " ",
                                " §aKlikkaa uudelleenladataksesi!",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        ))) {
                            @Override
                            public void onClick(Player clicker, ClickType clickType) {
                                gui.close(clicker);
                                openGui(clicker);
                            }
                        });

                        int[] glass = new int[] { 12,14  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.RED_STAINED_GLASS_PANE), slot); }
                        for(int i = 0; i < 27; i++) {
                            if(gui.getItem(i) != null) continue;
                            if(gui.getButton(i) != null) continue;
                            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                        }

                        cb.execute(gui);

                    });

                }
            });
        });
    }

    public static void openGui(final Player player) {

        final Gui loadGui = new Gui("Ladataan...", 27);

        loadGui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§eLadataan...", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Ladataan pelaajien rahatietoja",
                " §7tässä saattaa mennä hetki...",
                " ",
                " §cKlikkaa peruuttaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                loadGui.close(clicker);
                Chat.sendMessage(clicker, "Etsintä peruutettiin!");
            }
        });

        int[] glass = new int[] { 12,14  };
        for(int slot : glass) { loadGui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE), slot); }
        for(int i = 0; i < 27; i++) {
            if(loadGui.getItem(i) != null) continue;
            if(loadGui.getButton(i) != null) continue;
            loadGui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }

        loadGui.open(player);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                InventoryView current = player.getOpenInventory();
                if(current.getTitle().contains("Ladataan...")) {
                    loadGui(player, (gui) ->
                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), (runnable) -> {
                            loadGui.close(player);
                            // System.out.println("[/Baltop] Opening Gui... ");
                            //final long invStart = System.currentTimeMillis();
                            gui.open(player);
                            player.updateInventory();
                            runnable.cancel();
                            //System.out.println("[/Baltop] Gui opening took " + (System.currentTimeMillis() - invStart) + "ms. ");
                            //System.out.println("[/Baltop] It took in total " + (System.currentTimeMillis() - start) + "ms. ");
                        }, 5));
                }
            }
        }.runTaskLater(Main.getInstance(), 5);
    }
}
