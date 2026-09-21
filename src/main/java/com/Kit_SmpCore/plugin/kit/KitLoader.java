package com.Kit_SmpCore.plugin.kit;

import com.Kit_SmpCore.plugin.util.RegistryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

/**
 * Carica e valida i kit dalla configurazione kits.yml.
 */
public class KitLoader {
    
    private static final Set<Material> ARMOR_MATERIALS = EnumSet.of(
        Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
        Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
        Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
        Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
        Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
        Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
        Material.TURTLE_HELMET
    );
    
    /**
     * Carica tutti i presets dalla configurazione.
     */
    public static void loadPresets(@NotNull FileConfiguration config) {
        ConfigurationSection presetsSection = config.getConfigurationSection("presets");
        if (presetsSection == null) return;
        
        for (String presetName : presetsSection.getKeys(false)) {
            ConfigurationSection presetSection = presetsSection.getConfigurationSection(presetName);
            if (presetSection == null) continue;
            
            Map<String, Integer> enchants = new HashMap<>();
            for (String key : presetSection.getKeys(false)) {
                int level = presetSection.getInt(key);
                enchants.put(key, level);
            }
            
            Kit.registerPreset(presetName, enchants);
            Bukkit.getLogger().info("[KitLoader] Preset caricato: " + presetName + " (" + enchants.size() + " enchant)");
        }
    }
    
    /**
     * Carica un singolo kit dalla configurazione.
     */
    @Nullable
    public static Kit loadKit(@NotNull String id, @NotNull ConfigurationSection section, boolean allowUnsafe) {
        Kit kit = new Kit(id);
        
        // Dati base
        kit.setDisplayName(section.getString("display-name", id));
        kit.setTierLabel(section.getString("tier-label", ""));
        kit.setDescription(section.getStringList("description"));
        kit.setColorFrom(section.getString("color-from", "#FFFFFF"));
        kit.setColorTo(section.getString("color-to", "#FFFFFF"));
        kit.setPermission(section.getString("permission", "kit." + id));
        kit.setDefaultPermission(section.getBoolean("default-permission", false));
        kit.setCooldownSeconds(section.getLong("cooldown", 86400));
        kit.setAnnounceClaim(section.getBoolean("announce-claim", false));
        kit.setLockedHint(section.getString("locked-hint", "Non hai accesso a questo kit."));
        kit.setDeliveryType(section.getString("delivery", "direct"));
        
        // Icona
        String iconMat = section.getString("icon", "BARRIER");
        Material iconMaterial = Material.matchMaterial(iconMat);
        if (iconMaterial == null) {
            Bukkit.getLogger().warning("[KitLoader] Icona non valida per kit " + id + ": " + iconMat + ", uso BARRIER");
            iconMaterial = Material.BARRIER;
        }
        kit.setIconMaterial(iconMaterial);
        
        // Equipment
        ConfigurationSection equipSection = section.getConfigurationSection("equipment");
        if (equipSection != null) {
            kit.setHelmet(loadEquipmentItem(equipSection, "helmet", allowUnsafe));
            kit.setChestplate(loadEquipmentItem(equipSection, "chestplate", allowUnsafe));
            kit.setLeggings(loadEquipmentItem(equipSection, "leggings", allowUnsafe));
            kit.setBoots(loadEquipmentItem(equipSection, "boots", allowUnsafe));
            kit.setOffhand(loadEquipmentItem(equipSection, "offhand", allowUnsafe));
        }
        
        // Items
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                KitItem item = loadKitItem(itemsSection.getConfigurationSection(key), allowUnsafe);
                if (item != null) {
                    kit.addItem(item);
                }
            }
        } else if (section.isList("items")) {
            List<Map<?, ?>> itemsList = section.getMapList("items");
            for (Map<?, ?> itemData : itemsList) {
                KitItem item = loadKitItemFromMap(itemData, allowUnsafe);
                if (item != null) {
                    kit.addItem(item);
                }
            }
        }
        
        // Containers (shulker)
        ConfigurationSection containersSection = section.getConfigurationSection("containers");
        if (containersSection != null) {
            for (String key : containersSection.getKeys(false)) {
                KitContainer container = loadContainer(containersSection.getConfigurationSection(key), allowUnsafe);
                if (container != null) {
                    kit.addContainer(container);
                }
            }
        } else if (section.isList("containers")) {
            List<Map<?, ?>> containersList = section.getMapList("containers");
            for (Map<?, ?> containerData : containersList) {
                KitContainer container = loadContainerFromMap(containerData, allowUnsafe);
                if (container != null) {
                    kit.addContainer(container);
                }
            }
        }
        
        // Commands
        kit.getCommands().addAll(section.getStringList("commands"));
        
        return kit;
    }
    
    @Nullable
    private static KitItem loadEquipmentItem(@NotNull ConfigurationSection section, @NotNull String slot, boolean allowUnsafe) {
        ConfigurationSection itemSection = section.getConfigurationSection(slot);
        if (itemSection == null) return null;
        
        String matName = itemSection.getString("material");
        if (matName == null) return null;
        
        Material material = Material.matchMaterial(matName);
        if (material == null || !ARMOR_MATERIALS.contains(material) && 
            material != Material.SHIELD && material != Material.TOTEM_OF_UNDYING) {
            Bukkit.getLogger().warning("[KitLoader] Materiale equipment non valido per " + slot + ": " + matName);
            return null;
        }
        
        return loadKitItemInternal(itemSection, material, allowUnsafe);
    }
    
    @Nullable
    private static KitItem loadKitItem(@Nullable ConfigurationSection section, boolean allowUnsafe) {
        if (section == null) return null;
        
        String matName = section.getString("material");
        if (matName == null) return null;
        
        Material material = Material.matchMaterial(matName);
        if (material == null) {
            Bukkit.getLogger().warning("[KitLoader] Materiale item non valido: " + matName);
            return null;
        }
        
        return loadKitItemInternal(section, material, allowUnsafe);
    }
    
    @Nullable
    private static KitItem loadKitItemFromMap(@NotNull Map<?, ?> data, boolean allowUnsafe) {
        Object matObj = data.get("material");
        if (!(matObj instanceof String)) return null;
        
        Material material = Material.matchMaterial((String) matObj);
        if (material == null) {
            Bukkit.getLogger().warning("[KitLoader] Materiale item non valido: " + matObj);
            return null;
        }
        
        KitItem item = new KitItem(material);
        item.setAmount(data.containsKey("amount") ? ((Number) data.get("amount")).intValue() : 1);
        
        if (data.containsKey("custom-name")) {
            item.setCustomName((String) data.get("custom-name"));
        }
        
        if (data.containsKey("mark")) {
            item.setMark((Boolean) data.get("mark"));
        }
        
        // Enchants da preset
        if (data.containsKey("preset")) {
            String presetName = (String) data.get("preset");
            Map<String, Integer> preset = Kit.getPreset(presetName);
            if (preset != null) {
                for (Map.Entry<String, Integer> entry : preset.entrySet()) {
                    RegistryUtil.getEnchantment(entry.getKey()).ifPresent(e -> 
                        item.addEnchant(e, entry.getValue()));
                }
            } else {
                Bukkit.getLogger().warning("[KitLoader] Preset non trovato: " + presetName);
            }
        }
        
        // Enchants aggiuntivi
        if (data.containsKey("enchants")) {
            Object enchantsObj = data.get("enchants");
            if (enchantsObj instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) enchantsObj).entrySet()) {
                    if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                        RegistryUtil.getEnchantment((String) entry.getKey()).ifPresent(e ->
                            item.addEnchant(e, ((Number) entry.getValue()).intValue()));
                    }
                }
            }
        }
        
        // Potion type
        if (data.containsKey("potion")) {
            String potionKey = (String) data.get("potion");
            RegistryUtil.getPotionType(potionKey).ifPresent(item::setPotionType);
        }
        
        // Firework power
        if (data.containsKey("firework-power")) {
            item.setFireworkPower(((Number) data.get("firework-power")).intValue());
        }
        
        return item;
    }
    
    @NotNull
    private static KitItem loadKitItemInternal(@NotNull ConfigurationSection section, 
                                                @NotNull Material material, boolean allowUnsafe) {
        KitItem item = new KitItem(material);
        item.setAmount(section.getInt("amount", 1));
        item.setCustomName(section.getString("custom-name"));
        item.setLore(section.getStringList("lore"));
        item.setMark(section.getBoolean("mark", false));
        
        // Enchants da preset
        if (section.contains("preset")) {
            String presetName = section.getString("preset");
            Map<String, Integer> preset = Kit.getPreset(presetName);
            if (preset != null) {
                for (Map.Entry<String, Integer> entry : preset.entrySet()) {
                    RegistryUtil.getEnchantment(entry.getKey()).ifPresent(e ->
                        item.addEnchant(e, entry.getValue()));
                }
            } else {
                Bukkit.getLogger().warning("[KitLoader] Preset non trovato: " + presetName);
            }
        }
        
        // Enchants aggiuntivi
        ConfigurationSection enchantSection = section.getConfigurationSection("enchants");
        if (enchantSection != null) {
            for (String key : enchantSection.getKeys(false)) {
                int level = enchantSection.getInt(key);
                RegistryUtil.getEnchantment(key).ifPresent(e -> item.addEnchant(e, level));
            }
        }
        
        // Potion type
        if (section.contains("potion")) {
            String potionKey = section.getString("potion");
            RegistryUtil.getPotionType(potionKey).ifPresent(item::setPotionType);
        }
        
        // Firework power
        if (section.contains("firework-power")) {
            item.setFireworkPower(section.getInt("firework-power"));
        }
        
        // Trim
        ConfigurationSection trimSection = section.getConfigurationSection("trim");
        if (trimSection != null) {
            String patternKey = trimSection.getString("pattern");
            String materialKey = trimSection.getString("material");
            RegistryUtil.getTrimPattern(patternKey).ifPresent(item::setTrimPattern);
            RegistryUtil.getTrimMaterial(materialKey).ifPresent(item::setTrimMaterial);
        }
        
        return item;
    }
    
    @Nullable
    private static KitContainer loadContainer(@Nullable ConfigurationSection section, boolean allowUnsafe) {
        if (section == null) return null;
        
        String matName = section.getString("material");
        if (matName == null) return null;
        
        Material material = Material.matchMaterial(matName);
        if (material == null || !material.name().endsWith("_SHULKER_BOX")) {
            Bukkit.getLogger().warning("[KitLoader] Shulker box non valida: " + matName);
            return null;
        }
        
        KitContainer container = new KitContainer(material);
        container.setCustomName(section.getString("name"));
        
        // Contents
        List<Map<?, ?>> contentsList = section.getMapList("contents");
        for (Map<?, ?> contentData : contentsList) {
            KitItem contentItem = loadKitItemFromMap(contentData, allowUnsafe);
            if (contentItem != null) {
                container.addContent(contentItem);
            }
        }
        
        if (!container.isValidContent()) {
            Bukkit.getLogger().warning("[KitLoader] Shulker box " + matName + " ha più di 27 items, alcuni verranno ignorati");
        }
        
        return container;
    }
    
    @Nullable
    private static KitContainer loadContainerFromMap(@NotNull Map<?, ?> data, boolean allowUnsafe) {
        Object matObj = data.get("material");
        if (!(matObj instanceof String)) return null;
        
        Material material = Material.matchMaterial((String) matObj);
        if (material == null || !material.name().endsWith("_SHULKER_BOX")) {
            Bukkit.getLogger().warning("[KitLoader] Shulker box non valida: " + matObj);
            return null;
        }
        
        KitContainer container = new KitContainer(material);
        
        if (data.containsKey("name")) {
            container.setCustomName((String) data.get("name"));
        }
        
        if (data.containsKey("contents")) {
            Object contentsObj = data.get("contents");
            if (contentsObj instanceof List) {
                for (Object contentData : (List<?>) contentsObj) {
                    if (contentData instanceof Map) {
                        KitItem contentItem = loadKitItemFromMap((Map<?, ?>) contentData, allowUnsafe);
                        if (contentItem != null) {
                            container.addContent(contentItem);
                        }
                    }
                }
            }
        }
        
        return container;
    }
}
