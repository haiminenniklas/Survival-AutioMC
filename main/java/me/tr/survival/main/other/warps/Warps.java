package me.tr.survival.main.other.warps;

import me.tr.survival.main.Autio;
import me.tr.survival.main.database.SQL;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Warps {

    private static final List<Warp> warps = new ArrayList<>();

    public static void panel(Player player) {

        if(getWarps().isEmpty()) {
            Gui gui = new Gui("Ei warppeja", 27);
            gui.addItem(1, ItemUtil.makeItem(Material.PAPER, 1, "§cEi warppeja", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Warppeja ei ole tällä",
                    " §7hetkellä luotu. Voit",
                    " §7olla yhteydessä ",
                    " §6ylläpitoon§7, jos näin",
                    " §7asia ei pitäisi olla!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            )), 13);
            gui.open(player);
            return;
        }

        int size = 18 + (9 * ((int) Math.ceil((double) getWarps().size() / 7)));
        Gui gui = new Gui("Warpit", size);

        List<Warp> added = new ArrayList<>();

        for(Warp warp : getWarps()) {

            if(added.contains(warp)) continue;

            System.out.println(warp.getName());
            for(int i = 10; i < size - 10; i++) {
                Inventory inv = gui.getPages().get(1);

                if(gui.getButton(i) != null) continue;
                if(inv.getItem(i) != null) continue;

                if(i == 18 || i == 27 || i == 36 || i == 45 || i == 17 || i == 26 || i == 35 || i == 44)
                    continue;

                List<String> lore = new ArrayList<>();
                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                String[] text = Util.splitStringEvery(warp.getDescription(), 23);
                for(int j = 0; j < text.length; j++) {
                    lore.add(" §7" + ChatColor.translateAlternateColorCodes('&', text[j]));
                }
                lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                gui.addButton(new Button(1, i, ItemUtil.makeItem(Material.OAK_SIGN, 1,
                        ChatColor.translateAlternateColorCodes('&', warp.getDisplayName()), lore)) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        warp.teleport(player);
                    }
                });
                added.add(warp);
                break;

            }

        }

        gui.open(player);

    }

    public static Warp get(String name) {

        for(Warp warp : getWarps()) {
            if(warp.getName().equalsIgnoreCase(name))
                return warp;
        }

        return null;
    }

    @Deprecated
    public static void loadWarp(String name, TypedCallback<Boolean> dbc) {

        Autio.async(() -> {

            SQL.query("SELECT * FROM `warps` WHERE `name` = '" +  name + "';", (result, conn) -> {
                try {
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


        });

    }

    public static void deleteWarp(String name, TypedCallback<Boolean> cb) {

        Autio.log("Deleting warp " + name + "...");
        Autio.async(() -> {

            try {

                boolean update = SQL.update("DELETE FROM `warps` WHERE `name` = '" + name.toLowerCase() + "';");
                cb.execute(update);

            } catch(SQLException ex) {
                ex.printStackTrace();
                cb.execute(false);
            }

        });

    }

    public static List<Warp> getWarps() {
        return Warps.warps;
    }

    public static void saveWarp(Warp warp, TypedCallback<Boolean> cb) {

        Autio.log("Saving the warp " + warp.getName() + "...");
        Autio.async(() -> {

            try {

                cb.execute(SQL.update("UPDATE `warps` SET `display_name` = '" + warp.getDisplayName() +
                        "', `loc_x` = " + warp.getX() + ", `loc_y` = " + warp.getY() +
                        ", `loc_z` = " + warp.getZ() + ", `loc_pitch` = " + warp.getPitch() +
                        ", `loc_yaw` = " + warp.getYaw() + ", `world` = '" +  warp.getWorld().getName() +
                        "', `description` = '" + warp.getDescription() + "' WHERE `name` = '" + warp.getName() + "';"));

            } catch(SQLException ex) {
                ex.printStackTrace();
                cb.execute(false);
            }

        });

    }

    public static void loadWarps(TypedCallback<Boolean> callback) {

        Autio.log("Loading warps from Database...");

        Autio.async(() -> {

            SQL.query("SELECT * FROM `warps`", (result, conn) -> {
                try {
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

                        Autio.log("Loaded warp '" + warp.getName() + "' from the Database!");
                        if(!warps.contains(warps.add(warp))) {
                            warps.add(warp);
                        }
                        loaded++;

                    }

                    if(loaded < 1) {
                        callback.execute(false);
                    } else {
                        callback.execute(true);
                    }
                } catch(SQLException ex) {
                    callback.execute(false);
                }
            });
        });
    }

    public static void createWarp(Location loc, String name, TypedCallback<Boolean> c) {
        createWarp(loc, name, "§7Luo tähän warppiin description. §c/warp setDescription " + name + " <kuvaus>", name, c);
    }

    public static Warp createWarp(Location loc, String name, String description, String displayName, TypedCallback<Boolean> c) {

        Warp warp = new Warp(name.toLowerCase(), loc, description, displayName);

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

        if(!warps.contains(warp)) {
            Warps.getWarps().add(warp);
        }
        return warp;
    }

}
