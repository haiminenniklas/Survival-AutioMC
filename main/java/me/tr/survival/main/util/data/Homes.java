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

    public int getHomesAmount() {
        int amount = 0;
        for(Home home : get()) {
            if(home == null) continue;
            amount += 1;
        }
        return amount;
    }

    public void createHome(String pos, Location loc) {
        HashMap<String, Object> data = PlayerData.getData(this.owner.getUniqueId());
        data.put(pos, loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getName() + ";" + loc.getYaw() + ";" + loc.getPitch());
    }

    public void deleteHome(Player player, String position) {

        Gui gui = new Gui("Poiston varmistus", 27);

        gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahivsta", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                " §7Klikkaa vahvistaaksesi poiston!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                gui.close(player);
                deleteHomeReal(position);

                Chat.sendMessage(player, "Koti poistettiin!");

            }
        });

        gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7 Klikkaa peruuttaaksesi poiston!",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {

                gui.close(clicker);
                Homes.panel(clicker, clicker);
                Chat.sendMessage(clicker, "Kodin poisto peruutettiin!");

            }
        });


        gui.open(player);

    }

    public void deleteHomeReal(String pos) {
        HashMap<String, Object> data = PlayerData.getData(this.owner.getUniqueId());
        data.put(pos, "null");
    }

    public static Home parse(UUID owner, String text) {

        if(text.equalsIgnoreCase("null")) {
            return null;
        }

        String[] values = text.split(";");

        if(values.length == 4) {
            return new Home(owner, Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), values[3]);
        } else if(values.length >= 6) {
            return new Home(owner, Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), values[3], Float.parseFloat(values[4]), Float.parseFloat(values[5]));
        } else {
            throw new IllegalArgumentException("Given home data is incorrect. Given Data: '" + text + "'");
        }

    }

    public static void panel(Player opener, OfflinePlayer target) {

        UUID uuid = target.getUniqueId();
        Homes homeList = new Homes(target);
        ArrayList<Home> homes = homeList.get();

        if(Ranks.isVIP(uuid) || Ranks.isPartner(uuid) || target.isOp()) {
            Gui gui = new Gui("Kodit", 27);
            if(homes.get(0) == null) {
                gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§2Luo koti #1", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§aKlikkaa luodaksesi uuden kodin sijaintiisi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Loit kodin §a#1 §7sijaintiisi!");
                        homeList.createHome("first_home", clicker.getLocation());
                    }
                });
            } else {
                Home home = homes.get(0);
                gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.RED_BED, 1, "§2Koti #1", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§7Sijainti §o(x, y, z)§7:",
                        " §2" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                        " ",
                        "§aVasen-klikkaa: §7Teleporttaa kotiisi",
                        "§6Oikea-klikkaa: §7Poista koti",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            home.teleport(clicker);
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Sinua viedään kotiin §a#1§7... Odota §a3 sekuntia§7...");
                        } else if(clickType == ClickType.RIGHT) {
                            gui.close(clicker);
                            homeList.deleteHome(opener, "first_home");
                        }
                    }
                });
            }

            if(homes.get(1) == null) {
                gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§2Luo koti #2", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§aKlikkaa luodaksesi uuden kodin sijaintiisi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        homeList.createHome("second_home", clicker.getLocation());
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Loit kodin §a#2 §7sijaintiisi!");
                    }
                });
            } else {
                Home home = homes.get(1);
                gui.addButton(new Button(1, 13, ItemUtil.makeItem(Material.ORANGE_BED, 1, "§2Koti #2", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§7Sijainti §o(x, y, z)§7:",
                        " §2" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                        " ",
                        "§aVasen-klikkaa: §7Teleporttaa kotiisi",
                        "§aOikea-klikkaa: §7Poista koti",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            home.teleport(clicker);
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Sinua viedään kotiin §a#2§7... Odota §a3 sekuntia§7...");
                        } else if(clickType == ClickType.RIGHT) {
                            gui.close(clicker);
                            homeList.deleteHome(opener, "second_home");
                        }
                    }
                });
            }

            if(homes.get(2) == null) {
                gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§2Luo koti #3", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§aKlikkaa luodaksesi uuden kodin sijaintiisi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        homeList.createHome("third_home", clicker.getLocation());
                        Chat.sendMessage(clicker, "Loit kodin §a#3 §7sijaintiisi!");
                    }

                });
            } else {
                Home home = homes.get(2);
                gui.addButton(new Button(1, 15, ItemUtil.makeItem(Material.GREEN_BED, 1, "§2Koti #3", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§7Sijainti §o(x, y, z)§7:",
                        " §2" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                        " ",
                        "§aVasen-klikkaa: §7Teleporttaa kotiisi",
                        "§aOikea-klikkaa: §7Poista koti",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            home.teleport(clicker);
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Sinua viedään kotiin §a#3§7! Odota §a3 sekuntia§7...");
                        } else if(clickType == ClickType.RIGHT) {
                            gui.close(clicker);
                            homeList.deleteHome(opener, "third_home");
                        }
                    }
                });
            }

            gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                    Profile.openProfile(opener, opener.getUniqueId());
                }
            });

            gui.open(opener);

        } else {
            Gui gui = new Gui("Kodit", 27);
            if(homes.size() < 1 || homes.get(0) == null) {
                gui.addButton(new Button(1, 11, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§2Luo koti #1", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§aKlikkaa luodaksesi uuden kodin sijaintiisi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        homeList.createHome("first_home", clicker.getLocation());
                        gui.close(clicker);
                        Chat.sendMessage(clicker, "Loit kodin §a#1 §7sijaintiisi!");
                    }
                });
            } else {

                Home home = homes.get(0);

                gui.addButton(new Button(1,11, ItemUtil.makeItem(Material.RED_BED, 1, "§2Koti #1", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        "§7Sijainti §o(x, y, z)§7:",
                        " §2" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                        " ",
                        "§aVasen-klikkaa: §7Teleporttaa kotiisi",
                        "§aOikea-klikkaa: §7Poista koti",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            home.teleport(clicker);
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Sinua viedään kotiin §a#1§7... Odota §a3 sekuntia§7...");
                        } else if(clickType == ClickType.RIGHT) {
                            gui.close(clicker);
                            homeList.deleteHome(opener, "first_home");
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
                    Profile.openProfile(opener, opener.getUniqueId());
                }
            });

            gui.open(opener);
        }

    }

}
