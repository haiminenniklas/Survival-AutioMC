package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Profile;
import me.tr.survival.main.Settings;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDeathMessageManager implements Listener {

    public static void deathMessagePanel(Player player) {

        if(!Ranks.hasRank(player.getUniqueId(), "premiumplus") && !Ranks.isStaff(player.getUniqueId())) {
            Chat.sendMessage(player, "§7Tähän toimintoon tarvitset vähintään §aPremium§f+§7-arvon!");
            return;
        }

        UUID uuid = player.getUniqueId();

        Gui.openGui(player, "Kuolemanviestit", 27, (gui) -> {

            int index = 11;
            for(DeathMessage deathMessage : DeathMessage.values()) {
                List<String> lore = new ArrayList<>();
                final boolean selected = (getSelectedDeathMessage(uuid) != null) && getSelectedDeathMessage(uuid) == deathMessage;

                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                lore.add(" ");

                if(deathMessage != DeathMessage.DEFAULT) {
                    lore.add(" §7Tämä viesti näkyy chattiin");
                    lore.add(" §7kun kuolet. Viesti näyttää tältä:");
                    lore.add(" ");
                    String[] text = Util.splitPreservingWords(deathMessage.format, 32);
                    for(int j = 0; j < text.length; j++) {
                        String line = text[j];
                        line = "§7" + translateDeathMessage(player.getName(), line);
                        lore.add(line);
                    }
                } else {
                    lore.add(" §7Jos et halua, että sinulla");
                    lore.add(" §7näkyy kuolemanviestiä, aktivoi");
                    lore.add(" §7tämä.");
                }

                lore.add(" ");
                lore.add(selected ? "§cKlikkaa deaktivoidaksesi!" : "§aKlikkaa aktivoidaksesi!");
                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                ItemStack item = ItemUtil.makeItem(Material.PAPER, 1, deathMessage.title, lore);
                if(selected) item = Util.makeEnchanted(item);

                gui.addButton(new Button(1, index, item) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        if(!selected) {
                            selectDeathMessage(clicker.getUniqueId(), deathMessage);
                            Chat.sendMessage(player, "Valitsit kuolemanviestiksesi: " + deathMessage.title);
                        } else {
                            resetDeathMessages(clicker.getUniqueId());
                            Chat.sendMessage(player, "Tyhjensit kuolemanviestisi!");
                        }
                        deathMessagePanel(clicker);
                    }
                });

                index += 1;
                if(index > 15) break;

            }

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Settings.vipPanel(clicker);
                }
            });

            int[] glassSlots = new int[] {10,16};
            for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.RED_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }

        });

    }

    public static void killMessagePanel(Player player) {

        if(!Ranks.hasRank(player.getUniqueId(), "premiumplus") && !Ranks.isStaff(player.getUniqueId())) {
            Chat.sendMessage(player, "§7Tähän toimintoon tarvitset vähintään §aPremium§f+§7-arvon!");
        }

        UUID uuid = player.getUniqueId();

        Gui.openGui(player, "Tappoviestit", 27, (gui) -> {

            int index = 11;
            for(KillMessage killMessage : KillMessage.values()) {
                List<String> lore = new ArrayList<>();
                final boolean selected = (getSelectedKillMessage(uuid) != null) && getSelectedKillMessage(uuid) == killMessage;

                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                lore.add(" ");

                if(killMessage != KillMessage.DEFAULT) {
                    lore.add(" §7Tämä viesti näkyy chattiin");
                    lore.add(" §7kun tapat jonkun. Viesti näyttää tältä:");
                    lore.add(" ");
                    String[] text = Util.splitPreservingWords(killMessage.format, 32);
                    for(int j = 0; j < text.length; j++) {
                        String line = text[j];
                        line = "§7" + translateKillMessage("Zavast", player.getName(), line);
                        lore.add(line);
                    }
                } else {
                    lore.add(" §7Jos et halua, että sinulla");
                    lore.add(" §7näkyy tappoviestiä, aktivoi");
                    lore.add(" §7tämä.");
                }

                lore.add(" ");
                lore.add(selected ? "§cKlikkaa deaktivoidaksesi!" : "§aKlikkaa aktivoidaksesi!");
                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                ItemStack item = ItemUtil.makeItem(Material.PAPER, 1, killMessage.title, lore);
                if(selected) item = Util.makeEnchanted(item);

                gui.addButton(new Button(1, index, item) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        if(!selected) {
                            selectKillMessage(clicker.getUniqueId(), killMessage);
                            Chat.sendMessage(player, "Valitsit tappoviestiksesi: " + killMessage.title);
                        } else {
                            resetKillMessages(clicker.getUniqueId());
                            Chat.sendMessage(player, "Tyhjensit tappoviestisi!");
                        }
                        killMessagePanel(clicker);
                    }
                });

                index += 1;
                if(index > 15) break;

            }

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Settings.vipPanel(clicker);
                }
            });

            int[] glassSlots = new int[] {10,16};
            for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.LIME_STAINED_GLASS_PANE), slot); }

            for(int i = 0; i < 27; i++) {
                if(gui.getItem(i) != null) continue;
                if(gui.getButton(i) != null) continue;
                gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
            }


        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {

        Player victim = e.getEntity();
        e.setDeathMessage(null);

        if(victim.getLastDamageCause() != null
                && victim.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;

        if(victim.getLastDamageCause() == null) return;


        if(victim.getKiller() != null) {

            if(victim.getLastDamageCause().getEntityType() != EntityType.PLAYER) return;

            Player killer = victim.getKiller();
            KillMessage msg = getSelectedKillMessage(killer.getUniqueId());
            if(msg == null) return;
            if(msg == KillMessage.DEFAULT) return;

            String text = translateKillMessage(victim.getName(), killer.getName(), msg.format);
            Bukkit.broadcastMessage("§2§l» " + text);

        } else {

            DeathMessage msg = getSelectedDeathMessage(victim.getUniqueId());
            if(msg == null) return;
            if(msg == DeathMessage.DEFAULT) return;

            String text = translateDeathMessage(victim.getName(), msg.format);
            Bukkit.broadcastMessage("§2§l» " + text);

        }

    }

    public static DeathMessage getSelectedDeathMessage(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        String raw = String.valueOf(PlayerData.getValue(uuid, "death_message"));
        return DeathMessage.valueOf(raw.toUpperCase());
    }

    public static KillMessage getSelectedKillMessage(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        String raw = String.valueOf(PlayerData.getValue(uuid, "kill_message"));
        return KillMessage.valueOf(raw.toUpperCase());
    }

    public static void selectKillMessage(UUID uuid, KillMessage message) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        PlayerData.set(uuid, "kill_message", message.name());
    }

    public static void selectDeathMessage(UUID uuid, DeathMessage message) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        PlayerData.set(uuid, "death_message", message.name());
    }

    public static void resetKillMessages(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        PlayerData.set(uuid, "kill_message", "DEFAULT");
    }

    public static void resetDeathMessages(UUID uuid) {
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }
        PlayerData.set(uuid, "death_message", "DEFAULT");
    }

    public static String translateDeathMessage(String victim, String message) {
        message = message.replaceAll("%victim%", victim);
        return message;
    }

    public static String translateKillMessage(String victim, String killer, String message) {
        message = message.replaceAll("%victim%", victim);
        message = message.replaceAll("%killer%", killer);
        return message;
    }

    public enum DeathMessage {

        DEFAULT(null, "§aEi mitään"),
        TOOK_THE_L("§7Pelaaja §2%victim% §7otti L:än ja kuoli.", "§aL."),
        SILENT_MOMENT("§7Pidetään hiljainen hetki pelaajan §2%victim% §7vuoksi. Lepää rauhassa!", "§aHiljainen hetki..."),
        PLAY_GOD("§7Pelaaja §2%victim% §7yritti leikkiä Jumalaa...", "§aÄlä leiki Jumalaa"),
        DID_NOT_GO_WELL("§7Homma ei mennyt ihan nappiin pelaajalla §2%victim%§7...", "§aEi tainnut ihan mennä nappiin?");

        String format;
        String title;

        DeathMessage(String format, String title) {
            this.format = format;
            this.title = title;
        }
    }

    public enum KillMessage {

        DEFAULT(null, "§aEi mitään"),
        LOSER("§7Pelaaja §2%victim% §7oli tällä kertaa valitettavasti huonompi kuin §2%killer%§7...", "§aKumpi olikaan huonompi?"),
        POINTS_TO_KILLER("§7Pisteet pelaajalle §2%killer% §7pelaajan §2%victim% §7tappamisesta. Hyvin tehty!", "§aPisteet sulle!"),
        TALK_BIG("§7Pelaaja §2%victim% §7yritti isotella pelaajalle §2%killer% §7ja kuinkas kävikään?", "§aIsot sanat"),
        WHAT_SHOULD_I_SAY("§7Mitä tähän voi edes sanoa? Ehkä §2%victim% §7otti nyt opikseen pelaajalta §2%killer%§7.", "§aMitä edes sanoisin?");

        String format;
        String title;

        KillMessage(String format, String title) {
            this.format = format;
            this.title = title;
        }

    }

}
