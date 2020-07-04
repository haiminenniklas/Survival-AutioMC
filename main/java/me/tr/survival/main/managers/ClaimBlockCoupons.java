package me.tr.survival.main.managers;

import me.tr.survival.main.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

//TODO: Finish this
public class ClaimBlockCoupons implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull  String[] args) {
        return true;
    }

    public ItemStack registerCoupon(int claimBlocks) {

        ItemStack item = ItemUtil.makeItem(Material.PAPER, 1, "§aSuojauskuponki", Arrays.asList(
                "§7Tällä kupongilla pystyt lisäämään",
                "§7itsellesi suojausblockeja!",
                "§7Sinun pitää vain klikata tätä",
                "§7itemiä kun se on kädessäsi!",
                " ",
                " §7Tämä kuponki sisältää §e" + claimBlocks,
                " §7suojauspalikkaa!"
        ));



        return item;

    }

    public void withdrawCoupon(Player player, ItemStack coupon) {

    }

}
