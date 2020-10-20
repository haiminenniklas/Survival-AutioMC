package me.tr.survival.main.commands;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.data.NBTData;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.managers.Settings;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import me.tr.survival.main.managers.StaffManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class Essentials implements CommandExecutor, Listener {

    private Map<UUID, UUID> inInvsee = new HashMap<>();
    private Map<UUID, Long> spawnStoreTeleport = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if(cmd.getLabel().equalsIgnoreCase("broadcast")) {
                if(Ranks.isStaff(uuid)) {
                    if(args.length >= 1) {
                        StringBuilder sb = new StringBuilder();
                        for(int i = 0; i < args.length; i++) {
                            sb.append(args[i] + " ");
                        }
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "➤ &2&lILMOITUS §7" + sb.toString()));
                        Util.broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING);
                    } else Chat.sendMessage(player, "Täytyyhän sinun hyvä ihminen kirjoittaakin jotain! Käytä §a/broadcast <viesti>§7!");
                }
            } else if(cmd.getLabel().equalsIgnoreCase("clear")) {

                if(!Main.getStaffManager().hasStaffMode(player)) {
                    Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                    return true;
                }

                if(args.length < 1) {
                    if(Ranks.isStaff(uuid)) {
                        Util.clearInventory(player);
                        Chat.sendMessage(player, "Inventorysi tyhjennettiin!");
                    }
                } else {
                    if(Ranks.isStaff(uuid)) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, "Pelaajaa ei löydetty!");
                            return true;
                        }
                        Util.clearInventory(target);
                        Chat.sendMessage(target, "Pelaajan §a" + target.getName() + " §7inventory tyhjennettiin!");
                    }
                }

            } else if(cmd.getLabel().equalsIgnoreCase("world")) {

                if(Ranks.isStaff(uuid)) {

                    if(args.length < 1) {
                        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                        player.sendMessage(" §7Maailmat (" + Bukkit.getWorlds().size() +  "):");

                        for(World w : Bukkit.getWorlds()) {
                            if(player.getWorld().getName().equalsIgnoreCase(w.getName())) player.sendMessage("§7- §a" + w.getName() + " §8(sinä)");
                            else player.sendMessage("§7- §a" + w.getName());
                        }

                        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    } else {
                        String worldName = args[0];
                        World w = Bukkit.getWorld(worldName);
                        if(w != null) {
                            Chat.sendMessage(player, "Viedään maailmaan §a" + w.getName() + "§7...");
                            player.teleport(w.getSpawnLocation());
                        } else Chat.sendMessage(player, "Maailmaa ei löydetty...");
                    }
                }
            } else if(cmd.getLabel().equalsIgnoreCase("invsee")) {

                if(Ranks.isStaff(player.getUniqueId())) {

                    if(!Main.getStaffManager().hasStaffMode(player)) {
                        Util.sendClickableText(player, Chat.getPrefix() + " §7Tämä toimii vain §eStaff§7-tila päällä. (Tee §a/staffmode§7)", "/staffmode", "§7Klikkaa laittaaksesi §eStaff§7-tilan päälle!");
                        return true;
                    }

                    if(args.length < 1) Chat.sendMessage(player, "Käytä §a/invsee <pelaaja>");
                    else {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                            return true;
                        }
                        invsee(player, target);
                    }

                } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");

            } else if(cmd.getLabel().equalsIgnoreCase("sorsastore")) {

                teleportToStore(player);

            } else if(cmd.getLabel().equalsIgnoreCase("craft")) {

                if(Ranks.hasRank(uuid, "premiumplus") || Ranks.hasRank(uuid, "sorsa") || Ranks.isStaff(uuid)) {
                    player.openWorkbench(null, true);
                } else {
                    Chat.sendMessage(player, "Tämä toiminto on vain §6§lPremium§f+ §7ja ylemmille!");
                }

            } else if(cmd.getLabel().equalsIgnoreCase("nimeä") || cmd.getName().equalsIgnoreCase("rename")) {

                if(Ranks.hasRank(uuid, "premiumplus") || Ranks.hasRank(uuid, "sorsa") || Ranks.isStaff(uuid)) {

                    if(args.length < 1) {

                        Chat.sendMessage(player, "Käytä §a/nimeä <nimi> §7§o(Värikoodeja saa käyttää => §a§o/apua värikoodit§7§o) §c§lHUOM! §7Jos käytät rumia nimiä, niin tämä tavara voidaan poistaa sinulta.");

                    } else {

                        final ItemStack item = player.getInventory().getItemInMainHand();
                        if(item.getType() != Material.AIR) {

                            String givenName = StringUtils.join(args);

                            String[] blackListedWords = {
                                    "neekeri",
                                    "homo",
                                    "huora",
                                    "paska",
                                    "vittu",
                            };

                            // Check for bad names
                            for(String blacklistedWord : blackListedWords) {
                                String parsedName = givenName.replace("4", "a")
                                                             .replace("0", "o")
                                                             .replace("3", "e")
                                                             .replace("1", "i")
                                                             .toLowerCase();

                                if(parsedName.contains(blacklistedWord)) {
                                    Chat.sendMessage(player, "No tuohan on varsin ruma nimi... Kai ymmärrät, että tuo on todella hirvittävää käytöstä?");
                                    return true;
                                }
                            }

                            // Color correct
                            givenName = ChatColor.translateAlternateColorCodes('&', givenName);
                            this.openItemRenameConfirmal(player, item, givenName);
                        }
                    }

                } else {
                    Chat.sendMessage(player, "Tämä toiminto on vain §6§lPremium§f+ §7ja ylemmille!");
                }

            }
        }
        return true;
    }

    private void openItemRenameConfirmal(Player player, final ItemStack itemToChange, String givenName) {

        Gui gui = new Gui("Tavaran nimen vaihdos", 27);

        final int price = 1000;

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Haluat siis vaihtaa tavarasi",
                " §animen§7? Ei hätää! Maksa",
                " §7vain §e" + Util.formatDecimals(price) + "€§7,",
                " §7niin vaihdan tavarasi nimen!",
                " ",
                " §7Olet vaihtamassa tavaran nimeksi:",
                "   "  + givenName,
                " ",
                " §aKlikkaa vahvistaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(player);

                if(Balance.canRemove(player.getUniqueId(), price)) {

                    ItemMeta meta = itemToChange.getItemMeta();
                    meta.setDisplayName(givenName);
                    itemToChange.setItemMeta(meta);

                    player.updateInventory();

                    Chat.sendMessage(player, "Nimen vaihdos tehty! Onnittelut!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                    Balance.remove(player.getUniqueId(), price);

                } else {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän senkin hölmö!");
                }

            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Jäitkö aristamaan? Ei hätää!",
                " §7Voit kyllä vielä peruuttaa",
                " §7tämän toiminnon!",
                " ",
                " §cKlikkaa peruuttaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
            }
        });

        gui.open(player);

    }

    public void teleportToStore(Player player) {

        UUID uuid = player.getUniqueId();

        if(!Sorsa.isInFreeWorld(player)) {
            Chat.sendMessage(player, "Tämä komento ei toimi §cNetherissä§7, eikä §6PvP-maailmasssa§7! Käytä komentoa §a/spawn§7!");
            return;
        }

        if(spawnStoreTeleport.containsKey(uuid)) {
            long now = System.currentTimeMillis();
            long didTeleport = spawnStoreTeleport.get(uuid);
            long shouldBeAbleToTeleport = didTeleport + (1000 * 60 * 5); // 5 minutes
            if(now < shouldBeAbleToTeleport) {
                long timeLeftSeconds = (shouldBeAbleToTeleport - now) / 1000;
                long minutes = (int) timeLeftSeconds / 60;
                long seconds = timeLeftSeconds - (60 * minutes);
                String timeLeft = Util.formatTime((int) minutes, (int) seconds, true);
                Chat.sendMessage(player, "Et voi vielä päästä tällä komennolla kauppaan... Löydät kaupan §a/spawn§7! Odota vielä §c" + timeLeft + "§7...");
                return;
            }
        }

        Chat.sendMessage(player, "Sinut viedään kauppaan §a3 sekunnin §7kuluttua...");
        Sorsa.after(3, () -> {
            spawnStoreTeleport.put(uuid, System.currentTimeMillis());
            player.teleport(new Location(Sorsa.getSpawn().getWorld(), -1.5, 57.0, -27.5, 180f, -1.5f));
            Chat.sendMessage(player, "Sinut vietiin kauppaan!");
        });
    }

    private void invsee(Player opener, OfflinePlayer target) {

        if(target.isOnline()) {
            opener.openInventory(target.getPlayer().getInventory());
        } else {

            de.tr7zw.changeme.nbtapi.data.PlayerData playerNBTData = NBTData.getOfflinePlayerData(target.getUniqueId());
            NBTCompoundList Inv = playerNBTData.getCompound().getCompoundList("Inventory");

            final Inventory inv = Bukkit.createInventory(null, 27, "Inventory " + target.getName() + " (offline)");

            for(NBTCompound nbtCompound : Inv) {
                int count = nbtCompound.getInteger("Count");
                int slot = nbtCompound.getInteger("Slot");
                Material mat = Material.matchMaterial(nbtCompound.getString("id"));
                if(mat != null) {
                    ItemStack item = new ItemStack(mat, count);
                    inv.setItem(slot, item);
                }
            }

        }

        this.inInvsee.put(opener.getUniqueId(), target.getUniqueId());

    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {

        Player player = (Player) e.getPlayer();
        Inventory inv = e.getInventory();

        if(this.inInvsee.containsKey(player.getUniqueId())) {

            OfflinePlayer target = Bukkit.getOfflinePlayer(this.inInvsee.get(player.getUniqueId()));
            if(!target.isOnline()) {

                de.tr7zw.changeme.nbtapi.data.PlayerData playerNBTData = NBTData.getOfflinePlayerData(target.getUniqueId());
                NBTCompoundList Inv = playerNBTData.getCompound().getCompoundList("Inventory");

                Collection<NBTCompound> items = Collections.emptySet();
                for(ItemStack itemstack : inv.getContents()) {
                    if(itemstack != null) {
                        items.add(NBTItem.convertItemtoNBT(new ItemStack(Material.AIR)));
                    } else items.add(NBTItem.convertItemtoNBT(itemstack));
                }

                Inv.clear();
                for(NBTCompound com : items) {
                    Inv.addCompound(com);
                }

                playerNBTData.saveChanges();

            }


        }

        this.inInvsee.remove(player.getUniqueId());

    }

    @Deprecated
    public static ItemStack createCustomXPBottle(int experienceAmount) {

        ItemStack item = Util.makeEnchanted(ItemUtil.makeItem(Material.EXPERIENCE_BOTTLE, 1, "§a§lXP-Pullo", Arrays.asList(
                "§7Tämä pullo sisältää",
                "§d" + experienceAmount,
                "§7kokemusta! Klikkaa tätä itemiä",
                "§7saadaksesi kokemukset!"
        )));

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "xp-amount");
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getCustomTagContainer().setCustomTag(key, ItemTagType.INTEGER, experienceAmount);
        item.setItemMeta(itemMeta);

        return item;

    }

    @Deprecated
    private static int getExperienceFromCustomXPBottle(ItemStack bottle) {
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "xp-amount");
        ItemMeta itemMeta = bottle.getItemMeta();
        CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();

        if(tagContainer.hasCustomTag(key, ItemTagType.INTEGER)) {
            return tagContainer.getCustomTag(key, ItemTagType.INTEGER);
        }

        return 0;

    }

}
