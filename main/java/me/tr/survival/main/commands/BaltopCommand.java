package me.tr.survival.main.commands;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Balance;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class BaltopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(command.getLabel().equalsIgnoreCase("baltop")) {

                Chat.sendMessage(player, "Haetaan rahatietoja...");
                Balance.getBalances((rawMap) -> {

                    Map<UUID, Integer> map = Util.sortByValue(rawMap);

                    List<String> lore = new ArrayList<>();
                    lore.add("§7§m--------------------");

                    int looped = 1;
                    for(Map.Entry<UUID, Integer> e : map.entrySet()) {

                        OfflinePlayer op = Bukkit.getOfflinePlayer(e.getKey());
                        if(looped > 10) break;
                        lore.add("§e" + looped + ". §7" + op.getName() + " (§a" + e.getValue() + "€§7)");
                        looped += 1;

                    }

                    lore.add("§7§m--------------------");

                    Autio.task(() -> {
                        Gui.openGui(player, "TOP 10 - Rikkaimmat", 27, (gui) -> {

                            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§a§lTOP 10", lore), 13);

                        });
                    });

                });

            }

        }

        return true;
    }
}
