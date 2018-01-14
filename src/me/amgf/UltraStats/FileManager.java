package me.amgf.UltraStats;

import org.bukkit.plugin.java.JavaPlugin;

public class FileManager implements Runnable {
    private ManagedData managedData;

    FileManager(ManagedData managedData) {
        this.managedData = managedData;
    }

    @Override
    public void run() {
        if(managedData == null) {
            return;
        }

        managedData.saveToFile();
    }
}
