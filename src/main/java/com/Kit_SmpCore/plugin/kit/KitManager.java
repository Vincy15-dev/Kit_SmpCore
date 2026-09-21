package com.Kit_SmpCore.plugin.kit;

import com.Kit_SmpCore.plugin.MainClass;
import com.Kit_SmpCore.plugin.kit.storage.KitStorage;
import com.Kit_SmpCore.plugin.kit.storage.YamlKitStorage;
import com.Kit_SmpCore.plugin.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager principale per i kit: caricamento, gestione permessi, cache player.
 */
public class KitManager {
    
    private final MainClass plugin;
    private final Map<String, Kit> kits = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerData> playerCache = new ConcurrentHashMap<>();
    private KitStorage storage;
    private boolean allowUnsafeEnchants = false;
    
    public KitManager(@NotNull MainClass plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Carica tutti i kit dalla configurazione.
     */
    public void loadKits() {
        kits.clear();
        
        File kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(kitsFile);
        
        // Carica presets
        KitLoader.loadPresets(config);
        
        // Carica kit
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection == null) {
            plugin.getLogger().warning("[KitManager] Nessun kit trovato in kits.yml!");
            return;
        }
        
        boolean allowUnsafe = plugin.getConfig().getBoolean("options.allow-unsafe-enchants", false);
        this.allowUnsafeEnchants = allowUnsafe;
        
        for (String kitId : kitsSection.getKeys(false)) {
            try {
                Kit kit = KitLoader.loadKit(kitId, kitsSection.getConfigurationSection(kitId), allowUnsafe);
                if (kit != null) {
                    kits.put(kitId, kit);
                    registerPermission(kit);
                    plugin.getLogger().info("[KitManager] Kit caricato: " + kitId);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("[KitManager] Errore nel caricamento del kit " + kitId + ": " + e.getMessage());
            }
        }
        
        // Inizializza storage
        this.storage = new YamlKitStorage(plugin.getDataFolder());
        
        plugin.getLogger().info("[KitManager] " + kits.size() + " kit caricati.");
    }
    
    /**
     * Registra la permission per un kit.
     */
    private void registerPermission(@NotNull Kit kit) {
        String permName = kit.getPermission();
        PermissionDefault def = kit.isDefaultPermission() ? PermissionDefault.TRUE : PermissionDefault.FALSE;
        
        try {
            Permission perm = new Permission(permName, "Permesso per il kit " + kit.getId(), def);
            Bukkit.getPluginManager().addPermission(perm);
        } catch (IllegalArgumentException e) {
            // Permission già registrata
        }
    }
    
    /**
     * Annulla la registrazione di una permission.
     */
    public void unregisterPermission(@NotNull String permName) {
        try {
            Permission perm = Bukkit.getPluginManager().getPermission(permName);
            if (perm != null) {
                Bukkit.getPluginManager().removePermission(perm);
            }
        } catch (Exception ignored) {}
    }
    
    /**
     * Ottieni un kit per ID.
     */
    @Nullable
    public Kit getKit(@NotNull String id) {
        return kits.get(id);
    }
    
    /**
     * Ottieni tutti i kit.
     */
    @NotNull
    public Collection<Kit> getAllKits() {
        return Collections.unmodifiableCollection(kits.values());
    }
    
    /**
     * Ottieni i dati di un player (dalla cache).
     */
    @Nullable
    public PlayerData getPlayerData(@NotNull UUID playerId) {
        return playerCache.get(playerId);
    }
    
    /**
     * Carica i dati di un player async.
     */
    public void loadPlayerDataAsync(@NotNull UUID playerId, @NotNull Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData data = storage.load(playerId);
            playerCache.put(playerId, data);
            Bukkit.getScheduler().runTask(plugin, callback);
        });
    }
    
    /**
     * Salva i dati di un player async.
     */
    public void savePlayerDataAsync(@NotNull UUID playerId) {
        PlayerData data = playerCache.get(playerId);
        if (data != null && data.isDirty()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                storage.save(playerId, data);
                data.markClean();
            });
        }
    }
    
    /**
     * Rimuovi un player dalla cache.
     */
    public void unloadPlayer(@NotNull UUID playerId) {
        PlayerData data = playerCache.remove(playerId);
        if (data != null) {
            storage.save(playerId, data);
        }
    }
    
    /**
     * Controlla se un player ha il permesso per un kit.
     */
    public boolean hasPermission(@NotNull Player player, @NotNull Kit kit) {
        return player.hasPermission(kit.getPermission());
    }
    
    /**
     * Calcola la riduzione cooldown per un player.
     */
    public int getCooldownReduction(@NotNull Player player) {
        int maxReduction = 0;
        
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection reductionsSection = config.getConfigurationSection("cooldown-reductions");
        if (reductionsSection == null) return 0;
        
        for (String perm : reductionsSection.getKeys(false)) {
            if (player.hasPermission(perm)) {
                int reduction = reductionsSection.getInt(perm, 0);
                maxReduction = Math.max(maxReduction, reduction);
            }
        }
        
        // Cap al 90%
        return Math.min(maxReduction, 90);
    }
    
    /**
     * Controlla se un player ha il bypass cooldown.
     */
    public boolean hasCooldownBypass(@NotNull Player player) {
        return player.hasPermission("kit.cooldown.bypass");
    }
    
    /**
     * Ricarica tutto (comando /kit reload).
     */
    public void reload() {
        // Annulla permessi vecchi
        for (Kit kit : kits.values()) {
            unregisterPermission(kit.getPermission());
        }
        
        // Ricarica
        plugin.reloadConfig();
        loadKits();
    }
}
