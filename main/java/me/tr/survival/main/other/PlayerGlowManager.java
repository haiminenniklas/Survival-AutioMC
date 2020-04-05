package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Particles;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.UUID;

public class PlayerGlowManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;
            if(command.getLabel().equalsIgnoreCase("hehku")) {
                openMenu(player);
            }

        }

        return true;
    }

    public static void openMenu(Player player) {

        if(!Ranks.hasRank(player, "kuningas") && !Ranks.isStaff(player.getUniqueId())) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän toimintoon vaaditaan vähintään §6§lKUNINGAS§7 arvo!");
            return;
        }

        Gui.openGui(player, "Hehkuefektit", 45, (gui) -> {

            int colorIndex = 0;
            for(int i = 10; i < 35; i++) {

                if(i == 17 || i == 18 || i == 26 || i == 27) continue;

                ChatColor color = ChatColor.values()[colorIndex];
               // if(color == null) continue;

                if(color == ChatColor.GRAY || color == ChatColor.WHITE || color == ChatColor.DARK_PURPLE || color == ChatColor.DARK_RED
                    || color == ChatColor.DARK_GRAY || color == ChatColor.DARK_BLUE){
                    colorIndex += 1;
                    continue;
                }

                Button btn = new Button(1,  i, ItemUtil.makeItem(Util.ChatColorToDye(color), 1, color + Util.translateChatColor(color) + " ss", Arrays.asList(
                        "§7§m--------------------",
                        " §7Klikkaa vaihtaaksesi",
                        " §dhehkusi §7väriin",
                        " " + color + Util.translateChatColor(color) + "§7!",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        enableGlow(clicker, color);
                        Chat.sendMessage(clicker, "Hehkusi vaihdettiin väriin " + color + Util.translateChatColor(color) + "§7!");
                    }
                };

                gui.addButton(btn);

                colorIndex += 1;
                if(colorIndex > ChatColor.values().length) break;

            }

            gui.addButton(new Button(1, 36, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Particles.openMainGui(clicker);
                }
            });

            gui.addButton(new Button(1, 40, ItemUtil.makeItem(Material.BARRIER, 1, "§cTyhjennä")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    clicker.setGlowing(false);
                    resetGlowColor(clicker.getUniqueId());
                    Chat.sendMessage(clicker, "Et enää hehku!");
                }
            });

        });

    }

    public static String getGlowColor(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        return (String) PlayerData.getValue(uuid, "glow_effect");
    }

    public static void resetGlowColor(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        PlayerData.set(uuid, "glow_effect", "default");
    }

    public static void setGlowColor(UUID uuid, ChatColor color) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        PlayerData.set(uuid, "glow_effect", color.toString());
    }

    public static void setupColorTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (ChatColor c : ChatColor.values()) {
            Team team = scoreboard.registerNewTeam("GLOW_COLOR_" + String.valueOf(c.getChar()).toUpperCase());
            team.setColor(c);
            team.setPrefix(c + "");
            team.setSuffix(c + "");
            //team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }
    }

    public static void enableGlow(Entity entity, ChatColor color) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("GLOW_COLOR_" + String.valueOf(color.getChar()).toUpperCase());
        team.addEntry(entity.getName());
        setGlowColor(entity.getUniqueId(), color);
        entity.setGlowing(true);
        if(entity instanceof Player){
            ((Player) entity).setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

}
