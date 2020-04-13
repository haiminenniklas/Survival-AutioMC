package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Mail;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
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

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                String rankRaw = args[1];
                if(!rankRaw.equalsIgnoreCase("premium") && !rankRaw.equalsIgnoreCase("premiumplus") && !rankRaw.equalsIgnoreCase("kuningas")) {
                    sender.sendMessage("§cVain 'premium', 'premiumplus' tai 'kuningas'!");
                    return true;
                }

                if(!PlayerData.isLoaded(target.getUniqueId())) {

                    Autio.async(() -> {
                        PlayerData.loadPlayer(target.getUniqueId());
                        givePerks(target, rankRaw);
                    });

                } else {
                    givePerks(target, rankRaw);
                }

            }

        }

        return true;
    }

    private void givePerks(OfflinePlayer player, String rank) {

        UUID uuid = player.getUniqueId();

        if(rank.equalsIgnoreCase("premium")) {

            Crystals.add(uuid, 100);
            Balance.add(uuid, 20000);
            Mail.addTickets(uuid, 10);

        } else if(rank.equalsIgnoreCase("premiumplus")) {

            Crystals.add(uuid, 250);
            Balance.add(uuid, 30000);
            Mail.addTickets(uuid, 10);

        } else if(rank.equalsIgnoreCase("kuningas")) {

            Crystals.add(uuid, 1000);
            Balance.add(uuid, 100000);
            Mail.addTickets(uuid, 20);

        }
    }

}
