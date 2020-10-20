package me.tr.survival.main.managers;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

//TODO: Finish this
public class ClaimBlockCoupons implements CommandExecutor, Listener {

    private boolean ENABLED = true;
    private final List<UUID> inChequeConfirmal = new ArrayList<>();
    private final HashMap<UUID, Long> lastChequeWithdrawal = new HashMap<>();

    @Override
    public boolean onCommand( CommandSender sender,  Command command,  String label,   String[] args) {

        if(sender instanceof Player) {

            final Player player = (Player) sender;
            if(args.length < 1) {
                if(player.isOp()) {
                    Chat.sendMessage(player, "/cbc generate <amount>");
                    Chat.sendMessage(player, "/cbc (enable | disable)");
                }
                Chat.sendMessage(player, "Käytä §a/valtaus <määrä> §7niin voit ostaa itsellesi valtaustilaa! Yksi palikka maksaa §e10€§7!");
            } else {

                if(args[0].equalsIgnoreCase("help")) {
                    if(player.isOp()) {
                        Chat.sendMessage(player, "/cbc generate <amount>");
                        Chat.sendMessage(player, "/cbc (enable | disable)");
                    }
                } else if(args[0].equalsIgnoreCase("enable")) {

                    if(player.isOp()) {
                        this.ENABLED = true;
                        Chat.sendMessage(player, "ClaimBlock-kupongit käytössä!");
                    }
                } else if(args[0].equalsIgnoreCase("disable")) {
                    if(player.isOp()) {
                        this.ENABLED = false;
                        Chat.sendMessage(player, "ClaimBlock-kupongit poissa käytöstä!");
                    }
                } else if(args[0].equalsIgnoreCase("generate")) {

                    if(player.isOp()) {
                        if(args.length >= 2) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[1]);
                            } catch (NumberFormatException ex) {
                                Chat.sendMessage(player, "Käytä oikeita numeroita!");
                                return true;
                            }

                            if(amount < 0) {
                                Chat.sendMessage(player, "Ei negatiivisia numeroita!");
                                return true;
                            }
                            giveCoupon(player, amount);
                        } else Chat.sendMessage(player, "/cbc generate <amount>");
                    }

                } else {
                    int amount;
                    try {
                        amount = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ex) {
                        Chat.sendMessage(player, "Käytä vain positiivisia kokonaislukuja! Eli ykkösestä ylöspäin! Ei desimaaleja!");
                        return true;
                    }

                    if(amount < 0) {
                        Chat.sendMessage(player, "Ei negatiivisia numeroita!");
                        return true;
                    }

                    this.confirmCouponCreation(player, amount);

                }
            }
        }
        return true;
    }

    private void confirmCouponCreation(Player player, int amount) {

        Gui gui = new Gui("Varmista valtauskupongin luonti", 27);

        final int totalPrice = amount * 10;

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Vahvista saadaksesi itsellesi",
                " §b" + amount + " suojauspalikkaa§7!",
                " ",
                " §7Suojauspalikat maksavat: §e" + Util.formatDecimals(totalPrice) + "€",
                " ",
                " §aKlikkaa vahvistaaksesi",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                if(Balance.canRemove(clicker.getUniqueId(), totalPrice)) {
                    Balance.remove(clicker.getUniqueId(), totalPrice);
                    giveCoupon(clicker, amount);
                } else {
                    Chat.sendMessage(player, "Sinulla ei ole varaa tähän... Tämä toimenpide maksaa §e" + Util.formatDecimals(totalPrice) + "€");
                }
            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa peruuttaaksesi!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
            }
        });

        gui.open(player);

    }

    private ItemStack registerCoupon(int claimBlocks) {

        ItemStack item = ItemUtil.makeItem(Material.PAPER, 1, "§bSuojauskuponki", Arrays.asList(
                "§7Tällä kupongilla pystyt lisäämään",
                "§7itsellesi suojausblockeja!",
                "§7Sinun pitää vain klikata tätä",
                "§7itemiä kun se on kädessäsi!",
                " ",
                " §7Tämä kuponki sisältää §b" + claimBlocks,
                " §7suojauspalikkaa!",
                " ",
                " §7Kuponki on rekisteröity: §e" + Util.getToday()
        ));

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "claim-amount");
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, claimBlocks);
        meta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "write-time"), PersistentDataType.LONG, System.currentTimeMillis());
        item.setItemMeta(meta);
        return item;

    }

    public void giveCoupon(Player player, int amount) {
        ItemStack item = registerCoupon(amount);
        Sorsa.logColored(" §6[ClaimBlockCoupons] Player '" + player.getName() + "' (" + player.getUniqueId() + ") was given by the plugin a claim-block-coupon worth of " + Util.formatDecimals(amount) + " blocks! Date: " + Util.getToday());
        HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(Util.makeEnchanted(item));
        for(Map.Entry<Integer, ItemStack> entry : unadded.entrySet()) { player.getWorld().dropItemNaturally(player.getLocation(), entry.getValue()); }
    }

    public void generateCoupon(Player player) {
        int amount = new Random().nextInt(150);
        // If the generated amount was below 30
        if(amount < 30) {
            generateCoupon(player);
            return;
        }

        // If the generated amount is not divisible by 10
        if(amount % 10 != 0) {
            generateCoupon(player);
            return;
        }
        giveCoupon(player, amount);
    }

    private void withdrawCoupon(Player player, ItemStack coupon, UUID givenUUID) {

        if(!player.isOp() && !ENABLED) {
            Chat.sendMessage(player, "Tämä toiminto on toistaiseksi poissa käytöstä. Yritähän myöhemmin uudelleen.");
            return;
        }

        if(this.lastChequeWithdrawal.containsKey(player.getUniqueId())) {

            final long lastWithdrawal = this.lastChequeWithdrawal.get(player.getUniqueId());
            final long now = System.currentTimeMillis();

            long timePassed = now - lastWithdrawal;

            // Less than 5 minutes ago
            if(timePassed < 1000 * 60 * 60 * 5) {
                if(!Main.getStaffManager().hasStaffMode(player)) {
                    long whenCanWithdraw = lastWithdrawal + (1000 * 60 * 60 * 5);
                    long timeLeftRaw = (whenCanWithdraw - now) / 1000;

                    long minutes = (int) timeLeftRaw / 60;
                    long seconds = timeLeftRaw - (60 * minutes);

                    String timeLeft = Util.formatTime((int) minutes, (int) seconds, true);

                    Chat.sendMessage(player, Chat.Prefix.ERROR, "§7Voit nostaa kuponkeja §c5 minuutin§7 välein! " +
                            "Odotathan vielä siis §c" + timeLeft + "§7!");
                    return;
                }
            }

        }

        ItemMeta meta = coupon.getItemMeta();
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "claim-amount");
        NamespacedKey uuidKey = new NamespacedKey(Main.getInstance(), "uuid");

        if(meta != null) {

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if(container.has(key, PersistentDataType.INTEGER)) {

                int foundValue = container.get(key, PersistentDataType.INTEGER);

                if(player.getInventory().getItemInMainHand().getType() != Material.PAPER) {
                    Chat.sendMessage(player, "Näyttäisi siltä, että kuponki on jotenkin kadonnut kädestäsi..." +
                            " Kupongin täytyy olla kädessäsi, jotta nostaminen onnistuu! Otathan huomioon myös sen, että" +
                            " kupoingin täytyy olla sinun oikeassa kädessä, kun sitä nostat!");
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
                    Chat.sendMessage(player, "En tiedä mitä yrität, mutta se miten yrität nyt nostaa kuponkeja" +
                            " ei ihan toimi. Pidä nostettava kuponki aina kädessäsi, kun sitä yrität nostaa...");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }


                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "acb " + player.getName() + " " + foundValue);
                coupon.setAmount(coupon.getAmount() - 1);
                if(coupon.getAmount() < 1) player.getInventory().remove(coupon);
                player.updateInventory();
                Chat.sendMessage(player, "Sait §3" + foundValue + " claim blockia §7lisää! Nyt vaan laajentamaan kotia!");
                Sorsa.logColored(" §6[ClaimBlockCoupons] Player '" + player.getName() + "' (" + player.getUniqueId() + ") withdrew a claim-block-coupon worth " + Util.formatDecimals(foundValue) + " blocks! Date: " + Util.getToday());

            }
        }
    }

    public boolean isCoupon(ItemStack item) {

        if(item == null) return false;

        final ItemMeta meta = item.getItemMeta();
        if(meta != null && item.hasItemMeta() && item.getType() == Material.PAPER) {
            if(meta.hasLore() && meta.hasDisplayName()) {
                final PersistentDataContainer container = meta.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "claim-amount");
                if(container.has(key, PersistentDataType.INTEGER)) return true;
            }
        }
        return false;
    }

    private boolean containsUUID(final ItemStack item) {

        if(isCoupon(item)) {

            NamespacedKey uuidKey = new NamespacedKey(Main.getInstance(), "uuid");
            ItemMeta meta = item.getItemMeta();
            if(meta != null && item.hasItemMeta()) {
                return meta.getPersistentDataContainer().has(uuidKey, PersistentDataType.STRING);
            }

        }

        return false;
    }

    private void confirmWithdrawal(Player player, ItemStack coupon) {
        ItemMeta meta = coupon.getItemMeta();
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "claim-amount");
        NamespacedKey uuidKey = new NamespacedKey(Main.getInstance(), "uuid");

        if(meta != null) {

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(key, PersistentDataType.INTEGER)) {
                int foundValue = container.get(key, PersistentDataType.INTEGER);

                if(!containsUUID(coupon)) {
                    Chat.sendMessage(player, "Kupongissasi oli ongelma, joka piti korjata. Yritä uudelleen kupongin nostamista!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    meta.getPersistentDataContainer().set(uuidKey, PersistentDataType.STRING, UUID.randomUUID().toString());
                    coupon.setItemMeta(meta);
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

                Gui gui = new Gui("Vahvista nosto", 27);

                gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Kuponki katoaa inventorystäsi",
                        " §7ja saat käyttöösi §b" + foundValue,
                        " §7suojauspalikkaa!",
                        " ",
                        " §aKlikkaa nostaaksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        UUID foundUUID = UUID.fromString(container.get(new NamespacedKey(Main.getInstance(), "uuid"), PersistentDataType.STRING));
                        withdrawCoupon(clicker, coupon, foundUUID);
                    }
                });

                gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Kuponki jää inventoryysi",
                        " §7ja mitään ei tapahdu. Pystyt",
                        " §7silti nostamaan palikat myöhemmin",
                        " §7uudelleen!",
                        " ",
                        " §cKlikkaa peruuttaaksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Kupongin nostaminen peruutettiin");
                    }
                });

                gui.open(player);

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        final Action action = e.getAction();

        if(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            final ItemStack item = e.getItem();
            if(item != null) {
                if(this.isCoupon(item)) {
                    e.setCancelled(true);
                    confirmWithdrawal(player, item);
                }
            }
        }
    }
}
