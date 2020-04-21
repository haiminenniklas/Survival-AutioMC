package me.tr.survival.main;

import com.destroystokyo.paper.ParticleBuilder;
import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.particles.ParticleGroup;
import dev.esophose.playerparticles.particles.ParticlePair;
import dev.esophose.playerparticles.styles.*;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.other.PlayerGlowManager;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.other.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Particles implements Listener, CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getLabel().equalsIgnoreCase("kosmetiikka")) {
            if(sender instanceof Player) {
                Player player = (Player) sender;
                openMainGui(player);
            }
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        loadParticles(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Autio.getParticlesAPI().resetActivePlayerParticles(player);

    }

    public static void loadParticles(Player player) {
        UUID uuid = player.getUniqueId();
        if(!PlayerData.isLoaded(uuid)) {
            PlayerData.loadNull(uuid, false);
        }

        Autio.getParticlesAPI().resetActivePlayerParticles(player);
        String rawParticleID = String.valueOf(PlayerData.getValue(uuid, "particle"));
        String rawArrowTrailID = String.valueOf(PlayerData.getValue(uuid, "arrowtrail"));

        //System.out.println("Raw Particle: " + rawParticleID + ", Raw ArrowTrail: " + rawArrowTrailID);

        if(!rawArrowTrailID.equalsIgnoreCase("default") && !rawArrowTrailID.equalsIgnoreCase("null")) {
            Autio.getParticlesAPI().addActivePlayerParticle(player, findArrowTrail(Integer.parseInt(rawArrowTrailID)));
        }

        if(!rawParticleID.equalsIgnoreCase("default") && !rawParticleID.equalsIgnoreCase("null")) {
            Autio.getParticlesAPI().addActivePlayerParticle(player, findParticle(Integer.parseInt(rawParticleID)));
        }
        PlayerParticles.getInstance().reload();
    }

    public static void reloadParticles(Player player) {
        loadParticles(player);
    }

    public static void openMainGui(Player player) {

        if(Ranks.isVIP(player.getUniqueId())) {
            Gui.openGui(player, "Kosmetiikka", 27, (gui) -> {

               // System.out.println(((isUsingDefaultParticle(player) ? "default" : String.valueOf(getCurrentParticle(player).getId()))));
               // System.out.println(((isUsingDefaultArrowTrail(player) ? "default" : String.valueOf(getCurrentArrowTrail(player).getId()))));

                gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.HEART_OF_THE_SEA, 1, "§aPartikkelit", Arrays.asList(
                        "§7§m--------------------",
                        " §7Klikkaa hallinnoidaksesi",
                        " §apartikkeliefektejäsi§7!",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {

                        gui.close(player);
                        openParticlesGui(player);

                    }
                });

                gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.BOW, 1, "§bNuolijanat", Arrays.asList(
                        "§7§m--------------------",
                        " §7Klikkaa hallinnoidaksesi",
                        " §bnuolijanojasi§7!",
                        "§7§m--------------------"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {

                        gui.close(player);
                        openArrowTrailGui(player);

                    }
                });

                gui.addButton(new Button(1, 18, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        Profile.openProfile(player, clicker.getUniqueId());
                    }
                });

            });
        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän ominaisuuteen vaaditaan vähintään §a§lPremium§7-arvo!");
        }

    }

    public static void openArrowTrailGui(Player player) {
        if(Ranks.isVIP(player.getUniqueId())) {
            Gui.openGui(player, "Nuolijanat", 45, (gui) -> {

                int i = 0;
                for(int pos = 10; pos < 35; pos++) {

                    // 17 18 26 27 35 36
                    //int pos = i + 10;

                    if(pos == 17 || pos == 18 || pos == 26 || pos == 27) continue;

                    ParticlePair loopParticle = getAllArrowTrails().get(i);
                    ParticlePair particle = findArrowTrail(loopParticle.getId());

                    if(isAllowedForArrowTrail(player, particle)) {

                        List<String> lore = new ArrayList<>();

                        lore.add("§7§m--------------------");
                        lore.add(" §aKlikkaa aktivoidaksesi!");
                        lore.add("§7§m--------------------");

                        ItemStack item = ItemUtil.makeItem(particle.getItemMaterial(), 1, getDisplayNameForArrowTrail(particle.getId()), lore);

                        if(!isUsingDefaultArrowTrail(player) && getCurrentArrowTrail(player).getId() == particle.getId()) {
                            item = Util.makeEnchanted(item);
                            lore = Arrays.asList(
                                    "§7§m--------------------",
                                    " §cKlikkaa poistaaksesi",
                                    " §ckäytöstä!",
                                    "§7§m--------------------"
                            );
                            ItemMeta meta = item.getItemMeta();
                            meta.setLore(lore);
                            item.setItemMeta(meta);

                            gui.addButton(new Button(1, pos, item) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(player);
                                    removeCurrentArrowTrail(player);
                                    Chat.sendMessage(player, "Deaktivoit nuolijanan §6" + getDisplayNameForArrowTrail(particle.getId()));
                                }
                            });

                        } else {
                            gui.addButton(new Button(1, pos, item) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(player);
                                    setCurrentArrowTrail(player, particle);
                                    Chat.sendMessage(player, "Aktivoit nuolijanan §6" + getDisplayNameForArrowTrail(particle.getId()));
                                }
                            });
                        }

                    } else {
                        gui.addItem(1, ItemUtil.makeItem(Material.GRAY_DYE, 1, "§cEi saatavilla"), pos);
                    }

                    i += 1;
                    if(i >= getAllArrowTrails().size()) break;

                }

                gui.addButton(new Button(1, 36, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        openMainGui(player);
                    }
                });

            });
        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän ominaisuuteen vaaditaan vähintään §a§lPremium§7-arvo!");
        }
    }

    public static void openParticlesGui(Player player) {
        if(Ranks.isVIP(player.getUniqueId())) {
            Gui.openGui(player, "Partikkelit", 54, (gui) -> {

                int i = 0;
                for(int pos = 10; pos < 44; pos++) {

                    // 17 18 26 27 35 36
                    //int pos = i + 10;

                    if(pos == 17 || pos == 18 || pos == 26 || pos == 27 || pos == 35 || pos == 36) continue;

                    ParticlePair loopParticle = getAllParticles().get(i);
                    //System.out.println("Loop ID" + loopParticle.getId());
                    ParticlePair particle = findParticle(loopParticle.getId());

                    if(isAllowedForParticle(player, particle)) {

                        List<String> lore = new ArrayList<>();

                        lore.add("§7§m--------------------");
                        lore.add(" §7Klikkaa aktivoidaksesi!");
                        lore.add("§7§m--------------------");

                       // System.out.println("GUI ID" + particle.getId());

                        //System.out.println("Current ID: " + String.valueOf(PlayerData.getValue(player.getUniqueId(), "particle")));

                        ItemStack item = ItemUtil.makeItem(particle.getItemMaterial(), 1, getDisplayNameForParticle(particle.getId()), lore);

                        if(!isUsingDefaultParticle(player) && getCurrentParticle(player).getId() == particle.getId()) {
                            item = Util.makeEnchanted(item);
                            lore = Arrays.asList(
                                    "§7§m--------------------",
                                    " §cKlikkaa poistaaksesi",
                                    " §ckäytöstä!",
                                    "§7§m--------------------"
                            );
                            ItemMeta meta = item.getItemMeta();
                            meta.setLore(lore);
                            item.setItemMeta(meta);

                            gui.addButton(new Button(1, pos, item) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(player);
                                    removeCurrentParticle(player);
                                    Chat.sendMessage(player, "Deaktivoit partikkelin §6" + getDisplayNameForParticle(particle.getId()));
                                }
                            });

                        } else {
                            gui.addButton(new Button(1, pos, item) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(player);
                                   // System.out.println("Click ID" + particle.getId());
                                    setCurrentParticle(player, particle);
                                    Chat.sendMessage(player, "Aktivoit partikkelin §6" + getDisplayNameForParticle(particle.getId()));
                                }
                            });
                        }

                    } else {
                        gui.addItem(1, ItemUtil.makeItem(Material.GRAY_DYE, 1, "§cEi saatavilla"), pos);
                    }

                    i += 1;
                    if(i >= getAllParticles().size()) break;

                }

                gui.addButton(new Button(1, 45, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        openMainGui(player);
                    }
                });

            });
        } else {
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän ominaisuuteen vaaditaan vähintään §a§lPremium§7-arvo!");
        }
    }

    public static ParticlePair getCurrentParticle(Player player) {

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        if(!String.valueOf(PlayerData.getValue(player.getUniqueId(), "particle")).equalsIgnoreCase("default")) {

            if(String.valueOf(PlayerData.getValue(player.getUniqueId(), "particle")).equals("null")) {
                return null;
            }

            return Autio.getParticlesAPI().getActivePlayerParticle(player,
                    Integer.parseInt(String.valueOf(PlayerData.getValue(player.getUniqueId(), "particle"))));

        }

        return null;

    }

    public static ParticlePair findParticle(int id) {
        for(ParticlePair p : getAllParticles()) {
            if(p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public static ParticlePair findArrowTrail(int id) {
        for(ParticlePair p : getAllArrowTrails()) {
            if(p.getId() == id) {
                return p;
            }
        }
        return null;
    }


    public static void setCurrentParticle(Player player, ParticlePair particle) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        if(!isUsingDefaultParticle(player)) {
            Autio.getParticlesAPI().removeActivePlayerParticle(player, getCurrentParticle(player).getId());
        }

        Autio.getParticlesAPI().addActivePlayerParticle(player, particle);
        PlayerData.set(player.getUniqueId(), "particle", String.valueOf(particle.getId()));

    }

    public static boolean isUsingDefaultParticle(Player player) {
        return getCurrentParticle(player) == null;
    }

    public static ParticlePair getCurrentArrowTrail(Player player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        if(!String.valueOf(PlayerData.getValue(player.getUniqueId(), "arrowtrail")).equalsIgnoreCase("default")) {

            if(String.valueOf(PlayerData.getValue(player.getUniqueId(), "arrowtrail")).equals("null")) {
                return null;
            }

            return Autio.getParticlesAPI().getActivePlayerParticle(player,
                    Integer.parseInt(String.valueOf(PlayerData.getValue(player.getUniqueId(), "arrowtrail"))));

        }

        return null;
    }

    public static void setCurrentArrowTrail(Player player, ParticlePair particle) {
        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        if(!isUsingDefaultArrowTrail(player)) {
            Autio.getParticlesAPI().removeActivePlayerParticle(player, getCurrentArrowTrail(player).getId());
        }
        Autio.getParticlesAPI().addActivePlayerParticle(player, particle);
        PlayerData.set(player.getUniqueId(), "arrowtrail", String.valueOf(particle.getId()));

        PlayerParticles.getInstance().reload();

    }

    public static boolean isUsingDefaultArrowTrail(Player player) {
        return getCurrentArrowTrail(player) == null;
    }

    public static void removeCurrentParticle(Player player) {

        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        if(!isUsingDefaultParticle(player)) {
            Autio.getParticlesAPI().removeActivePlayerParticle(player, getCurrentParticle(player).getId());
        }
        PlayerData.set(player.getUniqueId(), "particle", "default");


    }

    public static void removeCurrentArrowTrail(Player player) {


        if(!PlayerData.isLoaded(player.getUniqueId())) {
            PlayerData.loadNull(player.getUniqueId(), false);
        }

        if(!isUsingDefaultArrowTrail(player)) {
            Autio.getParticlesAPI().removeActivePlayerParticle(player, getCurrentArrowTrail(player).getId());
        }
        PlayerData.set(player.getUniqueId(), "arrowtrail", "default");

    }

    public static boolean isAllowedForParticle(Player player, ParticlePair particle) {

        for(ParticlePair p : getParticlesForPlayer(player)) {
            if(p == null) continue;
            if(p.getId() == particle.getId()) return true;
        }

        return false;

    }

    public static List<ParticlePair> getParticlesForPlayer(Player player) {

        if(Ranks.hasRank(player, "premium")) {

            return getPremiumPlusParticles();

        } else if(Ranks.hasRank(player, "premiumplus")) {

            return getPremiumPlusParticles();

        } else if(Ranks.hasRank(player, "kuningas")) {

            return getKuningasParticles();

        } else if(Ranks.isStaff(player.getUniqueId())) {
            return getAllParticles();
        } else {
            return new ArrayList<>();
        }

    }

    public static List<ParticlePair> getPremiumParticles() {

        List<ParticlePair> particles = new ArrayList<>();

        particles.add(new ParticlePair(null, 1, ParticleEffect.ENCHANT, new ParticleStyleNormal(),
                Material.ENCHANTED_BOOK,null, null, null));

        particles.add(new ParticlePair(null, 2, ParticleEffect.EXPLOSION, new ParticleStyleNormal(),
                Material.FIRE_CHARGE, null, null, null));

        particles.add(new ParticlePair(null, 3, ParticleEffect.DUST, new ParticleStyleNormal(),
                Material.GUNPOWDER, null, null, null));

        particles.add(new ParticlePair(null, 4, ParticleEffect.BUBBLE_POP, new ParticleStyleNormal(),
                Material.LIGHT_BLUE_DYE, null, null, null));

        particles.add(new ParticlePair(null, 5, ParticleEffect.DRAGON_BREATH, new ParticleStyleNormal(),
                Material.DRAGON_BREATH, null, null, null));

        particles.add(new ParticlePair(null, 6, ParticleEffect.FALLING_WATER, new ParticleStyleOverhead(),
                Material.BLUE_DYE, null, null, null));


        return particles;

    }

    public static List<ParticlePair> getPremiumPlusParticles() {

        List<ParticlePair> particles = new ArrayList<>();

        for(ParticlePair p : getPremiumParticles()) {
            if(p == null) continue;
            particles.add(p);
        }

        particles.add(new ParticlePair(null, 7, ParticleEffect.BARRIER, new ParticleStyleFeet(),
                Material.RED_DYE, null, null, null));

        particles.add(new ParticlePair(null, 8, ParticleEffect.PORTAL, new ParticleStyleNormal(),
                Material.ENDER_PEARL, null, null, null));

        particles.add(new ParticlePair(null, 9, ParticleEffect.TOTEM_OF_UNDYING, new ParticleStyleQuadhelix(),
                Material.TOTEM_OF_UNDYING, null, null, null));

        particles.add(new ParticlePair(null, 10, ParticleEffect.CAMPFIRE_COSY_SMOKE, new ParticleStyleFeet(),
                Material.FERMENTED_SPIDER_EYE, null, null, null));

        particles.add(new ParticlePair(null, 11, ParticleEffect.FALLING_LAVA, new ParticleStyleVortex(),
                Material.LAVA_BUCKET, null, null, null));

        particles.add(new ParticlePair(null, 12, ParticleEffect.HEART, new ParticleStyleOverhead(),
                Material.APPLE, null, null, null));


        return particles;

    }

    public static List<ParticlePair> getKuningasParticles() {

        List<ParticlePair> particles = new ArrayList<>();

        for(ParticlePair p : getPremiumPlusParticles()) {
            if(p == null) continue;
            particles.add(p);
        }


        particles.add(new ParticlePair(null, 13, ParticleEffect.ANGRY_VILLAGER, new ParticleStyleSpin(),
                Material.BLAZE_ROD, null, null, null));


        particles.add(new ParticlePair(null, 14, ParticleEffect.DUST, new ParticleStyleWings(),
                Material.ELYTRA, null, new ParticleEffect.OrdinaryColor(255, 255, 255), null));

        particles.add(new ParticlePair(null, 15, ParticleEffect.ENTITY_EFFECT, new ParticleStyleWhirl(),
                Material.GLOWSTONE_DUST, null, null, null));


        particles.add(new ParticlePair(null, 16, ParticleEffect.ENCHANTED_HIT, new ParticleStyleSpiral(),
                Material.DIAMOND_SWORD, null, null, null));


        particles.add(new ParticlePair(null, 17, ParticleEffect.CRIT, new ParticleStyleCompanion(),
                Material.NAME_TAG, null, null, null));


        particles.add(new ParticlePair(null, 18, ParticleEffect.DUST, new ParticleStyleSpiral(),
                Material.REDSTONE, null, ParticleEffect.OrdinaryColor.RAINBOW, null));



        return particles;

    }

    public static String getDisplayNameForParticle(int id) {

        String name = "";

        switch(id) {
            case 1:
                name = "§5Lumottu";
                break;
            case 2:
                name = "§cRäjähdys";
                break;
            case 3:
                name = "§7Savu";
                break;
            case 4:
                name = "§bKupliva";
                break;
            case 5:
                name = "§dLohikäärmeen henkäys";
                break;
            case 6:
                name = "§9Vesisade";
                break;
            case 7:
                name = "§cSTOP!";
                break;
            case 8:
                name = "§5Portaali";
                break;
            case 9:
                name = "§eKuolemattomuuden toteemi";
                break;
            case 10:
                name = "§7Savuaskeleet";
                break;
            case 11:
                name = "§6Puotava Laava";
                break;
            case 12:
                name = "§4Sydämet";
                break;
            case 13:
                name = "§eSalamat";
                break;
            case 14:
                name = "§fEnkelin siivet";
                break;
            case 15:
                name = "§2Taikajuoma";
                break;
            case 16:
                name = "§3Spiraali";
                break;
            case 17:
                name = "§aToveri";
                break;
            case 18:
                name = "§cSa§etee§ank§baa§dri";
                break;

        }

        return name;

    }

    public static String getDisplayNameForArrowTrail(int id) {

        String name = "";

        switch(id) {
            case 100:
                name = "§4Sydämet";
                break;
            case 101:
                name = "§eSalamat";
                break;
            case 102:
                name = "§aEmerald";
                break;
            case 103:
                name = "§6Liekki";
                break;
            case 104:
                name = "§fLoitsu";
                break;
            case 105:
                name = "§dMusiikki";
                break;
            case 106:
                name = "§eKuolemattomuuden toteemi";
                break;
            case 107:
                name = "§cSa§etee§ank§baa§dri";
                break;
            case 108:
                name = "§5Noita";
                break;
            case 109:
                name = "§7Crit";
                break;
            case 110:
                name = "§4Veri";
                break;

        }

        return name;

    }

    public static List<ParticlePair> getAllParticles() {
        return getKuningasParticles();
    }

    public static boolean isAllowedForArrowTrail(Player player, ParticlePair particle) {

        for(ParticlePair p : getArrowTrailsForPlayer(player)) {
            if(p == null) continue;
            if(p.getId() == particle.getId()) return true;
        }

        return false;

    }

    public static List<ParticlePair> getArrowTrailsForPlayer(Player player) {

        if(Ranks.hasRank(player, "premium")) {

            return getPremiumArrowTrails();

        } else if(Ranks.hasRank(player, "premiumplus")) {

            return getPremiumPlusArrowTrails();

        } else if(Ranks.hasRank(player, "kuningas")) {

            return getKuningasArrowtrails();

        } else if(Ranks.isStaff(player.getUniqueId())) {
            return getAllArrowTrails();
        } else {
            return new ArrayList<>();
        }

    }

    public static List<ParticlePair> getPremiumArrowTrails() {

        List<ParticlePair> particles = new ArrayList<>();

        particles.add(new ParticlePair(null, 100, ParticleEffect.HEART, new ParticleStyleArrows(),
                Material.APPLE, null, null, null));

        particles.add(new ParticlePair(null, 101, ParticleEffect.ANGRY_VILLAGER, new ParticleStyleArrows(),
                Material.BLAZE_ROD, null, null, null));

        particles.add(new ParticlePair(null, 102, ParticleEffect.HAPPY_VILLAGER, new ParticleStyleArrows(),
                Material.EMERALD, null, null, null));

        particles.add(new ParticlePair(null, 103, ParticleEffect.FLAME, new ParticleStyleArrows(),
                Material.BLAZE_POWDER, null, null, null));


        return particles;

    }

    public static List<ParticlePair> getPremiumPlusArrowTrails() {

        List<ParticlePair> particles = new ArrayList<>();

        for(ParticlePair p : getPremiumArrowTrails()) {
            if(p == null) continue;
            particles.add(p);
        }

        particles.add(new ParticlePair(null, 104, ParticleEffect.SPELL, new ParticleStyleArrows(),
                Material.GLASS_BOTTLE, null, null, null));

        particles.add(new ParticlePair(null, 105, ParticleEffect.NOTE, new ParticleStyleArrows(),
                Material.GLISTERING_MELON_SLICE, null, null, null));

        particles.add(new ParticlePair(null, 106, ParticleEffect.TOTEM_OF_UNDYING, new ParticleStyleArrows(),
                Material.TOTEM_OF_UNDYING, null, null, null));

        return particles;

    }

    public static List<ParticlePair> getKuningasArrowtrails() {

        List<ParticlePair> particles = new ArrayList<>();

        for(ParticlePair p : getPremiumPlusArrowTrails()) {
            if(p == null) continue;
            particles.add(p);
        }

        particles.add(new ParticlePair(null, 107, ParticleEffect.DUST, new ParticleStyleArrows(),
                Material.SLIME_BALL, null, ParticleEffect.OrdinaryColor.RAINBOW, null));


        particles.add(new ParticlePair(null, 108, ParticleEffect.WITCH, new ParticleStyleArrows(),
                Material.GHAST_TEAR, null, null, null));


        particles.add(new ParticlePair(null, 109, ParticleEffect.CRIT, new ParticleStyleArrows(),
                Material.IRON_SWORD, null, null, null));


        particles.add(new ParticlePair(null, 110, ParticleEffect.DAMAGE_INDICATOR, new ParticleStyleArrows(),
                Material.REDSTONE, null, null, null));

        return particles;

    }

    public static List<ParticlePair> getAllArrowTrails() {
        return getKuningasArrowtrails();
    }

}
