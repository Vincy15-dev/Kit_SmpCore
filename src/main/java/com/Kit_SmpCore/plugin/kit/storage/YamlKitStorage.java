package com.Kit_SmpCore.plugin.kit.storage;

import com.Kit_SmpCore.plugin.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Implementazione YAML per lo storage dei dati dei player.
 * File: plugins/Kit_SmpCore/playerdata/<uuid>.yml
 */
public class YamlKitStorage implements KitStorage {
    
    private final File dataFolder;
    private final String fileName = "playerdata";
    
    public YamlKitStorage(@NotNull File dataFolder) {
        this.dataFolder = new File(dataFolder, fileName);
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }
    
    @Override
    @Nullable
    public PlayerData load(@NotNull UUID playerId) {
        File file = getPlayerFile(playerId);
        if (!file.exists()) {
            return new PlayerData(playerId);
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            PlayerData data = new PlayerData(playerId);
            
            // Carica cooldown
            if (config.isConfigurationSection("cooldowns")) {
                for (String kitId : config.getConfigurationSection("cooldowns").getKeys(false)) {
                    long timestamp = config.getLong("cooldowns." + kitId);
                    // Ignora timestamp nel futuro o invalidi
                    if (timestamp > 0 && timestamp <= System.currentTimeMillis()) {
                        data.setCooldown(kitId, timestamp);
                    }
                }
            }
            
            // Carica one-time claims
            if (config.isConfigurationSection("oneTimeClaims")) {
                for (String kitId : config.getConfigurationSection("oneTimeClaims").getKeys(false)) {
                    data.markClaimed(kitId);
                }
            }
            
            data.setLoaded(true);
            return data;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Errore nel caricamento dati per " + playerId, e);
            PlayerData data = new PlayerData(playerId);
            data.setLoaded(true);
            return data;
        }
    }
    
    @Override
    public void save(@NotNull UUID playerId, @NotNull PlayerData data) {
        saveSnapshot(playerId, data.getCooldownSnapshot(), data.getOneTimeSnapshot());
    }
    
    @Override
    public void saveSnapshot(@NotNull UUID playerId, 
                             @NotNull Map<String, Long> cooldowns,
                             @NotNull Map<String, Boolean> oneTimeClaims) {
        File file = getPlayerFile(playerId);
        FileConfiguration config = new YamlConfiguration();
        
        // Salva cooldown
        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            config.set("cooldowns." + entry.getKey(), entry.getValue());
        }
        
        // Salva one-time claims
        for (Map.Entry<String, Boolean> entry : oneTimeClaims.entrySet()) {
            config.set("oneTimeClaims." + entry.getKey(), true);
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Errore nel salvataggio dati per " + playerId, e);
        }
    }
    
    @Override
    public void delete(@NotNull UUID playerId) {
        File file = getPlayerFile(playerId);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @NotNull
    private File getPlayerFile(@NotNull UUID playerId) {
        return new File(dataFolder, playerId.toString() + ".yml");
    }
}
