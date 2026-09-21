package com.Kit_SmpCore.plugin.kit.storage;

import com.Kit_SmpCore.plugin.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Interfaccia per lo storage dei dati dei player.
 */
public interface KitStorage {
    
    /**
     * Carica i dati di un player (può essere async).
     */
    @Nullable
    PlayerData load(@NotNull UUID playerId);
    
    /**
     * Salva i dati di un player (può essere async).
     */
    void save(@NotNull UUID playerId, @NotNull PlayerData data);
    
    /**
     * Salva una snapshot immutabile (per save async).
     */
    void saveSnapshot(@NotNull UUID playerId, 
                      @NotNull Map<String, Long> cooldowns,
                      @NotNull Map<String, Boolean> oneTimeClaims);
    
    /**
     * Elimina i dati di un player.
     */
    void delete(@NotNull UUID playerId);
}
