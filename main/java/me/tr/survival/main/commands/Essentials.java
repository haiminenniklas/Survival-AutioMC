package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
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
import org.bukkit.event.player.PlayerMoveEvent;

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

                    player.sendMessage("§7§m-----------------------");
                    player.sendMessage(" §6§lApua-valikko");
                    player.sendMessage( "§7§oKirjoita §6§o/apua <...>");
                    player.sendMessage(" ");
                    player.sendMessage(" §6...komennot §7Hyödylliset komennot");
                    player.sendMessage(" §6...vaihto §7Tietoa vaihtokaupasta");
                    player.sendMessage(" §6...arvot §7Palvelimen VIP-arvot");
                    player.sendMessage(" §6...aloitus §7Vinkit survivalin aloitukseen");
                    player.sendMessage(" §6...tehostukset §7Mitä tehostukset ovat?");
                    player.sendMessage(" §6...asetukset §7Mitkä asetukset?");
                    player.sendMessage(" §6...valuutta §7Miten raha toimii täällä?");
                    player.sendMessage(" §6...tehtävät §7Mitä ne ovat?");
                    player.sendMessage("§7§m-----------------------");

                } else {

                    if(args[0].equalsIgnoreCase("komennot")) {

                        Gui.openGui(player, "Apua (Komennot)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Hyödylliset komennot", Arrays.asList(
                                    "§7§m--------------------",
                                    " §7Hyödylliset komennot:",
                                    "  §6/profiili §7tämä valikko",
                                    "  §6/rtp §7vie sinut arämaahan",
                                    "  §6/msg §7yksityisviestit",
                                    "  §6/tpa §7teleporttauspyyntö",
                                    "  §6/warp §7palvelimen warpit",
                                    "  §9/discord §7Discord-yhteisö",
                                    "  §6/huutokauppa §7huutokauppa",
                                    "  §a/osta §7verkkokauppa",
                                    "§7§m--------------------"
                            )), 13);
                        });
                    } else if(args[0].equalsIgnoreCase("vaihto")) {
                        Gui.openGui(player, "Apua (Vaihtokauppa)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Niin mikä?", Arrays.asList(
                                    "§7§m--------------------",
                                    " §7Palvelimellemme on rakennettu",
                                    " §6vaihtokauppa§7-järjestelmä, jonka",
                                    " §7on tarkoitus suojata kummankin",
                                    " §7osapuolen §barvotavarat§7.",
                                    " §7Järjestelmä pyrkii, ettei tapahdu",
                                    " §cpetoksia§7 tai §cvarastamista§7.",
                                    "§7§m--------------------"
                            )), 12);

                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Miten toimii?", Arrays.asList(
                                    "§7§m--------------------",
                                    " §7Vaihtokauppa tapahtuu niinkin",
                                    " §7yksinkertaisesti siten, että",
                                    " §7kirjoitat chattiin §6/vaihto <pelaaja>",
                                    " §7ja kun vastaanottaja hyväksyy pyynnön",
                                    " §7teille avautuu näkymä, johon voitte",
                                    " §7siirtää tavaranne ja vasta kun molemmat",
                                    " §7ovat hyväksyneet vaihdon, vaihtokauppa",
                                    " §7iskee voimaan.",
                                    "§7§m--------------------"
                            )), 14);

                        });
                    } else if(args[0].equalsIgnoreCase("arvot")) {

                        Gui.openGui(player, "Apua (Arvot)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6VIP-arvot", Arrays.asList(
                                    "§7§m--------------------",
                                    " §7Palvelimellamme on §62",
                                    " §7VIP-arvoa; §a§lPremium",
                                    " §7sekä §6§lPremium§e§l+§7.",
                                    " §7VIP-arvot ovat yksi tapa",
                                    " §7tukea palvelimen toimintaa",
                                    " §7ja pitämällä sen mahdollisimman",
                                    " §7pitkään toiminnassa",
                                    " ",
                                    " §7Lisää VIP-arvoista",
                                    " §7verkkokaupastamme:",
                                    " §animeton.fi/kauppa",
                                    "§7§m--------------------"
                            )), 13);
                        });

                    } else if(args[0].equalsIgnoreCase("aloitus")) {
                        Gui.openGui(player, "Apua (Aloittaminen)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Miten aloitan Survivalin?", Arrays.asList(
                                    "§7§m--------------------",
                                    " §7Tätä sivua ei olla tehty",
                                    " §7valmiiksi. Muistathan hoputtaa",
                                    " §cylläpitoa §7tekemäään töitään!",
                                    "§7§m--------------------"
                            )), 13);
                        });
                    } else if(args[0].equalsIgnoreCase("tehostukset")) {
                        Gui.openGui(player, "Apua (Tehostukset)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Mitkä ihmeen tehostukset?", Arrays.asList(
                                    "§7§m--------------------",
                                    " §7Palvelimellamme on käytössä",
                                    " §7ns. §atehostuksia§7, joiden",
                                    " §7tarkoitus vähän nopeutaa ja",
                                    " §7hauskuuttaa pelin kulkua, sekä",
                                    " §7tehdä meidän Survivalista hieman",
                                    " §7§ouniikimman§7, kuin muiden!",
                                    " ",
                                    " §7Lisätietoa tehostuksista",
                                    " §7komennolla §a/tehostus",
                                    "§7§m--------------------"
                            )), 13);
                        });
                    } else if(args[0].equalsIgnoreCase("asetukset")) {
                        Gui.openGui(player, "Apua (Asetukset)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Mitä hyödyn?", Arrays.asList(
                                    "§7§m--------------------",
                                    " §7Olemme tehneet juuri teitä",
                                    " §6pelaajia §7varten palvelimellemme",
                                    " §7asetuksia, joita voit muuttaa",
                                    " §7jotta pelaaminen olisi juuri",
                                    " §esinulle §7mieleisempää",
                                    " ",
                                    " §7Lisätietoa asetuksista komennolla",
                                    " §6/asetukset",
                                    "§7§m--------------------"
                            )), 13);
                        });
                    } else if(args[0].equalsIgnoreCase("valuutta")) {
                        Gui.openGui(player, "Apua (Raha/Valuutta)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Miten toimii?", Arrays.asList(
                                    "§7§m--------------------",
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
                                    "§7§m--------------------"
                            )), 12);
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Mitkä kristallit?", Arrays.asList(
                                    "§7§m--------------------",
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
                                    "§7§m--------------------"
                            )), 14);
                        });
                    } else if(args[0].equalsIgnoreCase("tehtävät")) {
                        Gui.openGui(player, "Apua (Aloittaminen)", 27, (gui) -> {
                            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§6Mitä tehtävät ovat?", Arrays.asList(
                                    "§7§m--------------------",
                                    " §7Tätä sivua ei olla tehty",
                                    " §7valmiiksi. Muistathan hoputtaa",
                                    " §cylläpitoa §7tekemäään töitään!",
                                    "§7§m--------------------"
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

                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', sb.toString()));

                }

            } else if(cmd.getLabel().equalsIgnoreCase("discord")) {

                Chat.sendMessage(player, "Discord: §6https://discord.gg/TBrTmZn");

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

                    player.sendMessage("§7§m---------------------");
                    player.sendMessage(" §7Maailmat (" + Bukkit.getWorlds().size() +  "):");

                    for(World w : Bukkit.getWorlds()) {
                        if(player.getWorld().getName().equalsIgnoreCase(w.getName())) {
                            player.sendMessage("§7- §6" + w.getName() + " §8(sinä)");
                        } else {
                            player.sendMessage("§7- §6" + w.getName());
                        }
                    }

                    player.sendMessage("§7§m---------------------");

                }

            }

        }

        return true;
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
