package me.tr.survival.main.managers.teleport;

import org.bukkit.entity.Player;

import java.util.*;

public class TeleportManager {

    private static Map<UUID, TeleportRequest> requests = new HashMap<>();

    public static Map<UUID, TeleportRequest> getActiveRequests() {
        return TeleportManager.requests;
    }

    public static TeleportRequest getRequestFromPlayer(UUID uuid) {
        if(getActiveRequests().containsKey(uuid)) {
            return getActiveRequests().get(uuid);
        }
        return null;
    }

    public static List<TeleportRequest> getRequestsFromRecipient(UUID uuid) {

        List<TeleportRequest> list = new ArrayList<>();

        for(TeleportRequest request : getActiveRequests().values()) {

            Player recipient = request.getRecipient();
            if(recipient == null) continue;

            if(recipient.getUniqueId().equals(uuid)) {
                list.add(request);
            }

        }

        return list;

    }

    public enum Teleport {

        REQUEST, FORCE

    }

}
