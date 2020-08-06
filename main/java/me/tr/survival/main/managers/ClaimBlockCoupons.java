package me.tr.survival.main.managers;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//TODO: Finish this
public class ClaimBlockCoupons implements CommandExecutor, Listener {

    private boolean ENABLED = true;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull  String[] args) {

        if(sender instanceof Player) {

            final Player player = (Player) sender;
            if(args.length < 1) {
                Chat.sendMessage(player, "/cbc generate <amount>");
                Chat.sendMessage(player, "/cbc (enable | disable)");
            } else {

                if(args[0].equalsIgnoreCase("help")) {
                    Chat.sendMessage(player, "/cbc generate <amount>");
                    Chat.sendMessage(player, "/cbc (enable | disable)");
                } else if(args[0].equalsIgnoreCase("enable")) {

                    this.ENABLED = true;
                    Chat.sendMessage(player, "ClaimBlock-kupongit käytössä!");

                } else if(args[0].equalsIgnoreCase("disable")) {
                    this.ENABLED = false;
                    Chat.sendMessage(player, "ClaimBlock-kupongit poissa käytöstä!");
                } else if(args[0].equalsIgnoreCase("generate")) {

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
            }
        }
        return true;
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

    public void withdrawCoupon(Player player, ItemStack coupon) {

        ItemMeta meta = coupon.getItemMeta();
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "claim-amount");

        if(meta != null) {

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if(container.has(key, PersistentDataType.INTEGER)) {

                int foundValue = container.get(key, PersistentDataType.INTEGER);
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

    public void confirmWithdrawal(Player player, ItemStack coupon) {
        ItemMeta meta = coupon.getItemMeta();
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "claim-amount");

        if(meta != null) {

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(key, PersistentDataType.INTEGER)) {
                int foundValue = container.get(key, PersistentDataType.INTEGER);

                Gui.openGui(player, "Varmista nosto", 27, (gui) -> {

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
                            gui.close(player);
                            withdrawCoupon(player, coupon);
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
                            Chat.sendMessage(clicker, "Shekin nostaminen peruutettiin");
                        }
                    });

                });

            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        final Player player = e.getPlayer();
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
