package me.tr.survival.main.managers.perks;

import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.particles.ParticlePair;
import dev.esophose.playerparticles.particles.data.OrdinaryColor;
import dev.esophose.playerparticles.styles.*;
import me.tr.survival.main.Sorsa;
import me.tr.survival.main.database.PlayerData;
import me.tr.survival.main.managers.Chat;
import me.tr.survival.main.managers.Profile;
import me.tr.survival.main.other.Ranks;
import me.tr.survival.main.util.Util;
import me.tr.survival.main.util.ItemUtil;
import me.tr.survival.main.util.gui.Button;
import me.tr.survival.main.util.gui.Gui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
        Sorsa.getParticlesAPI().resetActivePlayerParticles(player);
    }

    private void loadParticles(Player player) {
        UUID uuid = player.getUniqueId();
        if(!PlayerData.isLoaded(uuid)) PlayerData.loadNull(uuid, false);
        Sorsa.getParticlesAPI().resetActivePlayerParticles(player);
        String rawParticleID = String.valueOf(PlayerData.getValue(uuid, "particle"));
        String rawArrowTrailID = String.valueOf(PlayerData.getValue(uuid, "arrowtrail"));
        if(!rawArrowTrailID.equalsIgnoreCase("default") && !rawArrowTrailID.equalsIgnoreCase("null")) Sorsa.getParticlesAPI().addActivePlayerParticle(player, findArrowTrail(Integer.parseInt(rawArrowTrailID)));
        if(!rawParticleID.equalsIgnoreCase("default") && !rawParticleID.equalsIgnoreCase("null")) Sorsa.getParticlesAPI().addActivePlayerParticle(player, findParticle(Integer.parseInt(rawParticleID)));
        PlayerParticles.getInstance().reload();
    }

    public void reloadParticles(Player player) {
        loadParticles(player);
    }

    public void openMainGui(Player player) {

        if(Ranks.isVIP(player.getUniqueId())) {
            Gui.openGui(player, "Kosmetiikka", 27, (gui) -> {

               // System.out.println(((isUsingDefaultParticle(player) ? "default" : String.valueOf(getCurrentParticle(player).getId()))));
               // System.out.println(((isUsingDefaultArrowTrail(player) ? "default" : String.valueOf(getCurrentArrowTrail(player).getId()))));

                gui.addButton(new Button(1, 12, ItemUtil.makeItem(Material.HEART_OF_THE_SEA, 1, "§aPartikkelit", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Parikkeliefektit ovat siistejä",
                        " §7animaatioita jotka hyrräävät",
                        " §7ympärilläsi! Täydellinen tapa",
                        " §7tehdä ystävästi §ekateelliseksi§7!",
                        " ",
                        " §aKlikkaa avataksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                ))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {

                        gui.close(player);
                        openParticlesGui(player);

                    }
                });

                gui.addButton(new Button(1, 14, ItemUtil.makeItem(Material.BOW, 1, "§bNuolijanat", Arrays.asList(
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                        " §7Nuolijanoilla saat hienoja",
                        " §7partikkeleita seuraamaan",
                        " §7nuoliasi! Huimaa!",
                        " ",
                        " §aKlikkaa avataksesi!",
                        "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
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

                int[] glassSlots = new int[] { 11,13,15 };
                for(int slot : glassSlots) { gui.addItem(1, ItemUtil.makeItem(Material.CYAN_STAINED_GLASS_PANE), slot); }

                for(int i = 0; i < 27; i++) {
                    if(gui.getItem(i) != null) continue;
                    if(gui.getButton(i) != null) continue;
                    gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), i);
                }

            });
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän ominaisuuteen vaaditaan vähintään §e§lPremium§7-arvo!");
        }

    }

    private void openArrowTrailGui(Player player) {
        if(Ranks.isVIP(player.getUniqueId())) {
            Gui.openGui(player, "Nuolijanat", 36, (gui) -> {

                int i = 0;
                for(int pos = 10; pos < 26; pos++) {
                    if(pos == 17 || pos == 18) continue;

                    ParticlePair loopParticle = getAllArrowTrails().get(i);
                    ParticlePair particle = findArrowTrail(loopParticle.getId());

                    if(isAllowedForArrowTrail(player, particle)) {

                        List<String> lore = new ArrayList<>();

                        lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                        lore.add(" §aKlikkaa aktivoidaksesi!");
                        lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                        ItemStack item = ItemUtil.makeItem(particle.getItemMaterial(), 1, getDisplayNameForArrowTrail(particle.getId()), lore);

                        if(!isUsingDefaultArrowTrail(player) && getCurrentArrowTrail(player).getId() == particle.getId()) {
                            item = Util.makeEnchanted(item);
                            lore = Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §cKlikkaa poistaaksesi",
                                    " §ckäytöstä!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            );
                            ItemMeta meta = item.getItemMeta();
                            meta.setLore(lore);
                            item.setItemMeta(meta);

                            gui.addButton(new Button(1, pos, item) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(player);
                                    removeCurrentArrowTrail(player);
                                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                                    Chat.sendMessage(player, "Deaktivoit nuolijanan §6" + getDisplayNameForArrowTrail(particle.getId()));
                                }
                            });

                        } else {
                            gui.addButton(new Button(1, pos, item) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(player);
                                    setCurrentArrowTrail(player, particle);
                                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                                    Chat.sendMessage(player, "Aktivoit nuolijanan §6" + getDisplayNameForArrowTrail(particle.getId()));
                                }
                            });
                        }

                    } else gui.addItem(1, ItemUtil.makeItem(Material.GRAY_DYE, 1, "§cEi saatavilla"), pos);

                    i += 1;
                    if(i >= getAllArrowTrails().size()) break;
                }

                gui.addButton(new Button(1, 27, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        openMainGui(player);
                    }
                });

                gui.addButton(new Button(1, 35, ItemUtil.makeItem(Material.BARRIER, 1, "§cTyhjennä", Arrays.asList("§7Tyhjennä nykyinen valintasi!"))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                        Chat.sendMessage(player, "Deaktivoit nuolijanan §6" + getDisplayNameForArrowTrail(getCurrentArrowTrail(clicker).getId()));
                        removeCurrentArrowTrail(player);
                    }
                });

                for(int j = 0; j < 36; j++) {
                    if(gui.getButton(j) != null) continue;
                    if(gui.getItem(j) != null) continue;
                    if(j == 23 || j == 24 || j == 25) continue;
                    gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), j);

                }
            });
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän ominaisuuteen vaaditaan vähintään §e§lPremium§7-arvo!");
        }
    }

    private void openParticlesGui(Player player) {
        if(Ranks.isVIP(player.getUniqueId())) {
            Gui.openGui(player, "Partikkelit", 45, (gui) -> {

                int i = 0;
                for(int pos = 10; pos < 35; pos++) {
                    if(pos == 17 || pos == 18 || pos == 26 || pos == 27) continue;
                    ParticlePair loopParticle = getAllParticles().get(i);
                    ParticlePair particle = findParticle(loopParticle.getId());

                    if(isAllowedForParticle(player, particle)) {

                        List<String> lore = new ArrayList<>();

                        lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");
                        lore.add(" §7Klikkaa aktivoidaksesi!");
                        lore.add("§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤");

                        ItemStack item = ItemUtil.makeItem(particle.getItemMaterial(), 1, getDisplayNameForParticle(particle.getId()), lore);

                        if(!isUsingDefaultParticle(player) && getCurrentParticle(player).getId() == particle.getId()) {
                            item = Util.makeEnchanted(item);
                            lore = Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    " §cKlikkaa poistaaksesi",
                                    " §ckäytöstä!",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            );
                            ItemMeta meta = item.getItemMeta();
                            meta.setLore(lore);
                            item.setItemMeta(meta);

                            gui.addButton(new Button(1, pos, item) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(player);
                                    removeCurrentParticle(player);
                                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
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
                                    clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                                    Chat.sendMessage(player, "Aktivoit partikkelin §6" + getDisplayNameForParticle(particle.getId()));
                                }
                            });
                        }

                    } else gui.addItem(1, ItemUtil.makeItem(Material.GRAY_DYE, 1, "§cEi saatavilla"), pos);

                    i += 1;
                    if(i >= getAllParticles().size()) break;
                }

                gui.addButton(new Button(1, 36, ItemUtil.makeItem(Material.ARROW, 1, "§7Takaisin")) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(player);
                        openMainGui(player);
                    }
                });

                gui.addButton(new Button(1, 44, ItemUtil.makeItem(Material.BARRIER, 1, "§cTyhjennä", Arrays.asList("§7Tyhjennä nykyinen valintasi!"))) {
                    @Override
                    public void onClick(Player clicker, ClickType clickType) {
                        gui.close(clicker);
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                        Chat.sendMessage(player, "Deaktivoit partikkelin §6" + getDisplayNameForParticle(getCurrentParticle(clicker).getId()));
                        removeCurrentParticle(player);
                    }
                });

                for(int j = 0; j < 45; j++) {
                    if(gui.getButton(j) != null) continue;
                    if(gui.getItem(j) != null) continue;
                    if(j == 32 || j == 33 || j == 34) continue;
                    gui.addItem(1, ItemUtil.makeItem(Material.GRAY_STAINED_GLASS_PANE), j);
                }

            });
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            Chat.sendMessage(player, Chat.Prefix.ERROR, "Tähän ominaisuuteen vaaditaan vähintään §e§lPremium§7-arvo!");
        }
    }

    private ParticlePair getCurrentParticle(Player player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        if(!String.valueOf(PlayerData.getValue(player.getUniqueId(), "particle")).equalsIgnoreCase("default")) {
            if(String.valueOf(PlayerData.getValue(player.getUniqueId(), "particle")).equals("null")) return null;
            return Sorsa.getParticlesAPI().getActivePlayerParticle(player,
                    Integer.parseInt(String.valueOf(PlayerData.getValue(player.getUniqueId(), "particle"))));
        }
        return null;
    }

    private ParticlePair findParticle(int id) {
        for(ParticlePair p : getAllParticles()) { if(p.getId() == id) return p; }
        return null;
    }

    private ParticlePair findArrowTrail(int id) {
        for(ParticlePair p : getAllArrowTrails()) { if(p.getId() == id) return p; }
        return null;
    }


    private void setCurrentParticle(Player player, ParticlePair particle) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        if(!isUsingDefaultParticle(player)) Sorsa.getParticlesAPI().removeActivePlayerParticle(player, getCurrentParticle(player).getId());
        Sorsa.getParticlesAPI().addActivePlayerParticle(player, particle);
        PlayerData.set(player.getUniqueId(), "particle", String.valueOf(particle.getId()));
        Sorsa.logColored("§6[ParticleManager] The player " + player.getName() + " enabled the particle effect: " + particle.getId() + "!");
    }

    private boolean isUsingDefaultParticle(Player player) {
        return getCurrentParticle(player) == null;
    }

    private ParticlePair getCurrentArrowTrail(Player player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        if(!String.valueOf(PlayerData.getValue(player.getUniqueId(), "arrowtrail")).equalsIgnoreCase("default")) {
            if(String.valueOf(PlayerData.getValue(player.getUniqueId(), "arrowtrail")).equals("null")) return null;
            return Sorsa.getParticlesAPI().getActivePlayerParticle(player,
                    Integer.parseInt(String.valueOf(PlayerData.getValue(player.getUniqueId(), "arrowtrail"))));
        }
        return null;
    }

    private void setCurrentArrowTrail(Player player, ParticlePair particle) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        if(!isUsingDefaultArrowTrail(player)) Sorsa.getParticlesAPI().removeActivePlayerParticle(player, getCurrentArrowTrail(player).getId());
        Sorsa.getParticlesAPI().addActivePlayerParticle(player, particle);
        PlayerData.set(player.getUniqueId(), "arrowtrail", String.valueOf(particle.getId()));
        PlayerParticles.getInstance().reload();
        Sorsa.logColored("§6[ParticleManager] The player " + player.getName() + " enabled the arrow trail: " + particle.getId() + "!");
    }

    private boolean isUsingDefaultArrowTrail(Player player) {
        return getCurrentArrowTrail(player) == null;
    }

    public void removeCurrentParticle(Player player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        if(!isUsingDefaultParticle(player)) Sorsa.getParticlesAPI().removeActivePlayerParticle(player, getCurrentParticle(player).getId());
        PlayerData.set(player.getUniqueId(), "particle", "default");
        Sorsa.logColored("§6[ParticleManager] The player " + player.getName() + " deactivated their particle effect!");
    }

    public void removeCurrentArrowTrail(Player player) {
        if(!PlayerData.isLoaded(player.getUniqueId())) PlayerData.loadNull(player.getUniqueId(), false);
        if(!isUsingDefaultArrowTrail(player)) Sorsa.getParticlesAPI().removeActivePlayerParticle(player, getCurrentArrowTrail(player).getId());
        PlayerData.set(player.getUniqueId(), "arrowtrail", "default");
        Sorsa.logColored("§6[ParticleManager] The player " + player.getName() + " deactivated their arrow trail!");
    }

    private boolean isAllowedForParticle(Player player, ParticlePair particle) {
        for(ParticlePair p : getParticlesForPlayer(player)) {
            if(p == null) continue;
            if(p.getId() == particle.getId()) return true;
        }
        return false;
    }

    private List<ParticlePair> getParticlesForPlayer(Player player) {
        if(Ranks.isStaff(player.getUniqueId())) return getAllParticles();
        if(Ranks.hasRank(player, "sorsa")) return getKuningasParticles();
        if(Ranks.hasRank(player, "premiumplus")) return getPremiumPlusParticles();
        if(Ranks.hasRank(player, "premium")) return getPremiumPlusParticles();
        return new ArrayList<>();
    }

    private List<ParticlePair> getPremiumParticles() {

        List<ParticlePair> particles = new ArrayList<>();
        particles.add(new ParticlePair(null, 1, ParticleEffect.ENCHANT, DefaultStyles.NORMAL,
                Material.ENCHANTED_BOOK,null, null, null));

        particles.add(new ParticlePair(null, 2, ParticleEffect.EXPLOSION, DefaultStyles.NORMAL,
                Material.FIRE_CHARGE, null, null, null));

        particles.add(new ParticlePair(null, 3, ParticleEffect.DUST, DefaultStyles.NORMAL,
                Material.GUNPOWDER, null, null, null));

        particles.add(new ParticlePair(null, 4, ParticleEffect.BUBBLE_POP, DefaultStyles.NORMAL,
                Material.LIGHT_BLUE_DYE, null, null, null));

        particles.add(new ParticlePair(null, 5, ParticleEffect.DRAGON_BREATH, DefaultStyles.NORMAL,
                Material.DRAGON_BREATH, null, null, null));

        particles.add(new ParticlePair(null, 6, ParticleEffect.FALLING_WATER, DefaultStyles.OVERHEAD,
                Material.BLUE_DYE, null, null, null));

        return particles;
    }

    private List<ParticlePair> getPremiumPlusParticles() {

        List<ParticlePair> particles = new ArrayList<>();

        for(ParticlePair p : getPremiumParticles()) {
            if(p == null) continue;
            particles.add(p);
        }

        particles.add(new ParticlePair(null, 7, ParticleEffect.BARRIER, DefaultStyles.FEET,
                Material.RED_DYE, null, null, null));

        particles.add(new ParticlePair(null, 8, ParticleEffect.PORTAL, DefaultStyles.NORMAL,
                Material.ENDER_PEARL, null, null, null));

        particles.add(new ParticlePair(null, 9, ParticleEffect.TOTEM_OF_UNDYING, DefaultStyles.QUADHELIX,
                Material.TOTEM_OF_UNDYING, null, null, null));

        particles.add(new ParticlePair(null, 10, ParticleEffect.CAMPFIRE_COSY_SMOKE, DefaultStyles.FEET,
                Material.FERMENTED_SPIDER_EYE, null, null, null));

        particles.add(new ParticlePair(null, 11, ParticleEffect.FALLING_LAVA, DefaultStyles.VORTEX,
                Material.LAVA_BUCKET, null, null, null));

        particles.add(new ParticlePair(null, 12, ParticleEffect.HEART, DefaultStyles.OVERHEAD,
                Material.APPLE, null, null, null));

        return particles;
    }

    private List<ParticlePair> getKuningasParticles() {

        List<ParticlePair> particles = new ArrayList<>();
        for(ParticlePair p : getPremiumPlusParticles()) {
            if(p == null) continue;
            particles.add(p);
        }

        particles.add(new ParticlePair(null, 13, ParticleEffect.ANGRY_VILLAGER, DefaultStyles.SPIN,
                Material.BLAZE_ROD, null, null, null));

        particles.add(new ParticlePair(null, 14, ParticleEffect.DUST, DefaultStyles.WINGS,
                Material.ELYTRA, null, new OrdinaryColor(255, 255, 255), null));

        particles.add(new ParticlePair(null, 15, ParticleEffect.ENTITY_EFFECT, DefaultStyles.WHIRL,
                Material.GLOWSTONE_DUST, null, null, null));

        particles.add(new ParticlePair(null, 16, ParticleEffect.ENCHANTED_HIT, DefaultStyles.SPIRAL,
                Material.DIAMOND_SWORD, null, null, null));

        particles.add(new ParticlePair(null, 17, ParticleEffect.CRIT, DefaultStyles.COMPANION,
                Material.NAME_TAG, null, null, null));

        particles.add(new ParticlePair(null, 18, ParticleEffect.DUST, DefaultStyles.SPIRAL,
                Material.REDSTONE, null, OrdinaryColor.RAINBOW, null));

        return particles;
    }

    private String getDisplayNameForParticle(int id) {
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

    private String getDisplayNameForArrowTrail(int id) {
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

    private List<ParticlePair> getAllParticles() {
        return getKuningasParticles();
    }

    private boolean isAllowedForArrowTrail(Player player, ParticlePair particle) {
        for(ParticlePair p : getArrowTrailsForPlayer(player)) {
            if(p == null) continue;
            if(p.getId() == particle.getId()) return true;
        }
        return false;
    }

    private List<ParticlePair> getArrowTrailsForPlayer(Player player) {
        if(Ranks.isStaff(player.getUniqueId())) return getAllArrowTrails();
        if(Ranks.hasRank(player, "sorsa")) return getKuningasArrowtrails();
        if(Ranks.hasRank(player, "premiumplus")) return getPremiumPlusArrowTrails();
        if(Ranks.hasRank(player, "premium")) return getPremiumArrowTrails();
        return new ArrayList<>();
    }

    private List<ParticlePair> getPremiumArrowTrails() {

        List<ParticlePair> particles = new ArrayList<>();

        particles.add(new ParticlePair(null, 100, ParticleEffect.HEART, DefaultStyles.ARROWS,
                Material.APPLE, null, null, null));

        particles.add(new ParticlePair(null, 101, ParticleEffect.ANGRY_VILLAGER, DefaultStyles.ARROWS,
                Material.BLAZE_ROD, null, null, null));

        particles.add(new ParticlePair(null, 102, ParticleEffect.HAPPY_VILLAGER, DefaultStyles.ARROWS,
                Material.EMERALD, null, null, null));

        particles.add(new ParticlePair(null, 103, ParticleEffect.FLAME, DefaultStyles.ARROWS,
                Material.BLAZE_POWDER, null, null, null));

        return particles;
    }

    private List<ParticlePair> getPremiumPlusArrowTrails() {

        List<ParticlePair> particles = new ArrayList<>();
        for(ParticlePair p : getPremiumArrowTrails()) {
            if(p == null) continue;
            particles.add(p);
        }

        particles.add(new ParticlePair(null, 104, ParticleEffect.SPELL, DefaultStyles.ARROWS,
                Material.GLASS_BOTTLE, null, null, null));

        particles.add(new ParticlePair(null, 105, ParticleEffect.NOTE, DefaultStyles.ARROWS,
                Material.GLISTERING_MELON_SLICE, null, null, null));

        particles.add(new ParticlePair(null, 106, ParticleEffect.TOTEM_OF_UNDYING, DefaultStyles.ARROWS,
                Material.TOTEM_OF_UNDYING, null, null, null));

        return particles;
    }

    private List<ParticlePair> getKuningasArrowtrails() {

        List<ParticlePair> particles = new ArrayList<>();
        for(ParticlePair p : getPremiumPlusArrowTrails()) {
            if(p == null) continue;
            particles.add(p);
        }
        particles.add(new ParticlePair(null, 107, ParticleEffect.DUST, DefaultStyles.ARROWS,
                Material.SLIME_BALL, null, OrdinaryColor.RAINBOW, null));

        particles.add(new ParticlePair(null, 108, ParticleEffect.WITCH, DefaultStyles.ARROWS,
                Material.GHAST_TEAR, null, null, null));

        particles.add(new ParticlePair(null, 109, ParticleEffect.CRIT, DefaultStyles.ARROWS,
                Material.IRON_SWORD, null, null, null));

        particles.add(new ParticlePair(null, 110, ParticleEffect.DAMAGE_INDICATOR, DefaultStyles.ARROWS,
                Material.REDSTONE, null, null, null));
        return particles;
    }

    private List<ParticlePair> getAllArrowTrails() {
        return getKuningasArrowtrails();
    }

}
