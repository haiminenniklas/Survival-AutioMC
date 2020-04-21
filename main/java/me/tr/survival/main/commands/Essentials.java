package me.tr.survival.main.commands;

import com.comphenix.protocol.utility.MinecraftReflection;
import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
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

            if(cmd.getLabel().equalsIgnoreCase("afk")) {

                Chat.sendMessage(player, Chat.Prefix.ERROR, "Komento ei käytössä!");

              /*  if(afk.contains(uuid)) {
                    Chat.sendMessage(player, "Et ole enää AFK!");
                    Bukkit.broadcastMessage("§6§l» §7Pelaaja §6" + player.getName() + "§7 ei ole enää AFK!");
                } else {
                    Chat.sendMessage(player, "Olet nyt AFK!");
                    Bukkit.broadcastMessage("§6§l» §7Pelaaja §6" + player.getName() + "§7 on nyt AFK");
                } */

            } else if(cmd.getLabel().equalsIgnoreCase("apua")) {

                if(args.length < 1) {

                    player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    player.sendMessage(" §2§lApua-valikko");
                    player.sendMessage( "§7§oKirjoita §a§o/apua <...>");
                    player.sendMessage(" ");
                    player.sendMessage(" §a...komennot §7Hyödylliset komennot");
                    //player.sendMessage(" §6...vaihto §7Tietoa vaihtokaupasta");
                    player.sendMessage(" §a...huutokauppa §7Tietoa huutokaupasta");
                    player.sendMessage(" §a...arvot §7Palvelimen VIP-arvot");
                    player.sendMessage(" §a...aloitus §7Vinkit survivalin aloitukseen");
                    player.sendMessage(" §a...tehostukset §7Mitä tehostukset ovat?");
                    player.sendMessage(" §a...asetukset §7Mitkä asetukset?");
                    player.sendMessage(" §a...valuutta §7Miten raha toimii täällä?");
                    player.sendMessage(" §a...tehtävät §7Miten suoritat tehtäviä");
                    player.sendMessage(" §a...matkustaminen §7Miten toimii??");
                    player.sendMessage(" §a...reppu §7Kätevä tapa tallettaa tavarat");
                    player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                } else {

                    if(args[0].equalsIgnoreCase("komennot")) {

                        Gui.openGui(player, "Apua (Komennot)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Hyödylliset komennot", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Hyödylliset komennot:",
                                    "  §a/profiili §7tämä valikko",
                                    "  §a/rtp §7vie sinut arämaahan",
                                    "  §a/msg §7yksityisviestit",
                                    "  §a/tpa §7teleporttauspyyntö",
                                    "  §a/warp §7palvelimen warpit",
                                    "  §9/discord §7Discord-yhteisö",
                                    "  §a/huutokauppa §7huutokauppa",
                                    "  §e/osta §7verkkokauppa",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);
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

                        });
                    } else if(args[0].equalsIgnoreCase("arvot")) {

                        Gui.openGui(player, "Apua (Arvot)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6VIP-arvot", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellamme on §63",
                                    " §7VIP-arvoa: §aPremium§7,",
                                    " §aPremium§f+§7 sekä",
                                    " §a§lSORSA§7.",
                                    " §7VIP-arvot ovat yksi tapa",
                                    " §7tukea palvelimen toimintaa",
                                    " §7ja pitämällä sen mahdollisimman",
                                    " §7pitkään toiminnassa",
                                    " ",
                                    " §7Lisää VIP-arvoista",
                                    " §7verkkokaupassamme:",
                                    " §awww.sorsamc.fi/kauppa",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);
                        });

                    } else if(args[0].equalsIgnoreCase("aloitus")) {
                        Gui.openGui(player, "Apua (Aloittaminen)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Miten aloitan Survivalin?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Tätä sivua ei olla tehty",
                                    " §7valmiiksi. Muistathan hoputtaa",
                                    " §cylläpitoa §7tekemäään töitään!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);
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
                        });
                    } else if(args[0].equalsIgnoreCase("valuutta") || args[0].equalsIgnoreCase("raha")) {
                        Gui.openGui(player, "Apua (Raha/Valuutta)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Miten toimii?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellamme on valuutta §eEuro",
                                    " §7(§e€§7), jolla voit ostaa huuto-",
                                    " §7kaupasta esineitä. Saat myös näitä",
                                    " §eeuroja§7, kun saat huutokaupattua",
                                    " §7tavaroitasi. Palvelimellamme ei ole",
                                    " §7ylläpidön ylläpitämää kauppaa, vain",
                                    " §7pelaajien ylläpitämä huutokauppa.",
                                    " §7(§a/huutokauppa§7)",
                                    " §7Voit myös maksaa ja vastaanottaa",
                                    " §7rahaa muilta pelaajilta §7(§a/raha§7)!",
                                    " §7Aluksi kaikki pelaajat saavat",
                                    " §a1000€§7!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 12);
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitkä kristallit?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellamme on kuitenkin",
                                    " §7toisenlainenkin §6valuutta§7. Nämä",
                                    " §7ovat ns. §bkristallit§7. Näillä",
                                    " §7voit ostaa erilaisia §atehostuksia§7,",
                                    " §7sekä erikoistavaroita ja -kykyjä.",
                                    " §7Ne ovat harvinaisia ja niitä voi mm.",
                                    " §7tippua arvokkaiden mineraalien mukana!",
                                    " §7Niitä myös tuodaan sinulle §dpäivittäisen",
                                    " §dtoimituksen §7mukana, joka on",
                                    " §7haettavissa postimieheltä! Niitä voi",
                                    " §7myös ostaa §averkkokaupastamme!",
                                    " §7§o(/osta)",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 14);

                            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§aHyödylliset komennot", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §a/rahatilanne §7Rahatilanteesi",
                                    " §a/maksa §7Maksa rahaa pelaajalle",
                                    " §a/valuutta §7Vaihda rahaa",
                                    " §a/baltop §7Rikkaimmat pelaajat",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 26);

                        });
                    } else if(args[0].equalsIgnoreCase("tehtävät")) {
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
                    } else if(args[0].equalsIgnoreCase("matkustaminen")) {
                        Gui.openGui(player, "Apua (Matkustaminen)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitä tarkoittaa?", Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §7Palvelimellamme voit matkustaa",
                                    " §7sekä §5Endiin§7, että §cNetheriin§7!",
                                    " §7Netheriin matkustaminen on ilmaista,",
                                    " §7mutta Endiin se on §amaksullista§7.",
                                    " §7Endiin matkustamiseen vaaditaan erityis-",
                                    " §7esine. Tämän pysty craftaamaan normaalisti,",
                                    " §7ja sen resepti löytyy §9Discord§7-",
                                    " §7palvelimeltamme!",
                                    " ",
                                    " §7Lisätietoa matkustamisesta: §a/matkustaminen",
                                    " §7Discord: §9/discord",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            )), 13);
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
                        });
                    } else if(args[0].equalsIgnoreCase("huutokauppa")) {
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
                    }

                }

            } else if(cmd.getLabel().equalsIgnoreCase("broadcast")) {

                if(Ranks.isStaff(uuid)) {

                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < args.length; i++) {
                        sb.append(args[i] + " ");
                    }

                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "➤ &2&lILMOITUS " + sb.toString()));

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

                }

            } else if(cmd.getLabel().equalsIgnoreCase("pullota")) {

                Chat.sendMessage(player, "Ei käytössä!");

               /* if(args.length < 1) {
                    Chat.sendMessage(player, "Käytä: §6/pullota <XP Määrä>");
                    return true;
                } else {

                    int value;
                    try {
                        value = Integer.parseInt(args[0]);
                    } catch(NumberFormatException ex) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytä numeroita!");
                        return true;
                    }

                    if(value < 1) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Vain positiivisia numeroita!");
                        return true;
                    }

                    int playerTotalXP = player.getTotalExperience();
                    if(playerTotalXP - value < 0) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole tarpeeksi kokemusta!");
                        return true;
                    }

                    player.setTotalExperience(playerTotalXP - value);

                    ItemStack item = createCustomXPBottle(value);
                    player.getInventory().addItem(item);
                    Chat.sendMessage(player, "Pullotit §d" + value + " §7kokemusta!");

                } */

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

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();

        // Has moved a block
        if(from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ() || from.getBlockY() != to.getBlockY()) {

            if(afk.contains(player.getUniqueId())) {
                Bukkit.broadcastMessage("§6§l» §7Pelaaja §6" + player.getName() + "§7 ei ole enää AFK!");
            }

            moved.put(player.getUniqueId(), System.currentTimeMillis());
            Autio.afterAsync(120, () -> {
                long currentTime = System.currentTimeMillis();

                if(moved.containsKey(player.getUniqueId())) {

                    long lastMoved = moved.get(player.getUniqueId());

                    // If not moved in more than 2 minutes
                    if((currentTime - lastMoved) / 1000 / 60 / 60 >= 120 && Bukkit.getPlayer(player.getUniqueId()) != null) {
                        afk.add(player.getUniqueId());
                        Bukkit.broadcastMessage("§6§l» §7Pelaaja §6" + player.getName() + "§7 on nyt AFK");
                    }

                }

            });

        }

    }

}
