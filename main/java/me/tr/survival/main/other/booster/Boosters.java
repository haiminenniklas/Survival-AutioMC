package me.tr.survival.main.other.booster;

import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class Boosters implements Listener {


    public enum Booster {

        INSTANT_MINING(15, "§6§lVÄLITTÖMÄT ORET!",
                "§7Rikot oret §avälittömästi §7nopeammin millä tahansa työkalulla! Tehostus kestää §6§l15MIN§7!", 50),
        MORE_ORES(30, "§b§lENEMMÄN MINERAALEJA!",
                "§7Kun rikot oren, siitä putoaa §a25% §7enemmän mineraalia millä tahansa työkalulla. Tehostus kehtää §6§l30MIN§7!", 30),
        EXTRA_HEARTS(60, "§c§lLISÄSYDÄMET",
                "§7Kun tämä tehostus on päällä, sinulla on §c2 lisäsydäntä§7! Tehostus kestää §6§l1H§7!", 15),
        FIX_ITEMS(-1, "§6§lITEMIEN KORJAUS",
                "§7Tämä korjaa kaikki inventoryssasi olevat itemit.", 150),
        NO_HUNGER(25, "§6§lEI NÄLKÄÄ!",
                "§7Tällä tehostuksella et koe nälkää! Tehostus kestää §6§l25MIN§7!", 20)
        ;

        // Duration in minutes
        int duration;
        String displayName;
        String description;
        int cost;

        Booster(int duration, String displayName, String description, int cost) {
            this.duration = duration;
            this.displayName = displayName;
            this.description = description;
            this.cost = cost;
        }

        public int getDuration() {
            return duration;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public int getCost() {
            return cost;
        }
    }


}
