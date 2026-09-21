package com.Kit_SmpCore.plugin;

import com.Kit_SmpCore.plugin.kit.KitManager;
import com.Kit_SmpCore.plugin.storage.YamlKitStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main class del plugin Kit_SmpCore.
 */
public class MainClass extends JavaPlugin {

    private static MainClass instance;
    private KitManager kitManager;
    private YamlKitStorage storage;

    @Override
    public void onEnable() {
        instance = this;

        // Salva le risorse default se non esistono
        saveDefaultConfig();
        saveResource("config.yml", false);
        saveResource("kits.yml", false);
        saveResource("messages.yml", false);

        // Ricarica la config per leggere i file salvati
        reloadConfig();

        getLogger().info("Inizializzazione di Kit_SmpCore...");

        // Inizializza lo storage
        String dataFolder = getConfig().getString("storage.player-data-folder", "playerdata");
        storage = new YamlKitStorage(this, dataFolder);

        // Inizializza il KitManager e carica i kit
        kitManager = new KitManager(this);
        kitManager.loadKits();

        // TODO: Registrare listeners, commands, tab completer
        // registerListeners();
        // registerCommands();

        getLogger().info("Kit_SmpCore abilitato con successo!");
        getLogger().info("Kit caricati: " + kitManager.getAllKits().size());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabilitazione di Kit_SmpCore...");

        // Salva tutti i dati dei giocatori online
        if (getConfig().getBoolean("storage.save-on-disable", true)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var data = kitManager.getPlayerData(player.getUniqueId());
                if (data != null) {
                    storage.saveSync(data);
                }
            }
            getLogger().info("Dati giocatore salvati.");
        }

        // Chiudi lo storage
        if (storage != null) {
            storage.close();
        }

        // Shutdown del kit manager
        if (kitManager != null) {
            kitManager.shutdown();
        }

        getLogger().info("Kit_SmpCore disabilitato correttamente.");
    }

    /**
     * Ricarica le configurazioni dal disco.
     */
    public void reloadConfigs() {
        reloadConfig();
        if (kitManager != null) {
            kitManager.loadKits();
        }
    }

    public static MainClass getInstance() {
        return instance;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public YamlKitStorage getStorage() {
        return storage;
    }
}
