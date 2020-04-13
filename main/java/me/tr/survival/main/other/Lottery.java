package me.tr.survival.main.other;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Mail;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.data.Crystals;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Random;

public class Lottery {

    public static Prize findPrize() {

        double random = new Random().nextDouble();
        Prize randomPrize = Prize.values()[new Random().nextInt(Prize.values().length)];

        if(random <= randomPrize.getPercentage()) {
            return randomPrize;
        }

        return null;


    }

    public static Prize lot(Player player) {

        if(Mail.getTickets(player) < 1) {
            Chat.sendMessage(player, "Sinulla ei ole yhtään arpaa. Tee §a/vote §7saadaksesi niitä!");
            return null;
        }

        Mail.addTickets(player.getUniqueId(), -1);

        for(int i = 0; i < 6; i++) {
            Prize prize = findPrize();
            if(prize != null) {

                player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                player.sendMessage("  §6§lONNEA!");
                player.sendMessage(" ");
                player.sendMessage(" §7Voitit palkinnon");
                player.sendMessage(" §7" + prize.getDisplayName());
                player.sendMessage(" ");
                player.sendMessage(" §7Muista tehdä §a/vote§7,");
                player.sendMessage(" §7saadaksesi lisää arpoja!");
                player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                switch(prize) {
                    case VIP:
                        if(!Ranks.isStaff(player.getUniqueId()) &&
                                !Ranks.hasRank(player.getUniqueId(), "premiumplus")
                                && !Ranks.isPartner(player.getUniqueId())) {
                            PlayerData.set(player.getUniqueId(), "rank", "premium");
                        }
                        break;
                    case FOOD:
                        player.getInventory().addItem(
                                ItemUtil.makeItem(Material.COOKED_BEEF, 16),
                                ItemUtil.makeItem(Material.COOKED_PORKCHOP, 16),
                                ItemUtil.makeItem(Material.COOKED_CHICKEN, 16));
                        break;
                    case CRYSTALS:
                        Crystals.add(player.getUniqueId(), 5);
                        break;
                    case DIAMONDS:
                        player.getInventory().addItem(ItemUtil.makeItem(Material.DIAMOND, 3));
                        break;
                }

                return prize;
            }
        }

        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        player.sendMessage("  §c§lEi voittoa...");
        player.sendMessage(" ");
        player.sendMessage(" §7Tällä kertaa kohdallesi ei");
        player.sendMessage(" §7iskenyt voittoa. Yritäthän");
        player.sendMessage(" §7silti uudestaan myöhemmin!");
        player.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        return null;
    }

    public enum Prize {

        FOOD(50, "§c§lRuokaa"),
        DIAMONDS(10, "§b§lTimantteja"),
        VIP(0.01, "§a§lPremium"),
        CRYSTALS(5, "§b§lKristalleja");

        private double chance;
        private String displayName;
        Prize(double chance, String displayName) {
            this.chance = chance;
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public double getChance() {
            return this.chance;
        }


        public double getPercentage() {
            return getChance() / 100;
        }

    }

}
