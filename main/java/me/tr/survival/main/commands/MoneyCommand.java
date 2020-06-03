package me.tr.survival.main.commands;

import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Profile;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.database.data.Balance;
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
                if(args.length < 1){
                    Chat.sendMessage(player, "Rahatilanteesi: §e" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€");
                } else {
                    if(!player.isOp()){
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        if(player.getName().equals(target.getName())) Profile.openProfile(player, target.getUniqueId());
                        else Profile.openOther(player, target);
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

                                double value;
                                try { value = Double.parseDouble(args[2]);
                                } catch(NumberFormatException ex) {
                                    Chat.sendMessage(player, "Käytä numeroita!");
                                    return true;
                                }

                                if(value < 0) {
                                    Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei negatiivisia numeroita!");
                                    return true;
                                }

                                if(args[0].equalsIgnoreCase("add")) {
                                    Balance.add(target.getUniqueId(), value);
                                    Chat.sendMessage(player, "Pelaajalle §a" + target.getName() + " §7annettu §a" + value + "€§7! Hänen rahatilanteensa: §a" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€");
                                } else if(args[0].equalsIgnoreCase("remove")) {
                                    Balance.remove(target.getUniqueId(), value);
                                    Chat.sendMessage(player, "Pelaajalta §a" + target.getName() + " §7 poistettu §a" + value + "€! §7Hänen rahatilanteensa: §a" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€");
                                } else if(args[0].equalsIgnoreCase("set")) {
                                    Balance.set(target.getUniqueId(), value);
                                    Chat.sendMessage(player, "Pelaajan §a" + target.getName() + " §7nykyinen rahatilanne on nyt: §a" + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€");
                                }

                                Chat.sendMessage(player, "Tallenna pelaajan tiedot komennolla §a/save " + target.getName());

                            } else if(args[0].equalsIgnoreCase("get")) Chat.sendMessage(player, "Pelaajan §a" + target.getName() + " rahatilanne: " + Util.formatDecimals(Balance.get(player.getUniqueId())) + "€");
                        }

                    }
                }
            } else if(command.getLabel().equalsIgnoreCase("pay")) {

                if(args.length < 2) Chat.sendMessage(player, "Käytä: §a/maksa <pelaaja> <määrä>");
                else {

                    Player target = Bukkit.getPlayer(args[0]);
                    if(target == null) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Pelaajaa ei löytynyt!");
                        return true;
                    }

                    if(target.getName().equals(player.getName())) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Et voi lähettää itsellesi rahaa!");
                        return true;
                    }

                    double amount;
                    try { amount = Double.parseDouble(args[1]);
                    } catch(NumberFormatException ex) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Käytäthän numeroita!");
                        return true;
                    }

                    if(amount < 1) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei negatiivisia numeroita, tai nolla!");
                        return true;
                    }
                    amount = Util.round(amount);
                    if(!Balance.canRemove(uuid, amount)) {
                        Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinulla ei ole tarpeeksi rahaa!");
                        return true;
                    } else {
                        Balance.remove(uuid, amount);
                        Balance.add(target.getUniqueId(), amount);
                        Chat.sendMessage(player, "Annoit §e" + amount + "€ §7pelaajalle §a" + target.getName() + "§7!");
                        Chat.sendMessage(target, "Sait §e" + amount + "€ §7pelaajalta §a" + player.getName() + "§7!");
                    }
                }
            }
        }
        return true;
    }
}
