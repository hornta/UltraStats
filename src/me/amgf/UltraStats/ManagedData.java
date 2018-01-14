package me.amgf.UltraStats;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManagedData {
    private Plugin plugin;
    private ConcurrentHashMap<UUID, ConcurrentHashMap<EntityType, Integer>> mobKills;

    ManagedData(Plugin plugin) {
        mobKills = new ConcurrentHashMap<>();
        this.plugin = plugin;
    }

    public ConcurrentHashMap<UUID, ConcurrentHashMap<EntityType, Integer>> getMobKills() {
        return mobKills;
    }

    public Boolean hasEntityKills(UUID uuid, Entity entityType) {
        if(!mobKills.containsKey(uuid)) {
            return false;
        }

        if(!mobKills.get(uuid).containsKey(entityType)) {
            return false;
        }

        return true;
    }

    public Integer numberOfKills() {
        int total = 0;

        for(Map.Entry<UUID, ConcurrentHashMap<EntityType, Integer>> playerKills : mobKills.entrySet()) {
            for(Map.Entry<EntityType, Integer> entityKills : playerKills.getValue().entrySet()) {
                total += entityKills.getValue();
            }
        }

        return total;
    }

    public Integer numberOfKills(EntityType entityType) {
        int total = 0;

        for(Map.Entry<UUID, ConcurrentHashMap<EntityType, Integer>> playerKills : mobKills.entrySet()) {
            if(playerKills.getValue().containsKey(entityType)) {
                total += playerKills.getValue().get(entityType);
            }
        }

        return total;
    }

    public Integer numberOfKills(UUID uuid, EntityType entityType) {
        if(!mobKills.containsKey(uuid)) {
            return 0;
        }

        if(!mobKills.get(uuid).containsKey(entityType)) {
            return 0;
        }

        return mobKills.get(uuid).get(entityType);
    }

    public Integer numberOfKills(UUID uuid) {
        int total = 0;

        if(!mobKills.containsKey(uuid)) {
            return total;
        }

        for(Map.Entry<EntityType, Integer> entityKills : mobKills.get(uuid).entrySet()) {
            total += entityKills.getValue();
        }

        return total;
    }

    public void receiveEntityDeath(UUID playerUUID, EntityType entityType) {
        if(!mobKills.containsKey(playerUUID)) {
            mobKills.put(playerUUID, new ConcurrentHashMap<>());
        }

        if(!mobKills.get(playerUUID).containsKey(entityType)) {
            mobKills.get(playerUUID).put(entityType, 0);
        }

        Integer killAmount = mobKills.get(playerUUID).get(entityType);

        mobKills.get(playerUUID).put(entityType, killAmount + 1);
    }

    public void loadFromFile() {
        File dataFolder = new File(plugin.getDataFolder(), "players");
        File[] playerFiles = dataFolder.listFiles();

        if(playerFiles == null) {
            return;
        }

        for(File file : playerFiles) {
            String filename = file.getName();
            String fileExtension = FilenameUtils.getExtension(filename);

            if(!fileExtension.equals("yml")) {
                return;
            }

            String filenameWithoutExtension = FilenameUtils.removeExtension(filename);
            UUID playerUUID;

            try {
                playerUUID = UUID.fromString(filenameWithoutExtension);
            } catch(IllegalArgumentException e) {
                plugin.getLogger().warning(e.getMessage());
                return;
            }

            if(mobKills.containsKey(playerUUID)) {
                return;
            }

            mobKills.put(playerUUID, new ConcurrentHashMap<>());

            YamlConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
            Map<String, Object> playerStats = playerFile.getValues(false);
            for(Map.Entry stat : playerStats.entrySet()) {
                EntityType entityType = EntityType.valueOf(stat.getKey().toString());
                Integer killAmount;

                if(stat.getValue() instanceof Integer) {
                    killAmount = (Integer) stat.getValue();
                } else {
                    plugin.getLogger().severe("Incorrect format in " + filename);
                    continue;
                }

                if(mobKills.get(playerUUID).containsKey(entityType)) {
                    continue;
                }
                mobKills.get(playerUUID).put(entityType, killAmount);
            }
        }
    }

    public void saveToFile() {
        File playersFolder = new File(plugin.getDataFolder(), "players");

        if(!playersFolder.exists()) {
            return;
        }

        for(ConcurrentHashMap.Entry<UUID, ConcurrentHashMap<EntityType, Integer>> entry : mobKills.entrySet()) {
            File playerFile = new File(playersFolder, entry.getKey() + ".yml");

            if(playerFile.exists()) {
                try {
                    playerFile.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe(e.getMessage());
                }
            }

            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(playerFile);

            for(ConcurrentHashMap.Entry<EntityType, Integer> mobTypes : entry.getValue().entrySet()) {
                yamlConfiguration.set(mobTypes.getKey().toString(), mobTypes.getValue());
            }

            try {
                yamlConfiguration.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().severe(e.getMessage());
            }
        }
    }
}
