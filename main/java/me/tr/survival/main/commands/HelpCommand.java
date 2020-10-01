package me.tr.survival.main.commands;

import me.tr.survival.main.Main;
import me.tr.survival.main.managers.Settings;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            final Player player = (Player) sender;

            if(args.length < 1) {

                player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                player.sendMessage(" §2§lApua-valikko");
                player.sendMessage( "§7§oKirjoita §a§o/apua <...>");
                player.sendMessage(" ");
                Util.sendClickableText(player, " §a...komennot §7Hyödylliset komennot", "/apua komennot", "§7Klikkaa avataksesi! (§a/apua komennot§7)!");
                Util.sendClickableText(player, " §a...vaihto §7Tietoa vaihtokaupasta", "/apua vaihto", "§7Klikkaa avataksesi! (§a/apua vaihto§7)!");
                Util.sendClickableText(player, " §a...arvot §7Palvelimen VIP-arvot", "/apua arvot", "§7Klikkaa avataksesi! (§a/apua arvot§7)!");
                Util.sendClickableText(player, " §a...aloitus §7Vinkit survivalin aloitukseen", "/apua aloitus", "§7Klikkaa avataksesi! (§a/apua aloitus§7)!");
                Util.sendClickableText(player, " §a...tehostukset §7Mitä tehostukset ovat?", "/apua tehostukset", "§7Klikkaa avataksesi! (§a/apua tehostukset§7)!");
                Util.sendClickableText(player, " §a...asetukset §7Mitkä asetukset?", "/apua asetukset", "§7Klikkaa avataksesi! (§a/apua asetukset§7)!");
                Util.sendClickableText(player, " §a...valuutta §7Miten raha toimii täällä?", "/apua valuutta", "§7Klikkaa avataksesi! (§a/apua valuutta§7)!");
                Util.sendClickableText(player, " §a...matkustaminen §7Miten toimii??", "/apua matkustaminen", "§7Klikkaa avataksesi! (§a/apua matkustaminen§7)! ");
                Util.sendClickableText(player, " §a...reppu §7Kätevä tapa tallettaa tavarat", "/apua reppu", "§7Klikkaa avataksesi! (§a/apua reppu§7)!");
                Util.sendClickableText(player, " §a...ääri §7Miten ja ketkä pääsevät sinne?", "/apua ääri", "§7Klikkaa avataksesi! (§a/apua ääri§7)!");
                Util.sendClickableText(player, " §a...chat §7Miten lähetän kaikille viestin?", "/apua chat", "§7Klikkaa avataksesi! (§a/apua chat§7)!");
                Util.sendClickableText(player, " §a...kylät §7Pelaajakylät? Whaaat?", "/apua kylät", "§7Klikkaa avataksesi! (§a/apua kylät§7)!");
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

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

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

                        int[] glass = new int[] { 11,13,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

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

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

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
                                " §7Saat §e100€ §7aloitusrahaksi, jonka",
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

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

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

                        gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.SUNFLOWER, 1, "§eMatkustaminen", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §7Lue lisää matkustamisesta",
                                " §7palvelimellamme!",
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

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

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

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

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
                                " §a100€§7!",
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

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

                        for(int i = 0; i < 27; i++) {
                            if(gui.getItem(i) != null) continue;
                            if(gui.getButton(i) != null) continue;
                            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                        }


                    });
                } else if(args[0].equalsIgnoreCase("matkustaminen")) {
                    Gui.openGui(player, "Apua (Matkustaminen)", 27, (gui) -> {

                        gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitä tarkoittaa?", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §7Palvelimellamme voit matkustaa",
                                " §7sekä §5Endiin§7, että §cNetheriin§7!",
                                " §7Netheriin matkustaminen on ilmaista,",
                                " §7mutta Endiin se on §amaksullista§7.",
                                " §7Endiin matkustamiseen vaaditaan ",
                                " §e175 000€ §7rahaa ja voit kutsua",
                                " §a2 kaveriasi §7mukaan samalla!",
                                " ",
                                " §7Lisätietoa matkustamisesta: §a/matkustaminen",
                                " §7Discord: §9/discord",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        )), 13);

                        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.MAP, 1, "§eMatkustaminen", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §7Tästä klikkaamalla, pääset",
                                " §7matkustamaan eri §emaailmoihin§7!",
                                " ",
                                " §aKlikkaa avataksesi",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        ))) {
                            @Override
                            public void onClick(Player clicker, ClickType clickType) {
                                gui.close(clicker);
                                Main.getTravelManager().gui(clicker);
                            }
                        });

                        gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.BOOK, 1, "§2Lue lisää", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §7Lue lisää tietääksesi",
                                " §7enemmän §5Endin §7toiminnasta",
                                " §7ja mm. miten sinne pääsee,",
                                " §7paljonko maksaa ja pääseekö",
                                " §ekaveritkin §7mukaan!",
                                " ",
                                " §aKlikkaa lukeaksesi!",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        ))) {
                            @Override
                            public void onClick(Player clicker, ClickType clickType) {
                                gui.close(clicker);
                                Bukkit.dispatchCommand(clicker, "apua ääri");
                            }
                        });

                        int[] glass = new int[] { 11,12, 14, 15 };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

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

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

                        for(int i = 0; i < 27; i++) {
                            if(gui.getItem(i) != null) continue;
                            if(gui.getButton(i) != null) continue;
                            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                        }

                    });
                } else if(args[0].equalsIgnoreCase("chat")) {

                    Gui.openGui(player, "Apua (Chat)", 27, (gui) -> {
                        gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.BOOK, 1, "§2Paikallinen vai Globaali?", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §7Palvelimella chatilla on kaksi",
                                " §7eri tilaa: §aGlobaali §7ja §ePaikallinen§7!",
                                " §7ja pystyt tilaa vaihtelemaan näiden",
                                " §7välillä. Kuitenkin, jos sinulla on",
                                " §ePaikallinen §7tila käytössä, §aGlobaali",
                                " §7toimii kun laitat '§a!§7'-merkin",
                                " §7chat-viestisi eteen!",
                                " ",
                                " §ePaikallinen §7chat lähettää viestin",
                                " §7lähimmille pelaajille alueella.",
                                " §7Alue on §a400x400 §7blockia sijainnistasi!",
                                " §aGlobaali §7chat lähettää viestin kaikille",
                                " §7Survival-palvelimella!",
                                " ",
                                " §7Lisätietoa §a/asetukset§7!",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        ))) {
                            @Override
                            public void onClick(Player clicker, ClickType clickType) {
                                gui.close(clicker);
                                Settings.panel(clicker);
                            }
                        });

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

                        for(int i = 0; i < 27; i++) {
                            if(gui.getItem(i) != null) continue;
                            if(gui.getButton(i) != null) continue;
                            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                        }

                    });
                } else if(args[0].equalsIgnoreCase("kylät") || args[0].equalsIgnoreCase("kylä")
                        || args[0].equalsIgnoreCase("pelaajakylät") || args[0].equalsIgnoreCase("pelaajakylä")) {

                    Gui.openGui(player, "Apua (Pelaajakylät)", 27, (gui) -> {
                        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.BOOK, 1, "§2Mitkä kylät?", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §ePelaajakylät §7ovat tapa luoda yhteisöjä",
                                " §7ja tapa tienata rahaa yhdessä helposti!",
                                " §7Pystytte perustamaan yhteisen julkisen",
                                " §atorin §7ja myydä esineitä, sekä maksaa",
                                " §7myöhemmin §everoja §7yhteen pussiin ja",
                                " §7tulla suurimmaksi yhteisöksi koko palvelimella!",
                                " ",
                                " §7Lisätietoa §a/kylä§7! Tai §apaina minua§7!",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        ))) {
                            @Override
                            public void onClick(Player clicker, ClickType clickType) {
                                gui.close(clicker);
                                Main.getVillageManager().mainGui(clicker);
                            }
                        });

                        gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§2Hyödylliset komennot", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §a/kylä §7Pää näkymä",
                                " §a/kylä top §7Rikkaimmat kylät",
                                " §a/kylä poistu §7Poistu kylästä",
                                " §a/kylä nimi <uusi nimi> §7Nimeä kyläsi uudelleen",
                                " §a/kylä kutsu <pelaaja> §7Kutsu pelaaja kylääsi",
                                " §a/kylä <hakutermi> §7Etsi kyliä",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        )), 14);

                        int[] glass = new int[] { 11, 15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

                        for(int i = 0; i < 27; i++) {
                            if(gui.getItem(i) != null) continue;
                            if(gui.getButton(i) != null) continue;
                            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                        }

                    });
                } else if(args[0].equalsIgnoreCase("ääri") || args[0].equalsIgnoreCase("end")) {
                    Gui.openGui(player, "Apua (Ääri)", 27, (gui) -> {
                        gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Miten Endi toimii?", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §5End §7on paikka missä moni",
                                " §7pystyy rikastumaan ja saamaan",
                                " §7himoitun §9Elytran§7! Sinne",
                                " §7pääsy maksaa, ja paljon. Huimat",
                                " §a§l175 000€§7! Mutta jotta matka",
                                " §7olisi edes jotenkin väärti, niin",
                                " §7on kuitenkin mahdollista kutsua",
                                " §a2 kaveria §7mukaan seikkailuun!",
                                " §7On silti huomioitavaa, että",
                                " §7Endissä on §cPVP §7päällä sekä",
                                " §7sen, että Endin vuokraus",
                                " §7kestää §c3h§7!",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        )), 13);

                        gui.addButton(new Button(1, 8, ItemUtil.makeItem(Material.MAP, 1, "§eMatkustaminen", Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                " §7Tästä klikkaamalla, pääset",
                                " §7matkustamaan eri §emaailmoihin§7!",
                                " ",
                                " §aKlikkaa avataksesi",
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        ))) {
                            @Override
                            public void onClick(Player clicker, ClickType clickType) {
                                gui.close(clicker);
                                Main.getTravelManager().gui(clicker);
                            }
                        });

                        int[] glass = new int[] { 11,12, 14,15  };
                        for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

                        for(int i = 0; i < 27; i++) {
                            if(gui.getItem(i) != null) continue;
                            if(gui.getButton(i) != null) continue;
                            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                        }

                    });
                } else if(args[0].equalsIgnoreCase("suojaus")) {

                    player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    player.sendMessage(" ");
                    player.sendMessage(" §7Linkki ohjeisiin siitä, miten suojata talo:");
                    player.sendMessage(" §ahttps://www.youtube.com/watch?v=Ju4B3UlaMNk §7(Klikkaa)");
                    player.sendMessage(" ");
                    player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                }
            }

        }

        return true;
    }
}
