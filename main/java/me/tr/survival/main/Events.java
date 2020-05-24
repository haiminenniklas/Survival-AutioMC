package me.tr.survival.main;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.backpacks.Backpack;
import me.tr.survival.main.other.booster.Boosters;
import me.tr.survival.main.other.events.LevelUpEvent;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import me.tr.survival.main.util.staff.StaffManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
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
        PlayerData.loadPlayer(uuid, (res) -> {});
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
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
        player.sendMessage(" §ahttp://www.sorsamc.fi");
        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        if(!Boosters.isActive(Boosters.Booster.EXTRA_HEARTS)) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);
        }
        // Fix vanish
        for(UUID vanished : StaffManager.hidden) {
            Player v = Bukkit.getPlayer(vanished);
            if(v == null) continue;
            player.hidePlayer(Main.getInstance(), v);

        }

        // Setup backpacks
        Backpack.Level bLvl = Backpack.getLevel(player.getUniqueId());
         if(Ranks.hasRank(player, "premiumplus")) {
             if(bLvl == Backpack.Level.ONE) {
                 Backpack.setLevel(player.getUniqueId(), Backpack.Level.TWO);
             }
        } else if(Ranks.hasRank(player, "sorsa")) {
             if(bLvl != Backpack.Level.THREE) {
                 Backpack.setLevel(player.getUniqueId(), Backpack.Level.THREE);
             }
        }
        e.setJoinMessage(null);
         if(StaffManager.hidden.contains(player.getUniqueId())) {
             Chat.sendMessage(player, "Olet piilossa pelaajilta!");
         }
        if(!player.hasPlayedBefore()) {
            ItemStack[] firstKit = new ItemStack[] {
                    new ItemStack(Material.WOODEN_SWORD, 1),
                    new ItemStack(Material.WOODEN_AXE, 1),
                    new ItemStack(Material.WOODEN_HOE, 1),
                    new ItemStack(Material.WOODEN_PICKAXE, 1),
                    new ItemStack(Material.WOODEN_SHOVEL, 1),
                    new ItemStack(Material.GOLDEN_SHOVEL, 1),
                    new ItemStack(Material.COOKED_BEEF, 16)
            };
            player.getInventory().addItem(firstKit);
            Autio.teleportToSpawn(e.getPlayer());
        }

        Util.joined.put(player.getUniqueId(), System.currentTimeMillis());

        Main.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            PlayerData.loadPlayer(player.getUniqueId(), (result) -> {});
        }, 20 * 2);

        Autio.updatePlayer(player);
        Autio.everyAsync(3, () -> Autio.sendTablist(player));

   }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {

        e.setQuitMessage(null);

        Player player = e.getPlayer();

        // Disable staff mode
        if(StaffManager.hasStaffMode(player)) {
            StaffManager.disableStaffMode(player);
        }

        // Save player's data
        Main.getInstance().getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> PlayerData.savePlayer(player.getUniqueId()));

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null) return;
        if(e.getCurrentItem() == null) return;

        Player player = (Player) e.getWhoClicked();

        if(Gui.getGui(player) != null) {
            Gui gui = Gui.getGui(player);

            if(e.getView().getTitle().contains("§r")) {
                if(gui.isPartiallyTouchable()) {
                    // If user didn't do the allowed procedures with a partially touchable inventory
                    if(!e.getView().getBottomInventory().contains(e.getCurrentItem()) && !gui.clickedAllowedSlot(e.getSlot())) {
                        e.setCancelled(true);
                    }
                } else {
                    e.setCancelled(true);
                }
            }

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

    @EventHandler
    public void onEntityKill(EntityDeathEvent e) {

        if(e.getEntity() instanceof Villager) {
            Villager villager = (Villager) e.getEntity();
            if(villager.getKiller() != null) {
                Player killer = villager.getKiller();
                int amount = new Random().nextInt(25) + 1;
                if(amount >= 1 && (Math.random() <= 0.5)) {
                    Balance.add(killer.getUniqueId(), (double) amount);
                    Chat.sendMessage(killer, "Tapoit raa'asti kyläläisen, mutta häneltä tippui §e" + amount + "€§7!");
                    killer.playSound(killer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                }

            }

        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {

        Player player = e.getPlayer();

        if(!Boosters.isActive(Boosters.Booster.EXTRA_HEARTS)) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);
        }

        if(!player.hasPermission("deathisland.bypass") && !deathIsland.contains(player.getUniqueId())) {

            Autio.after(1, () -> {
                Location deathSpawn = Autio.getDeathSpawn();
                player.teleport(deathSpawn);
                Util.heal(player);

                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 30, 999, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 30, 1, true, false));
                player.playSound(player.getLocation(), Sound.MUSIC_DISC_13, 1, 1);
                deathIsland.add(player.getUniqueId());
            });

            new BukkitRunnable() {

                int timer = 30;

                @Override
                public void run() {
                    if(!deathIsland.contains(player.getUniqueId())) {
                        cancel();
                        return;
                    }

                    if(timer > 0) {
                        player.sendTitle(new Title("§c§lKUOLIT", "§7Pääset §c" + timer + "s §7päästä takaisin!", 15, 20, 15));
                        timer--;
                    } else if(timer <= 0){

                        Autio.task(() -> {
                            Util.heal(player);
                            player.sendTitle(new Title("§a§lTAKAISIN", "§7Olet taas elossa!", 15, 20, 15));
                            deathIsland.remove(player.getUniqueId());
                            player.stopSound(Sound.MUSIC_DISC_13);
                            Autio.teleportToSpawn(player);
                        });
                        cancel();

                    }

                }
            }.runTaskTimerAsynchronously(Main.getInstance(), 25, 20);

        } else {
            Autio.teleportToSpawn(player);
        }


    }

    private static final Map<UUID, Long> lastCommand = new HashMap<>();

    @EventHandler
    public void onCommandPreProcessEvent(PlayerCommandPreprocessEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if(!lastCommand.containsKey(uuid)) {

            lastCommand.put(uuid, System.currentTimeMillis());

        } else {

            long last = lastCommand.get(uuid);
            long now = System.currentTimeMillis();

            if(now - last < 1000 * 2) {
                if(!Ranks.isStaff(uuid)) {
                    e.setCancelled(true);
                    Chat.sendMessage(player, "Rauhoituthan noiden komentojen kanssa!");
                }
            } else {
                lastCommand.remove(uuid);
            }

        }

        if(deathIsland.contains(player.getUniqueId())) {
            if(player.isOp()) return;
            e.setCancelled(true);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Kun olet odottamassa pääsyä takaisin elävien joukkoon et voi suorittaa mitään komentoja!");
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
                    PlayerData.add(uuid, "iron", 1);
                } else {
                    item = new ItemStack(Material.GOLD_INGOT);
                    PlayerData.add(uuid, "gold", 1);
                }

                Util.sendNotification(player, "§a§lTEHOSTUS §7Välittömät oret!");
                e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), item.clone());

            }

        }

        double random = new Random().nextDouble();

        /*

        Chances:

        Emerald: 35% (0.35)
        Diamond: 15% (0.15)
        Gold: 3% (0.03)
        Iron: 2% (0.02)
        Coal 0.5% (0.005)
        Redstone: 2% (0.02)
        Lapis 3% (0.03)

        Crystals removed for now

        if(block.getType() == Material.DIAMOND_ORE) {
            PlayerData.add(uuid, "diamond", 1);
            if(random <= 0.15) {
                int add = new Random().nextInt(3) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.GOLD_ORE && Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {
            PlayerData.add(uuid, "gold", 1);
            if(random <= 0.03) {
                int add = new Random().nextInt(3) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.IRON_ORE && Boosters.isActive(Boosters.Booster.INSTANT_MINING)) {
            PlayerData.add(uuid, "iron", 1);
            if(random <= 0.02) {
                int add = new Random().nextInt(3) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.COAL_ORE) {
            PlayerData.add(uuid, "coal", 1);
            if(random <= 0.005) {
                int add = new Random().nextInt(3) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.EMERALD_ORE) {
            if(random <= 0.35) {
                int add = new Random().nextInt(3) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.LAPIS_ORE) {
            if(random <= 0.03) {
                int add = new Random().nextInt(3) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } else if(block.getType() == Material.REDSTONE_ORE) {
            if(random <= 0.02) {
                int add = new Random().nextInt(3) + 1;
                Crystals.add(uuid, add);
                Chat.sendMessage(player, "§7Löysit §b" + add  + " §7kristallia!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        } */


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
    public void onPortalEnter(PlayerPortalEvent e) {
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL || e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            e.setCancelled(true);
            e.getPlayer().teleport(e.getFrom());
            Chat.sendMessage(e.getPlayer(), Chat.Prefix.ERROR, "Portaalit eivät valitettavasti toimi. Pääset kuitenkin toisiin maailmoihin komennolla §a/matkusta§7!");
        }
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        if(e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            e.setCancelled(true);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Portaalit eivät valitettavasti toimi. Pääset kuitenkin toisiin maailmoihin komennolla §a/matkusta§7!");
        }
    }

    @EventHandler
    public void onTrade(InventoryOpenEvent e) {
        if(e.getInventory().getType() == InventoryType.MERCHANT) {
            e.setCancelled(true);
        }
    }

}
