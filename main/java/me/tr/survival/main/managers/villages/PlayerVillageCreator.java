package me.tr.survival.main.managers.villages;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.managers.Chat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class PlayerVillageCreator implements Listener {

    private final Map<UUID, Integer> createProcess = new HashMap<>();
    private final Map<UUID, Map<Integer, String>> createValues = new HashMap<>();

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent e) {

        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();

        if(this.isInCreatingProcess(uuid)) {

            e.setCancelled(true);

            if(Main.getVillageManager().hasJoinedVillage(uuid)) {
                Chat.sendMessage(player, "Sinä olet jo kylässä! Poistu siitä, ennen kuin luot omasi!");
                this.cancelCreation(uuid);
                return;
            }

            final int step = createProcess.get(uuid);
            /*
             * The steps to creating a player village:
             * 1. Title
             * 2. Private
             * 3. Tags
             *
             * Other settings can be changed later
             */

            if(step < 0) this.cancelCreation(uuid);
            else if(step > 2) this.finishCreation(uuid);
            else {
                String givenValue = e.getMessage().trim();
                if(givenValue.equalsIgnoreCase("takaisin") || givenValue.equalsIgnoreCase("back")) {
                    createProcess.put(uuid, step - 1);
                    this.updateCreationState(player);
                } else if(givenValue.equalsIgnoreCase("jatka") || givenValue.equalsIgnoreCase("valmis") || givenValue.equalsIgnoreCase("continue")) {
                    createProcess.put(uuid, step + 1);
                    this.updateCreationState(player);
                } else if(givenValue.equalsIgnoreCase("peruuta") || givenValue.equalsIgnoreCase("lopeta") || givenValue.equalsIgnoreCase("stop")) {
                    createProcess.put(uuid, -1);
                    this.cancelCreation(uuid);
                    Chat.sendMessage(player, "Peruutit kylän luomisen!");
                    return;
                }
                else {

                    final Map<Integer, String> currentValues = createValues.getOrDefault(uuid, new HashMap<>());

                    if(step == 0) {
                        currentValues.put(0, givenValue);
                        Chat.sendMessage(player, "Kyläsi nimi on nyt: §a" + givenValue + "§7! Jos olet tästä varma, kirjoita §ajatka§7!");
                    } else if(step == 1) {

                        if(givenValue.equalsIgnoreCase("kyllä") || givenValue.equalsIgnoreCase("yes") || givenValue.equalsIgnoreCase("true")) {
                            currentValues.put(1, "true");
                            Chat.sendMessage(player, "Kyläsi on julkinen! Eli kaikki pystyvät sinne liittyä! Jos olet varma tästä, kirjoita §ajatka §7chattiin!");
                        } else if(givenValue.equalsIgnoreCase("ei") || givenValue.equalsIgnoreCase("no") || givenValue.equalsIgnoreCase("false")) {
                            currentValues.put(1, "false");
                            Chat.sendMessage(player, "Kyläsi on yksityinen! Eli vain kutsutut voivat liittyä! Jos olet varma tästä, kirjoita §ajatka §7chattiin!");
                        } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Kirjoita arvo §a'kyllä' §7tai §c'ei' §7tähän asetukseen!");

                    } else if(step == 2) {

                        String[] rawTags = givenValue.split(",");
                        final List<String> tags = new ArrayList<>();
                        if(rawTags.length < 1) {
                            currentValues.put(2, "");
                            Chat.sendMessage(player, "Et lisännyt yhtään tägiä... Oletko varma? Jatka prosessia kirjoittamalla §ajatka §7chattiin!");
                            return;
                        }
                        for(String rawTag : rawTags) tags.add(rawTag.trim());

                        String tagsAsString = StringUtils.join(tags, ", ");
                        currentValues.put(2, tagsAsString);
                        Chat.sendMessage(player, "Asetit tageiksi: §a" + tagsAsString + "§7! Jos olet tästä varma, kirjoita §avalmis §7chattiin! §c§lHuom! " +
                                "§7olet nyt viimeisellä askeleella kylän luontia! Jos haluat muuttaa jotain, käytä §atakaisin §7ja §ajatka §7viestejä chatissa!");

                    }

                    // Update values to the main map
                    createValues.put(uuid, currentValues);
                }
            }
        }

    }

    public void startCreatingProcess(Player player) {
        createProcess.put(player.getUniqueId(), 0);
        Chat.sendMessage(player, "§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");
        Chat.sendMessage(player, " ");
        Chat.sendCenteredMessage(player, " §a§lPelaajakylän luominen");
        Chat.sendMessage(player, " ");
        Chat.sendCenteredMessage(player, " §7Pystyt luomaan oman yhteisösi, kerätä veroja, tienata rahaa ");
        Chat.sendCenteredMessage(player, " §7tehdä yhteistyötä, kauppaa ja vaikka mitä pelaajakylien avulla!");
        Chat.sendMessage(player, " ");
        Chat.sendCenteredMessage(player, "§7Aloita luominen kirjoittamalla kyläsi nimen!");
        Chat.sendMessage(player, " ");
        Chat.sendCenteredMessage(player, " §7Lopeta luominen kirjoittamalla §alopeta §7chattiin!");
        Chat.sendMessage(player, "§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");
    }

    private void updateCreationState(Player player) {

        final UUID uuid = player.getUniqueId();
        final int step = createProcess.get(uuid);

        if(step < 0) this.cancelCreation(uuid);
        else if(step > 2) this.finishCreation(uuid);
        else {
            player.sendMessage("§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");
            player.sendMessage(" ");
            Chat.sendCenteredMessage(player, " §a§lPelaajakylän luominen");
            player.sendMessage(" ");
            Chat.sendCenteredMessage(player, " §7Pystyt luomaan oman yhteisösi, kerätä veroja, tienata rahaa ");
            Chat.sendCenteredMessage(player, " §7tehdä yhteistyötä, kauppaa ja vaikka mitä pelaajakylien avulla!");
            player.sendMessage(" ");

            if(step == 1) {
                Chat.sendCenteredMessage(player, " §7Aseta kyläsi joko §cyksityiseksi §7tai §ajulkiseksi§7. ");
                Chat.sendCenteredMessage(player, " §7Kirjoita §akyllä §7tai §aei§7, mikäli haluat kyläsi julkiseksi!");
                Chat.sendCenteredMessage(player, " §7Jatka seuraavaan steppiin kirjoittamalla §ajatka§7!");
            } else if(step == 2) {
                Chat.sendCenteredMessage(player, " §7Aseta kyläsi tägit!");
                Chat.sendCenteredMessage(player, " §7Kirjoita tägit alle, pilkulla erotettuna ja kun olet valmis kirjoita §ajatka§7!");
            }

            player.sendMessage(" ");
            Chat.sendCenteredMessage(player, " §7Lopeta luominen kirjoittamalla §alopeta §7chattiin!");
            player.sendMessage("§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");
        }

    }

    public boolean isInCreatingProcess(UUID uuid) {
        return createProcess.containsKey(uuid);
    }

    private void cancelCreation(UUID uuid) {
        this.createProcess.remove(uuid);
        this.createValues.remove(uuid);
    }

    private void finishCreation(UUID uuid) {

        final Player player = Bukkit.getPlayer(uuid);

        if(player != null) {
            final Map<Integer, String> currentValues = createValues.getOrDefault(uuid, new HashMap<>());
            if(currentValues.size() >= 1) {

                final String title = currentValues.get(0);
                final boolean isPrivate = Boolean.parseBoolean(currentValues.get(1));

                final List<String> tags = new ArrayList<>();
                for(String rawTag : currentValues.get(2).split(",")) tags.add(rawTag.trim());

                final List<UUID> citizens = new ArrayList<>();
                citizens.add(uuid);


                final PlayerVillage village = new PlayerVillage(
                        UUID.randomUUID(),
                        title,
                        uuid,
                        new ArrayList<>(),
                        citizens,
                        150,
                        Sorsa.getSpawn(),
                        8,
                        isPrivate,
                        tags
                );

                this.createProcess.remove(uuid);
                this.createValues.remove(uuid);

                Main.getVillageManager().addVillageToList(village);

                player.sendMessage("§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");
                player.sendMessage(" ");
                Chat.sendCenteredMessage(player, " §a§lPelaajakylän luominen");
                player.sendMessage(" ");
                Chat.sendCenteredMessage(player, " §7Kyläsi on nyt luotu! Pääset tarkastelemaan sitä §a/kylä§7!");
                player.sendMessage(" ");
                player.sendMessage("§7[§a!§7]§f§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤§7[§a!§7]");

            } else Chat.sendMessage(player, "Sinun täytyy laittaa joitain arvoja, jotta voit luoda kylän!");

        }
    }

}
