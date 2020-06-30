package me.tr.survival.main.commands;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.features.Homes;
import me.tr.survival.main.other.Ranks;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

import static me.tr.survival.main.Sorsa.getLuckPerms;

public class HomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(!Ranks.isStaff(player.getUniqueId())) Homes.panel(player, player);
            else {
                if(args.length >= 1) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                    if(!PlayerData.isLoaded(target.getUniqueId())) {

                        Sorsa.async(() ->
                            PlayerData.loadPlayer(target.getUniqueId(), (result) -> {

                                // Check if player is loaded into LuckPerms
                                User user = getLuckPerms().getUserManager().getUser(target.getUniqueId());
                                if(user == null) {
                                    // If the data doesn't exist, load it manually.
                                    // This should fix #NullPointerException
                                    CompletableFuture<User> load = Sorsa.getLuckPerms().getUserManager().loadUser(target.getUniqueId());
                                    if(load.isCancelled() || !load.isDone()) {
                                        // Couldn't fetch LuckPerms data, stop the function right now
                                        return;
                                    }
                                }

                                if(result) Sorsa.task(() -> Homes.panel(player, target));
                                else Chat.sendMessage(player, "Pelaajan §a" + target.getName() + " §7koteja ei ole ladattu. Tee §a/debug load " + target.getName() + " §7ja kokeile uudestaan!");

                            }));

                    } else Homes.panel(player, target);
                } else Homes.panel(player, player);
            }
        }
        return false;
    }
}
