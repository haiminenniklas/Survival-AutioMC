package me.tr.survival.main.other.warps;

import com.google.common.base.Splitter;
import me.tr.survival.main.Autio;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.callback.DatabaseCallback;
import me.tr.survival.main.util.callback.TypedCallback;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Warps {

    private static final HashMap<String, Warp> warps = new HashMap<>();

    public static void panel(Player player) {

        if(getWarps().isEmpty()) {
            Gui gui = new Gui("Ei warppeja", 27);
            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§cEi warppeja", Arrays.asList(
                    "§7§m--------------------",
                    " §7Warppeja ei ole tällä",
                    " §7hetkellä luotu. Voit",
                    " §7olla yhteydessä ",
                    " §6ylläpitoon§7, jos näin",
                    " §7asia ei pitäisi olla!",
                    "§7§m--------------------"
            )), 13);
            gui.open(player);
            return;
        }

        int size = 9 * (2 + (Math.floorDiv(getWarps().size(), 7)));
        Gui gui = new Gui("Warpit", size);

        for(Map.Entry<String, Warp> e : getWarps().entrySet()) {

            Warp warp = e.getValue();
            for(int i = 10; i < size - 10; i++) {
                Inventory inv = gui.getPages().get(1);
                if(inv.getItem(i) != null) continue;

                if(i == 9 || i == 18 || i == 36 || i == 45 || i == 17 || i == 26 || i == 35 || i == 45)
                    continue;

                List<String> lore = new ArrayList<>();
                lore.add("§7§m--------------------");
                Iterable<String> lines = Splitter.fixedLength(23).split(warp.getDescription());
                while(lines.iterator().hasNext()) {
                    lore.add(" §7" + ChatColor.translateAlternateColorCodes('&', lines.iterator().next()));
                }
                lore.add("§7§m--------------------");

                gui.addButton(new Button(1, i, ItemUtil.makeItem(Material.OAK_SIGN, 1, warp.getDisplayName(), lore)) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        warp.teleport(player);
                    }
                });

            }

        }

        gui.open(player);

    }

    public static void loadWarp(String name, TypedCallback<Boolean> dbc) {

        Autio.async(() -> {

            try {

                ResultSet result = SQL.query("SELECT * FROM `warps` WHERE `name` = '" +  name + "';");
                if(result.next()) {
                    dbc.execute(true);
                } else {
                    dbc.execute(false);
                }

            } catch(SQLException ex) {
                ex.printStackTrace();
                dbc.execute(false);
            }

        });

    }

    public static HashMap<String, Warp> getWarps() {
        return Warps.warps;
    }

    public static void loadWarps(TypedCallback<Boolean> callback) {

        System.out.println("Loading warps from Database...");

        Autio.async(() -> {

            try {

                ResultSet result = SQL.query("SELECT * FROM `warps`");
                int loaded = 0;
                while(result.next()) {

                    Warp warp = new Warp(
                            result.getString("name"),
                            new Location(Bukkit.getWorld(result.getString("world")),
                                    result.getInt("loc_x"), result.getInt("loc_y"),
                                    result.getInt("loc_z"), result.getFloat("loc_yaw"),
                                    result.getFloat("loc_pitch")),
                            result.getString("description"),
                            result.getString("display_name")
                    );

                    System.out.println("Loaded warp '" + warp.getName() + "' from the Database!");
                    warps.putIfAbsent(warp.getName(), warp);
                    loaded++;

                }

                if(loaded < 1) {
                    callback.execute(false);
                } else {
                    callback.execute(true);
                }

            } catch(SQLException ex) {
                ex.printStackTrace();
                callback.execute(false);
            }

        });
    }

    public static void createWarp(Location loc, String name, TypedCallback<Boolean> c) {
        createWarp(loc, name, name, "§7Luo tähän warppiin description. §c/warp setDescription " + name + " <kuvaus>", c);
    }

    public static Warp createWarp(Location loc, String name, String description, String displayName, TypedCallback<Boolean> c) {

        Warp warp = new Warp(name, loc, description, displayName);

        Autio.async(() -> {

            try {

                boolean result = SQL.update("INSERT INTO `warps` VALUES('" + name + "', '" + displayName + "', " + loc.getBlockX() + ", " +
                        loc.getBlockY() + ", " + loc.getBlockZ() + ", " + loc.getPitch() + ", " + loc.getY() + ", '" + loc.getWorld().getName() + "', '" +
                        description + "');");

                c.execute(result);

            } catch(SQLException ex) {
                ex.printStackTrace();
                c.execute(false);
            }

        });

        Warps.getWarps().putIfAbsent(name, warp);
        return warp;
    }

}
