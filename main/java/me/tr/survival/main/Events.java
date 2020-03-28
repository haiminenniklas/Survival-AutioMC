package me.tr.survival.main;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import me.tr.survival.main.database.PlayerAliases;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.CountdownTimer;
import me.tr.survival.main.other.PlayerGlowManager;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.backpacks.Backpack;
import me.tr.survival.main.other.booster.Boosters;
import me.tr.survival.main.other.events.LevelUpEvent;
import me.tr.survival.main.util.RTP;
import me.tr.survival.main.util.data.Crystals;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import me.tr.survival.main.util.staff.StaffManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Events implements Listener {

    public static final HashMap<UUID, Boolean> adminMode = new HashMap<>();
    public static final HashMap<UUID, Location> lastLocation = new HashMap<>();
    public static final ArrayList<UUID> deathIsland = new ArrayList<>();

    @EventHandler
    public void onException(ServerExceptionEvent e) {

        for(Player player : Bukkit.getOnlinePlayers()) {

            if(player.isOp() && adminMode.containsKey(player.getUniqueId())) {
                if(adminMode.get(player.getUniqueId())) {

                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Tapahtui virhe: §c" + e.getException().getMessage());

                }
            }

        }

    }

    @EventHandler
    public void onLevelUp(LevelUpEvent e){
        Player player = e.getPlayer();

        Util.sendNotification(player, "§a§lTASO! §7Nousit tasolle §a" + e.getLevel() + "§7!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PlayerLoginEvent e) {

        Player player = e.getPlayer();
        if(e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if(player.hasPermission("server.join.full")) {
                e.allow();
            } else {
                e.disallow(PlayerLoginEvent.Result.KICK_FULL, "§7Palvelin täynnä! Mikäli haluat ohittaa tämän, sinun täytyy omistaa vähintään §a§lPremium§7-arvo!");
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncLogin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        PlayerData.loadPlayer(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        FileConfiguration config = Main.getInstance().getConfig();

        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        player.sendMessage(" §7Tervetuloa §a" + player.getName() + " §7meidän Survival-");
        player.sendMessage(" §7palvelimellemme! Alla löydät muutama nappia, joista löydät");
        player.sendMessage( "§7hyödyllistä tietoa, asetuksia ja ominaisuuksia. Alkuun");
        player.sendMessage(" §7pääset myös komennolla §a/apua§7!");
        player.sendMessage(" ");
        player.sendMessage(" §7Mukavia pelihetkiä!");
        player.sendMessage(" ");
        TextComponent mail = new TextComponent(" §d§lPOSTI  ");

        mail.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/posti"));
        mail.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa tarkastellaksesi §dpäivittäisiä toimituksia§7!")));

        TextComponent stats = new TextComponent("§b§lPROFIILI  ");

        stats.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats"));
        stats.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa tarkastellaksesi §bprofiiliasi§7!")));


        TextComponent boosters = new TextComponent("§a§lTEHOSTUKSET");

        boosters.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/boosters"));
        boosters.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa tarkastellaksesi §atehostuksia§7!")));

        stats.addExtra(boosters);
        mail.addExtra(stats);

        player.spigot().sendMessage(mail);

        player.sendMessage(" ");

        player.sendMessage(" §ahttp://www.sorsa.gg");
        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        // Enable glow
        if(!PlayerGlowManager.getGlowColor(uuid).equalsIgnoreCase("default")) {
            PlayerGlowManager.enableGlow(player, ChatColor.valueOf(PlayerGlowManager.getGlowColor(uuid)));
        }

        // Setup backpacks

        Backpack.Level bLvl = Backpack.getLevel(player.getUniqueId());
         if(Ranks.hasRank(player, "premiumplus")) {
             if(bLvl == Backpack.Level.ONE) {
                 Backpack.setLevel(player.getUniqueId(), Backpack.Level.TWO);
             }
        } else if(Ranks.hasRank(player, "kuningas")) {
             if(bLvl != Backpack.Level.THREE) {
                 Backpack.setLevel(player.getUniqueId(), Backpack.Level.THREE);
             }
        }

        e.setJoinMessage(null);

         if(StaffManager.hidden.contains(player.getUniqueId())) {
             Chat.sendMessage(player, "Olet piilossa pelaajilta!");
         }

        /*if(Ranks.isVIP(player.getUniqueId()) && !Ranks.isStaff(player.getUniqueId())) {
            e.setJoinMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            Main.getInstance().getConfig().getString("messages.join").replaceAll("%player%", player.getName())));
        } else {
            e.setJoinMessage(null);
        }*/

        if(!player.hasPlayedBefore()) {
            Autio.teleportToSpawn(e.getPlayer());
        }

        Util.joined.put(player.getUniqueId(), System.currentTimeMillis());

        // DISABLED FOR NOW
        /*player.setPlayerListHeaderFooter(
                ChatColor.translateAlternateColorCodes('&', config.getString("tablist.header")),
                ChatColor.translateAlternateColorCodes('&', config.getString("tablist.footer"))
        ); */

        Main.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {

            //PlayerAliases.load(player);
           // PlayerAliases.add(player, player.getAddress().getHostName());
            PlayerData.loadPlayer(player.getUniqueId());

        }, 20 * 2);

        Autio.updatePlayer(player);

        Autio.everyAsync(3, () -> Autio.sendTablist(player));

   }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {

        Player player = e.getPlayer();

        // Save player's data
        Main.getInstance().getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> PlayerData.savePlayer(player.getUniqueId()));

        if(Ranks.isVIP(player.getUniqueId()) || Ranks.isStaff(player.getUniqueId())) {
            //e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("messages.leave").replaceAll("%player%", player.getName())));
            e.setQuitMessage(null);
        } else {
            e.setQuitMessage(null);
        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        Location loc = e.getTo();

        // RTP Portal
      /*  if(loc.getBlockZ() == -39 && (loc.getBlockX() <= 42 && loc.getBlockX() >= 38) &&
                e.getTo().getWorld().getName().equalsIgnoreCase(Autio.getSpawn().getWorld().getName())
                && loc.getY() <= 136 && loc.getY() >= 133) {
            if(!RTP.teleport(player)) {
                // Bounce player back
                Util.bounceBack(player, e.getFrom(), e.getTo());
            }
        } */

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null) return;
        if(e.getCurrentItem() == null) return;

        if(e.getView().getTitle().contains("§r")) e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();

        if(Gui.getGui(player) != null) {
            Gui gui = Gui.getGui(player);
            if(e.getCurrentItem() != null) {

                for(Button b : gui.getButtons()) {
                    if(b.item.clone().equals(e.getCurrentItem())) {
                        b.onClick(player, e.getClick());
                    }
                }
            }

        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent e) {

        Player player = (Player) e.getPlayer();
        if(e.getView().getTitle().contains("§r") && Gui.getGui(player) != null) {
            Gui.getGui(player).close(player);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {

        Player player = e.getEntity();
/*
        Block firstBlock = player.getLocation().getBlock();
        if(firstBlock.getType() == Material.AIR) {

            Block secondBlock = player.getLocation().add(1, 0, 0).getBlock();
            if(secondBlock.getType() == Material.AIR) {

                BlockData first = Material.CHEST.createBlockData();
                BlockData second = Material.CHEST.createBlockData();

                firstBlock.setBlockData(first, false);
                secondBlock.setBlockData(second, false);

                ((Chest)firstBlock).getInventory().addItem(player.getInventory().getContents());
                ((Chest)firstBlock).getInventory().addItem(player.getInventory().getArmorContents());

                Chat.sendMessage(player, "Sinun kuolinsijainnilla on chesti, jossa on kaikki tavarasi. Ole nopea, ennen kuin joku muu ehtii löytää sen!");

            }

        }
 */
        lastLocation.put(player.getUniqueId(), player.getLocation());

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent e) {

        if(Boosters.isActive(Boosters.Booster.DOUBLE_XP) && e.getEntity().getKiller() != null) {
            e.setDroppedExp(e.getDroppedExp() * 2);
            if(e.getDroppedExp() >= 1) {
                Util.sendNotification(e.getEntity().getKiller(), "§a§lTEHOSTUS §7Tupla XP!", false);
            }

        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {

        Player player = e.getPlayer();
        FileConfiguration config = Main.getInstance().getConfig();

        if(deathIsland.contains(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        if(config.getBoolean("effects.teleport.enabled")) {

            if(e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) return;
            if(e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) return;

            player.getWorld().playSound(e.getFrom(),
                    Sound.valueOf(config.getString("effects.teleport.sound")), 1, 1);
        }

        if(!Boosters.isActive(Boosters.Booster.EXTRA_HEARTS)) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);
        }

        lastLocation.put(player.getUniqueId(), e.getFrom());

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if(Boosters.isActive(Boosters.Booster.NO_HUNGER)) {
            e.setCancelled(true);
        } else {
            e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {

        Player player = e.getPlayer();

        if(!player.hasPermission("deathisland.bypass") && !deathIsland.contains(player.getUniqueId())) {
            player.teleport(Autio.getDeathSpawn());
            Util.heal(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 999, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, true, false));

            player.playSound(player.getLocation(), Sound.MUSIC_DISC_13, 1, 1);

            deathIsland.add(player.getUniqueId());

            CountdownTimer timer = new CountdownTimer(Main.getInstance(), 30, () -> {},
                    () -> {
                        player.sendTitle(new Title("§a§lTAKAISIN", "§7Olet taas elossa!", 15, 20, 15));
                        Autio.teleportToSpawn(player);
                        deathIsland.remove(player.getUniqueId());
                    }, (t) -> {
                player.sendTitle(new Title("§c§lKUOLIT", "§7Pääset §c" + t.getSecondsLeft() + "s §7päästä takaisin!", 15, 20, 15));
            });

// Start scheduling, don't use the "run" method unless you want to skip a second
            timer.scheduleTimer();

        } else {
            Autio.teleportToSpawn(player);
        }


    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();
        UUID uuid = player.getUniqueId();
        Material mat = block.getType();

        PlayerData.add(player.getUniqueId(), "total", 1);

        if(Boosters.isActive(Boosters.Booster.MORE_ORES)) {

            Collection<ItemStack> drops = block.getDrops();

            if(mat == Material.EMERALD_ORE || mat == Material.DIAMOND_ORE || mat == Material.LAPIS_ORE) {
                // Add one of every drop (2x)
                for(ItemStack item : drops) {
                    e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), item.clone());
                }
                Util.sendNotification(player, "§a§lTEHOSTUS §72x oret!");
            }


        }

        if(Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {

            if(mat == Material.IRON_ORE || mat == Material.GOLD_ORE) {

                block.setType(Material.AIR);

                ItemStack item;

                if(mat == Material.IRON_ORE) {
                    item = new ItemStack(Material.IRON_INGOT);
                } else {
                    item = new ItemStack(Material.GOLD_INGOT);
                }

                Util.sendNotification(player, "§a§lTEHOSTUS §7Välittömät oret!");
                e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), item.clone());

            }

        }

        double random = new Random().nextDouble();

        /*

        Chances:

        Emerald: 50% (0.5)
        Diamond: 25% (0.25)
        Gold: 3% (0.03)
        Iron: 2% (0.02)
        Coal 0.5% (0.005)
        Redstone: 7% (0.07)
        Lapis 5% (0.05)

         */

        if(block.getType() == Material.DIAMOND_ORE) {
            PlayerData.add(uuid, "diamond", 1);
            if(random <= 0.25) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.GOLD_ORE && Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {
            PlayerData.add(uuid, "gold", 1);
            if(random <= 0.03) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.IRON_ORE && Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {
            PlayerData.add(uuid, "iron", 1);
            if(random <= 0.02) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.COAL_ORE) {
            PlayerData.add(uuid, "coal", 1);
            if(random <= 0.005) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.EMERALD_ORE) {
            if(random <= 0.5) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.LAPIS_ORE) {
            if(random <= 0.05) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.REDSTONE_ORE) {
            if(random <= 0.07) {
                int add = new Random().nextInt(5) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }


    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getReason().equalsIgnoreCase("disconnect.spam")) {
            return;
        }
        e.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getType().equals(EntityType.PHANTOM)) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {

    }
}
