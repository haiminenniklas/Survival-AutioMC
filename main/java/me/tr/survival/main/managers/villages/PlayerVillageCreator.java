package me.tr.survival.main.managers.villages;

import me.tr.survival.main.managers.Chat;
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
                } else if(givenValue.equalsIgnoreCase("jatka") || givenValue.equalsIgnoreCase("valmis") || givenValue.equalsIgnoreCase("continue")) {
                    createProcess.put(uuid, step + 1);
                } else {

                    final Map<Integer, String> currentValues = createValues.getOrDefault(uuid, new HashMap<>());

                    if(step == 0) {
                        currentValues.put(0, givenValue);
                    } else if(step == 1) {

                        if(givenValue.equalsIgnoreCase("kyllä") || givenValue.equalsIgnoreCase("yes") || givenValue.equalsIgnoreCase("true")) {
                            currentValues.put(1, "true");
                            Chat.sendMessage(player, "Kyläsi on julkinen! Eli kaikki pystyvät sinne liittyä!");
                        } else if(givenValue.equalsIgnoreCase("ei") || givenValue.equalsIgnoreCase("no") || givenValue.equalsIgnoreCase("false")) {
                            currentValues.put(1, "false");

                        } else Chat.sendMessage(player, Chat.Prefix.ERROR, "Kirjoita arvo §a'kyllä' §7tai §c'ei' §7tähän asetukseen!");

                    } else if(step == 2) {

                    }
                }
            }
        }

    }

    public boolean isInCreatingProcess(UUID uuid) {
        return createProcess.containsKey(uuid);
    }

    private void cancelCreation(UUID uuid) {

    }

    private void finishCreation(UUID uuid) {

    }

}
