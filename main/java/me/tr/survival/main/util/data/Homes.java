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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

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


        if(String.valueOf(data.get("fourth_home")) != "null" && data.get("fourth_home") != null) {
            Home home = Homes.parse(player.getUniqueId(), (String) data.get("fourth_home"));
            this.homes.add(home);
        } else {
            this.homes.add(null);
        }


        if(String.valueOf(data.get("fifth_home")) != "null" && data.get("fifth_home") != null) {
            Home home = Homes.parse(player.getUniqueId(), (String) data.get("fifth_home"));
            this.homes.add(home);
        } else {
            this.homes.add(null);
        }


        if(String.valueOf(data.get("sixth_home")) != "null" && data.get("sixth_home") != null) {
            Home home = Homes.parse(player.getUniqueId(), (String) data.get("sixth_home"));
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

    public boolean createHome(Player creator, String pos, Location loc) {

        if(!loc.getWorld().getName().equals("world")) {
            Chat.sendMessage(creator, Chat.Prefix.ERROR, "Pystyt luomaan kotisi vain tavalliseen maailmaan!");
            return false;
        }

        HashMap<String, Object> data = PlayerData.getData(this.owner.getUniqueId());
        data.put(pos, loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getName() + ";" + loc.getYaw() + ";" + loc.getPitch());
        return true;
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

    private static String getHomeString(int pos) {
        switch (pos) {
            case 1: return "first_home";
            case 2: return "second_home";
            case 3: return "third_home";
            case 4: return "fourth_home";
            case 5: return "fifth_home";
            case 6: return "sixth_home";
            default: return "first_home";
        }
    }

    private static Material getBedColor(int pos) {
        switch (pos) {
            case 1: return Material.RED_BED;
            case 2: return Material.GREEN_BED;
            case 3: return Material.BLUE_BED;
            case 4: return Material.YELLOW_BED;
            case 5: return Material.ORANGE_BED;
            case 6: return Material.BROWN_BED;
            default: return Material.RED_BED;
        }
    }

    public static void panel(Player opener, OfflinePlayer target) {

        UUID uuid = target.getUniqueId();
        Homes homeList = new Homes(target);
        ArrayList<Home> homes = homeList.get();

        Gui gui = new Gui("Kodit", 36);

        int maxHomes = 3;
        if(Ranks.isVIP(uuid) || Ranks.isStaff(uuid)) maxHomes = homes.size();

        int[] positions = new int[] {
            12,13,14,
            21,22,23
        };

        int[] glassPositions = new int[] {
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10,              16,17,
                18,19,              25,26,
                  28,29,30,31,32,33,34,35
        };

        int[] colorGlassPositions = new int [] {
          11,15,
          20,24
        };

        for(int i = 0; i < homes.size(); i++) {

            Home home = homes.get(i);
            int homePos = i + 1;
            int itemPos = positions[i];

            if(i < maxHomes || homes.get(i) != null) {
                if(home == null) {
                    gui.addButton(new Button(1, itemPos, ItemUtil.makeItem(Material.OBSIDIAN, 1, "§2Luo koti #" + homePos, Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            "§aKlikkaa luodaksesi uuden kodin sijaintiisi!",
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            gui.close(clicker);
                            Chat.sendMessage(clicker, "Loit kodin §a#" + homePos + " §7sijaintiisi!");
                            homeList.createHome(clicker, getHomeString(homePos), clicker.getLocation());
                        }
                    });
                } else {
                    gui.addButton(new Button(1, itemPos, ItemUtil.makeItem(getBedColor(homePos), 1, "§2Koti #" + homePos, Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            "§7Sijainti §o(x, y, z)§7:",
                            " §2" + (int) home.getX() + ", " + (int) home.getY() + ", " + (int) + home.getZ(),
                            " ",
                            "§aVasen-klikkaa: §7Teleporttaa kotiisi",
                            "§cOikea-klikkaa: §7Poista koti",
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            if(clickType == ClickType.LEFT) {
                                Chat.sendMessage(clicker, "Sinua viedään kotiin §a#" + homePos + "§7... Odota §a3 sekuntia§7...");
                                home.teleport(clicker, (res) -> { });
                                gui.close(clicker);
                            } else if(clickType == ClickType.RIGHT) {
                                gui.close(clicker);
                                homeList.deleteHome(opener, getHomeString(homePos));
                            }
                        }
                    });
                }
            } else {

                gui.addButton(new Button(1, itemPos, ItemUtil.makeItem(Material.BARRIER, 1, "§cLukittu", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §cTämä koti on lukittu sinulle!",
                        " ",
                        " §7§oPystyt avaamaan kaikki kodit",
                        " §6VIP§7§o-arvolla tai ostamalla sen!",
                        " §7§oLisätietoa VIP-arvoista §a/kauppa",
                        " ",
                        " §7Hinta: §e80 000€",
                        " ",
                        " §aKlikkaa avataksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        confirmHomePurchase(clicker, homePos, homeList);
                    }
                });

            }

        }

        for(int glassPos : glassPositions) {
            gui.addItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), glassPos);
        }

        for(int colorGlassPos : colorGlassPositions) {
            gui.addItem(1, new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1), colorGlassPos);
        }

        gui.addButton(new Button(1, 27, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                Profile.openProfile(opener, opener.getUniqueId());
            }
        });

        gui.open(opener);

    }

    private static void confirmHomePurchase(Player player, int homePos, Homes homeList) {

        int price = 80000;

        Gui.openGui(player, "Vahvista kodin osto", 27, (gui) -> {

            gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.GREEN_CONCRETE, 1, "§a§lVahvista", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    " §7Klikkaa vahvistaaksesi oston!",
                    " §7Kodin avaus maksaa: §e" + price + "€§7!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(player);
                    if(Balance.canRemove(player.getUniqueId(), price)) {
                        Balance.remove(player.getUniqueId(), price);
                        homeList.createHome(clicker, getHomeString(homePos), clicker.getLocation());
                        Chat.sendMessage(clicker, "Ostit, sekä loit kodin §a#" + homePos + " §7sijaintiisi!");
                    } else {
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        Chat.sendMessage(clicker, Chat.Prefix.ERROR, "Sinulla ei ole varaa tähän! Kodin osto maksaa §e80 000€§7!");
                    }

                }
            });

            gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.RED_CONCRETE, 1, "§c§lPeruuta", Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7 Klikkaa peruuttaaksesi oston!",
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    gui.close(clicker);
                }
            });

        });
    }

}
