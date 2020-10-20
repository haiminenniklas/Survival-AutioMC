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
import org.bukkit.*;
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

        if(player.getWorld().getEnvironment() == World.Environment.NETHER) {
            Chat.sendMessage(player, "§7Tämä ei toimi §cNetherissä§7!");
            return;
        }

        if(Sorsa.isInPvPWorld(player) && !Main.getStaffManager().hasStaffMode(player)) {
            Chat.sendMessage(player, "Tämä ei toimi tässä maailmassa. Tee §a/spawn §7päästäksesi tavalliseen maailmaan!");
            return;
        }

        int[] glassSlots = new int[] { 12, 14, 22 };

        Gui.openGui(player, "Matkusta", 36, (gui) -> {


            World netherWorld = Bukkit.getWorld("world_nether3");

            gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.NETHERRACK, 1, "§cNether", Arrays.asList(

                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa matkustaaksesi",
                    " §cNetheriin§7! ",
                    " ",
                    " §7Hinta: §a§lILMAINEN",
                    " ",
                    " §7Tämänhetkiset pelaajat",
                    " §7maailmassa: §a" + netherWorld.getPlayerCount(),
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

            World endWorld = Bukkit.getWorld("world_the_end");

            gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.END_STONE, 1, "§5End", Arrays.asList(

                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa matkustaaksesi",
                    " §5Endiin§7! ",
                    " ",
                    " §7Tämänhetkiset pelaajat",
                    " §7maailmassa: §a" + endWorld.getPlayerCount(),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Main.getEndManager().panel(clicker);
                }
            });

            World pvpWorld = Bukkit.getWorld("warzone");

            gui.addButton(new Button(1, 21, ItemUtil.makeItem(Material.IRON_SWORD, 1, "§6PvP", Arrays.asList(

                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa matkustaaksesi",
                    " §ePvP§7-areenalle! ",
                    " ",
                    " §7Tämänhetkiset pelaajat",
                    " §7maailmassa: §a" + pvpWorld.getPlayerCount(),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    teleportToWarzone(clicker);
                }
            });

            gui.addButton(new Button(1, 23, ItemUtil.makeItem(Material.SADDLE, 1, "§eVälietapit", Arrays.asList(

                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa matkustaaksesi",
                    " §7tärkeisiin paikkoihin ",
                    " §aSurvival§7-palvelimella!",
                    " ",
                    " §aKlikkaa minua!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    openWarpMenu(clicker);
                }
            });


            gui.addButton(new Button(1, 27, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(player);
                    Profile.openProfile(player, clicker.getUniqueId());
                }
            });


            for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.BLUE_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 36; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }
        });
    }

    public void teleportToWarzone(Player player) {
        World pvpWorld = Bukkit.getWorld("warzone");
        if(pvpWorld != null) {
            player.teleport(new Location(pvpWorld, 104.5, 39, 299.5, -90f, -1f));
        } else {
            Chat.sendMessage(player, "Jotain meni vikaan... Yritä myöhemmin uudelleen!");
        }
    }

    private void nether(Player player) {

        if(Sorsa.getNetherWorld() != null) {
            Sorsa.teleportToNether(player);
            Sorsa.logColored("§6[TravelManager] The player '" + player.getName() + "' was teleported to the Nether!");
        }
        else Chat.sendMessage(player, Chat.Prefix.ERROR, "Matkustaminen epäonnistui...");
    }

    public void openWarpMenu(Player player) {

        Gui gui = new Gui("Välietapit", 27);

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.CHEST, 1, "§2SorsaStore", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa minua viedäksesi",
                " §7itsesi palvelimen",
                " §aKauppaan§7!",
                " ",
                " §7Toimii myös §a/myy§7!",
                " ",
                " §aKlikkaa minua!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Main.getEssentials().teleportToStore(clicker);
            }
        });

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.OAK_DOOR, 1, "§2Pelaajakylät", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa minua viedäksesi",
                " §7itsesi aulaan, jossa sijaitsee",
                " §7kaikki palvelimen §akylät!§7!",
                " ",
                " §7Toimii myös §a/kylä lista§7!",
                " ",
                " §aKlikkaa minua!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                World spawnWorld = Sorsa.getSpawn().getWorld();
                clicker.teleport(new Location(spawnWorld, 13.5, 58, -18.5, 42f, 7.5f));
            }
        });

        for(int i = 13; i < 16; i++) {
            gui.addItem(1, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§2Tulossa...", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tähän kohtaan ei olla",
                    " §7keksitty vielä yhtään",
                    " §eVälietappia§7...",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), i);
        }

        int[] glassSlots = new int[] { 10, 16 };
        for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.YELLOW_STAINED_GLASS_PANE), slot); }

        for(int i = 0; i < 27; i++) {
            if(gui.getItem(i) != null) continue;
            if(gui.getButton(i) != null) continue;
            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
        }

        gui.open(player);

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
