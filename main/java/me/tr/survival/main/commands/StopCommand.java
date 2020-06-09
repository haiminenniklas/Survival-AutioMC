package me.tr.survival.main.commands;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.Main;
import me.tr.survival.main.managers.features.Houkutin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getLabel().equalsIgnoreCase("stop")) {
            if(sender.isOp()) stopServer();
        } else if(command.getLabel().equalsIgnoreCase("forcestop")) {
            if(sender.isOp()) {
                Bukkit.getServer().setWhitelist(true);
                for(Player player : Bukkit.getOnlinePlayers()) {
                   // player.kickPlayer("§cPalvelin sammui \n §7Palvelin käynnistyy uudelleen §anoin minuutin §7kuluttua! Nähdään taas pian!");
                    Sorsa.sendBungeeMessage(player, "Connect", "lobby");
                    Sorsa.sendBungeeMessage(player, "Message", player.getName(), Chat.getPrefix() + " Palvelin, jossa aikaisemmin olit suljettiin ja sinut vietiin aulaamme. Odotathan noin §aminuutin§7, jotta palvelin saadaan uudelleen toimintaan!");
                }
                Main.getHoukutin().deactivate();
                Sorsa.every(1, () -> {
                    if(Bukkit.getOnlinePlayers().size() < 1) {
                        Bukkit.shutdown();
                    }
                });
            }
        }

        return true;
    }

    private void stopServer() {
        new BukkitRunnable() {
            int timer = 300;

            @Override
            public void run() {

                if(timer < 0) {
                    Main.getHoukutin().deactivate();
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        //player.kickPlayer("§cPalvelin sammui \n §7Palvelin käynnistyy uudelleen §anoin minuutin §7kuluttua! Nähdään taas pian!");
                        Sorsa.sendBungeeMessage(player, "Connect", "lobby");
                        Sorsa.sendBungeeMessage(player, "Message", player.getName(), Chat.getPrefix() + " Palvelin, jossa aikaisemmin olit suljettiin ja sinut vietiin aulaamme. Odotathan noin §aminuutin§7, jotta palvelin saadaan uudelleen toimintaan!");
                    }
                    Bukkit.shutdown();
                    cancel();
                    return;
                }

                if(timer == 300) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l5 minuutin §7kuluttua");
                else if(timer == 180) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l3 minuutin §7kuluttua");
                else if(timer == 120) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l2 minuutin §7kuluttua");
                else if(timer == 60) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l1 minuutin §7kuluttua");
                else if(timer == 30) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l30 sekunnin §7kuluttua");
                else if(timer == 10) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l10 sekunnin §7kuluttua");
                else if(timer == 5) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l5...");
                else if(timer == 4) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l4...");
                else if(timer == 3) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l3...");
                else if(timer == 2) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l2...");
                else if(timer == 1) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen §c§l1...");
                else if(timer == 0) Bukkit.broadcastMessage("§c§l! §7Palvelin käynnistyy uudelleen nyt!");
                timer -= 1;
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);
    }

}
