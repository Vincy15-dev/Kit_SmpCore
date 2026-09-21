package com.Kit_SmpCore.plugin.model;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dati per-player per i cooldown e claim dei kit.
 * Thread-safe per accesso concorrente.
 */
public class PlayerData {

    private final UUID playerId;
    
    // Mappa kitId -> timestamp ultimo claim (millis)
    private final Map<String, Long> cooldowns;
    
    // Set di kit one-time già reclamati
    private final Set<String> claimedOneTime;
    
    // Timestamp di primo join per first-join-kit
    private long firstJoinTime;
    
    // Flag se il first-join-kit è stato dato
    private boolean firstJoinKitGiven;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.cooldowns = new ConcurrentHashMap<>();
        this.claimedOneTime = ConcurrentHashMap.newKeySet();
        this.firstJoinTime = System.currentTimeMillis();
        this.firstJoinKitGiven = false;
    }

    /**
     * Ottiene l'ID del giocatore.
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Controlla se un kit è in cooldown.
     * @param kitId ID del kit
     * @param cooldownSeconds Cooldown in secondi (-1 = one-time)
     * @return true se in cooldown o già reclamato (one-time)
     */
    public boolean isOnCooldown(String kitId, long cooldownSeconds) {
        if (!cooldowns.containsKey(kitId)) {
            return false;
        }

        long lastClaim = cooldowns.get(kitId);
        
        if (cooldownSeconds < 0) {
            // One-time kit: se esiste timestamp, è già reclamato
            return true;
        }

        long cooldownMillis = cooldownSeconds * 1000L;
        long now = System.currentTimeMillis();
        
        return (now - lastClaim) < cooldownMillis;
    }

    /**
     * Ottiene il tempo rimanente al cooldown in millisecondi.
     * @param kitId ID del kit
     * @param cooldownSeconds Cooldown in secondi
     * @return Tempo rimanente in millis (0 se scaduto)
     */
    public long getRemainingCooldown(String kitId, long cooldownSeconds) {
        if (!cooldowns.containsKey(kitId)) {
            return 0;
        }

        long lastClaim = cooldowns.get(kitId);
        
        if (cooldownSeconds < 0) {
            // One-time: sempre "in cooldown" se già reclamato
            return Long.MAX_VALUE;
        }

        long cooldownMillis = cooldownSeconds * 1000L;
        long elapsed = System.currentTimeMillis() - lastClaim;
        long remaining = cooldownMillis - elapsed;
        
        return Math.max(0, remaining);
    }

    /**
     * Imposta il cooldown per un kit.
     * @param kitId ID del kit
     */
    public void setCooldown(String kitId) {
        cooldowns.put(kitId, System.currentTimeMillis());
    }

    /**
     * Segna un kit one-time come reclamato.
     * @param kitId ID del kit
     */
    public void markAsClaimed(String kitId) {
        claimedOneTime.add(kitId);
        cooldowns.put(kitId, System.currentTimeMillis());
    }

    /**
     * Controlla se un kit one-time è già stato reclamato.
     * @param kitId ID del kit
     * @return true se già reclamato
     */
    public boolean isClaimedOneTime(String kitId) {
        return claimedOneTime.contains(kitId);
    }

    /**
     * Resetta il cooldown/claim di un kit specifico.
     * @param kitId ID del kit o "all" per tutti
     */
    public void resetCooldown(String kitId) {
        if ("all".equalsIgnoreCase(kitId)) {
            cooldowns.clear();
            claimedOneTime.clear();
        } else {
            cooldowns.remove(kitId);
            claimedOneTime.remove(kitId);
        }
    }

    /**
     * Ottiene tutti i cooldown come mappa immutabile.
     */
    public Map<String, Long> getCooldowns() {
        return Map.copyOf(cooldowns);
    }

    /**
     * Ottiene tutti i kit one-time claimati come set immutabile.
     */
    public Set<String> getClaimedOneTime() {
        return Set.copyOf(claimedOneTime);
    }

    /**
     * Controlla se il first-join-kit è stato dato.
     */
    public boolean isFirstJoinKitGiven() {
        return firstJoinKitGiven;
    }

    /**
     * Segna che il first-join-kit è stato dato.
     */
    public void setFirstJoinKitGiven(boolean given) {
        this.firstJoinKitGiven = given;
    }

    /**
     * Ottiene il timestamp del primo join.
     */
    public long getFirstJoinTime() {
        return firstJoinTime;
    }

    /**
     * Conta quanti kit sono pronti (non in cooldown).
     * @param allKits Lista di tutti i kit ID con i loro cooldown
     * @return Numero di kit pronti
     */
    public int countReadyKits(Map<String, Long> allKits) {
        int count = 0;
        for (Map.Entry<String, Long> entry : allKits.entrySet()) {
            String kitId = entry.getKey();
            long cooldown = entry.getValue();
            
            if (!isOnCooldown(kitId, cooldown)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Serializza i dati per il salvataggio YAML.
     * Restituisce una mappa immutabile sicura per I/O async.
     */
    public Map<String, Object> serialize() {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("firstJoinTime", firstJoinTime);
        data.put("firstJoinKitGiven", firstJoinKitGiven);
        data.put("cooldowns", new ConcurrentHashMap<>(cooldowns));
        data.put("claimedOneTime", Set.copyOf(claimedOneTime));
        return data;
    }

    /**
     * Deserializza i dati da YAML.
     */
    public static PlayerData deserialize(UUID playerId, Map<String, Object> data) {
        if (data == null) {
            return new PlayerData(playerId);
        }

        PlayerData playerData = new PlayerData(playerId);
        
        if (data.containsKey("firstJoinTime")) {
            Object timeObj = data.get("firstJoinTime");
            if (timeObj instanceof Number) {
                playerData.firstJoinTime = ((Number) timeObj).longValue();
            }
        }
        
        if (data.containsKey("firstJoinKitGiven")) {
            Object givenObj = data.get("firstJoinKitGiven");
            if (givenObj instanceof Boolean) {
                playerData.firstJoinKitGiven = (Boolean) givenObj;
            }
        }

        if (data.containsKey("cooldowns")) {
            Object cooldownsObj = data.get("cooldowns");
            if (cooldownsObj instanceof Map) {
                Map<?, ?> cooldownsMap = (Map<?, ?>) cooldownsObj;
                for (Map.Entry<?, ?> entry : cooldownsMap.entrySet()) {
                    if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                        playerData.cooldowns.put((String) entry.getKey(), ((Number) entry.getValue()).longValue());
                    }
                }
            }
        }

        if (data.containsKey("claimedOneTime")) {
            Object claimedObj = data.get("claimedOneTime");
            if (claimedObj instanceof Iterable) {
                for (Object item : (Iterable<?>) claimedObj) {
                    if (item instanceof String) {
                        playerData.claimedOneTime.add((String) item);
                    }
                }
            }
        }

        return playerData;
    }
}
