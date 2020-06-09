package me.tr.survival.main.managers.features;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Profile;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

public class Backpack implements CommandExecutor, Listener {

    private boolean enabled = true;
    private final List<UUID> opened = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getLabel().equalsIgnoreCase("reppu")) {

            if(sender instanceof Player) {

                Player player = (Player) sender;

                if(args.length < 1) openBackpack(player);
                else {

                   if(args[0].equalsIgnoreCase("päivitä") || args[0].equalsIgnoreCase("upgrade")) Main.getBackpack().upgradeConfirm(player);
                   else if(args[0].equalsIgnoreCase("help"))  {
                       if(Ranks.isStaff(player.getUniqueId())){
                           Chat.sendMessage(player, "/backpack katso <pelaaja>");
                           Chat.sendMessage(player, "/backpack (enable | disable)");
                       }
                   } else if(args[0].equalsIgnoreCase("enable") && player.isOp()) {
                       enabled = true;
                       Chat.sendMessage(player,"Reput ovat nyt päällä!");
                   } else if(args[0].equalsIgnoreCase("disable") && player.isOp()) {
                        enabled = false;
                        Chat.sendMessage(player,"Reput ovat nyt pois päältä");
                   }

                   if(args.length >= 2) {

                       if(Ranks.isStaff(player.getUniqueId())) {
                           if(args[0].equalsIgnoreCase("katso")) {

                               OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                               if(!PlayerData.isLoaded(target.getUniqueId())) {

                                   Chat.sendMessage(player, "Pelaajaa ei löydetty... Yritetään ladata tiedot tietokannasta");

                                   Sorsa.async(() ->
                                       PlayerData.loadPlayer(target.getUniqueId(), (result) -> {
                                           if(result) openOther(player, target);
                                           else Chat.sendMessage(player, "Pelaajaa ei löydetty... Ei voida avata reppua!");
                                       }));

                               } else openOther(player, target);
                           }
                       } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
                   }
                }
            }
        }
        return true;
    }

    private void openOther(Player opener, OfflinePlayer target) {

        ItemStack[] items = getSavedInventory(target.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, getLevel(target.getUniqueId()).size, "Tarkastele reppua (" + target.getName() + ")");

        for(ItemStack item : items) {
            if(item == null) continue;
            if(item.getType() == Material.AIR) continue;
            inv.addItem(item);
        }

        opener.openInventory(inv);
    }

    public void openBackpack(Player player) {

        if(!enabled && !player.isOp()) {
            Chat.sendMessage(player, "Reput ovat tällä hetkellä pois päältä! Ei kuitenkaan hätää! Tavarasi eivät ole hävinneet!");
            return;
        }

        UUID uuid = player.getUniqueId();
        Level level =  getLevel(player.getUniqueId());

        ItemStack[] items = getSavedInventory(uuid);
        Inventory inv = Bukkit.createInventory(null, level.size + 18, "Reppu (" + level.displayName + "§8)");

        for(int i = 0; i < 9; i++) {
            if(i == 4) continue;
            inv.setItem(i, ItemUtil.makeItem(Material.PINK_STAINED_GLASS_PANE));
        }

        String upgradeText = (getLevel(uuid) == Level.THREE) ? "§cReppu on ylimmällä tasolla" : "§aKlikkaa päivittääksesi!";

        inv.setItem(4, ItemUtil.makeItem(Material.EXPERIENCE_BOTTLE, 1, "§eReppusi", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Reppusi taso: §a" + getLevelNumber(uuid),
                " ",
                " " + upgradeText,
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));

        inv.setItem(inv.getSize() - 5, ItemUtil.makeSkullItem(player, 1, "§aProfiili", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Avaa sinun profiili-",
                " §7näkymäsi, josta voit",
                " §7hallinnoida pelaamistasi!",
                " ",
                " §aKlikkaa avataksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));

        int itemIndex = 0;
        if(items.length >= 1) {
            for(int i = 9; i < level.size + 9; i++) {
                if(itemIndex >= items.length) break;
                ItemStack item = items[itemIndex];
                if(item == null) continue;
                inv.setItem(i, item);
                itemIndex += 1;
            }
        }

        for(int i = level.size + 9; i < level.size + 18; i++) {
            if(i == inv.getSize() - 5) continue;
            inv.setItem(i, ItemUtil.makeItem(Material.PINK_STAINED_GLASS_PANE));
        }

        opened.remove(player.getUniqueId());
        opened.add(player.getUniqueId());

        player.openInventory(inv);

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInvClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        UUID uuid = player.getUniqueId();

        if(opened.contains(uuid) && e.getView().getTitle().contains("Reppu")) {
            if(e.getCurrentItem() != null && e.getClickedInventory() != null) {

                ItemStack item = e.getCurrentItem();
                if(item.getType() == Material.PINK_STAINED_GLASS_PANE) {

                    int index = e.getSlot();
                    if(index >= e.getInventory().getSize() - 9 && index < e.getInventory().getSize()) e.setCancelled(true);
                    else if(index >= 0 && index <= 8) e.setCancelled(true);

                } else if(item.getType() == Material.EXPERIENCE_BOTTLE && e.getSlot() == 4) {
                    if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        if(item.getItemMeta().getDisplayName().equalsIgnoreCase("§eReppusi")) {
                            e.setCancelled(true);
                            if(getLevel(uuid) != Level.THREE) upgradeConfirm(player);
                            else player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        }
                    }
                } else if(item.getType() == Material.PLAYER_HEAD && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if(meta != null && meta.hasDisplayName() && meta.hasLore()) {
                        if(meta.getDisplayName().equalsIgnoreCase("§aProfiili")) {
                            e.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1,1 );
                            Profile.openProfile(player, player.getUniqueId());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent e) {

        Player player = (Player) e.getPlayer();

        Inventory inv = e.getView().getTopInventory();
        if(opened.contains(player.getUniqueId()) && e.getView().getTitle().contains("Reppu")) {
            Inventory correctInv = Bukkit.createInventory(null, getLevel(player.getUniqueId()).size);
            int itemIndex = 0;
            for(int i = 0; i < inv.getStorageContents().length; i++) {
                ItemStack item = inv.getStorageContents()[i];
                if(item != null && item.getType() != Material.AIR) {
                    if(i > 8 && i < (9 + getLevel(player.getUniqueId()).size)) {
                        if(!Util.isIllegalItem(item)) {
                            correctInv.addItem(item);
                        }
                    }
                }
                if(itemIndex >= inv.getStorageContents().length) break;
                itemIndex += 1;
            }
            saveInventory(player.getUniqueId(), correctInv.getStorageContents());
            opened.remove(player.getUniqueId());
            return;
        }

        if(e.getView().getTitle().startsWith("Tarkastele reppua")) {
            String title = e.getView().getTitle();
            String playerName = title.substring(title.indexOf('('));
            playerName = playerName.replace(")", "");
            playerName = playerName.replace("(", "");
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            saveInventory(target.getUniqueId(), inv.getContents());
            Chat.sendMessage(player, "Tallennettiin pelaajan §6" + playerName + " §7reppu!");
        }
    }

    private String getRawSavedInventory(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return String.valueOf(PlayerData.getValue(uuid, "backpack_inventory"));
    }

    private void setRawSavedInventory(UUID uuid, String inv) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.set(uuid, "backpack_inventory", inv);
    }

    private ItemStack[] getSavedInventory(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        String raw = getRawSavedInventory(uuid);
        if(raw.equalsIgnoreCase("null")) return new ItemStack[0];
        else {
            try { return Util.itemStackArrayFromBase64(raw);
            } catch(IOException ex) { ex.printStackTrace(); }
        }
        return new ItemStack[0];
    }

    private void saveInventory(UUID uuid, ItemStack[] inv) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        String raw = Util.itemStackArrayToBase64(inv);
        setRawSavedInventory(uuid, raw);
    }

    public Level getLevel(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        return Level.valueOf(String.valueOf(PlayerData.getValue(uuid, "backpack_level")));
    }

    public int getLevelNumber(UUID uuid) {
        if(getLevel(uuid) == Level.ONE) return 1;
        else if(getLevel(uuid) == Level.TWO) return 2;
        else return 3;
    }

    public void setLevel(UUID uuid, Level level) {
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        PlayerData.set(uuid, "backpack_level", level.toString());
    }

    private void upgradeConfirm(Player player) {
        UUID uuid = player.getUniqueId();
        Level current = getLevel(uuid);
        int price = 10000;
        if(current == Level.TWO) price = 20000;
        final int finalPrice = price;
        Gui.openGui(player, "Päivitä reppusi", 27, (gui) -> {

            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa vahvistaaksesi päivityksen!",
                    " §7Päivitys maksaa: §e" + Util.formatDecimals(finalPrice) + "€§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(player);
                    upgrade(clicker);
                }
            });

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7 Klikkaa peruuttaaksesi päivityksen!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    openBackpack(clicker);
                }
            });
        });
    }

    private void upgrade(Player player) {

        UUID uuid = player.getUniqueId();
        Level current = getLevel(uuid);

        int price = 10000;
        if(current == Level.TWO) price = 20000;

        if(Balance.canRemove(uuid, price)) {
            if(addLevel(uuid)) {
                Balance.add(uuid, -price);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                player.sendMessage(" §a§lREPPU PÄIVITETTIIN!");
                player.sendMessage(" ");
                player.sendMessage(" §7Reppusi taso: " + getLevel(uuid).displayName);
                player.sendMessage(" ");
                player.sendMessage(" §7Avaa reppu komennolla §a/reppu");
                player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                openBackpack(player);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Reppusi on jo ylimmällä tasolla!");
            }
        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän! Päivitys maksaa §e" + price + "€§7!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    private boolean addLevel(UUID uuid) {

        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);

        Level current = getLevel(uuid);
        Level newLevel = null;
        if(current == Level.ONE) newLevel = Level.TWO;
        else if(current == Level.TWO) newLevel = Level.THREE;
        else return false;

        setLevel(uuid, newLevel);
        return true;
    }


    public enum Level {

        ONE(9, "§a§lLEVEL 1"),
        TWO(18, "§e§lLEVEL 2"),
        THREE(36, "§c§lLEVEL 3");

        private int size;
        private String displayName;

        Level(int size, String displayName) {
            this.size = size;
            this.displayName = displayName;
        }

    }

}
