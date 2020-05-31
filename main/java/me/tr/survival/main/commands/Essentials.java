package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.*;

public class Essentials implements CommandExecutor, Listener {

    public static List<UUID> afk = new ArrayList<UUID>();
    public static Map<UUID, Long> moved = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if(cmd.getLabel().equalsIgnoreCase("apua")) {

                if(args.length < 1) {

                    player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    player.sendMessage(" §2§lApua-valikko");
                    player.sendMessage( "§7§oKirjoita §a§o/apua <...>");
                    player.sendMessage(" ");
                    player.sendMessage(" §a...komennot §7Hyödylliset komennot");
                    player.sendMessage(" §a...vaihto §7Tietoa vaihtokaupasta");
                    player.sendMessage(" §a...arvot §7Palvelimen VIP-arvot");
                    player.sendMessage(" §a...aloitus §7Vinkit survivalin aloitukseen");
                    player.sendMessage(" §a...tehostukset §7Mitä tehostukset ovat?");
                    player.sendMessage(" §a...asetukset §7Mitkä asetukset?");
                    player.sendMessage(" §a...valuutta §7Miten raha toimii täällä?");
                    player.sendMessage(" §a...matkustaminen §7Miten toimii??");
                    player.sendMessage(" §a...reppu §7Kätevä tapa tallettaa tavarat");
                    player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                } else {

                    if(args[0].equalsIgnoreCase("komennot")) {

                        Gui.openGui(player, "Apua (Komennot)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Hyödylliset komennot", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Hyödylliset komennot:",
                                    "  §a/profiili §7sinun tietosi",
                                    "  §a/rtp §7vie sinut erämaahan",
                                    "  §a/msg §7yksityisviestit",
                                    "  §a/tpa §7teleporttauspyyntö",
                                    "  §a/matkusta §7matkusatminen",
                                    "  §6/vaihto §7vaihtokauppa",
                                    "  §9/discord §7Discord-yhteisö",
                                    "  §e/osta §7verkkokauppa",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }

                        });
                    } else if(args[0].equalsIgnoreCase("vaihto")) {
                        Gui.openGui(player, "Apua (Vaihtokauppa)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Niin mikä?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellemme on rakennettu",
                                    " §6vaihtokauppa§7-järjestelmä, jonka",
                                    " §7on tarkoitus suojata kummankin",
                                    " §7osapuolen §barvotavarat§7.",
                                    " §7Järjestelmä pyrkii, ettei tapahdu",
                                    " §cpetoksia§7 tai §cvarastamista§7.",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 12);

                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Miten toimii?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Vaihtokauppa tapahtuu niinkin",
                                    " §7yksinkertaisesti siten, että",
                                    " §7kirjoitat chattiin §6/vaihto <pelaaja>",
                                    " §7ja kun vastaanottaja hyväksyy pyynnön",
                                    " §7teille avautuu näkymä, johon voitte",
                                    " §7siirtää tavaranne ja vasta kun molemmat",
                                    " §7ovat hyväksyneet vaihdon, vaihtokauppa",
                                    " §7iskee voimaan.",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 14);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }

                        });
                    } else if(args[0].equalsIgnoreCase("arvot")) {

                        Gui.openGui(player, "Apua (Arvot)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6VIP-arvot", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellamme on §63",
                                    " §7VIP-arvoa: §e§öPremium§7,",
                                    " §6§lPremium§f+§7, sekä §2§lSORSA§7!",
                                    " §7VIP-arvot ovat yksi tapa",
                                    " §7tukea palvelimen toimintaa",
                                    " §7pitämällä sen mahdollisimman",
                                    " §7pitkään toiminnassa!",
                                    " ",
                                    " §7Lisää VIP-arvoista",
                                    " §7verkkokaupassamme: §a/osta",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }

                        });

                    } else if(args[0].equalsIgnoreCase("aloitus")) {
                        Gui.openGui(player, "Apua (Aloittaminen)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Miten aloitan Survivalin?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Survivalin aloittaminen meillä on",
                                    " §7lähes samanlaista, kuin tavallisestikin!",
                                    " §7Saat aluksi hieman tavaraa, millä",
                                    " §7päästä alkuun ja tehtävänäsi on",
                                    " §aselviytyä mahdollisimman pitkään!",
                                    " §7Saat §e1 000€ §7aloitusrahaksi, jonka",
                                    " §7voit käyttää palvelimen kauppaan. ",
                                    " §aRahaa §7tienaat myymällä esineitäsi!",
                                    " ",
                                    " §7Discord palvelimella saat apua",
                                    " §cylläpidolta §7ja muilta pelaajilta",
                                    " §7sekä pelin sisäisesti §a/apua§7-",
                                    " §7komennolla! Onnea matkaan ja mukavia",
                                    " §7pelihetkiä!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }

                        });
                    } else if(args[0].equalsIgnoreCase("tehostukset")) {
                        Gui.openGui(player, "Apua (Tehostukset)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitkä ihmeen tehostukset?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellamme on käytössä",
                                    " §7ns. §atehostuksia§7, joiden",
                                    " §7tarkoitus vähän nopeutaa ja",
                                    " §7hauskuuttaa pelin kulkua, sekä",
                                    " §7tehdä meidän Survivalista hieman",
                                    " §7§ouniikimman§7, kuin muiden!",
                                    " ",
                                    " §7Lisätietoa tehostuksista",
                                    " §7komennolla §a/tehostus",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }

                        });
                    } else if(args[0].equalsIgnoreCase("asetukset")) {
                        Gui.openGui(player, "Apua (Asetukset)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitä hyödyn?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Olemme tehneet juuri teitä",
                                    " §6pelaajia §7varten palvelimellemme",
                                    " §7asetuksia, joita voit muuttaa",
                                    " §7jotta pelaaminen olisi juuri",
                                    " §esinulle §7mieleisempää",
                                    " ",
                                    " §7Lisätietoa asetuksista komennolla",
                                    " §6/asetukset",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }

                        });
                    } else if(args[0].equalsIgnoreCase("valuutta") || args[0].equalsIgnoreCase("raha")) {
                        Gui.openGui(player, "Apua (Raha/Valuutta)", 27, (gui) -> {

                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Miten toimii?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellamme on valuutta §eEuro",
                                    " §7(§e€§7), jolla voit ostaa palvelimen",
                                    " §7kaupasta esineitä. Saat myös näitä",
                                    " §eeuroja§7, kun saat myytyä",
                                    " §7tavaroitasi.",
                                    " §7Voit myös maksaa ja vastaanottaa",
                                    " §7rahaa muilta pelaajilta §7(§a/raha§7)!",
                                    " §7Aluksi kaikki pelaajat saavat",
                                    " §a1 000€§7!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);

                            gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eRikkaimmat", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Katso ketkä ovat palvelimen",
                                    " §7rikkaimmat pelaajat, sekä",
                                    " §7miten itse sijoitut!",
                                    " ",
                                    " §aKlikkaa tästä!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    BaltopCommand.openGui(clicker);
                                }
                            });

                            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§aHyödylliset komennot", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §a/rahatilanne §7Rahatilanteesi",
                                    " §a/maksa §7Maksa rahaa pelaajalle",
                                    " §a/valuutta §7Vaihda rahaa",
                                    " §a/baltop §7Rikkaimmat pelaajat",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 26);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }


                        });
                    }/* else if(args[0].equalsIgnoreCase("tehtävät")) {
                        Gui.openGui(player, "Apua (Tehtävät)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitä tehtävät ovat?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Pitääksemme pelaamisen mielekkäänä",
                                    " §7ja hauskana, olemme lisänneet",
                                    " §7monenlaisia tehtäviä, joita voit",
                                    " §7suorittaa ja saada niistä palkintoja!",
                                    " ",
                                    " §7Lista tehtävistä: §6/tehtävät§7!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);
                        });
                    }*/ else if(args[0].equalsIgnoreCase("matkustaminen")) {
                        Gui.openGui(player, "Apua (Matkustaminen)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitä tarkoittaa?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellamme voit matkustaa",
                                    " §7sekä §5Endiin§7, että §cNetheriin§7!",
                                    " §7Netheriin matkustaminen on ilmaista,",
                                    " §7mutta Endiin se on §amaksullista§7.",
                                    " §7Endiin matkustamiseen vaaditaan ",
                                    " §e500 000€ §7rahaa ja voit kutsua",
                                    " §7kaverisi mukaan samalla hinnalla!",
                                    " ",
                                    " §7Lisätietoa matkustamisesta: §a/matkustaminen",
                                    " §7Discord: §9/discord",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }

                        });
                    } else if(args[0].equalsIgnoreCase("reppu")) {
                        Gui.openGui(player, "Apua (Reppu)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitkä ihmeen reput?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Kaikki me tiedämme, että on",
                                    " §7olemassa §5Ender chestit§7, ",
                                    " §7mutta halusimme tuoda teille",
                                    " §7paremman ja kätevämmän tavan",
                                    " §7tallettaa tavaroita! §7Kun teet",
                                    " §7komennon §a/reppu§7, niin voit ",
                                    " §7sinne tallettaa tavarasi missä vain!",
                                    " ",
                                    " §7On kolmen tason reppua, 1, 2 ja 3",
                                    " §7ja koko suurenee tason myötä",
                                    " §7Päivittää reppusi voit komennolla",
                                    " §a/reppu päivitä§7!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);

                            for(int i = 0; i < 27; i++) {
                                if(gui.getItem(i) != null) continue;
                                if(gui.getButton(i) != null) continue;
                                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                            }

                        });
                    }/* else if(args[0].equalsIgnoreCase("huutokauppa")) {
                        Gui.openGui(player, "Apua (Huutokauppa)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Miten tienaan rahaa?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Huutokauppa on palvelimemme yleinen",
                                    " §akauppapaikka§7, jossa voit myydä",
                                    " §7esineitäsi ja tienata siitä §erahaa§7!",
                                    " §7Muista seurata tavaroiden hintaa ja",
                                    " §7ota myy ne parhaaseen mahdolliseen hintaan!",
                                    " ",
                                    " §7Huutokauppaan pääset komennolla",
                                    " §a/huutokauppa",
                                    " §7Lisää tietoa rahasta palvelimellamme",
                                    " §a/apua valuutta",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);
                        });
                    } */

                }

            } else if(cmd.getLabel().equalsIgnoreCase("broadcast")) {

                if(Ranks.isStaff(uuid)) {

                    if(args.length >= 1) {
                        StringBuilder sb = new StringBuilder();
                        for(int i = 0; i < args.length; i++) {
                            sb.append(args[i] + " ");
                        }

                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "➤ &2&lILMOITUS " + sb.toString()));
                    } else {
                        Chat.sendMessage(player, "Täytyyhän sinun hyvä ihminen kirjoittaakin jotain! Käytä §a/broadcast <viesti>§7!");
                    }

                }

            } else if(cmd.getLabel().equalsIgnoreCase("discord")) {

                Chat.sendMessage(player, "Discord: §9www.sorsamc.fi/discord");

            } else if(cmd.getLabel().equalsIgnoreCase("clear")) {

                if(Ranks.isStaff(uuid)) {
                    Util.clearInventory(player);
                    Chat.sendMessage(player, "Inventorysi tyhjennettiin!");
                }

            } else if(cmd.getLabel().equalsIgnoreCase("kordinaatit")) {

                if(Ranks.isStaff(uuid)) {
                    Location loc = player.getLocation();
                    Chat.sendMessage(player, "Sijaintisi: §a" + loc.getX() + "§7, §a" + loc.getY() + "§7, §a" + loc.getZ() + " §7-> §a" + loc.getWorld().getName());
                }

            } else if(cmd.getLabel().equalsIgnoreCase("world")) {

                if(Ranks.isStaff(uuid)) {

                    if(args.length < 1) {
                        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                        player.sendMessage(" §7Maailmat (" + Bukkit.getWorlds().size() +  "):");

                        for(World w : Bukkit.getWorlds()) {
                            if(player.getWorld().getName().equalsIgnoreCase(w.getName())) {
                                player.sendMessage("§7- §a" + w.getName() + " §8(sinä)");
                            } else {
                                player.sendMessage("§7- §a" + w.getName());
                            }
                        }

                        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    } else {
                        String worldName = args[0];
                        World w = Bukkit.getWorld(worldName);
                        if(w != null) {
                            Chat.sendMessage(player, "Viedään maailmaan §a" + w.getName() + "§7...");
                            player.teleport(w.getSpawnLocation());
                        } else {
                            Chat.sendMessage(player, "Maailmaa ei löydetty...");
                        }
                    }

                }

            } else if(cmd.getLabel().equalsIgnoreCase("invsee")) {

                if(Ranks.isStaff(player.getUniqueId())) {

                    if(args.length < 1) {
                        Chat.sendMessage(player, "Käytä §a/invsee <pelaaja>");
                    } else {

                        Player target = Bukkit.getPlayer(args[0]);
                        if(target == null) {
                            Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                            return true;
                        }

                        invsee(player, target);


                    }

                } else {
                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
                }
            }

        }

        return true;
    }

    public static void invsee(Player opener, Player target) {

        Inventory inv = Bukkit.createInventory(target, InventoryType.PLAYER, "Tarkastele inventoryä (" + target.getName() + ")");
        for(ItemStack item : target.getInventory().getContents()) {
            if(item == null) {
                item = new ItemStack(Material.AIR);
            }

            inv.addItem(item);

        }
        opener.openInventory(inv);

    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {

        Player player = (Player) e.getPlayer();

        if(e.getView().getTitle().startsWith("Tarkastele inventoryä")) {
            String title = e.getView().getTitle();

            String playerName = title.substring(title.indexOf('('));
            playerName = playerName.replace(")", "");
            playerName = playerName.replace("(", "");

            Player target = Bukkit.getPlayer(playerName);
            if(target == null) {
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty!");
                return;
            }

            target.getInventory().setContents(e.getInventory().getContents());

        }


    }

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

    public static int getExperienceFromCustomXPBottle(ItemStack bottle) {
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "xp-amount");
        ItemMeta itemMeta = bottle.getItemMeta();
        CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();

        if(tagContainer.hasCustomTag(key, ItemTagType.INTEGER)) {
            return tagContainer.getCustomTag(key, ItemTagType.INTEGER);
        }

        return 0;

    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        if(e.getItem() != null) {

            ItemStack item = e.getItem();
            if(item.hasItemMeta()) {

                ItemMeta meta = item.getItemMeta();
                if(meta.getDisplayName().equalsIgnoreCase("§a§lXP-Pullo")) {

                    int exp = getExperienceFromCustomXPBottle(item);
                    if(exp > 0) {
                        e.setCancelled(true);
                        player.giveExp(exp);
                        Chat.sendMessage(player, "Sait §d" + exp + " §7kokemusta!");
                        item.setAmount(item.getAmount() - 1);
                        if(item.getAmount() < 1) player.getInventory().remove(item);
                    }

                }

            }

        }

    }

}
