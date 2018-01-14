package me.amgf.UltraStats;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {
    private ManagedData managedData;

    @Override
    public void onEnable() {
        managedData = new ManagedData(this);
        managedData.loadFromFile();

        setupFolderStructure();
        setupListeners();
        setupTasks();
        setupCommands();
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        managedData.saveToFile();
    }

    private void setupListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new EntityDeathListener(managedData), this);
    }

    private void setupTasks() {
        FileManager fileManager = new FileManager(managedData);

        Integer saveToFileDelay = 10; // 10 seconds

        getServer().getScheduler().runTaskTimerAsynchronously(this, fileManager, 0, 20 * saveToFileDelay);
    }

    private void setupCommands() {
        getCommand("ustat").setExecutor(new UStatCommand(this, managedData));
    }

    private void setupFolderStructure() {
        File root = getDataFolder();

        if(!root.exists()) {
            root.mkdir();
        }

        File playerDirectory = new File(root, "players");

        if(!playerDirectory.exists()) {
            String relativePath = root.toURI().relativize(playerDirectory.toURI()).getPath();
            getLogger().info("Missing folder: " + relativePath + ". Creating...");
            boolean result = playerDirectory.mkdir();
            if(result) {
                getLogger().info("Created folder: " + relativePath);
            } else {
                getLogger().warning("Failed to create folder: " + relativePath);
            }
        }
    }
}
