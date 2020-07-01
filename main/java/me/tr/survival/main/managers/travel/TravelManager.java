package me.tr.survival.main.managers.travel;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Profile;
import me.tr.survival.main.managers.RTP;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class TravelManager implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("matkusta")) gui(player);
        }
        return true;
    }

    public void gui(Player player) {

        if(player.getWorld().getName().equals("world_nether")) {
            Chat.sendMessage(player, "§7Tämä ei toimi §cNetherissä§7!");
            return;
        }

        int[] glassSlots = new int[] { 12, 14 };

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
                    gui.close(clicker);
                    nether(clicker);
                }
            });

            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.COMPASS, 1, "§2RTP", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa matkustaaksesi",
                    " §7satunnaiseen paikkaan",
                    " §2maailmassa§7!",
                    " ",
                    " §7Toimii myös §a/rtp§7!",
                    " ",
                    " §7Hinta: §a§lILMAINEN",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    RTP.teleport(clicker);
                }
            });

            gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.END_STONE, 1, "§5End", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa matkustaaksesi",
                    " §5Endiin§7! ",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1,1 );
                    Main.getEndManager().panel(clicker);
                }
            });

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(player);
                    Profile.openProfile(player, clicker.getUniqueId());
                }
            });


            for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.BLUE_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }
        });
    }

    private void nether(Player player) {

        if(Sorsa.getNetherWorld() != null) {
            Sorsa.teleportToNether(player);
            Sorsa.logColored("§6[TravelManager] The player '" + player.getName() + "' was teleported to the Nether!");
        }
        else Chat.sendMessage(player, Chat.Prefix.ERROR, "Matkustaminen epäonnistui...");
    }

    @Deprecated
    public void removePearl(Player player) {
        for(ItemStack item : player.getInventory().getContents()) {

            if(item.getType() != Material.ENDER_PEARL) continue;

            if(item.isSimilar(getPearlItem())) {
                item.setAmount(item.getAmount() - 1);
                player.updateInventory();
                break;
            }

        }
    }

    @Deprecated
    public boolean hasPearls(Player player) { return player.getInventory().containsAtLeast(getPearlItem(), 1); }

    @Deprecated
    public int getPearls(Player player) {

        int amount = 0;
        for(ItemStack item : player.getInventory().getContents()) {
            if(item.getType() != Material.ENDER_PEARL) continue;
            if(item.isSimilar(getPearlItem())) amount += item.getAmount();
        }
        return amount;

    }

    public ItemStack getPearlItem() {
        return Util.makeEnchanted(ItemUtil.makeItem(Material.ENDER_PEARL, 1, "§6§lHelmi", Arrays.asList(
                "§7Tämä esine toimii avaimena",
                "§5Endiin§7! Käytä tätä spawnilla",
                "§7tai komennolla §a/matkusta§7!"
        )));
    }

}
