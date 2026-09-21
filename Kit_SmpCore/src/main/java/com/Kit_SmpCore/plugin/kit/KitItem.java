package com.Kit_SmpCore.plugin.kit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modello per un item singolo all'interno di un kit.
 */
public class KitItem {

    private final Material material;
    private final int amount;
    private final String name;
    private final List<String> lore;
    private final String preset;
    private final Map<String, Integer> enchants;
    private final String potionType;
    private final Integer fireworkPower;
    private final TrimConfig trim;
    private final boolean mark;

    public static class TrimConfig {
        private final String pattern;
        private final String material;

        public TrimConfig(String pattern, String material) {
            this.pattern = pattern;
            this.material = material;
        }

        public String getPattern() { return pattern; }
        public String getMaterial() { return material; }
    }

    private KitItem(Material material, int amount, String name, List<String> lore,
                    String preset, Map<String, Integer> enchants, String potionType,
                    Integer fireworkPower, TrimConfig trim, boolean mark) {
        this.material = material;
        this.amount = amount;
        this.name = name;
        this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
        this.preset = preset;
        this.enchants = enchants != null ? new HashMap<>(enchants) : new HashMap<>();
        this.potionType = potionType;
        this.fireworkPower = fireworkPower;
        this.trim = trim;
        this.mark = mark;
    }

    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public String getName() { return name; }
    public List<String> getLore() { return new ArrayList<>(lore); }
    public String getPreset() { return preset; }
    public Map<String, Integer> getEnchants() { return new HashMap<>(enchants); }
    public String getPotionType() { return potionType; }
    public Integer getFireworkPower() { return fireworkPower; }
    public TrimConfig getTrim() { return trim; }
    public boolean isMark() { return mark; }

    /**
     * Deserializza un KitItem da ConfigurationSection.
     */
    public static KitItem fromConfig(ConfigurationSection section) {
        if (section == null) return null;

        String matName = section.getString("material");
        Material material = matName != null ? Material.matchMaterial(matName.toUpperCase()) : Material.STONE;
        if (material == null) {
            material = Material.STONE; // Fallback
        }

        int amount = section.getInt("amount", 1);
        String name = section.getString("name");
        List<String> lore = section.getStringList("lore");
        String preset = section.getString("preset");
        
        Map<String, Integer> enchants = new HashMap<>();
        ConfigurationSection enchantSection = section.getConfigurationSection("enchants");
        if (enchantSection != null) {
            for (String key : enchantSection.getKeys(false)) {
                enchants.put(key.toLowerCase(), enchantSection.getInt(key));
            }
        }

        String potionType = section.getString("potion");
        Integer fireworkPower = section.getInt("firework-power", -1);
        if (fireworkPower < 0) fireworkPower = null;

        TrimConfig trim = null;
        ConfigurationSection trimSection = section.getConfigurationSection("trim");
        if (trimSection != null) {
            String pattern = trimSection.getString("pattern");
            String trimMat = trimSection.getString("material");
            if (pattern != null && trimMat != null) {
                trim = new TrimConfig(pattern, trimMat);
            }
        }

        boolean mark = section.getBoolean("mark", false);

        return new KitItem(material, amount, name, lore, preset, enchants, 
                          potionType, fireworkPower, trim, mark);
    }

    /**
     * Serializza un KitItem in una mappa per il salvataggio.
     */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", material.name());
        if (amount > 1) map.put("amount", amount);
        if (name != null) map.put("name", name);
        if (!lore.isEmpty()) map.put("lore", lore);
        if (preset != null && !preset.isEmpty()) map.put("preset", preset);
        if (!enchants.isEmpty()) map.put("enchants", enchants);
        if (potionType != null) map.put("potion", potionType);
        if (fireworkPower != null) map.put("firework-power", fireworkPower);
        if (trim != null) {
            Map<String, String> trimMap = new HashMap<>();
            trimMap.put("pattern", trim.getPattern());
            trimMap.put("material", trim.getMaterial());
            map.put("trim", trimMap);
        }
        map.put("mark", mark);
        return map;
    }
}
