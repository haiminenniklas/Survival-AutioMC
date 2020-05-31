package me.tr.survival.main.util.teleport;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Settings;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.other.travel.EndManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class TeleportRequest {

    private Player one, two;
    private TeleportManager.Teleport teleport;
    private boolean accepted;
    private boolean denied;

    private long requestTime;
    private boolean expired;
    private boolean here;

    public TeleportRequest(Player one, Player two, TeleportManager.Teleport request) {

        this.one = one;
        this.two = two;

        this.teleport = request;
        this.accepted = false;
        this.denied = false;

        this.requestTime = 0L;
        this.here = false;

    }

    public void ask() {
        this.ask(false);
    }

    public void ask(boolean here) {

        this.here = here;

        if(Settings.get(two.getUniqueId(), "privacy") && this.getType() != TeleportManager.Teleport.FORCE) {
            Chat.sendMessage(one, Chat.Prefix.ERROR, "Pelaajalla §a" + two.getName() + " §7on yksityinen tila päällä!");
            return;
        }

        // If player tries to request from himself
        if(this.one.getUniqueId().equals(this.two.getUniqueId())) {
            Chat.sendMessage(this.one, Chat.Prefix.ERROR, "Et voi lähettää §eTeleport§7-pyyntöä itsellesi!");
            return;
        }

        TeleportManager.getActiveRequests().put(one.getUniqueId(), this);

        if(this.getType() == TeleportManager.Teleport.FORCE) {
            accept();
        } else {

            if(EndManager.isInEnd(this.one) || EndManager.isInEnd(this.two)) {
                Chat.sendMessage(this.one, Chat.Prefix.ERROR, "Teleporttaus evättiin, sillä §5Endiin §7teleporttaaminen on kiellettyä.");
                this.remove();
                return;
            }

            TextComponent accept =  new TextComponent(TextComponent.fromLegacyText("  §a§lHYVÄKSY"));
            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klikkaa hyväksyäksesi §o(§a/tpaccept§7§o)").create()));
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + one.getName()));

            TextComponent deny =  new TextComponent(TextComponent.fromLegacyText("§c§lKIELLÄ"));
            deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klikkaa kieltääksesi §o(§c/tpdeny§7§o)").create()));
            deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + one.getName()));

            accept.addExtra("  ");
            accept.addExtra(deny);

            two.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
            two.sendMessage(" §7Pelaaja §a" + one.getName() + " §7pyytää");
            if(here) {
                two.sendMessage(" §7pyytää sinua hänen luokseen!");
            } else {
                two.sendMessage(" §7lupaa teleportata luoksesi!");
            }
            two.sendMessage(" ");
            two.spigot().sendMessage(accept);
            two.sendMessage(" ");
            two.sendMessage(" §7Sinulla on §a60 sekuntia §7aikaa hyväksyä");
            two.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            Chat.sendMessage(one, "Lähetit §eteleporttaus§7-pyynnön pelaajalle §6" + two.getName() + "§7!" +
                    " Pyyntö umpeutuu automaattisesti §e60 sekunnin §7kuluttua...");

            this.requestTime = System.currentTimeMillis();

            // Set the request as expired after 60 seconds
            Autio.afterAsync(60, () -> {

                if(!this.expired) {
                    this.setExpired(true);
                    Chat.sendMessage(one, "Teleporttauspyyntö pelaajalle §a" + two.getName() + " §7on nyt umpeutunut!");
                    Chat.sendMessage(two, "Teleporttauspyyntö pelaajalta §a" + one.getName() + " §7on nyt umpeutunut!");
                    remove();
                }

            });

        }

    }

    public void deny(){

        this.denied = true;

        if(this.isExpired()) {
            Chat.sendMessage(two, Chat.Prefix.ERROR, "Teleporttauspyyntö pelaajalta §6" + one.getName() + " §7on jo umpeutunut!");
        } else {
            Chat.sendMessage(two, "§7Kielsit teleporttauspyynnön pelaajalta §a" + one.getName());
            Chat.sendMessage(one, "§7Pelaaja §a" + two.getName() + " §7kielsi sinun teleporttauspyyntösi!");
        }

        remove();
    }

    public void accept() {


        this.accepted = true;

        if(this.isExpired()) {
            Chat.sendMessage(two, Chat.Prefix.ERROR, "Teleporttauspyyntö pelaajalta §a" + one.getName() + " §7on jo umpeutunut!");
        } else {

            if(this.getType() == TeleportManager.Teleport.FORCE) {
                this.teleport();
            } else {

                if(!this.here) {
                    Chat.sendMessage(two, "Hyväksyit Teleport-pyynnön pelaajalta §a" + one.getName() + "§7!");
                    Chat.sendMessage(one, "Pelaaja §a" + two.getName() + " §7hyväksyi Teleport-pyyntösi! Teleportataan §e3 sekunnin §7kuluttua!");
                } else {
                    Chat.sendMessage(two, "Hyväksyit Teleport-pyynnön pelaajalta §a" + one.getName() + "§7! Teleportataan §e3 sekunnin §7kuluttua!");
                    Chat.sendMessage(one, "Pelaaja §a" + two.getName() + " §7hyväksyi Teleport-pyyntösi!");
                }

                Autio.after(3, () -> this.teleport() );
            }

        }

        remove();

    }



    public void teleport() {

        if(this.here) {
            two.teleport(one.getLocation());
            Util.sendNotification(two, "§7Sinua viedään pelaajan §a" + one.getName() + " §7luo!");
        } else {
            one.teleport(two.getLocation());
            Util.sendNotification(one, "§7Sinua viedään pelaajan §a" + two.getName() + " §7luo!");
        }

    }

    private void remove() {

        this.expired = true;

        if(this.one == null) {
            throw new IllegalArgumentException("Neither of the players in the Teleportation request cannot be null!");
        }
        TeleportManager.getActiveRequests().remove(this.one.getUniqueId());

    }

    public void setExpired(boolean value) {
        this.expired = value;
    }

    public boolean isExpired() {
        return expired;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isDenied() {
        return denied;
    }

    public Player getRequestor() {
        return one;
    }

    public Player getRecipient() {
        return two;
    }

    public TeleportManager.Teleport getType() {
        return teleport;
    }
}
