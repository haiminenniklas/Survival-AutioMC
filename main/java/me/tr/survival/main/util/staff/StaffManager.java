package me.tr.survival.main.util.staff;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Profile;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import me.tr.survival.main.util.teleport.TeleportManager;
import me.tr.survival.main.util.teleport.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffManager implements Listener, CommandExecutor {

    private static Map<UUID, Map<Material, Integer>> blocksPerHour = new HashMap<>();

    public static int getBlockMinedPerHour(UUID uuid, Material mat) {

        if(blocksPerHour.containsKey(uuid)) {

            Map<Material, Integer> blockData = blocksPerHour.get(uuid);
            if(blockData.containsKey(mat)) {

                int minedTotal = blockData.get(mat);
                long hoursPlayed = ((System.currentTimeMillis() - Util.getWhenLogged(uuid)) / 1000 / 60 / 60) + 1;
                System.out.println("BPH -> " + (System.currentTimeMillis() - Util.getWhenLogged(uuid)) + " / " + hoursPlayed + " / " + minedTotal);

                return minedTotal / (int) hoursPlayed;

            }

        }

        return 0;

    }

    public static void panel(Player player) {

        Gui gui = new Gui("Ylläpito", 36);

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAPER, 1, "§bChat-asetukset", Arrays.asList(
                "§7§m--------------------",
                " §7Klikkaa avataksesi",
                " §bChat§7-asetukset!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.panel(clicker);
            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§cPoistu §7(Fake)", Arrays.asList(
                "§7§m--------------------",
                " §7Klikkaa piiloutuaksesi",
                " §7muilta pelaajilta",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Bukkit.dispatchCommand(player, "leave");
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.PAPER, 1, "§aLiity §7(Fake)", Arrays.asList(
                "§7§m--------------------",
                " §7Klikkaa itsesi näkyväksi",
                " §7muille pelaajille!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Bukkit.dispatchCommand(player, "join");
            }
        });

        gui.open(player);

    }

    public static void panel(Player player, Player target) {
        Gui gui = new Gui("Valvonta " + target.getName(), 27);

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.IRON_PICKAXE, 1, "§6X-RAY-tilastot", Arrays.asList(
                "§7§m--------------------",
                " §7Oreja per tunti:",
                " ",
                " §7Timantti: §b" + getBlockMinedPerHour(target.getUniqueId(), Material.DIAMOND_ORE),
                " §7Emerald: §a" + getBlockMinedPerHour(target.getUniqueId(), Material.EMERALD_ORE),
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.OAK_SIGN, 1, "§6Teleporttaa", Arrays.asList(
                "§7§m--------------------",
                " §7Klikkaa teleporttaaksesi",
                " §7pelaajan §6" + target.getName(),
                " §7luo!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                TeleportRequest request = new TeleportRequest(player, target, TeleportManager.Teleport.FORCE);
                request.ask();
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.BOOK, 1, "§6Pelaajan profiili", Arrays.asList(
                "§7§m--------------------",
                " §7Klikkaa avataksesi",
                " §7pelaajan §6" + target.getName(),
                " §7profiilin!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Profile.openProfile(player, target.getUniqueId());
            }
        });

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.PAPER, 1, "§cYlläpitopaneeli", Arrays.asList(
                "§7§m--------------------",
                " §7Klikkaa avataksesi",
                " §cylläpitopaneelin§7!",
                "§7§m--------------------"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                StaffManager.panel(player);
            }
        });

        gui.open(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if(Ranks.isStaff(uuid)) {
                if(args.length < 1) {
                    StaffManager.panel(player);
                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                        return true;
                    }
                    StaffManager.panel(player, target);

                }
            }

        }

        return true;
    }

    @EventHandler
    public void onMine(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();
        UUID uuid = player.getUniqueId();

        if(Util.isMineralOre(block)) {

            if(!blocksPerHour.containsKey(uuid)) {
                Map<Material, Integer> map = new HashMap<>();
                map.put(block.getType(), 1);
                blocksPerHour.put(uuid, map);
            } else {

                Map<Material, Integer> map = blocksPerHour.get(uuid);
                if(map.containsKey(block.getType())) {

                    int current = map.get(block.getType());
                    map.put(block.getType(), current + 1);

                    int minedPerHour = getBlockMinedPerHour(uuid, block.getType());

                    if(block.getType() == Material.DIAMOND_ORE) {

                        if(minedPerHour >= 15 && minedPerHour % 5 == 0) {

                            Util.broadcastStaff("§6§lXRAY §7» Pelaajan §6" + player.getName() + " §7BPH §o(blockit per tunti) §btimanteille §7on §6" + minedPerHour + "§7!");

                        }

                    } else if(block.getType() == Material.EMERALD_ORE) {
                        if(minedPerHour >= 5 && minedPerHour % 5 == 0) {

                            Util.broadcastStaff("§6§lXRAY §7» Pelaajan §6" + player.getName() + " §7BPH §o(blockit per tunti) §aemeraldeille §7on §6" + minedPerHour + "§7!");

                        }
                    }


                } else {
                    map.put(block.getType(), 1);
                }

            }


        }


    }


}
