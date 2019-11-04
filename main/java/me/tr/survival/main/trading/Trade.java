package me.tr.survival.main.trading;

import me.tr.survival.main.Autio;
import me.tr.survival.main.Chat;
import me.tr.survival.main.Settings;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Gui;
import me.tr.survival.main.util.teleport.TeleportManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Trade {

    private UUID trader, target;
    private long requestTime;
    private boolean expired;

    private Gui gui;

    private HashMap<UUID, List<ItemStack>> items;
    private HashMap<UUID, Boolean> accepted;

    public Trade(UUID trader, UUID target) {
        this.trader = trader;
        this.target = target;
        this.requestTime = 0L;
        this.expired = false;
        this.gui = this.initGui();

        this.items = new HashMap<>();
        this.accepted = new HashMap<>();
        this.accepted.put(trader, false);
        this.accepted.put(target, false);

        TradeManager.getOngoingTrades().add(this);

    }

    private Gui initGui() {

        Gui gui = new Gui("Vaihtokauppa ei vielä voimassa", 27);

        gui.addItem(1, ItemUtil.makeItem(Material.BARRIER, 1, "§cEi vielä voimassa", Arrays.asList(
                "§7§m--------------------",
                " §7Tämä vaihtokauppa ei",
                " §7ole voimassa vielä, eikä",
                " §7sen molemmat osapuolet ole",
                " §7sitä vielä hyväksyneet",
                " ",
                " §7Jos näin ei pitäisi asian olla",
                " §7niin ole yhteydessä",
                " §cylläpitoon§7, jotta saat apua!",
                "§7§m--------------------"
        )), 13);

        return gui;

    }

    public void ask() {

        Player trader = getTrader();
        Player target = getTarget();

        /*
            Privacy & Self-asking checks
         */

        if(Settings.get(target.getUniqueId(), "privacy")) {
            Chat.sendMessage(trader, Chat.Prefix.ERROR, "Pelaajalla §6" + target.getName() + " §7on yksityinen tila päällä!");
            return;
        }

        // If player tries to request from himself
        if(this.trader.equals(this.target)) {
            Chat.sendMessage(trader, Chat.Prefix.ERROR, "Et voi lähettää §6Vaihtokauppa§7-pyyntöä itsellesi!");
            return;
        }

        /*
            Trading Messages
         */

        trader.sendMessage("§7§m---------------------");
        trader.sendMessage(" §6§lVaihtokauppa");
        trader.sendMessage(" ");
        trader.sendMessage(" §7Lähetetään vaihtokauppapyyntöä");
        trader.sendMessage(" §7pelaajalle §6" + target.getName() + "§7!");
        trader.sendMessage(" §7Odota, kunnes vaihtopyyntö on");
        trader.sendMessage(" §ahyväksytty§7!");
        trader.sendMessage(" §7");
        trader.sendMessage(" §6Pyyntö umpeutuu 60s kuluttua!");
        trader.sendMessage("§7§m---------------------");

        target.sendMessage("§7§m---------------------");
        target.sendMessage(" §6§lVaihtokauppa");
        target.sendMessage(" ");
        target.sendMessage(" §7Sinä olet saanut vaihtokauppa-");
        target.sendMessage(" §7pyynnön pelaajalta §6" + trader.getName() + "§7!");
        target.sendMessage(" ");

        TextComponent accept =  new TextComponent(TextComponent.fromLegacyText("  §a§lHYVÄKSY"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klikkaa hyväksyäksesi pyynnön").create()));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + trader.getName()));

        TextComponent deny =  new TextComponent(TextComponent.fromLegacyText("§c§lKIELLÄ"));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Klikkaa kieltääksesi pyynnön").create()));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + trader.getName()));

        accept.addExtra("  ");
        accept.addExtra(deny);
        target.spigot().sendMessage(accept);

        target.sendMessage(" ");
        target.sendMessage(" §6Pyyntö umpeutuu 60s kuluttua");

        target.sendMessage("§7§m---------------------");

        this.requestTime = System.currentTimeMillis();

        Autio.afterAsync(60, () -> {
            if(!this.hasExpired()) {
                this.expired = true;

                trader.sendMessage("§7§m---------------------");
                trader.sendMessage(" §6§lVaihtokauppa");
                trader.sendMessage(" ");
                trader.sendMessage(" §7Pelaaja §6" + target.getName() + " §7ei");
                trader.sendMessage(" §7hyväksynyt vaihtokauppapyyntöäsi");
                trader.sendMessage(" §7ajoissa... Pyyntö on §cumpeutunut§7!");
                trader.sendMessage("§7§m---------------------");

                target.sendMessage("§7§m---------------------");
                target.sendMessage(" §6§lVaihtokauppa");
                target.sendMessage(" ");
                target.sendMessage(" §7Et hyväksynyt pelaajan §6" + trader.getName());
                target.sendMessage(" §7vaihtopyyntöä ajoissa, joten se");
                target.sendMessage(" §7on nyt §cumpeutunut§7!");
                target.sendMessage("§7§m---------------------");

            }
        });

    }

    public void accept() {

        Player trader = getTrader();
        Player target = getTarget();

        trader.sendMessage("§7§m---------------------");
        trader.sendMessage(" §6§lVaihtokauppa");
        trader.sendMessage(" ");
        trader.sendMessage(" §7Pelaaja §6" + target.getName() + " §7on");
        trader.sendMessage(" §7hyväksynyt vaihtokauppapyyntösi!");
        trader.sendMessage(" §7Teille avataan vaihtokauppa-näkymää...");
        trader.sendMessage("§7§m---------------------");

        target.sendMessage("§7§m---------------------");
        target.sendMessage(" §6§lVaihtokauppa");
        target.sendMessage(" ");
        target.sendMessage(" §7Hyväksyit vaihtokauppa-pyynnön");
        target.sendMessage(" §7pelaajalta §6" + trader.getName() + "§7!");
        target.sendMessage(" §7Teille avataan vaihtokauppa-näkymää...");
        target.sendMessage("§7§m---------------------");

        this.expired = true;

        this.initExchangeGui();
        this.updateGui();

    }

    public boolean hasExpired() {

        if(!this.expired) {
            if(this.requestTime > 0L) {
                return (System.currentTimeMillis() - this.requestTime) / 1000 > 60;
            }
            return true;
        } else return true;

    }

    public void deny() {
        Player trader = getTrader();
        Player target = getTarget();

        trader.sendMessage("§7§m---------------------");
        trader.sendMessage(" §6§lVaihtokauppa");
        trader.sendMessage(" ");
        trader.sendMessage(" §7Pelaaja §6" + target.getName() + " §7on");
        trader.sendMessage(" §ckieltänyt §7vaihtokauppapyyntösi!");
        trader.sendMessage("§7§m---------------------");

        Chat.sendMessage(target, "Kielsit vaihtopyynnön pelaajalta §6" + trader.getName());
    }

    public void updateGui() {
        Player trader = getTrader();
        Player target = getTarget();
        if(this.items.containsKey(trader.getUniqueId())) {

            for(int i = 0; i < this.items.get(trader.getUniqueId()).size(); i++) {
                switch(i) {
                    case 0:
                        this.gui.addItem(1, this.items.get(trader.getUniqueId()).get(i), 19);
                    case 1:
                        this.gui.addItem(1, this.items.get(trader.getUniqueId()).get(i), 20);
                    case 2:
                        this.gui.addItem(1, this.items.get(trader.getUniqueId()).get(i), 24);
                    case 3:
                        this.gui.addItem(1, this.items.get(trader.getUniqueId()).get(i), 25);
                }
            }

        }

        if(this.items.containsKey(target.getUniqueId())) {
            for(int i = 0; i < this.items.get(target.getUniqueId()).size(); i++) {
                switch(i) {
                    case 0:
                        this.gui.addItem(1, this.items.get(target.getUniqueId()).get(i), 28);
                    case 1:
                        this.gui.addItem(1, this.items.get(target.getUniqueId()).get(i), 29);
                    case 2:
                        this.gui.addItem(1, this.items.get(target.getUniqueId()).get(i), 33);
                    case 3:
                        this.gui.addItem(1, this.items.get(target.getUniqueId()).get(i), 34);
                }
            }
        }

        this.gui.addItem(1, ItemUtil.makeSkullItem(trader.getName(), 1, "§a" + trader.getName(), Arrays.asList(
                "§7§m--------------------",
                " §7Tila: " + ((this.accepted.get(trader.getUniqueId()) ? "§aHyväksynyt" : "§cOdottaa")),
                "§7§m--------------------"
        )), 1);
        this.gui.addItem(1, ItemUtil.makeSkullItem(target.getName(), 1, "§c" + target.getName(), Arrays.asList(
                "§7§m--------------------",
                " §7Tila" + ((this.accepted.get(target.getUniqueId()) ? "§aHyväksynyt" : "§cOdottaa")),
                "§7§m--------------------"
        )), 7);

        this.gui.open(trader);
        this.gui.open(target);

    }

    public void returnItems(Player player) {
        if(this.items.containsKey(player.getUniqueId())) {
            for(int i = 0; i < this.items.get(player.getUniqueId()).size(); i++) {
                player.getInventory().addItem(this.items.get(player.getUniqueId()).get(i));
            }
        }
        Chat.sendMessage(player, "Tavarasi palautettiin!");

    }

    public void returnItems() {

        Player trader = getTrader();
        Player target = getTarget();

        if(this.items.containsKey(trader.getUniqueId())) {
            for(int i = 0; i < this.items.get(trader.getUniqueId()).size(); i++) {
                trader.getInventory().addItem(this.items.get(trader.getUniqueId()).get(i));
            }
        }

        if(this.items.containsKey(target.getUniqueId())) {
            for(int i = 0; i < this.items.get(target.getUniqueId()).size(); i++) {
                target.getInventory().addItem(this.items.get(target.getUniqueId()).get(i));
            }
        }

    }

    public void addItem(Player player, ItemStack item) {

        UUID uuid = player.getUniqueId();

        if(!uuid.equals(this.target) && !uuid.equals(this.trader)) {
            throw new IllegalArgumentException("Player not included in the trade");
        }

        List<ItemStack> playerItems;

        if(!this.items.containsKey(uuid)) {
            playerItems = new ArrayList<>();
        } else {
            playerItems = this.items.get(uuid);
        }

        if(playerItems.size() >= 4) return;

        playerItems.add(item.clone());
        player.getInventory().remove(item);
        this.updateGui();

    }

    private void initExchangeGui() {

        Player trader = getTrader();
        Player target = getTarget();

        Gui gui = new Gui("Vaihtokauppa", 54);

        gui.addItem(1, ItemUtil.makeSkullItem(trader.getName(), 1, "§a" + trader.getName(), Arrays.asList(
                "§7§m--------------------",
                " §7Tila: " + ((this.accepted.get(trader.getUniqueId()) ? "§aHyväksynyt" : "§cOdottaa")),
                "§7§m--------------------"
        )), 1);
        gui.addItem(1, ItemUtil.makeSkullItem(target.getName(), 1, "§c" + target.getName(), Arrays.asList(
                "§7§m--------------------",
                " §7Tila" + ((this.accepted.get(target.getUniqueId()) ? "§aHyväksynyt" : "§cOdottaa")),
                "§7§m--------------------"
        )), 7);

        for(int i = 10; i < 44; i++) {

            // Item Slots
            if(i == 19 || i == 20 || i == 24 || i == 25 || i == 28 || i == 29 || i == 33 || i == 34)
                continue;

            if(i == 18 || i == 27 || i == 36 || i == 17 || i == 26 || i == 35)
                continue;

            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, ""), i);

        }

        for(int i = 0; i < 54; i++) {
            // Item Slots
            if(i == 19 || i == 20 || i == 24 || i == 25 || i == 28 || i == 29 || i == 33 || i == 34)
                continue;

            if(i == 1 || i == 7 || i == 47 || i == 51 || i == 49) continue;

            if(i > 9 && i < 17) continue;
            if (i == 21 || i == 22 || i == 23 || i == 30 || i == 31 || i == 32) continue;
            if (i > 36 && i < 44) continue;

            gui.addItem(1, ItemUtil.makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""), i);

        }

        gui.addItem(1, ItemUtil.makeItem(Material.EMERALD_BLOCK, 1, "§a§lHYVÄKSY", Arrays.asList(
                "§7§m--------------------",
                " §7Kun olet hyväksynyt molempien",
                " §7antamat §etavarat§7, paina tästä",
                " §ahyväksyäksesi §7tarjouksen!",
                "§7§m--------------------"
        )), 47);

        gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§b§lTYHJENNÄ", Arrays.asList(
                "§7§m--------------------",
                " §7Mikäli laitoit vahingossa",
                " §7väärät itemit tarjoukseen",
                " §7niin tästä voit tyhjentää ne",
                " §7itemit!",
                "§7§m--------------------"
        )), 49);

        gui.addItem(1, ItemUtil.makeItem(Material.REDSTONE_BLOCK, 1, "§c§lHYLKÄÄ", Arrays.asList(
                "§7§m--------------------",
                " §7Mikäli toinen osapuoli, ei",
                " §7suostu laittamaan §esovittuja",
                " §7tavaraoita, niin voit painaa",
                " §7tästä §chylätäksesi§7 vaihtokaupan.",
                "§7§m--------------------"
        )), 51);

        this.gui = gui;

    }

    public void acceptPlayer(Player player) {

        this.accepted.put(player.getUniqueId(), true);
        Chat.sendMessage(this.getTarget(),"Pelaaja §6" + player.getName() + " §7hyväksyi tarjousken!");
        Chat.sendMessage(this.getTrader(),"Pelaaja §6" + player.getName() + " §7hyväksyi tarjousken!");

        if(this.accepted.get(this.target) && this.accepted.get(this.trader)) {
            this.acceptItems();
        }

    }
    public void acceptItems() {

        Player trader = getTrader();
        Player target = getTarget();

        if(this.items.containsKey(trader.getUniqueId())) {
            for(int i = 0; i < this.items.get(trader.getUniqueId()).size(); i++) {
                target.getInventory().addItem(this.items.get(trader.getUniqueId()).get(i));
            }
        }

        if(this.items.containsKey(target.getUniqueId())) {
            for(int i = 0; i < this.items.get(target.getUniqueId()).size(); i++) {
                trader.getInventory().addItem(this.items.get(target.getUniqueId()).get(i));
            }
        }

        Chat.sendMessage(this.getTarget(),"Vaihtokauppa valmis! Tavarat vaihdettu!");
        Chat.sendMessage(this.getTrader(),"Vaihtokauppa valmis! Tavarat vaihdettu!");

        this.finish();

    }

    public void denyItems() {
        Chat.sendMessage(this.getTarget(),"Vaihtokauppa peruttu! Tavarat palautettu.");
        Chat.sendMessage(this.getTrader(),"Vaihtokauppa peruttu! Tavarat palautettu.");
        this.returnItems();
        this.finish();
    }

    public void close() {

        Chat.sendMessage(this.getTarget(), Chat.Prefix.ERROR, "Vaihtokauppa suljettiin virheen takia, yrittäkää pian uudestaan!");
        Chat.sendMessage(this.getTrader(), Chat.Prefix.ERROR, "Vaihtokauppa suljettiin virheen takia, yrittäkää pian uudestaan!");

        this.returnItems();
        this.finish();
    }

    private void finish() {

        if(TradeManager.getOngoingTrades().contains(this)) {
            TradeManager.getOngoingTrades().remove(this);
        }

        getTrader().closeInventory();
        getTarget().closeInventory();

    }

    public UUID getTargetUUID() {
        return target;
    }

    public UUID getTraderUUID() {
        return trader;
    }

    public Player getTarget() {
        Player player = Bukkit.getPlayer(this.target);
        if(player == null) throw new IllegalArgumentException("Player is null!");
        return player;
    }

    public Player getTrader() {
        Player player = Bukkit.getPlayer(this.trader);
        if(player == null) throw new IllegalArgumentException("Player is null!");
        return player;
    }

}
