package me.tr.survival.main.commands;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Mail;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.database.data.Balance;
import me.tr.survival.main.database.data.Crystals;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class VipCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender.isOp()) {
            if(args.length < 2) {
                sender.sendMessage("§c/givevip <player> <rank>");
                return true;
            } else {
                final OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                final String rankRaw = args[1];
                if(!rankRaw.equalsIgnoreCase("premium") && !rankRaw.equalsIgnoreCase("premiumplus") && !rankRaw.equalsIgnoreCase("sorsa")) {
                    sender.sendMessage("§cVain 'premium', 'premiumplus' tai 'sorsa'!");
                    return true;
                }
                if(!PlayerData.isLoaded(target.getUniqueId())) {
                    Sorsa.async(() -> {
                        PlayerData.loadPlayer(target.getUniqueId(), (res) -> {
                            givePerks(target, rankRaw);
                            sender.sendMessage("§a[Vips] Perks of '" + rankRaw.toLowerCase() + "' to " + target.getName());
                        });

                    });
                } else {
                    givePerks(target, rankRaw);
                    sender.sendMessage("§a[Vips] Perks of '" + rankRaw.toLowerCase() + "' to " + target.getName());
                }
            }
        }
        return true;
    }

    private void givePerks(final OfflinePlayer player, String rank) {
        final UUID uuid = player.getUniqueId();
        if(rank.equalsIgnoreCase("premium")) Mail.addTickets(uuid, 10);
        else if(rank.equalsIgnoreCase("premiumplus")) Mail.addTickets(uuid, 10);
        else if(rank.equalsIgnoreCase("sorsa")) Mail.addTickets(uuid, 20);
        Sorsa.logColored("§6[VIP] Gave VIP-perks for rank '" + rank + "' to player " + player.getName() + " (" + player.getUniqueId() + ")");
    }
}
