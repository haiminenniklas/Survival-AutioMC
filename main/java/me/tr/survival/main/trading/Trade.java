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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Trade {

    private UUID trader, target;
    private long requestTime;
    private boolean expired;
    private boolean available;

   // private Gui gui;
    private Inventory inv;

    private HashMap<UUID, List<ItemStack>> items;
    private HashMap<UUID, Boolean> accepted;

    public Trade(UUID trader, UUID target) {
        this.trader = trader;
        this.target = target;
        this.requestTime = 0L;
        this.expired = false;
        this.inv = null;
        this.available = false;

        this.items = new HashMap<>();
        this.accepted = new HashMap<>();
        this.accepted.put(trader, false);
        this.accepted.put(target, false);

        TradeManager.getOngoingTrades().add(this);

    }

    private Gui initGui() {

        Gui gui = new Gui("Vaihtokauppa ei vielä voimassa", 27);

        gui.addItem(1, ItemUtil.makeItem(Material.BARRIER, 1, "§cEi vielä voimassa", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tämä vaihtokauppa ei",
                " §7ole voimassa vielä, eikä",
                " §7sen molemmat osapuolet ole",
                " §7sitä vielä hyväksyneet",
                " ",
                " §7Jos näin ei pitäisi asian olla",
                " §7niin ole yhteydessä",
                " §cylläpitoon§7, jotta saat apua!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )), 13);

        return gui;

    }

    public void ask() {

        Player trader = getTrader();
        Player target = getTarget();

        if(trader.getName().equals(target.getName())) {
            Chat.sendMessage(trader, Chat.Prefix.ERROR, "Et pysty lähettämään vaihtokauppapyyntöä itsellesi!");
            TradeManager.getOngoingTrades().remove(this);
            return;
        }

        /*
            Privacy & Self-asking checks
         */

        if(Settings.get(target.getUniqueId(), "privacy")) {
            Chat.sendMessage(trader, Chat.Prefix.ERROR, "Pelaajalla §a" + target.getName() + " §7on yksityinen tila päällä!");

            TradeManager.getOngoingTrades().remove(this);
            return;
        }

        // If player tries to request from himself
        if(this.trader.equals(this.target)) {
            Chat.sendMessage(trader, Chat.Prefix.ERROR, "Et voi lähettää §6Vaihtokauppa§7-pyyntöä itsellesi!");
            TradeManager.getOngoingTrades().remove(this);
            return;
        }

        /*
            Trading Messages
         */

        trader.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        trader.sendMessage(" §a§lVaihtokauppa");
        trader.sendMessage(" ");
        trader.sendMessage(" §7Lähetetään vaihtokauppapyyntöä");
        trader.sendMessage(" §7pelaajalle §a" + target.getName() + "§7!");
        trader.sendMessage(" §7Odota, kunnes vaihtopyyntö on");
        trader.sendMessage(" §ahyväksytty§7!");
        trader.sendMessage(" §7");
        trader.sendMessage(" §ePyyntö umpeutuu 60s kuluttua!");
        trader.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        target.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        target.sendMessage(" §a§lVaihtokauppa");
        target.sendMessage(" ");
        target.sendMessage(" §7Sinä olet saanut vaihtokauppa-");
        target.sendMessage(" §7pyynnön pelaajalta §a" + trader.getName() + "§7!");
        target.sendMessage(" ");

        this.available = true;

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
        target.sendMessage(" §ePyyntö umpeutuu 60s kuluttua");

        target.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        this.requestTime = System.currentTimeMillis();

        Autio.afterAsync(60, () -> {
            if(!this.hasExpired() && this.isAvailable()
                    && TradeManager.getOngoingTrades().contains(this)) {

                this.expired = true;

                trader.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                trader.sendMessage(" §a§lVaihtokauppa");
                trader.sendMessage(" ");
                trader.sendMessage(" §7Pelaaja §a" + target.getName() + " §7ei");
                trader.sendMessage(" §7hyväksynyt vaihtokauppapyyntöäsi");
                trader.sendMessage(" §7ajoissa... Pyyntö on §cumpeutunut§7!");
                trader.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                target.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                target.sendMessage(" §a§lVaihtokauppa");
                target.sendMessage(" ");
                target.sendMessage(" §7Et hyväksynyt pelaajan §a" + trader.getName());
                target.sendMessage(" §7vaihtopyyntöä ajoissa, joten se");
                target.sendMessage(" §7on nyt §cumpeutunut§7!");
                target.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

            }
        });

    }

    public void accept() {

        Player trader = getTrader();
        Player target = getTarget();

        trader.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        trader.sendMessage(" §a§lVaihtokauppa");
        trader.sendMessage(" ");
        trader.sendMessage(" §7Pelaaja §a" + target.getName() + " §7on");
        trader.sendMessage(" §7hyväksynyt vaihtokauppapyyntösi!");
        trader.sendMessage(" §7Teille avataan vaihtokauppa-näkymää...");
        trader.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        target.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        target.sendMessage(" §a§lVaihtokauppa");
        target.sendMessage(" ");
        target.sendMessage(" §7Hyväksyit vaihtokauppa-pyynnön");
        target.sendMessage(" §7pelaajalta §a" + trader.getName() + "§7!");
        target.sendMessage(" §7Teille avataan vaihtokauppa-näkymää...");
        target.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        this.expired = true;

        this.initExchangeGui();
        this.updateGui();

    }

    public boolean isAvailable() {
        return this.available;
    }

    public boolean hasExpired() {

        if(!this.expired) {
            if(this.requestTime > 0L) {
                return (System.currentTimeMillis() - this.requestTime) / 1000 > 60;
            }
            return true;
        } else return true;

    }

    public Inventory getInventory() {
        return this.inv;
    }

    public void deny() {

        Player trader = getTrader();
        Player target = getTarget();

        trader.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
        trader.sendMessage(" §a§lVaihtokauppa");
        trader.sendMessage(" ");
        trader.sendMessage(" §7Pelaaja §a" + target.getName() + " §7on");
        trader.sendMessage(" §ckieltänyt §7vaihtokauppapyyntösi!");
        trader.sendMessage("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

        Chat.sendMessage(target, "Kielsit vaihtopyynnön pelaajalta §a" + trader.getName());

        this.available = false;
        TradeManager.getOngoingTrades().remove(this);

    }

    public void updateGui() {
        Player trader = getTrader();
        Player target = getTarget();
        if(this.items.containsKey(this.trader)) {

            List<ItemStack> playerItems = this.items.get(this.trader);
            int[] slots = new int[] { 19, 20, 28, 29 };
            int itemIndex = 0;

            for(int i = 0; i < slots.length; i++) {
                int slot = slots[i];
                ItemStack item = (itemIndex < playerItems.size()) ? playerItems.get(itemIndex) : new ItemStack(Material.AIR);
                if(item == null) item = new ItemStack(Material.AIR);
                this.inv.setItem(slot, item);
                itemIndex += 1;
            }


        }

        if(this.items.containsKey(this.target)) {
            List<ItemStack> playerItems = this.items.get(this.target);
            int[] slots = new int[] { 24, 25, 33, 34 };
            int itemIndex = 0;

            for(int i = 0; i < slots.length; i++) {
                int slot = slots[i];
                ItemStack item = (itemIndex < playerItems.size()) ? playerItems.get(itemIndex) : new ItemStack(Material.AIR);
                if(item == null) item = new ItemStack(Material.AIR);
                this.inv.setItem(slot, item);
                itemIndex += 1;
            }
        }

        this.inv.setItem(1, ItemUtil.makeSkullItem(trader.getName(), 1, "§a" + trader.getName(), Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tila: " + ((this.accepted.get(trader.getUniqueId()) ? "§aHyväksynyt" : "§cOdottaa")),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));

        this.inv.setItem(7, ItemUtil.makeSkullItem(target.getName(), 1, "§c" + target.getName(), Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tila: " + ((this.accepted.get(target.getUniqueId()) ? "§aHyväksynyt" : "§cOdottaa")),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));

        trader.updateInventory();
        target.updateInventory();

    }

    public void returnItems(Player player, boolean updateGui) {
        if(this.items.containsKey(player.getUniqueId())) {
            List<ItemStack> playerItems = this.items.get(player.getUniqueId());
            int timesLooped = 0;
            for(int i = 0; i < playerItems.size(); i++) {
                if(playerItems.get(i) == null) continue;
                if(playerItems.get(i).getType() == Material.AIR) continue;
                HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(playerItems.get(i));
                for(Map.Entry<Integer, ItemStack> entry : unadded.entrySet()) {
                    player.getWorld().dropItem(player.getLocation(), entry.getValue());
                }
                playerItems.set(i, new ItemStack(Material.AIR));
                timesLooped += 1;
            }
            this.items.put(player.getUniqueId(), playerItems);

            if(timesLooped < 1) {
                Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei ole tavaroita mitä palauttaa!");
            } else {
                this.accepted.put(player.getUniqueId(), false);
                Chat.sendMessage(player, "Tavarasi palautettiin!");
            }

            if(updateGui) this.updateGui();

        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Ei ole tavaroita mitä palauttaa!");
        }

    }

    public void returnItems(Player player) {
        this.returnItems(player, true);
    }

    public void returnItems(boolean updateGui) {

        Player trader = getTrader();
        Player target = getTarget();

        this.returnItems(trader,updateGui);
        this.returnItems(target,updateGui);

        if(updateGui) this.updateGui();

    }

    public void returnItems() {
        this.returnItems(true);
    }

    public void addItem(Player player, ItemStack item) {

        UUID uuid = player.getUniqueId();

        if(!uuid.equals(this.target) && !uuid.equals(this.trader)) {
            throw new IllegalArgumentException("Player not included in the trade");
        }

        List<ItemStack> playerItems = this.items.getOrDefault(uuid, new ArrayList<>());

        if(playerItems.size() >= 4) return;

        playerItems.add(item);

        if(this.items.containsKey(uuid))
            this.items.replace(uuid, playerItems);
        else
            this.items.put(uuid, playerItems);

        this.updateGui();

    }

    private void initExchangeGui() {

        Player trader = getTrader();
        Player target = getTarget();

      //  Gui gui = new Gui("Vaihtokauppa", 54);

        this.inv = Bukkit.createInventory(null, 54, "Vaihtokauppa");

      //  gui.setPartiallyTouchable(true);
       // gui.setAllowedSlots(19,20,24,25,28,29,33,34);

        this.inv.setItem(1, ItemUtil.makeSkullItem(trader.getName(), 1, "§a" + trader.getName(), Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tila: " + ((this.accepted.get(trader.getUniqueId()) ? "§aHyväksynyt" : "§cOdottaa")),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));
        this.inv.setItem(7, ItemUtil.makeSkullItem(target.getName(), 1, "§c" + target.getName(), Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Tila: " + ((this.accepted.get(target.getUniqueId()) ? "§aHyväksynyt" : "§cOdottaa")),
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));

        for(int i = 10; i < 44; i++) {

            // Item Slots
            if(i == 19 || i == 20 || i == 24 || i == 25 || i == 28 || i == 29 || i == 33 || i == 34)
                continue;

            if(i == 18 || i == 27 || i == 36 || i == 17 || i == 26 || i == 35)
                continue;

            this.inv.setItem(i, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, ""));

        }

        for(int i = 0; i < 54; i++) {
            // Item Slots
            if(i == 19 || i == 20 || i == 24 || i == 25 || i == 28 || i == 29 || i == 33 || i == 34)
                continue;

            if(i == 1 || i == 7 || i == 47 || i == 51 || i == 49) continue;

            if(i > 9 && i < 17) continue;
            if (i == 21 || i == 22 || i == 23 || i == 30 || i == 31 || i == 32) continue;
            if (i > 36 && i < 44) continue;

            this.inv.setItem(i, ItemUtil.makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));

        }

        this.inv.setItem(47, ItemUtil.makeItem(Material.EMERALD_BLOCK, 1, "§a§lHYVÄKSY", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Kun olet hyväksynyt molempien",
                " §7antamat §etavarat§7, paina tästä",
                " §ahyväksyäksesi §7tarjouksen!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));

        this.inv.setItem(49, ItemUtil.makeItem(Material.PAPER, 1, "§b§lTYHJENNÄ", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Mikäli laitoit vahingossa",
                " §7väärät itemit tarjoukseen",
                " §7niin tästä voit tyhjentää ne",
                " §7itemit!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));

        this.inv.setItem(51, ItemUtil.makeItem(Material.REDSTONE_BLOCK, 1, "§c§lHYLKÄÄ", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Mikäli toinen osapuoli, ei",
                " §7suostu laittamaan §esovittuja",
                " §etavaraoita§7, niin voit painaa",
                " §7tästä §chylätäksesi§7 vaihtokaupan.",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        )));

        trader.openInventory(this.inv);
        target.openInventory(this.inv);

    }

    public void acceptPlayer(Player player) {

        if(!this.items.containsKey(player.getUniqueId())) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinun täytyy laittaa jotain tavaroita!");
            return;
        }

        if(this.items.get(player.getUniqueId()).size() < 1) {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Sinun täytyy laittaa jotain tavaroita!");
            return;
        }

        this.accepted.put(player.getUniqueId(), true);
        Chat.sendMessage(this.getTarget(),"Pelaaja §a" + player.getName() + " §7hyväksyi tarjousken!");
        Chat.sendMessage(this.getTrader(),"Pelaaja §a" + player.getName() + " §7hyväksyi tarjousken!");

        if(this.accepted.get(this.target) && this.accepted.get(this.trader)) {
            this.acceptItems();
        }

        this.updateGui();

    }

    public void acceptItems() {

        Player trader = getTrader();
        Player target = getTarget();

        if(this.items.containsKey(trader.getUniqueId())) {
            List<ItemStack> traderItems = this.items.get(this.trader);
            for(int i = 0; i < traderItems.size(); i++) {
                ItemStack item = traderItems.get(i);
                if(item == null) continue;
                if(item.getType() == Material.AIR) continue;
                HashMap<Integer, ItemStack> unadded = target.getInventory().addItem(item);
                for(Map.Entry<Integer, ItemStack> entry : unadded.entrySet()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), entry.getValue());
                }
            }
        }

        if(this.items.containsKey(target.getUniqueId())) {
            List<ItemStack> targetItems = this.items.get(this.target);
            for(int i = 0; i < targetItems.size(); i++) {
                ItemStack item = targetItems.get(i);
                if(item == null) continue;
                if(item.getType() == Material.AIR) continue;
                HashMap<Integer, ItemStack> unadded = trader.getInventory().addItem(item);
                for(Map.Entry<Integer, ItemStack> entry : unadded.entrySet()) {
                    trader.getWorld().dropItemNaturally(trader.getLocation(), entry.getValue());
                }
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

        Chat.sendMessage(this.getTarget(), Chat.Prefix.ERROR, "Vaihtokauppa suljettiin! Jos uskotte tämän olevan virhe tai bugi, niin ilmoittakaa siitä §9Discord§7-palvelimellamme tai yrittäkää pian uudestaan!");
        Chat.sendMessage(this.getTrader(), Chat.Prefix.ERROR, "Vaihtokauppa suljettiin! Jos uskotte tämän olevan virhe tai bugi, niin ilmoittakaa siitä §9Discord§7-palvelimellamme tai yrittäkää pian uudestaan!");

        this.returnItems(false);
        this.finish();
    }

    private void finish() {

        TradeManager.getOngoingTrades().remove(this);

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
