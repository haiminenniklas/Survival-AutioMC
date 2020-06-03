package me.tr.survival.main.managers.teleport;

import org.bukkit.entity.Player;

import java.util.*;

public class TeleportManager {

    private static final Map<UUID, TeleportRequest> requests = new HashMap<>();
    static Map<UUID, TeleportRequest> getActiveRequests() {
        return TeleportManager.requests;
    }

    public static List<TeleportRequest> getRequestsFromRecipient(UUID uuid) {
        List<TeleportRequest> list = new ArrayList<>();
        for(TeleportRequest request : getActiveRequests().values()) {
            Player recipient = request.getRecipient();
            if(recipient == null) continue;
            if(recipient.getUniqueId().equals(uuid)) list.add(request);
        }
        return list;
    }

    public enum Teleport { REQUEST, FORCE }

}
