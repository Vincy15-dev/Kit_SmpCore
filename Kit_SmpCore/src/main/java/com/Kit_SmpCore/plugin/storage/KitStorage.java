package com.Kit_SmpCore.plugin.storage;

import com.Kit_SmpCore.plugin.model.PlayerData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaccia per lo storage dei dati dei giocatori.
 */
public interface KitStorage {

    /**
     * Carica i dati di un giocatore in modo async.
     * @param playerId UUID del giocatore
     * @return CompletableFuture con i dati caricati
     */
    CompletableFuture<PlayerData> loadAsync(UUID playerId);

    /**
     * Salva i dati di un giocatore in modo async.
     * @param playerData Dati da salvare
     * @return CompletableFuture che completa quando il salvataggio è finito
     */
    CompletableFuture<Void> saveAsync(PlayerData playerData);

    /**
     * Salva i dati di un giocatore in modo sincrono (per onDisable).
     * @param playerData Dati da salvare
     */
    void saveSync(PlayerData playerData);

    /**
     * Controlla se i dati per un giocatore esistono.
     * @param playerId UUID del giocatore
     * @return true se esistono dati salvati
     */
    boolean hasData(UUID playerId);

    /**
     * Elimina i dati di un giocatore.
     * @param playerId UUID del giocatore
     */
    void delete(UUID playerId);

    /**
     * Chiude lo storage e libera risorse.
     */
    void close();
}
