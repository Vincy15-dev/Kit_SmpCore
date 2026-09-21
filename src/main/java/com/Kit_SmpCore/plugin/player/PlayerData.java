package com.Kit_SmpCore.plugin.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dati per-player: cooldown e claim one-time.
 * Thread-safe: usa ConcurrentHashMap.
 */
public class PlayerData {
    
    private final UUID playerId;
    // Kit ID -> timestamp ultimo claim (millis)
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    // Kit ID claimati one-time
    private final Map<String, Boolean> oneTimeClaims = new ConcurrentHashMap<>();
    // Flag se i dati sono stati modificati (per save dirty)
    private volatile boolean dirty = false;
    // Flag se i dati sono caricati
    private volatile boolean loaded = false;
    
    public PlayerData(@NotNull UUID playerId) {
        this.playerId = playerId;
    }
    
    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }
    
    /**
     * Imposta il cooldown per un kit.
     */
    public void setCooldown(@NotNull String kitId, long timestamp) {
        cooldowns.put(kitId, timestamp);
        dirty = true;
    }
    
    /**
     * Ottieni il timestamp dell'ultimo claim.
     */
    public long getLastClaim(@NotNull String kitId) {
        return cooldowns.getOrDefault(kitId, 0L);
    }
    
    /**
     * Controlla se un kit è in cooldown.
     */
    public boolean isOnCooldown(@NotNull String kitId, long cooldownSeconds) {
        if (cooldownSeconds < 0) {
            // One-time: controlla se già claimato
            return oneTimeClaims.containsKey(kitId);
        }
        
        long lastClaim = getLastClaim(kitId);
        if (lastClaim <= 0) return false;
        
        long elapsed = (System.currentTimeMillis() - lastClaim) / 1000;
        return elapsed < cooldownSeconds;
    }
    
    /**
     * Ottieni il tempo rimanente in secondi.
     */
    public long getRemainingCooldown(@NotNull String kitId, long cooldownSeconds) {
        if (cooldownSeconds < 0) {
            return oneTimeClaims.containsKey(kitId) ? -1 : 0;
        }
        
        long lastClaim = getLastClaim(kitId);
        if (lastClaim <= 0) return 0;
        
        long elapsed = (System.currentTimeMillis() - lastClaim) / 1000;
        long remaining = cooldownSeconds - elapsed;
        return Math.max(0, remaining);
    }
    
    /**
     * Marca un kit come claimato (one-time).
     */
    public void markClaimed(@NotNull String kitId) {
        oneTimeClaims.put(kitId, true);
        dirty = true;
    }
    
    /**
     * Controlla se un kit one-time è già stato claimato.
     */
    public boolean hasClaimedOneTime(@NotNull String kitId) {
        return oneTimeClaims.containsKey(kitId);
    }
    
    /**
     * Resetta il cooldown di un kit.
     */
    public void resetCooldown(@NotNull String kitId) {
        cooldowns.remove(kitId);
        dirty = true;
    }
    
    /**
     * Resetta tutti i cooldown.
     */
    public void resetAll() {
        cooldowns.clear();
        oneTimeClaims.clear();
        dirty = true;
    }
    
    /**
     * Ottieni una snapshot immutabile dei cooldown per save async.
     */
    @NotNull
    public Map<String, Long> getCooldownSnapshot() {
        return new HashMap<>(cooldowns);
    }
    
    @NotNull
    public Map<String, Boolean> getOneTimeSnapshot() {
        return new HashMap<>(oneTimeClaims);
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    public void markClean() {
        dirty = false;
    }
    
    public boolean isLoaded() {
        return loaded;
    }
    
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
