package com.Kit_SmpCore.plugin.kit;

import com.Kit_SmpCore.plugin.util.RegistryUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;

/**
 * Caricamento e validazione dei kit da configurazione.
 */
public class KitLoader {

    private final Plugin plugin;
    private final Map<String, Map<String, Integer>> presets;

    public KitLoader(Plugin plugin) {
        this.plugin = plugin;
        this.presets = new HashMap<>();
    }

    /**
     * Carica tutti i kit da kits.yml.
     * @return Mappa di kit validi per ID
     */
    public Map<String, Kit> loadKits() {
        plugin.reloadConfig();
        ConfigurationSection kitsSection = plugin.getConfig().getConfigurationSection("kits");
        
        if (kitsSection == null) {
            plugin.getLogger().warning("Nessun kit trovato in kits.yml!");
            return new HashMap<>();
        }

        // Prima carica i presets
        loadPresets();

        Map<String, Kit> kits = new HashMap<>();
        for (String key : kitsSection.getKeys(false)) {
            ConfigurationSection section = kitsSection.getConfigurationSection(key);
            if (section == null) continue;

            try {
                Kit kit = Kit.fromConfig(key, section);
                if (kit != null) {
                    // Valida il kit
                    List<String> warnings = validateKit(kit);
                    if (!warnings.isEmpty()) {
                        for (String warning : warnings) {
                            plugin.getLogger().warning(warning);
                        }
                    }
                    kits.put(key, kit);
                    plugin.getLogger().info("Kit caricato: " + kit.getDisplayName());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Errore nel caricamento del kit: " + key, e);
            }
        }

        return kits;
    }

    /**
     * Carica i presets di enchant da configurazione.
     */
    private void loadPresets() {
        ConfigurationSection presetsSection = plugin.getConfig().getConfigurationSection("presets");
        if (presetsSection == null) return;

        for (String key : presetsSection.getKeys(false)) {
            ConfigurationSection presetSection = presetsSection.getConfigurationSection(key);
            if (presetSection == null) continue;

            Map<String, Integer> enchants = new HashMap<>();
            for (String enchantKey : presetSection.getKeys(false)) {
                int level = presetSection.getInt(enchantKey);
                enchants.put(enchantKey.toLowerCase(), level);
            }
            presets.put(key.toUpperCase(), enchants);
        }
    }

    /**
     * Ottiene un preset di enchant per nome.
     */
    public Map<String, Integer> getPreset(String name) {
        if (name == null || name.isEmpty()) return new HashMap<>();
        return new HashMap<>(presets.getOrDefault(name.toUpperCase(), new HashMap<>()));
    }

    /**
     * Valida un kit e restituisce warning per problemi non fatali.
     */
    private List<String> validateKit(Kit kit) {
        List<String> warnings = new ArrayList<>();

        if (kit.getId() == null || kit.getId().isEmpty()) {
            warnings.add("Kit ha ID nullo o vuoto!");
            return warnings;
        }

        // Controlla che gli enchant nei preset esistano
        for (Map.Entry<String, Integer> entry : presets.entrySet()) {
            for (String enchantKey : entry.getValue().keySet()) {
                Enchantment enchant = RegistryUtil.getEnchantment(enchantKey);
                if (enchant == null) {
                    warnings.add("Preset '" + entry.getKey() + "' ha enchant invalido: " + enchantKey);
                }
            }
        }

        // Controlla conflitti Density/Breach su mace
        // Questa logica sarà applicata durante la build degli item

        return warnings;
    }

    /**
     * Risolve gli enchant per un KitItem usando preset + enchants extra.
     * Gestisce conflitti e livelli massimi.
     */
    public Map<Enchantment, Integer> resolveEnchants(KitItem item, boolean allowUnsafe) {
        Map<Enchantment, Integer> result = new LinkedHashMap<>();

        // Prima applica il preset
        if (item.getPreset() != null && !item.getPreset().isEmpty()) {
            Map<String, Integer> presetEnchants = getPreset(item.getPreset());
            for (Map.Entry<String, Integer> entry : presetEnchants.entrySet()) {
                Enchantment enchant = RegistryUtil.getEnchantment(entry.getKey());
                if (enchant != null) {
                    result.put(enchant, entry.getValue());
                }
            }
        }

        // Poi applica enchants extra
        for (Map.Entry<String, Integer> entry : item.getEnchants().entrySet()) {
            Enchantment enchant = RegistryUtil.getEnchantment(entry.getKey());
            if (enchant == null) {
                plugin.getLogger().warning("Enchant invalido: " + entry.getKey());
                continue;
            }

            // Controlla conflitti
            boolean conflicts = false;
            for (Enchantment existing : result.keySet()) {
                if (RegistryUtil.conflictsWith(existing, enchant)) {
                    plugin.getLogger().warning("Conflitto enchant: " + existing.getKey() + " vs " + enchant.getKey());
                    conflicts = true;
                    break;
                }
            }

            if (!conflicts) {
                int level = entry.getValue();
                if (!allowUnsafe) {
                    level = Math.min(level, enchant.getMaxLevel());
                }
                result.put(enchant, level);
            }
        }

        return result;
    }
}
