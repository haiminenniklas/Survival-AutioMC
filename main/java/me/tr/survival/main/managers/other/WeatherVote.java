package me.tr.survival.main.managers.other;

import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.Weathers;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WeatherVote implements Listener, CommandExecutor {

    private boolean voteEnabled = false;
    private long voteStart = 0L;
    private final HashMap<WeatherType, Integer> votes = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(!voteEnabled) {
                Chat.sendMessage(player, "Sää-äänestys ei ole nyt päällä! Se käynnistyy aina sään vaihtuessa!");
                return true;
            }

            if(args.length < 1) {
                Chat.sendMessage("Käytä §a/säävote (selkeä | sateinen)");
                return true;
            } else {

                WeatherType voted = Weathers.getWeatherType(args[0]);
                if(voted == null) {
                    Chat.sendMessage("Voit äänestää näitä säätiloja: §aselkeä §7ja §asateinen§7!");
                    return true;
                }

                this.vote(voted);
                Chat.sendMessage(player, "Äänestyksesi rekisteröitiin! Ne käsitellään tuota pikaa!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

            }

        }

        return true;
    }

    private int getVotes(WeatherType type) {
        return votes.getOrDefault(type, 0);
    }

    private int getAllVotes() {
        int total = 0;
        for(int i : votes.values()) {
            total += i;
        }
        return total;
    }

    private void vote(WeatherType type) {
        int current = getVotes(type);
        votes.put(type, current + 1);
    }

    private WeatherType getMostVoted() {
        return  Collections.max(votes.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {

        if(!voteEnabled && e.toWeatherState()) {

            Util.broadcastSound(Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            for(Player player : Bukkit.getOnlinePlayers()) {

                player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                player.sendMessage(" §a§lSÄÄ-ÄÄNESTYS!");
                player.sendMessage(" ");
                player.sendMessage(" §7Kävi huono tuuri ja sää muuttui surkeaksi,");
                player.sendMessage(" §7mutta ei hätää! Täällä meillä on");
                player.sendMessage(" §5taikavoimia§7, joilla voimme pistää");
                player.sendMessage(" §7sään takaisin ihanaan aurinkoiseen!");
                player.sendMessage(" ");

                TextComponent comp = new TextComponent(TextComponent.fromLegacyText(" §e§lAURINKOINEN "));
                comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/säävote selkeä"));
                comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Äänestä §eselkeää§7! (§a/säävote selkeä§7)")));

                comp.addExtra(" §8| ");

                TextComponent other = new TextComponent(TextComponent.fromLegacyText(" §9§lSATEINEN "));
                comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/säävote sateinen"));
                comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Äänestä §9sateista§7! (§a/säävote sateinen§7)")));

                comp.addExtra(other);

                player.spigot().sendMessage(comp);

                player.sendMessage(" ");
                player.sendMessage(" §aÄänestys loppuu 30s päästä!");
                player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            }

            this.voteEnabled = true;
            this.voteStart = System.currentTimeMillis();

            Sorsa.after(30, () -> {
                WeatherType most = getMostVoted();
                Util.broadcastSound(Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                    player.sendMessage(" §a§lSÄÄ-ÄÄNESTYS!");
                    player.sendMessage(" ");
                    player.sendMessage(" §7Sää-äänestys loppui ja on aika pistää");
                    player.sendMessage(" §5taikavoimamme §7kehiin! Eniten ääniä");
                    player.sendMessage(" §7sai säätila:");
                    player.sendMessage(" ");
                    player.sendMessage(" " + (most == WeatherType.CLEAR ? "§eAurinkoinen" : "§9Sateinen") + " (" + (Util.round((double) getVotes(most) / getAllVotes())));
                    player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                }

                World world = Bukkit.getWorld("world");
                if(most == WeatherType.CLEAR) {
                    world.setThunderDuration(0);
                    world.setWeatherDuration(0);
                    world.setThundering(false);
                    world.setStorm(false);
                } else {
                    world.setStorm(true);
                }

                voteEnabled = false;
                voteStart = 0L;

            });

        }

    }
}
