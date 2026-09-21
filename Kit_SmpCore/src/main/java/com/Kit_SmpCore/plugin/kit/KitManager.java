package com.Kit_SmpCore.plugin.kit;

import com.Kit_SmpCore.plugin.MainClass;
import com.Kit_SmpCore.plugin.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager principale per i kit e i dati dei giocatori.
 */
public class KitManager {

    private final MainClass plugin;
    private final Map<String, Kit> kits;
    private final Map<UUID, PlayerData> playerDataCache;
    private final KitLoader kitLoader;

    public KitManager(MainClass plugin) {
        this.plugin = plugin;
        this.kits = new ConcurrentHashMap<>();
        this.playerDataCache = new ConcurrentHashMap<>();
        this.kitLoader = new KitLoader(plugin);
    }

    /**
     * Carica tutti i kit da configurazione.
     */
    public void loadKits() {
        kits.clear();
        Map<String, Kit> loaded = kitLoader.loadKits();
        kits.putAll(loaded);
        
        // Registra permessi dinamici
        registerPermissions();
    }

    /**
     * Registra i permessi per ogni kit.
     */
    private void registerPermissions() {
        for (Kit kit : kits.values()) {
            String permName = kit.getPermission();
            PermissionDefault def = kit.isDefaultPermission() ? 
                PermissionDefault.TRUE : PermissionDefault.FALSE;
            
            try {
                Permission perm = new Permission(permName, 
                    "Accesso al kit " + kit.getDisplayName(), def);
                Bukkit.getPluginManager().addPermission(perm);
            } catch (Exception e) {
                // Permesso già registrato
            }
        }
    }

    /**
     * Annulla la registrazione dei permessi (per reload).
     */
    public void unregisterPermissions() {
        for (Kit kit : kits.values()) {
            try {
                Permission perm = Bukkit.getPluginManager().getPermission(kit.getPermission());
                if (perm != null) {
                    Bukkit.getPluginManager().removePermission(perm);
                }
            } catch (Exception e) {
                // Ignora
            }
        }
    }

    /**
     * Ottiene un kit per ID.
     */
    public Kit getKit(String id) {
        return kits.get(id);
    }

    /**
     * Ottiene tutti i kit.
     */
    public Collection<Kit> getAllKits() {
        return Collections.unmodifiableCollection(kits.values());
    }

    /**
     * Ottiene i dati di un giocatore dalla cache.
     */
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataCache.get(playerId);
    }

    /**
     * Controlla se i dati di un giocatore sono caricati.
     */
    public boolean isDataLoaded(UUID playerId) {
        return playerDataCache.containsKey(playerId);
    }

    /**
     * Mette in cache i dati di un giocatore.
     */
    public void cachePlayerData(PlayerData data) {
        if (data != null) {
            playerDataCache.put(data.getPlayerId(), data);
        }
    }

    /**
     * Rimuove i dati di un giocatore dalla cache.
     */
    public void removePlayerData(UUID playerId) {
        playerDataCache.remove(playerId);
    }

    /**
     * Controlla se un giocatore ha il permesso per un kit.
     */
    public boolean hasPermission(Player player, Kit kit) {
        if (player == null || kit == null) return false;
        return player.hasPermission(kit.getPermission());
    }

    /**
     * Conta quanti kit sono pronti per un giocatore.
     */
    public int countReadyKits(Player player) {
        if (player == null) return 0;
        PlayerData data = playerDataCache.get(player.getUniqueId());
        if (data == null) return 0;

        Map<String, Long> cooldowns = new HashMap<>();
        for (Kit kit : kits.values()) {
            cooldowns.put(kit.getId(), kit.getCooldownSeconds());
        }
        return data.countReadyKits(cooldowns);
    }

    /**
     * Chiude il manager e libera risorse.
     */
    public void shutdown() {
        unregisterPermissions();
        playerDataCache.clear();
        kits.clear();
    }
}
