package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.Profile;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.*;

public class BaltopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(command.getLabel().equalsIgnoreCase("baltop")) {

                Chat.sendMessage(player, "Haetaan rahatietoja, odota hetki...");
                openGui(player);
            }

        }

        return true;
    }

    public static void openGui(final Player player) {
        final long start = System.currentTimeMillis();
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

                    final List<ItemStack> heads = new ArrayList<>();

                    for (final Map.Entry<UUID, Double> e : balanceMap.entrySet()) {
                        final OfflinePlayer target = Bukkit.getOfflinePlayer(e.getKey());
                        double balance = e.getValue();
                        int placement = i + 1;
                        int slot = playerSlots[i];
                        if (player.getUniqueId().equals(e.getKey())) {

                            ItemUtil.makeSkullItem(target, 1, "§e#" + placement + " §6§lSinä", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Rahatilanne: §a" + Util.formatDecimals(balance) + "€",
                                    " ",
                                    " §aKlikkaa nähdäksesi lisätietoja",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ), (item) -> {
                                heads.add(item);
                                gui.addButton(new Button(1, slot, item) {
                                    @Override
                                    public void onClick(Player clicker, ClickType clickType) {
                                        gui.close(clicker);
                                        Profile.openProfile(clicker, clicker.getUniqueId());
                                    }
                                });
                            });

                        } else {

                            ItemUtil.makeSkullItem(target, 1, "§e#" + placement + " §7" + target.getName(), Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Rahatilanne: §a" + Util.formatDecimals(balance) + "€",
                                    " ",
                                    " §aKlikkaa nähdäksesi lisätietoja",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ), (item) -> {
                                heads.add(item);
                                gui.addButton(new Button(1, slot, item) {
                                    @Override
                                    public void onClick(Player clicker, ClickType clickType) {
                                        System.out.println(1);
                                        gui.close(clicker);
                                        Profile.openProfile(clicker, e.getKey());
                                    }
                                });
                            });
                        }

                        i++;
                        if (i > 12) break;
                    }
                    //System.out.println("[/Baltop] Map Entry Loop took " + (System.currentTimeMillis() - loopStart) + "ms");

                    gui.addButton(new Button(1, 4, ItemUtil.makeSkullItem(player, 1, "§a" + player.getName(), Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            " §7Rahatilanteesi: §e" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€",
                            " §7Sijoituksesi: §e#" + (new ArrayList<>(balanceMap.keySet()).indexOf(player.getUniqueId()) + 1),
                            " ",
                            " §aKlikkaa nähdäksesi lisätietoja",
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            gui.close(clicker);
                            Profile.openProfile(clicker, clicker.getUniqueId());
                        }
                    });

                    //System.out.println("[/Baltop] Adding Glass Panes... ");
                    for (int slot : yellowGlassSlots) {
                        gui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE), slot);
                    }
                    for (int j = 0; j < 54; j++) {
                        if (gui.getItem(j) != null) continue;
                        if (gui.getButton(j) != null) continue;
                        gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), j);
                    }

                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), (runnable) -> {
                        // System.out.println("[/Baltop] Opening Gui... ");
                        //final long invStart = System.currentTimeMillis();
                        if(heads.size() < 12) {
                            openGui(player);
                        } else {
                            gui.open(player);
                            player.updateInventory();
                        }
                        runnable.cancel();
                        //System.out.println("[/Baltop] Gui opening took " + (System.currentTimeMillis() - invStart) + "ms. ");
                        //System.out.println("[/Baltop] It took in total " + (System.currentTimeMillis() - start) + "ms. ");
                    }, 10);

                } else {
                    Autio.task(() ->
                            Gui.openGui(player, "TOP 10 - Rikkaimmat", 27, (gui) -> {
                                gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§cEi tietoja", Arrays.asList(
                                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                        " §7Pelaajien rahatietoja ei",
                                        " §7löydetty... Yritä",
                                        " §7myöhemmin uudestaan!",
                                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                                )), 13);
                            }));
                }
            });
        });
    }
}
