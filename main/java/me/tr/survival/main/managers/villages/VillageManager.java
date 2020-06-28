package me.tr.survival.main.managers.villages;

import me.tr.survival.main.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.UUID;

public class VillageManager implements Listener, CommandExecutor {

    private final List<PlayerVillage> villages = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }

    // File Management
    private File villageFile;
    private FileConfiguration villageConfig;

    public synchronized void loadVillagesFromFile() {

        villages.clear();
        final FileConfiguration config = this.getVillageConfig();
        final ConfigurationSection villagesSection = config.getConfigurationSection("villages");

        for(final String rawUuid : villagesSection.getKeys(false)) {

            final ConfigurationSection villageSection = villagesSection.getConfigurationSection(rawUuid);
            final PlayerVillage village = parseVillageFromConfig(villageSection);

            villages.add(village);

        }

    }

    private synchronized PlayerVillage parseVillageFromConfig(final ConfigurationSection section) throws IllegalFormatException {

        return null;

    }

    public void savePlayerVillages() {

        final FileConfiguration config = this.getVillageConfig();
        final ConfigurationSection villagesSection = config.getConfigurationSection("villages");

        for(final PlayerVillage village : villages) {



        }

    }

    public void createVillageConfig() {
        villageFile = new File(Main.getInstance().getDataFolder(), "villages.yml");
        if (!villageFile.exists()) {
            villageFile.getParentFile().mkdirs();
            Main.getInstance().saveResource("villages.yml", false);
        }
        villageConfig= new YamlConfiguration();
        reloadVillageConfig();
    }


    private void saveVillageConfig() {



        try { villageConfig.save(villageFile);
        } catch(IOException e) { e.printStackTrace(); }
    }

    private void reloadVillageConfig() {
        try { villageConfig.load(villageFile);
        } catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
    }

    private FileConfiguration getVillageConfig() {
        return villageConfig;
    }



}
