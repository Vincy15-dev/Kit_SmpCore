package com.Kit_SmpCore.plugin.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Utility per operazioni sui registry di Minecraft (1.21.x).
 * Usa solo API registry ufficiali, mai valueOf() o values().
 */
public class RegistryUtil {
    
    private static final Logger LOGGER = Bukkit.getLogger();
    
    /**
     * Ottieni un Enchantment dal registry tramite chiave NamespacedKey.
     * @param key Chiave dell'enchantment (es. "minecraft:protection")
     * @return Optional contenente l'Enchantment se trovato
     */
    @NotNull
    public static Optional<Enchantment> getEnchantment(@NotNull String key) {
        try {
            NamespacedKey namespacedKey = new NamespacedKey(key.contains(":") ? key.split(":")[0] : "minecraft", 
                    key.contains(":") ? key.split(":")[1] : key);
            return Optional.ofNullable(Registry.ENCHANTMENT.get(namespacedKey));
        } catch (Exception e) {
            LOGGER.warning("Enchantment non valido: " + key + " - " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Ottieni un PotionType dal registry tramite chiave.
     * @param key Chiave del potion type (es. "strong_swiftness")
     * @return Optional contenente il PotionType se trovato
     */
    @NotNull
    public static Optional<PotionType> getPotionType(@NotNull String key) {
        try {
            NamespacedKey namespacedKey = new NamespacedKey(key.contains(":") ? key.split(":")[0] : "minecraft",
                    key.contains(":") ? key.split(":")[1] : key);
            return Optional.ofNullable(Registry.POTION.get(namespacedKey));
        } catch (Exception e) {
            LOGGER.warning("PotionType non valido: " + key + " - " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Ottieni un Sound dal registry tramite chiave.
     * @param key Chiave del suono (es. "entity.player.levelup")
     * @return Optional contenente il Sound se trovato
     */
    @NotNull
    public static Optional<Sound> getSound(@NotNull String key) {
        try {
            NamespacedKey namespacedKey = new NamespacedKey(key.contains(":") ? key.split(":")[0] : "minecraft",
                    key.contains(":") ? key.split(":")[1] : key);
            // In Spigot 1.21.x, i suoni si ottengono tramite Registry.SOUNDS
            return Optional.ofNullable(Registry.SOUNDS.get(namespacedKey));
        } catch (Exception e) {
            LOGGER.warning("Sound non valido: " + key + " - " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Ottieni un TrimPattern dal registry.
     * @param key Chiave del pattern (es. "minecraft:silence")
     * @return Optional contenente il TrimPattern se trovato
     */
    @NotNull
    public static Optional<TrimPattern> getTrimPattern(@NotNull String key) {
        try {
            NamespacedKey namespacedKey = new NamespacedKey(key.contains(":") ? key.split(":")[0] : "minecraft",
                    key.contains(":") ? key.split(":")[1] : key);
            return Optional.ofNullable(Registry.TRIM_PATTERN.get(namespacedKey));
        } catch (Exception e) {
            LOGGER.warning("TrimPattern non valido: " + key + " - " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Ottieni un TrimMaterial dal registry.
     * @param key Chiave del materiale (es. "minecraft:amethyst")
     * @return Optional contenente il TrimMaterial se trovato
     */
    @NotNull
    public static Optional<TrimMaterial> getTrimMaterial(@NotNull String key) {
        try {
            NamespacedKey namespacedKey = new NamespacedKey(key.contains(":") ? key.split(":")[0] : "minecraft",
                    key.contains(":") ? key.split(":")[1] : key);
            return Optional.ofNullable(Registry.TRIM_MATERIAL.get(namespacedKey));
        } catch (Exception e) {
            LOGGER.warning("TrimMaterial non valido: " + key + " - " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Applica un enchantment a un ItemStack, gestendo conflitti e livelli massimi.
     * @param item L'ItemStack da enchantare
     * @param enchantment L'Enchantment da applicare
     * @param level Il livello dell'enchantment
     * @param allowUnsafe Se permettere livelli oltre il massimo
     * @return true se applicato con successo
     */
    public static boolean applyEnchantment(@NotNull ItemStack item, @NotNull Enchantment enchantment, 
                                           int level, boolean allowUnsafe) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        // Controlla conflitti con enchantments esistenti
        for (Enchantment existing : meta.getEnchants().keySet()) {
            if (enchantment.conflictsWith(existing)) {
                LOGGER.warning("Enchantment " + enchantment.getKey() + " confligge con " + existing.getKey() 
                        + ", saltato.");
                return false;
            }
        }
        
        // Clampa al livello massimo se non unsafe
        int actualLevel = allowUnsafe ? level : Math.min(level, enchantment.getMaxLevel());
        
        meta.addEnchant(enchantment, actualLevel, true);
        item.setItemMeta(meta);
        return true;
    }
    
    /**
     * Applica armor trim a un pezzo di armatura.
     * @param item L'ItemStack (deve essere armor)
     * @param pattern Il TrimPattern
     * @param material Il TrimMaterial
     * @return true se applicato con successo
     */
    public static boolean applyArmorTrim(@NotNull ItemStack item, @NotNull TrimPattern pattern, 
                                         @NotNull TrimMaterial material) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof ArmorMeta armorMeta)) return false;
        
        armorMeta.setTrim(new org.bukkit.inventory.meta.trim.ArmorTrim(material, pattern));
        item.setItemMeta(armorMeta);
        return true;
    }
}
