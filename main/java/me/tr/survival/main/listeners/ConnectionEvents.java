package me.tr.survival.main.listeners;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.Main;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.*;
import me.tr.survival.main.managers.features.Backpack;
import me.tr.survival.main.managers.features.Boosters;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ConnectionEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        if(e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if(player.hasPermission("server.join.full")) e.allow();
            else e.disallow(PlayerLoginEvent.Result.KICK_FULL, "§7Palvelin täynnä! Mikäli haluat ohittaa tämän, sinun täytyy omistaa vähintään §e§lPremium§7-arvo!");
        }
    }

    @EventHandler
    public void onAsyncLogin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        if(e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            PlayerData.loadPlayer(uuid, (r) -> {});
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        player.sendMessage("   ");
        player.sendMessage("   ");
        player.sendMessage("   ");
        player.sendMessage("   ");
        Chat.sendCenteredMessage(player, "§a§lSurvival");
        player.sendMessage("   ");
        Chat.sendCenteredMessage(player, " §7Tervetuloa §a" + player.getName() + " §7meidän Survival-palvelimelle!");
        Chat.sendCenteredMessage(player, " §7Alkuun pääset helposti komennolla §a/apua§7!");
        player.sendMessage("§3 §6 §3 §6 §3 §6 §e"); // Disable mini map
        Chat.sendCenteredMessage(player, " §7Mukavia pelihetkiä!");
        player.sendMessage(" ");
        TextComponent mail = new TextComponent("                  §d§lPOSTI §8| ");
        mail.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/posti"));
        mail.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa tarkastellaksesi §dpäivittäisiä toimituksia§7!")));
        TextComponent stats = new TextComponent("§b§lPROFIILI §8| ");
        stats.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats"));
        stats.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa tarkastellaksesi §bprofiiliasi§7!")));
        TextComponent boosters = new TextComponent("§a§lTEHOSTUKSET");
        boosters.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/boosters"));
        boosters.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Klikkaa tarkastellaksesi §atehostuksia§7!")));
        stats.addExtra(boosters);
        mail.addExtra(stats);
        player.spigot().sendMessage(mail);
        player.sendMessage(" ");

        if(!Boosters.isActive(Boosters.Booster.EXTRA_HEARTS)) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);

        // Fix vanish
        for(UUID vanished : StaffManager.hidden) {
            if(StaffManager.hasStaffMode(player)) break;
            Player v = Bukkit.getPlayer(vanished);
            if(v == null) continue;
            player.hidePlayer(Main.getInstance(), v);
        }

        // Setup backpacks
        Backpack.Level bLvl = Backpack.getLevel(player.getUniqueId());
        if(Ranks.hasRank(player, "premiumplus")) {
            if(bLvl == Backpack.Level.ONE) Backpack.setLevel(player.getUniqueId(), Backpack.Level.TWO);
        } else if(Ranks.hasRank(player, "sorsa")) {
            if(bLvl != Backpack.Level.THREE) Backpack.setLevel(player.getUniqueId(), Backpack.Level.THREE);
        }
        e.setJoinMessage(null);
        if(StaffManager.hidden.contains(player.getUniqueId())) Chat.sendMessage(player, "Olet piilossa pelaajilta!");

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
            Sorsa.teleportToSpawn(e.getPlayer());
        }
        Util.joined.put(player.getUniqueId(), System.currentTimeMillis());

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), (r) -> {
            if(Ranks.isStaff(player.getUniqueId()) && !StaffManager.hasStaffMode(player)) StaffManager.enableStaffMode(player);
            Settings.scoreboard(player);
            r.cancel();
        }, 20 * 2);

        Sorsa.everyAsync(3, () -> Sorsa.sendTablist(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        Player player = e.getPlayer();
        if(Settings.scoreboardRunnables.containsKey(player.getUniqueId())) {
            Settings.scoreboardRunnables.get(player.getUniqueId()).cancel();
            Settings.scoreboardRunnables.remove(player.getUniqueId());
        }
        // Disable staff mode
        if(StaffManager.hasStaffMode(player)) StaffManager.disableStaffMode(player);
        // Save player's data
        Main.getInstance().getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> PlayerData.savePlayer(player.getUniqueId()));
    }

}
