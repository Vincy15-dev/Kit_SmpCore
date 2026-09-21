package com.Kit_SmpCore.plugin.kit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modello per un container (shulker box) all'interno di un kit.
 */
public class KitContainer {

    private final Material material;
    private final String name;
    private final List<KitItem> contents;

    public KitContainer(Material material, String name, List<KitItem> contents) {
        this.material = material != null ? material : Material.SHULKER_BOX;
        this.name = name;
        this.contents = contents != null ? new ArrayList<>(contents) : new ArrayList<>();
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public List<KitItem> getContents() { return new ArrayList<>(contents); }

    /**
     * Deserializza un KitContainer da ConfigurationSection.
     */
    public static KitContainer fromConfig(ConfigurationSection section) {
        if (section == null) return null;

        String matName = section.getString("material");
        Material material = matName != null ? Material.matchMaterial(matName.toUpperCase()) : Material.SHULKER_BOX;
        if (material == null || !material.name().endsWith("_SHULKER_BOX")) {
            material = Material.SHULKER_BOX; // Fallback
        }

        String name = section.getString("name");
        
        List<KitItem> contents = new ArrayList<>();
        List<?> contentList = section.getMapList("contents");
        if (contentList != null) {
            for (Object obj : contentList) {
                if (obj instanceof ConfigurationSection) {
                    KitItem item = KitItem.fromConfig((ConfigurationSection) obj);
                    if (item != null) {
                        contents.add(item);
                    }
                } else if (obj instanceof Map) {
                    // Converti Map a ConfigurationSection-like
                    Map<?, ?> map = (Map<?, ?>) obj;
                    ConfigurationSection fakeSection = new MapConfigurationSection(map);
                    KitItem item = KitItem.fromConfig(fakeSection);
                    if (item != null) {
                        contents.add(item);
                    }
                }
            }
        }

        return new KitContainer(material, name, contents);
    }

    /**
     * Serializza un KitContainer in una mappa per il salvataggio.
     */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", material.name());
        if (name != null) map.put("name", name);
        
        List<Map<String, Object>> contentsList = new ArrayList<>();
        for (KitItem item : contents) {
            contentsList.add(item.serialize());
        }
        map.put("contents", contentsList);
        
        return map;
    }

    /**
     * Wrapper per convertire Map a ConfigurationSection.
     */
    private static class MapConfigurationSection extends org.bukkit.configuration.MemorySection {
        private final Map<?, ?> data;

        public MapConfigurationSection(Map<?, ?> data) {
            this.data = data;
        }

        @Override
        public Object get(String path) {
            return data.get(path);
        }

        @Override
        public String getString(String path) {
            Object val = get(path);
            return val != null ? val.toString() : null;
        }

        @Override
        public int getInt(String path) {
            Object val = get(path);
            if (val instanceof Number) return ((Number) val).intValue();
            return 0;
        }

        @Override
        public ConfigurationSection getConfigurationSection(String path) {
            Object val = get(path);
            if (val instanceof Map) {
                return new MapConfigurationSection((Map<?, ?>) val);
            }
            return null;
        }

        @Override
        public List<String> getStringList(String path) {
            Object val = get(path);
            if (val instanceof List) {
                List<String> result = new ArrayList<>();
                for (Object item : (List<?>) val) {
                    result.add(item != null ? item.toString() : "");
                }
                return result;
            }
            return new ArrayList<>();
        }
    }
}
