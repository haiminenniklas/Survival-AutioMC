package me.tr.survival.main.util.data;

import me.tr.survival.main.Chat;
import me.tr.survival.main.Home;
import me.tr.survival.main.Profile;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Homes {

    private OfflinePlayer owner;
    private ArrayList<Home> homes;

    public Homes(OfflinePlayer player) {

        this.owner = player;
        this.homes = new ArrayList<>();

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        HashMap<String, Object> data = PlayerData.getData(player.getUniqueId());
        if(String.valueOf(data.get("first_home")) != "null" && data.get("first_home") != null) {
            Home home = Homes.parse(player.getUniqueId(), (String) data.get("first_home"));
            this.homes.add(home);
        } else {
            this.homes.add(null);
        }

        if(String.valueOf(data.get("second_home")) != "null" && data.get("second_home") != null) {
            Home home = Homes.parse(player.getUniqueId(), (String) data.get("second_home"));
            this.homes.add(home);
        } else {
            this.homes.add(null);
        }


        if(String.valueOf(data.get("third_home")) != "null" && data.get("third_home") != null) {
            Home home = Homes.parse(player.getUniqueId(), (String) data.get("third_home"));
            this.homes.add(home);
        } else {
            this.homes.add(null);
        }


    }

    public ArrayList<Home> get() {
        return this.homes;
    }

    public void createHome(String pos, Location loc) {
        HashMap<String, Object> data = PlayerData.getData(this.owner.getUniqueId());
        data.put(pos, loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getName());
    }

    public void deleteHome(String pos) {
        HashMap<String, Object> data = PlayerData.getData(this.owner.getUniqueId());
        data.put(pos, "null");
    }

    public static Home parse(UUID owner, String text) {

        if(text.equalsIgnoreCase("null")) {
            return null;
        }

        String[] values = text.split(";");

        return new Home(owner, Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), values[3]);

    }

    public static void panel(Player player) {

        UUID uuid = player.getUniqueId();
        Homes homeList = new Homes(player);
        ArrayList<Home> homes = homeList.get();

        if(Ranks.isVIP(uuid) || Ranks.isPartner(uuid) || player.isOp()) {
            Gui gui = new Gui("Kodit", 27);
            if(homes.get(0) == null) {
                gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§6Luo koti #1", Arrays.asList(
                        "§7§m--------------------",
                        "§6Klikkaa luodaksesi uuden kodin sijaintiisi!",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Loit kodin §6#1 §7sijaintiisi!");
                        homeList.createHome("first_home", clicker.getLocation());
                    }
                });
            } else {
                Home home = homes.get(0);
                gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.RED_BED, 1, "§6Koti #1", Arrays.asList(
                        "§7§m--------------------",
                        "§7Sijainti:",
                        " §6" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                        " ",
                        "§6Vasen-klikkaa: §7Teleporttaa kotiisi",
                        "§6Oikea-klikkaa: §7Aseta koti tähän sijaintiin",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            home.teleport();
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Sinua viedään kotiin §6#1§7...");
                        } else if(clickType == ClickType.RIGHT) {
                            homeList.deleteHome("first_home");
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Poistit kodin §6#1§7!");
                        }
                    }
                });
            }

            if(homes.get(1) == null) {
                gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§6Luo koti #2", Arrays.asList(
                        "§7§m--------------------",
                        "§6Klikkaa luodaksesi uuden kodin sijaintiisi!",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        homeList.createHome("second_home", clicker.getLocation());
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Loit kodin §6#2 §7sijaintiisi!");
                    }
                });
            } else {
                Home home = homes.get(1);
                gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.ORANGE_BED, 1, "§6Koti #2", Arrays.asList(
                        "§7§m--------------------",
                        "§7Sijainti:",
                        " §6" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                        " ",
                        "§6Vasen-klikkaa: §7Teleporttaa kotiisi",
                        "§6Oikea-klikkaa: §7Aseta koti tähän sijaintiin",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            home.teleport();
                            gui.close(clicker);
                            clicker.sendMessage("§6§lAutio §7» Sinua viedään kotiin §6#2§7...");
                        } else if(clickType == ClickType.RIGHT) {
                            homeList.deleteHome("second_home");
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Poistit kodin §6#2§7!");
                        }
                    }
                });
            }

            if(homes.get(2) == null) {
                gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§6Luo koti #3", Arrays.asList(
                        "§7§m--------------------",
                        "§6Klikkaa luodaksesi uuden kodin sijaintiisi!",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        homeList.createHome("third_home", clicker.getLocation());
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Loit kodin §6#3 §7sijaintiisi!");
                    }

                });
            } else {
                Home home = homes.get(2);
                gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.GREEN_BED, 1, "§6Koti #3", Arrays.asList(
                        "§7§m--------------------",
                        "§7Sijainti:",
                        " §6" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                        " ",
                        "§6Vasen-klikkaa: §7Teleporttaa kotiisi",
                        "§6Oikea-klikkaa: §7Aseta koti tähän sijaintiin",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            home.teleport();
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Sinua viedään kotiin §6#3§7...");
                        } else if(clickType == ClickType.RIGHT) {
                            homeList.deleteHome("third_home");
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Poistit kodin §6#3§7!");
                        }
                    }
                });
            }

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Profile.openProfile(player, player.getUniqueId());
                }
            });

            gui.open(player);

        } else {
            Gui gui = new Gui("Kodit", 27);
            if(homes.size() < 1 || homes.get(0) == null) {
                gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§6Luo koti #1", Arrays.asList(
                        "§7§m--------------------",
                        "§6Klikkaa luodaksesi uuden kodin sijaintiisi!",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        homeList.createHome("first_home", clicker.getLocation());
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Loit kodin §6#1 §7sijaintiisi!");
                    }
                });
            } else {

                Home home = homes.get(0);

                gui.addButton(new Button(1,11, ItemUtil.makeItem(Material.RED_BED, 1, "§6Koti #1", Arrays.asList(
                        "§7§m--------------------",
                        "§7Sijainti:",
                        " §6" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                        " ",
                        "§6Vasen-klikkaa: §7Teleporttaa kotiisi",
                        "§6Oikea-klikkaa: §7Aseta koti tähän sijaintiin",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            home.teleport();
                            gui.close(clicker);
                            clicker.sendMessage("§6§lAutio §7» Sinua viedään kotiin §6#1§7... ");
                        } else if(clickType == ClickType.RIGHT) {
                            homeList.deleteHome("first_home");
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Postit kodin §6#1§7!");
                        }
                    }
                });

            }

            gui.addItem(1, ItemUtil.makeItem(Material.BARRIER, 1, "§c§lLUKITTU"), 13);
            gui.addItem(1, ItemUtil.makeItem(Material.BARRIER, 1, "§c§lLUKITTU"), 15);

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Profile.openProfile(player, player.getUniqueId());
                }
            });

            gui.open(player);
        }

    }

}