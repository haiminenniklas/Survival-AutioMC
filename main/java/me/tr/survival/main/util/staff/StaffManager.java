package me.tr.survival.main.util.staff;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.Profile;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.callback.SpigotCallback;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import me.tr.survival.main.util.teleport.TeleportManager;
import me.tr.survival.main.util.teleport.TeleportRequest;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class StaffManager implements Listener, CommandExecutor {

    private static Map<UUID, Map<Material, Integer>> blocksPerHour = new HashMap<>();
    public static List<UUID> hidden = new ArrayList<>();
    private static Map<UUID, Boolean> staffMode = new HashMap<>();
    private static Map<UUID, Location> lastLocation = new HashMap<>();

    public static int getBlockMinedPerHour(UUID uuid, Material mat) {

        if(blocksPerHour.containsKey(uuid)) {

            Map<Material, Integer> blockData = blocksPerHour.get(uuid);
            if(blockData.containsKey(mat)) {

                int minedTotal = blockData.get(mat);
                long hoursPlayed = ((System.currentTimeMillis() - Util.getWhenLogged(uuid)) / 1000 / 60 / 60) + 1;
                //System.out.println("BPH -> " + (System.currentTimeMillis() - Util.getWhenLogged(uuid)) + " / " + hoursPlayed + " / " + minedTotal);

                return minedTotal / (int) hoursPlayed;

            }

        }

        return 0;

    }

    public static void panel(Player player) {

        UUID uuid = player.getUniqueId();

        if(!Ranks.isStaff(player.getUniqueId())) {
            return;
        }

        Gui gui = new Gui("Ylläpitopaneeli", 45);

        int[] glassPanes = new int[] {
                0,1,2,3,4,5,6,7,8,
                9,  11,  13,  15,  17,
                18, 20,  22,  24,  26,
                27, 29,  31,  33,  35,
                36,37,38,39,40,41,42,43,44
        };

        for(int i = 0; i < glassPanes.length; i++) {

            gui.addItem(1, ItemUtil.makeItem(Material.RED_STAINED_GLASS_PANE, 1, ""), glassPanes[i]);

        }

        gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.PAPER, 1, "§bChat-asetukset", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa avataksesi chat-",
                " §7asetukset!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Chat.panel(clicker);
            }
        });

        gui.addItem(1, ItemUtil.makeItem(Material.PLAYER_HEAD, 1, "§6Pelaajan valvonta", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Katso pelaajien tietoja",
                " §7komennolla",
                " §6/staff <pelaaja>§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"

        )),12);

        String isVanished = (hidden.contains(player.getUniqueId())) ? "§cPiiloutunut" : "§aNäkyvillä";

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.ENDER_PEARL, 1, "§9Piiloutuminen", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Piiloudu muilta pelaajilta",
                " §7palvelimella!",
                " ",
                " §7Tila: " + isVanished,
                " ",
                " §eKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                toggleVanish(player);
                panel(player);
            }
        });

        gui.addButton(new Button(1, 16, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eSää", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Muokkaa maailman säätä!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                weatherGui(clicker);
            }
        });

        gui.addButton(new Button(1, 28, ItemUtil.makeItem(Material.CLOCK, 1, "§bAika", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Muokkaa maailman aikaa!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                timeGui(clicker);
            }
        });

        String hasStaffMode = hasStaffMode(player) ? "§aPäällä" : "§cPois päältä";

        gui.addButton(new Button(1, 30, ItemUtil.makeItem(Material.REPEATER, 1, "§eYlläpito-tila", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tämä tila päällä saat",
                " §7ilmoituksia pelaajien",
                " §7käytöksestä, kuten",
                " §atimantti§7- ja §aemeraldi§7-",
                " §7blockien mainauksesta.",
                " ",
                " §7Tila: " + hasStaffMode,
                " ",
                " §aKlikkaa vaihtaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                toggleStaffMode(clicker);
                panel(clicker);
            }
        });

        int[] emptySlots = new int[] { 32,34 };

        for(int i = 0; i < emptySlots.length; i++) {
            gui.addItem(1, ItemUtil.makeItem(Material.EMERALD, 1, "§7Tyhjä..."), emptySlots[i]);
        }

        gui.open(player);

    }

    public static void panel(Player player, Player target) {
        Gui gui = new Gui("Valvonta " + target.getName(), 27);

        gui.addButton(new Button(1, 10, ItemUtil.makeItem(Material.IRON_PICKAXE, 1, "§6X-RAY-tilastot", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Oreja per tunti:",
                " ",
                " §7Timantti: §b" + getBlockMinedPerHour(target.getUniqueId(), Material.DIAMOND_ORE),
                " §7Emerald: §a" + getBlockMinedPerHour(target.getUniqueId(), Material.EMERALD_ORE),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
            }
        });

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.OAK_SIGN, 1, "§6Teleporttaa", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa teleporttaaksesi",
                " §7pelaajan §6" + target.getName(),
                " §7luo!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                staffTeleport(clicker, target);
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.BOOK, 1, "§6Pelaajan profiili", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa avataksesi",
                " §7pelaajan §6" + target.getName(),
                " §7profiilin!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Profile.openProfile(player, target.getUniqueId());
            }
        });

        gui.addButton(new Button(1, 16, ItemUtil.makeItem(Material.CHEST, 1, "§6Pelaajan reppu", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa avataksesi",
                " §7pelaajan §6" + target.getName(),
                " §7repun!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Bukkit.dispatchCommand(clicker, "reppu katso " + target.getName());
            }
        });

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.ARROW, 1, "§cYlläpitopaneeli", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa avataksesi",
                " §cylläpitopaneelin§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                StaffManager.panel(player);
            }
        });

        gui.open(player);
    }

    public static void staffTeleport(Player teleporter, Player target) {
        if(!hidden.contains(teleporter.getUniqueId())) {
            StaffManager.hide(teleporter);
            Chat.sendMessage(teleporter, "Olet nyt piilossa! Tee §a/vanish §7tullaksesi takaisin näkyviin!");
        }
        TeleportRequest request = new TeleportRequest(teleporter, target, TeleportManager.Teleport.FORCE);
        request.ask();
    }

    public static boolean toggleStaffMode(Player player) {

        UUID uuid = player.getUniqueId();

        if(staffMode.containsKey(uuid)) {

            if(!staffMode.get(uuid)) {
                enableStaffMode(player);
                return true;
            }

            disableStaffMode(player);
            return false;

        } else {
            enableStaffMode(player);
            return true;
        }
    }

    public static void enableStaffMode(Player player) {

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        Chat.sendMessage(player, "Ylläpito-tila §a§lPÄÄLLÄ§7!");

        lastLocation.put(player.getUniqueId(), player.getLocation());
        hide(player);
        staffMode.put(player.getUniqueId(), true);
        player.setGameMode(GameMode.SPECTATOR);

    }

    public static void disableStaffMode(Player player) {

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        Chat.sendMessage(player, "Ylläpito-tila §c§lPOIS PÄÄLTÄ§7!");

        if(lastLocation.containsKey(player.getUniqueId())) {
            player.teleport(lastLocation.get(player.getUniqueId()));
        }
        show(player);
        staffMode.put(player.getUniqueId(), false);
        player.setGameMode(GameMode.SURVIVAL);
        Util.heal(player);
        Util.removePotionEffects(player);

    }

    public static boolean hasStaffMode(Player player) {
        return staffMode.containsKey(player.getUniqueId()) && staffMode.get(player.getUniqueId());
    }

    public static boolean toggleVanish(Player player) {

        if(hidden.contains(player.getUniqueId())) {
            show(player);
            return false;

        } else {
            hide(player);
            return true;
        }

    }

    public static void hide(Player player) {
        hidden.add(player.getUniqueId());
        //Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("messages.leave").replaceAll("%player%", player.getName())));


        player.setPlayerListName("§7" + player.getName() + " §8[PIILOSSA]");

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 999999));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 999999));

        for(Player online : Bukkit.getOnlinePlayers()) {

            if(online.getUniqueId().equals(player.getUniqueId())) continue;
            if(Ranks.isStaff(online.getUniqueId())) continue;
            online.hidePlayer(Main.getInstance(), player);

        }

        Chat.sendMessage(player, "Piilouduit pelaajilta!");
    }

    public static void show(Player player) {
        hidden.remove(player.getUniqueId());

        player.setPlayerListName(player.getName());

        //Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("messages.join").replaceAll("%player%", player.getName())));

        for(Player online : Bukkit.getOnlinePlayers()) {

            if(online.getUniqueId().equals(player.getUniqueId())) continue;

            online.showPlayer(Main.getInstance(), player);
        }

        Util.heal(player);

        Chat.sendMessage(player, "Olet nyt esillä kaikille!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if(command.getLabel().equalsIgnoreCase("staff")) {
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
            } else if(command.getLabel().equalsIgnoreCase("staffmode")) {
                if(Ranks.isStaff(uuid)) {
                    StaffManager.toggleStaffMode(player);
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

                if(block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {

                    for(Player staff : Bukkit.getOnlinePlayers()) {
                        if(hasStaffMode(staff)) {
                            String blockName = (block.getType() == Material.DIAMOND_ORE) ? "§btimanttia" : "§aemeraldia";
                            TextComponent msg = new TextComponent(TextComponent.fromLegacyText("§7[§c§l!§7] » Pelaaja §c" + player.getName() + " §7löysi " + blockName + "§7! "));
                            TextComponent tpMsg = new TextComponent();
                            tpMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa teleportataksi pelaajan §c" + player.getName() + " §7luo!")));
                            SpigotCallback.createCommand(tpMsg, (clicker) -> {
                                staffTeleport(staff, player);
                            });
                            msg.addExtra(tpMsg);
                            staff.spigot().sendMessage(msg);
                        }
                    }

                }

                Map<Material, Integer> map = blocksPerHour.get(uuid);
                if(map.containsKey(block.getType())) {

                    int current = map.get(block.getType());
                    map.put(block.getType(), current + 1);

                    int minedPerHour = getBlockMinedPerHour(uuid, block.getType());

                    if(block.getType() == Material.DIAMOND_ORE) {

                        if(minedPerHour >= 15 && minedPerHour % 5 == 0) {

                            Util.broadcastStaff("§7[§c§l!§7] » Pelaajan §c" + player.getName() + " §7BPH §o(blockit per tunti) §btimanteille §7on §c" + minedPerHour + "§7!");

                        }

                    } else if(block.getType() == Material.EMERALD_ORE) {
                        if(minedPerHour >= 5 && minedPerHour % 5 == 0) {

                            Util.broadcastStaff("§7[§c§l!§7] » Pelaajan §c" + player.getName() + " §7BPH §o(blockit per tunti) §aemeraldeille §7on §c" + minedPerHour + "§7!");

                        }
                    }


                } else {
                    map.put(block.getType(), 1);
                }

            }


        }

    }

    public static void weatherGui(Player player) {
        Gui gui = new Gui("Säätila", 27);

        if(!player.isOp()) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Vain §cYlläpitäjille§7!");
            return;
        }

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAPER, 1, "§a§lSelkeä")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                player.getWorld().setThunderDuration(0);
                player.getWorld().setWeatherDuration(0);
                player.getWorld().setThundering(false);
                player.getWorld().setStorm(false);
                Chat.sendMessage(clicker, "Sää vaihdettu selkeäksi!");
            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§9§lSateinen")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                player.getWorld().setStorm(true);
                Chat.sendMessage(clicker, "Sää vaihdettu sateiseksi!");
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.PAPER, 1, "§b§lMyrsykyinen")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                player.getWorld().setThundering(true);
                player.getWorld().setStorm(true);
                Chat.sendMessage(clicker, "Sää vaihdettu myrskyiseksi!");
            }
        });

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.ARROW, 1, "§cYlläpitopaneeli", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa avataksesi",
                " §cylläpitopaneelin§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                StaffManager.panel(player);
            }
        });

        gui.open(player);
    }

    public static void timeGui(Player player) {
        Gui gui = new Gui("Maailman aika", 27);

        if(!player.isOp()) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Vain §cYlläpitäjille§7!");
            return;
        }

        gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.PAPER, 1, "§ePäivä")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                Bukkit.dispatchCommand(clicker, "time day");
            }
        });

        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER, 1, "§eKeskipäivä")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                Bukkit.dispatchCommand(clicker, "time noon");
            }
        });

        gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.PAPER, 1, "§eIlta")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                Bukkit.dispatchCommand(clicker, "time night");
            }
        });

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.ARROW, 1, "§cYlläpitopaneeli", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa avataksesi",
                " §cylläpitopaneelin§7!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                StaffManager.panel(player);
            }
        });

        gui.open(player);
    }

}
