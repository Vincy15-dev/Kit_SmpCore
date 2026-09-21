package com.Kit_SmpCore.plugin.storage;

import com.Kit_SmpCore.plugin.model.PlayerData;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Implementazione YAML dello storage per i dati dei giocatori.
 * Un file per giocatore: playerdata/<uuid>.yml
 */
public class YamlKitStorage implements KitStorage {

    private final Plugin plugin;
    private final File dataFolder;
    private final ExecutorService executor;

    public YamlKitStorage(Plugin plugin, String folderName) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), folderName);
        this.executor = Executors.newFixedThreadPool(2);

        // Crea la cartella se non esiste
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Impossibile creare la cartella: " + dataFolder.getAbsolutePath());
        }
    }

    @Override
    public CompletableFuture<PlayerData> loadAsync(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            File file = getFileForPlayer(playerId);
            
            if (!file.exists()) {
                return new PlayerData(playerId);
            }

            try {
                org.bukkit.configuration.file.YamlConfiguration config = 
                    org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
                
                Map<String, Object> data = config.getValues(false);
                return PlayerData.deserialize(playerId, data);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Errore nel caricamento dei dati per " + playerId, e);
                return new PlayerData(playerId);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveAsync(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            saveSync(playerData);
        }, executor);
    }

    @Override
    public void saveSync(PlayerData playerData) {
        if (playerData == null) return;

        File file = getFileForPlayer(playerData.getPlayerId());
        
        try {
            // Crea snapshot immutabile dei dati
            Map<String, Object> serialized = playerData.serialize();
            
            org.bukkit.configuration.file.YamlConfiguration config = 
                new org.bukkit.configuration.file.YamlConfiguration();
            
            for (Map.Entry<String, Object> entry : serialized.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }

            // Salva il file
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, 
                "Errore nel salvataggio dei dati per " + playerData.getPlayerId(), e);
        }
    }

    @Override
    public boolean hasData(UUID playerId) {
        return getFileForPlayer(playerId).exists();
    }

    @Override
    public void delete(UUID playerId) {
        File file = getFileForPlayer(playerId);
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("Impossibile eliminare il file: " + file.getName());
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    /**
     * Ottiene il file YAML per un giocatore.
     */
    private File getFileForPlayer(UUID playerId) {
        return new File(dataFolder, playerId.toString() + ".yml");
    }

    /**
     * Carica tutti i file YAML nella cartella (per debug/migrazione).
     */
    public void migrateAll() {
        if (!dataFolder.exists()) return;

        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                String fileName = file.getName();
                String uuidString = fileName.substring(0, fileName.length() - 4);
                UUID playerId = UUID.fromString(uuidString);
                
                // Forza il ricaricamento del file
                org.bukkit.configuration.file.YamlConfiguration config = 
                    org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
                config.save(file);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Errore nella migrazione del file: " + file.getName(), e);
            }
        }
    }
}
