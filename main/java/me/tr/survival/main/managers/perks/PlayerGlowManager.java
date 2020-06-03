package me.tr.survival.main.managers.perks;

import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.other.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerGlowManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("hehku")) {

                if(args.length < 1) {
                    toggle(player);
                } else {

                    if(!player.isOp()) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei oikeuksia!");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löydetty...");
                        return true;
                    }

                    Chat.sendMessage(player, (toggle(target) ? "Hehku on §apäällä" : "Hehku on §cpois päältä") + " pelaajalla §a" + target.getName());

                }

            }

        }

        return true;
    }

    public static boolean toggle(Player player) {
        if(!Ranks.isStaff(player.getUniqueId()) && !Ranks.hasRank(player, "sorsa")) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän toimintoon tarvitset §2§lSORSA§7-arvon! Lisätietoa §a/kauppa§7!");
            return false;
        }

        if(!hasGlowEnabled(player)) {
            enableGlow(player);
            return true;
        } else {
            disableGlow(player);
            return false;
        }
    }

    public static void enableGlow(Player player) {

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        PlayerData.set(player.getUniqueId(), "glow_effect", true);
        player.setGlowing(true);
        Chat.sendMessage(player, "Sinulla on nyt hehku §apäällä§7!");

    }

    public static void disableGlow(Player player) {

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        PlayerData.set(player.getUniqueId(), "glow_effect", false);
        player.setGlowing(false);
        Chat.sendMessage(player, "Sinulla on nyt hehku §cpois päältä§7!");

    }

    public static boolean hasGlowEnabled(Player player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }
        return (boolean) PlayerData.getValue(player.getUniqueId(), "glow_effect");
    }

}
