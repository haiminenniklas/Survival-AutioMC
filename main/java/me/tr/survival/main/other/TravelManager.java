package me.tr.survival.main.other;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Profile;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class TravelManager implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("matkusta")) {
                gui(player);
            }

        }

        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        if(e.getItem() != null) {

            ItemStack item = e.getItem();
            if(item.isSimilar(getPearlItem())) {
                e.setCancelled(true);
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Et voi heittää helmeä pois!");
            }

        }

    }

    public static void gui(Player player) {

        Gui.openGui(player, "Matkusta", 27, (gui) -> {

            gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.NETHERRACK, 1, "§cNether", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa matkustaaksesi",
                    " §cNetheriin§7! ",
                    " ",
                    " §7Hinta: §a§lILMAINEN",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {

                    clicker.closeInventory();
                    nether(clicker);

                }
            });

            gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.END_STONE, 1, "§5End", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa matkustaaksesi",
                    " §5Endiin§7! ",
                    " ",
                    " §7Hinta: §6§l1 Helmi",
                    " §7Sinun helmesi: §6" + getPearls(player),
                    " ",
                    " §7§oTietoa Helmistä: ",
                    " §a/apua matkustaminen§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {

                    clicker.closeInventory();
                    end(clicker);

                }
            });

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(player);
                    Profile.openProfile(player, clicker.getUniqueId());
                }
            });


        });

    }

    public static void end(Player player) {

        if(hasPearls(player)) {
            removePearl(player);
            Chat.sendMessage(player, Chat.Prefix.DEFAULT, "Sinua viedään §5Endiin§7...");

            if(Autio.getEndWorld() != null) {
                player.teleportAsync(Autio.getEndWorld().getSpawnLocation());
            } else {
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Matkustaminen epäonnistui...");
            }
        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tarvitset §6§lHelmiä§7, jotta voit matkustaa §5Endiin§7!");
        }


    }

    public static void nether(Player player) {

        if(Autio.getNetherWorld() != null) {
            player.teleportAsync(Autio.getNetherWorld().getSpawnLocation());
        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Matkustaminen epäonnistui...");
        }

    }

    public static void removePearl(Player player) {
        for(ItemStack item : player.getInventory().getContents()) {

            if(item == null) continue;
            if(item.getType() != Material.ENDER_PEARL) continue;

            if(item.isSimilar(getPearlItem())) {
                item.setAmount(item.getAmount() - 1);
                player.updateInventory();
                break;
            }

        }
    }

    public static boolean hasPearls(Player player) {
        return player.getInventory().containsAtLeast(getPearlItem(), 1);
    }

    public static int getPearls(Player player) {

        int amount = 0;
        for(ItemStack item : player.getInventory().getContents()) {

            if(item == null) continue;
            if(item.getType() != Material.ENDER_PEARL) continue;

            if(item.isSimilar(getPearlItem())) {
                amount += item.getAmount();
            }

        }
        return amount;

    }

    public static ItemStack getPearlItem() {
        return Util.makeEnchanted(ItemUtil.makeItem(Material.ENDER_PEARL, 1, "§6§lHelmi", Arrays.asList(
                "§7Tämä esine toimii avaimena",
                "§5Endiin§7! Käytä tätä spawnilla",
                "§7tai komennolla §a/matkusta§7!"
        )));
    }

}
