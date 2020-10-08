package me.tr.survival.main.managers;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.database.data.Crystals;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MoneyManager implements CommandExecutor, Listener {

    private static boolean ENABLED = true;
    private final List<UUID> inChequeConfirmal = new ArrayList<>();
    private final HashMap<UUID, Long> lastChequeWithdrawal = new HashMap<>();

    private final int MAX_CHEQUE_AMOUNT = 50000;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("valuutta")) main(player);
            else if(command.getLabel().equalsIgnoreCase("shekki")) {

                if(args.length < 1) {
                    Chat.sendMessage(player, "Käytä §6/shekki <haluttu määrä>");
                    cheques(player);
                    return true;
                } else {
                    if(player.isOp()) {
                        if(args[0].equalsIgnoreCase("help")) {
                            sender.sendMessage("§7/shekki (enable | disable)");
                        } else if(args[0].equalsIgnoreCase("disable")) {
                            ENABLED = false;
                            sender.sendMessage("§7Shekit on nyt §cpois päältä§7!");
                            return true;
                        } else if(args[0].equalsIgnoreCase("enable")) {
                            ENABLED = true;
                            sender.sendMessage("§77Shekit on nyt §apäällä§7!");
                            return true;
                        }
                    }

                    if(!player.isOp() && !ENABLED) {
                        Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä. Yritähän myöhemmin uudelleen.");
                        return true;
                    }

                    int value;
                    try { value = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ex) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Et kirjoittanut numeroasi oikein..." +
                                " Muistathan, että ei negatiivisia numeroita, desimaaleja tai muuta tyhmää!");
                        return true;
                    }

                    if(value < 1) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei negatiivisia numeroita, tai nollaa!");
                        return true;
                    }

                    if(value > MAX_CHEQUE_AMOUNT) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Hei hei hei! Shekkien suurin määrä voi " +
                                "olla vain §e" + Util.formatDecimals(MAX_CHEQUE_AMOUNT) + "€§7! Yritätkö esittää jotain?");
                        return true;
                    }

                    if(Balance.canRemove(player.getUniqueId(), value)) writeCheque(player, value);
                    else Chat.sendMessage(player, Chat.Prefix.ERROR, "Mitä yrität oikein tehdä? " +
                            "Huijata? Sinulla ei ole rahaa kirjoittaa näin suuria shekkejä...");
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (item != null) {

            ItemMeta meta = item.getItemMeta();
            if (item.getType() == Material.PAPER && item.hasItemMeta() && meta != null) {
                if (meta.hasLore() && meta.hasDisplayName() && meta.hasLore()) {
                    e.setCancelled(true);
                    confirmChequeWithdrawal(player, item);
                }
            }
        }
    }

    public void main(Player player) {

        Gui.openGui(player, "Finanssivalvonta", 27, (gui) -> {

            gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.PAPER,1, "§aShekki", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Haluatko pitää siirtää tai",
                    " §7säilyttää rahaa hieman",
                    " §eperinteisemmällä §7tavalla?",
                    " §7Voit kirjoittaa shekkejä,",
                    " §7joihin voit tallettaa",
                    " §7haluamasi rahamäärän. Myöhemmin",
                    " §7kun klikkaat tätä shekkiä,",
                    " §7saat kyseisen rahamäärän",
                    " §7tilillesi!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    cheques(clicker);
                }
            });

            int[] glass = new int[] { 11,12, 14,15 };
            for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.ORANGE_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }

        });

    }

    private void cheques(Player player) {

        Gui.openGui(player, "Kirjoita shekkejä", 27, (gui) -> {
            gui.addButton(new Button(1, 11, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a1 000€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää",
                    " §a1 000€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 50);
                }
            });

            gui.addButton(new Button(1, 12, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a5 000€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää",
                    " §a5 000€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 100);
                }
            });

            gui.addButton(new Button(1, 13, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a10 000€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää",
                    " §a10 000€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 10000);
                }
            });

            gui.addButton(new Button(1, 14, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a25 000€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää",
                    " §a25 000€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, 25000);
                }
            });

            gui.addButton(new Button(1, 15, Util.makeEnchanted(ItemUtil.makeItem(Material.PAPER, 1, "§a" + Util.formatDecimals(MAX_CHEQUE_AMOUNT) + "€", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Tämä kirjoittaa sinulle",
                    " §7shekin, joka sisältää",
                    " §a" + Util.formatDecimals(MAX_CHEQUE_AMOUNT) + "€§7!",
                    " ",
                    " §aKlikkaa kirjoittaaksesi!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    writeCheque(clicker, MAX_CHEQUE_AMOUNT);
                }
            });

            gui.addItem(1, ItemUtil.makeItem(Material.BOOK, 1, "§2Muu määrä?", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Jos haluat kirjoittaa jonkin",
                    " §7toisen määrän, se onnistuu",
                    " §7komennolla:",
                    " §a/shekki <haluttu rahamäärä>§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 8);


            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    main(clicker);
                }
            });

            int[] glass = new int[] { 10, 16 };
            for(int slot : glass) { gui.addItem(1, ItemUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }

        });

    }

    @Deprecated
    private String getMoneyString(Player player, int crystalsWanted) {
        int price = getPriceForCrystals(crystalsWanted);
        return Balance.canRemove(player.getUniqueId(), price) ? "§a" + price : "§c" + price;
    }

    @Deprecated
    public void changeMoneyToCrystals(Player player, int crystalsWanted) {

        int price = 15000 * crystalsWanted;
        if(Balance.canRemove(player.getUniqueId(), price)) {
            Balance.remove(player.getUniqueId(), price);
            Crystals.add(player.getUniqueId(), crystalsWanted);
        } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän...");

    }

    private void writeCheque(Player player, int amount) {

        if(!player.isOp() && !ENABLED) {
            Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä. Yritähän myöhemmin uudelleen.");
            return;
        }

        if(!Balance.canRemove(player.getUniqueId(), amount)) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän...");
            return;
        }

        if(amount > MAX_CHEQUE_AMOUNT) return;

        Balance.remove(player.getUniqueId(), amount);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        forceWriteCheque(player, amount);
        Chat.sendMessage(player, "Kirjoitit shekin, joka sisältää §a" + amount + "€§7!");

        if(amount >= 10000) {
            for(Player online : Bukkit.getOnlinePlayers()) {
                if(Main.getStaffManager().hasStaffMode(online)) {
                    Util.sendClickableText(online, "§8[§e§l⚡§8] §fPelaaja §e" + player.getName() + " §fkirjoitti shekin!", "/dummy", "§7Määrä: §a" + amount + "€§7");
                }
            }
        }

    }

    public void forceWriteCheque(Player player, int amount) {

        final String today = Util.getToday();
        ItemStack item = ItemUtil.makeItem(Material.PAPER, 1, "§a§lShekki", Arrays.asList(
                " §7Tämä shekki sisältää ",
                " §a" + amount + "€§7!",
                " §7Kun klikkaat tätä itemiä",
                " §7saat pankkitilillesi rahat!",
                " §7Voit antaa tämän myös kaverillesi",
                " §7pienenä §dlahjoituksena§7!",
                " ",
                " §7Shekin kirjoittanut: §a" + player.getName(),
                " §7Shekki kirjoitettu: §e" + today
        ));

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "cheque-amount");
        final long now = System.currentTimeMillis();
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, amount);
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "write-time"), PersistentDataType.LONG, now);
            UUID createdUUID = UUID.randomUUID();
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "uuid"), PersistentDataType.STRING, createdUUID.toString());
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "writer"), PersistentDataType.STRING, player.getName());
            item.setItemMeta(itemMeta);

            Sorsa.logColored(" §6[Cheques] Player '" + player.getName() + "' (" + player.getUniqueId() + ") wrote or was given by the plugin a cheque worth of " + Util.formatDecimals(amount) + "! Date: " + today + " UUID: " + createdUUID);


            HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(Util.makeEnchanted(item));
            for(Map.Entry<Integer, ItemStack> entry : unadded.entrySet()) { player.getWorld().dropItemNaturally(player.getLocation(), entry.getValue()); }
        }
    }

    public boolean isLegacyCheque(final ItemStack item) {

        if(isCheque(item)) {

            NamespacedKey writeTimeKey = new NamespacedKey(Main.getInstance(), "write-time");
            ItemMeta meta = item.getItemMeta();
            if(meta != null && meta.hasLore()) {
                if(!meta.getPersistentDataContainer().has(writeTimeKey, PersistentDataType.LONG)) return true;
            }

        }

        return false;
    }

    private boolean containsUUID(final ItemStack item) {

        if(isCheque(item)) {

            NamespacedKey uuidKey = new NamespacedKey(Main.getInstance(), "uuid");
            ItemMeta meta = item.getItemMeta();
            if(meta != null && item.hasItemMeta()) {
                return meta.getPersistentDataContainer().has(uuidKey, PersistentDataType.STRING);
            }

        }

        return false;
    }

    private void withdrawCheque(Player player, ItemStack cheque, UUID givenUUID) {

        if(!player.isOp() && !ENABLED) {
            Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä. Yritähän myöhemmin uudelleen.");
            return;
        }

        if(this.lastChequeWithdrawal.containsKey(player.getUniqueId())) {

            final long lastWithdrawal = this.lastChequeWithdrawal.get(player.getUniqueId());
            final long now = System.currentTimeMillis();

            long timePassed = now - lastWithdrawal;

            // Less than 5 minutes ago
            if(timePassed < 1000 * 60 * 5) {
                if(!Main.getStaffManager().hasStaffMode(player)) {
                    long whenCanWithdraw = lastWithdrawal + (1000 * 60 * 5);
                    long timeLeftRaw = (whenCanWithdraw - now) / 1000;

                    long minutes = (int) timeLeftRaw / 60;
                    long seconds = timeLeftRaw - (60 * minutes);

                    String timeLeft = Util.formatTime((int) minutes, (int) seconds, true);

                    Chat.sendMessage(player, Chat.Prefix.ERROR, "§7Voit nostaa shekkejä §c5 minuutin§7 välein! " +
                            "Odotathan vielä siis §c" + timeLeft + "§7!");
                    return;
                }
            }

        }

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "cheque-amount");
        NamespacedKey uuidKey = new NamespacedKey(Main.getInstance(), "uuid");
        NamespacedKey writerKey = new NamespacedKey(Main.getInstance(), "writer");
        ItemMeta itemMeta = cheque.getItemMeta();
        if(itemMeta != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            if(container.has(key, PersistentDataType.INTEGER)) {

                if(isLegacyCheque(cheque)) {
                    Chat.sendMessage(player, "Valitettavasti tuo shekki ei ole enää kelpoinen nostettavaksi! " +
                            "Mikäli shekin rahan määrä on huomattava, voit olla ylläpitoon yhteydessä rahan takaisin saamiseksi!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }

                if(player.getInventory().getItemInMainHand().getType() != Material.PAPER) {
                    Chat.sendMessage(player, "Näyttäisi siltä, että shekki on jotenkin kadonnut kädestäsi..." +
                            " Shekin täytyy olla kädessäsi, jotta nostaminen onnistuu! Otathan huomioon myös sen, että" +
                            " shekin täytyy olla sinun oikeassa kädessä, kun sitä nostat!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }

                if(!inChequeConfirmal.contains(player.getUniqueId())) {
                    Chat.sendMessage(player, "Jokin meni nyt hassusti... Yritäppä uudelleen.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }

                UUID foundUUID = UUID.fromString(container.get(uuidKey, PersistentDataType.STRING));

                if(!foundUUID.equals(givenUUID)) {
                    Chat.sendMessage(player, "En tiedä mitä yrität, mutta se miten yrität nyt nostaa shekkejä" +
                            " ei ihan toimi. Pidä nostettava shekki aina kädessäsi, kun sitä yrität nostaa...");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }

                int foundValue = container.get(key, PersistentDataType.INTEGER);

                if(foundValue > MAX_CHEQUE_AMOUNT) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    Chat.sendMessage(player, "Et yksin pysty nostamaan yli §e50 000€ §7shekkejä... Sinun " +
                            "täytyy pyytää ylläpidoltamme apua tämän nostamiseen! Tee apulipuke meidän Discord-palvelimellamme! " +
                            "§9/discord§7!");
                    return;
                }

                String writer = "No writer found";
                if(container.has(writerKey, PersistentDataType.STRING)) {
                    writer = container.get(writerKey, PersistentDataType.STRING);
                }

                Balance.add(player.getUniqueId(), foundValue);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                cheque.setAmount(cheque.getAmount() - 1);
                if(cheque.getAmount() < 1) player.getInventory().remove(cheque);
                player.updateInventory();
                Chat.sendMessage(player, "Nostit shekin, joka sisälsi §e" + foundValue + "€§7! Shekkejä voit kirjoittaa komennolla §a/valuutta§7!");
                Sorsa.logColored(" §6[Cheques] Player '" + player.getName() + "' (" + player.getUniqueId() + ") withdrew a cheque worth " + Util.formatDecimals(foundValue) + "! Date: " + Util.getToday() + ", Writer: " + writer);
                this.lastChequeWithdrawal.put(player.getUniqueId(), System.currentTimeMillis());
                if(foundValue >= 10000) {
                    for(Player online : Bukkit.getOnlinePlayers()) {
                        if(Main.getStaffManager().hasStaffMode(online)) {
                            Util.sendClickableText(online, "§8[§e§l⚡§8] §fPelaaja §e" + player.getName() + " §fnosti shekin! (§e" + Util.formatDecimals(foundValue) + "€§f)", "/dummy", "§7Määrä: §a" + Util.formatDecimals(foundValue) + "€§7");
                        }
                    }
                }
                inChequeConfirmal.remove(player.getUniqueId());
            }
        }

    }

    private void confirmChequeWithdrawal(Player player, ItemStack cheque) {

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "cheque-amount");
        ItemMeta itemMeta = cheque.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        // Confirm that the item is an actual cheque

        if (container.has(key, PersistentDataType.INTEGER)) {
            int foundValue = container.get(key, PersistentDataType.INTEGER);

            if(foundValue > MAX_CHEQUE_AMOUNT) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                Chat.sendMessage(player, "Et yksin pysty nostamaan yli §e50 000€ §7shekkejä... Sinun " +
                        "täytyy pyytää ylläpidoltamme apua tämän nostamiseen! Tee apulipuke meidän Discord-palvelimellamme! " +
                        "§9/discord§7!");
                return;
            }

            if(!containsUUID(cheque)) {
                Chat.sendMessage(player, "Shekissäsi oli ongelma, joka piti korjata. Yritä uudelleen shekin nostamista!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, UUID.randomUUID().toString());
                cheque.setItemMeta(itemMeta);
                player.updateInventory();
                return;
            }

            if(inChequeConfirmal.contains(player.getUniqueId())) {
                inChequeConfirmal.remove(player.getUniqueId());
                Chat.sendMessage(player, "Jokin meni nyt hassusti... Yritäppä uudelleen. ");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                return;
            }

            inChequeConfirmal.add(player.getUniqueId());

            Gui.openGui(player, "Varmista Shekin nosto (" + foundValue + "€)", 27, (gui) -> {

                gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Shekki katoaa inventorystäsi",
                        " §7ja tilillesi laitetaan shekin",
                        " §7sisältämä summa (§e" + foundValue + "€§7)",
                        " ",
                        " §aKlikkaa nostaaksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        UUID foundUUID = UUID.fromString(container.get(new NamespacedKey(Main.getInstance(), "uuid"), PersistentDataType.STRING));
                        withdrawCheque(player, cheque, foundUUID);
                    }
                });

                gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Shekki jää inventoryysi",
                        " §7ja mitään ei tapahdu. Pystyt",
                        " §7silti nostamaan shekin rahan",
                        " §7tilillesi myöhemmin!",
                        " ",
                        " §cKlikkaa peruuttaaksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Shekin nostaminen peruutettiin");
                        inChequeConfirmal.remove(player.getUniqueId());
                    }
                });
            });
        }
    }

    public boolean isCheque(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if(meta != null && meta.hasLore() && meta.hasDisplayName()) {
            if(item.getType() == Material.PAPER) {
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "cheque-amount");
                PersistentDataContainer container = meta.getPersistentDataContainer();
                return container.has(key, PersistentDataType.INTEGER);
            }
        }
        return false;
    }

    @Deprecated
    private int getPriceForCrystals(int crystalsWanted) { return 15000 * crystalsWanted; }

}
