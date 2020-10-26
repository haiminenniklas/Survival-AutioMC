package me.tr.survival.main.managers.features;

import me.tr.survival.main.Main;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Mail;
import me.tr.survival.main.managers.MoneyManager;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Random;

public class Lottery {

    private static Prize findPrize(int givenTries) {
        double random = Math.random();
        Prize randomPrize = Prize.values()[new Random().nextInt(Prize.values().length)];
        if(random <= randomPrize.getPercentage()) return randomPrize;
        else {
            if(givenTries < 10) {
                return findPrize(givenTries + 1);
            } else return null;
        }
    }

    public static Prize lot(Player player) {

        if(Mail.getTickets(player) < 1) {
            Chat.sendMessage(player, "Sinulla ei ole yhtään arpaa. Tee §a/vote §7saadaksesi niitä!");
            return null;
        }

        Mail.addTickets(player.getUniqueId(), -1);

        Sorsa.logColored("§6[Lottery] Player " + player.getName() + " (" + player.getUniqueId() + ") opened a lottery ticket!");

        for(int i = 0; i < 6; i++) {
            final Prize prize = findPrize(0);
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
                                && !Ranks.isPartner(player.getUniqueId())
                                && !Ranks.hasRank(player.getUniqueId(), "sorsa")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent set premium");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givevip " + player.getName() + " premium");
                        } else {
                            Chat.sendMessage(player, "Sait (lähes) parhaimman onnen arvasta, mutta sinulla on jo parempi arvo, kuin Premium!");
                        }
                        break;
                    case FOOD:
                        player.getInventory().addItem(
                                ItemUtil.makeItem(Material.COOKED_BEEF, 16),
                                ItemUtil.makeItem(Material.COOKED_PORKCHOP, 16));
                        break;
                    case DIAMONDS:
                        player.getInventory().addItem(ItemUtil.makeItem(Material.DIAMOND, 3));
                        break;
                    case MONEY:
                        Main.getMoneyManager().forceWriteCheque(player, 500);
                        break;
                    case CLAIMBLOCKS:
                        Main.getClaimBlockCouponsManager().generateCoupon(player);
                        break;
                    case NOTCH_APPLES:
                        player.getInventory().addItem(ItemUtil.makeItem(Material.ENCHANTED_GOLDEN_APPLE, 1));
                        break;
                    case WITHER_SKULL:
                        player.getInventory().addItem(ItemUtil.makeItem(Material.WITHER_SKELETON_SKULL, 1));
                        break;
                    case NETHER_INGOT:
                        player.getInventory().addItem(ItemUtil.makeItem(Material.NETHERITE_INGOT, 5));
                        break;
                    case MUSIC_DISC:
                        int random = new Random().nextInt(Util.getMusicDiscs().length);
                        player.getInventory().addItem(ItemUtil.makeItem(Util.getMusicDiscs()[random], 1));
                        break;
                    case CHICKEN:
                        player.getInventory().addItem(
                                ItemUtil.makeItem(Material.COOKED_CHICKEN, 16));
                        break;
                    case MILK:
                        player.getInventory().addItem(
                                ItemUtil.makeItem(Material.MILK_BUCKET, 1));
                        break;
                    case WOOD:
                        player.getInventory().addItem(
                                ItemUtil.makeItem(Material.OAK_LOG, 32));
                        break;
                    case MORE_MONEY:
                        Main.getMoneyManager().forceWriteCheque(player, 1500);
                        break;
                    case SORSA:
                        if(!Ranks.hasRank(player.getUniqueId(), "sorsa") && !Ranks.isStaff(player.getUniqueId())) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent set sorsa");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givevip " + player.getName() + " sorsa");
                        } else {
                            Chat.sendMessage(player, "Sait parhaimman onnen arvasta, mutta sinulla on jo parempi arvo, kuin Premium!");
                        }

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

        FOOD(33, "§c§lRuokaa"),
        CHICKEN(20, "§6§lKANAA"),
        MONEY(20, "§e§lRahaa"),
        CLAIMBLOCKS(15, "§9§lClaim Blockeja"),
        MILK(10, "§f§lMaitoa"),
        WOOD(10, "§6§lPUUTA"),
        DIAMONDS(10, "§b§lTimantteja"),
        MORE_MONEY(7, "§e§lLisää Rahaa"),
        NOTCH_APPLES(3, "§d§lKULTA OMENAT"),
        NETHER_INGOT(2, "§7§lNetherite-harkko"),
        MUSIC_DISC(1, "§e§lMusiikkilevy"),
        WITHER_SKULL(1, "§8§lWITHER-KALLO"),
        VIP(0.1, "§6§lPremium-arvo!"),
        SORSA(0.01, "§2§lSORSA-arvo!"),
        ;

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
