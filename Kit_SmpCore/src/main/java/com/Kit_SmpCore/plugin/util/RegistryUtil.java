package com.Kit_SmpCore.plugin.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.trim.TrimMaterial;
import org.bukkit.trim.TrimPattern;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

/**
 * Utility per il lookup sicuro di oggetti dal Registry (Spigot 1.21.x).
 * Evita l'uso di valueOf()/values() che possono lanciare eccezioni.
 */
public class RegistryUtil {

    private static NamespacedKey getKey(Plugin plugin, String key) {
        if (key == null || key.isEmpty()) return null;
        // Se non ha namespace, usa "minecraft"
        if (!key.contains(":")) {
            key = "minecraft:" + key.toLowerCase();
        }
        return NamespacedKey.fromString(key);
    }

    /**
     * Lookup di un Enchantment dal registry.
     * @param key Chiave dell'enchant (es. "sharpness", "minecraft:protection")
     * @return Enchantment o null se non trovato
     */
    public static Enchantment getEnchantment(String key) {
        if (key == null) return null;
        NamespacedKey namespacedKey = getKey(null, key);
        if (namespacedKey == null) return null;
        
        try {
            return Registry.ENCHANTMENT.get(namespacedKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lookup di una PotionType dal registry.
     * @param key Chiave della pozione (es. "strong_swiftness", "minecraft:healing")
     * @return PotionType o null se non trovato
     */
    public static PotionType getPotionType(String key) {
        if (key == null) return null;
        NamespacedKey namespacedKey = getKey(null, key);
        if (namespacedKey == null) return null;
        
        try {
            return Registry.POTION_TYPE.get(namespacedKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lookup di un TrimPattern dal registry.
     * @param key Chiave del pattern (es. "silence", "minecraft:ward")
     * @return TrimPattern o null se non trovato
     */
    public static TrimPattern getTrimPattern(String key) {
        if (key == null) return null;
        NamespacedKey namespacedKey = getKey(null, key);
        if (namespacedKey == null) return null;
        
        try {
            return Registry.TRIM_PATTERN.get(namespacedKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lookup di un TrimMaterial dal registry.
     * @param key Chiave del materiale (es. "amethyst", "minecraft:gold")
     * @return TrimMaterial o null se non trovato
     */
    public static TrimMaterial getTrimMaterial(String key) {
        if (key == null) return null;
        NamespacedKey namespacedKey = getKey(null, key);
        if (namespacedKey == null) return null;
        
        try {
            return Registry.TRIM_MATERIAL.get(namespacedKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lookup di un Sound dal registry.
     * @param key Chiave del suono (es. "entity_player_levelup", "minecraft:block.note_block.chime")
     * @return Sound o null se non trovato
     */
    public static Sound getSound(String key) {
        if (key == null) return null;
        NamespacedKey namespacedKey = getKey(null, key);
        if (namespacedKey == null) return null;
        
        try {
            Optional<Sound> optional = Registry.SOUNDS.getOptional(namespacedKey);
            return optional.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Suona un suono a un giocatore usando la chiave registry.
     * @param player Giocatore
     * @param soundKey Chiave del suono
     * @param volume Volume (0.0-1.0)
     * @param pitch Pitch (0.5-2.0)
     */
    public static void playSound(Player player, String soundKey, float volume, float pitch) {
        if (player == null || soundKey == null) return;
        
        Sound sound = getSound(soundKey);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    /**
     * Applica una PotionType a un PotionMeta.
     * @param meta PotionMeta da modificare
     * @param potionKey Chiave della pozione
     * @return true se applicato con successo
     */
    public static boolean applyPotionType(PotionMeta meta, String potionKey) {
        if (meta == null || potionKey == null) return false;
        
        PotionType type = getPotionType(potionKey);
        if (type != null) {
            meta.setBasePotionType(type);
            return true;
        }
        return false;
    }

    /**
     * Controlla se due enchantments sono in conflitto.
     * @param enchant1 Primo enchant
     * @param enchant2 Secondo enchant
     * @return true se in conflitto
     */
    public static boolean conflictsWith(Enchantment enchant1, Enchantment enchant2) {
        if (enchant1 == null || enchant2 == null) return false;
        try {
            return enchant1.conflictsWith(enchant2);
        } catch (Exception e) {
            return false;
        }
    }
}
