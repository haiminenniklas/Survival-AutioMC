package me.tr.survival.main.commands;

import me.tr.survival.main.Chat;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.data.Crystals;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MoneyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if(command.getLabel().equalsIgnoreCase("bal")) {
                if(args.length == 0){
                    Chat.sendMessage(player, "Rahatilanne: §6" + Balance.get(uuid) + "€");
                    Chat.sendMessage(player, "Kristallit: §6" + Crystals.get(player.getUniqueId()));
                } else if(args.length > 0) {
                    if(!player.isOp()){
                        Chat.sendMessage(player, "Rahatilanne: §6" + Balance.get(uuid) + "€");
                        Chat.sendMessage(player, "Kristallit: §6" + Crystals.get(player.getUniqueId()));
                    } else {

                        if(args.length == 1 && args[0].equalsIgnoreCase("help")) {
                            Chat.sendMessage(player, "/bal add <player> <amount>");
                            Chat.sendMessage(player, "/bal remove <player> <amount>");
                            Chat.sendMessage(player, "/bal get <player>");
                        } else if(args.length >= 2) {

                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if(!PlayerData.isLoaded(target.getUniqueId())) {
                                Chat.sendMessage(player, "(Pelaajan " + target.getName() + " tietoja ei ole ladattu)");
                            }

                            if(args.length >= 3) {

                                int value;
                                try {
                                    value = Integer.parseInt(args[2]);
                                } catch(NumberFormatException ex) {
                                    Chat.sendMessage(player, "Käytä numeroita!");
                                    return true;
                                }

                                if(args[0].equalsIgnoreCase("add")) {
                                    PlayerData.add(target.getUniqueId(), "money", value);
                                    Chat.sendMessage(player, "Pelaajalle annettu §6" + value + "€§7! Hänen rahatilanteensa: §6" + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                } else if(args[0].equalsIgnoreCase("remove")) {
                                    PlayerData.add(target.getUniqueId(), "money", -value);
                                    Chat.sendMessage(player, "Pelaajalta poistettu §6" + value + "€! §7Hänen rahatilanteensa: §6" + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                }

                                Chat.sendMessage(player, "Tallenna pelaajan tiedot komennolla §6/save " + target.getName());

                            } else {
                                if(args[0].equalsIgnoreCase("get")) {
                                    Chat.sendMessage(player, "Pelaajan §4" + target.getName() + " rahatilanne: " + PlayerData.getValue(target.getUniqueId(), "money") + "€");
                                }
                            }

                        }

                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("pay")) {

                if(args.length < 2) {
                    Chat.sendMessage(player, "Käytä: §6/maksa <pelaaja> <määrä>");
                } else {

                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löytynyt!");
                        return true;
                    }

                    int amount;
                    try {
                        amount = Integer.parseInt(args[1]);
                    } catch(NumberFormatException ex) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytäthän numeroita!");
                        return true;
                    }

                    if(amount < 1) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei negatiivisia numeroita, tai nolla!");
                        return true;
                    }

                    if(!Balance.canRemove(uuid, amount)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole tarpeeksi rahaa!");
                        return true;
                    } else {

                        Balance.remove(uuid, amount);
                        Balance.add(target.getUniqueId(), amount);

                        Chat.sendMessage(player, "Annoit §a" + amount + "€ §7pelaajalle §6" + target.getName() + "§7!");
                        Chat.sendMessage(target, "Sait §a" + amount + "€ §7pelaajalta §6" + player.getName() + "§7!");

                    }

                }

            }
        }

        return true;
    }
}