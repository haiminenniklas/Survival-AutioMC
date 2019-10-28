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

import java.util.Arrays;
import java.util.UUID;

public class Trade {

    private UUID trader, target;
    private long requestTime;
    private boolean expired;

    private Gui gui;

    public Trade(UUID trader, UUID target) {
        this.trader = trader;
        this.target = target;
        this.requestTime = 0L;
        this.expired = false;
        this.gui = this.initGui();
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

        this.initExhangeGui();
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
        this.gui.open(trader);
        this.gui.open(target);
    }

    private void initExhangeGui() {

        Player trader = getTrader();
        Player target = getTarget();

        Gui gui = new Gui("Vaihtokauppa", 54);

        gui.addItem(1, ItemUtil.makeSkullItem(trader.getName(), 1, "§a" + trader.getName()), 0);
        gui.addItem(1, ItemUtil.makeSkullItem(target.getName(), 1, "§c" + target.getName()), 8);

        for(int i = 9; i < 45; i++) {

            // Item Slots
            if(i == 19 || i == 20 || i == 24 || i == 25 || i == 28 || i == 29 || i == 33 || i == 34)
                continue;

            gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, ""), i);

        }

        this.gui = gui;

    }

    public void acceptItems() {

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