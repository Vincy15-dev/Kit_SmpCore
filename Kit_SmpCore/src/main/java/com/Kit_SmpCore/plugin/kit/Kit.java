package com.Kit_SmpCore.plugin.kit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

/**
 * Modello principale per un kit completo.
 */
public class Kit {

    private final String id;
    private final String displayName;
    private final String tierLabel;
    private final List<String> description;
    private final Material iconMaterial;
    private final String colorFrom;
    private final String colorTo;
    private final String permission;
    private final boolean defaultPermission;
    private final long cooldownSeconds; // -1 = one-time
    private final boolean announceClaim;
    private final String lockedHint;
    private final String delivery;
    
    // Equipment
    private final KitItem helmet;
    private final KitItem chestplate;
    private final KitItem leggings;
    private final KitItem boots;
    private final KitItem offhand;
    
    // Items e containers
    private final List<KitItem> items;
    private final List<KitContainer> containers;
    
    // Comandi console da eseguire al claim
    private final List<String> commands;

    public Kit(String id, String displayName, String tierLabel, List<String> description,
               Material iconMaterial, String colorFrom, String colorTo,
               String permission, boolean defaultPermission, long cooldownSeconds,
               boolean announceClaim, String lockedHint, String delivery,
               KitItem helmet, KitItem chestplate, KitItem leggings, 
               KitItem boots, KitItem offhand,
               List<KitItem> items, List<KitContainer> containers, List<String> commands) {
        this.id = id;
        this.displayName = displayName;
        this.tierLabel = tierLabel;
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
        this.iconMaterial = iconMaterial != null ? iconMaterial : Material.BARRIER;
        this.colorFrom = colorFrom != null ? colorFrom : "#FFFFFF";
        this.colorTo = colorTo != null ? colorTo : "#FFFFFF";
        this.permission = permission;
        this.defaultPermission = defaultPermission;
        this.cooldownSeconds = cooldownSeconds;
        this.announceClaim = announceClaim;
        this.lockedHint = lockedHint != null ? lockedHint : "Bloccato";
        this.delivery = delivery != null ? delivery : "direct";
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.offhand = offhand;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.containers = containers != null ? new ArrayList<>(containers) : new ArrayList<>();
        this.commands = commands != null ? new ArrayList<>(commands) : new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getTierLabel() { return tierLabel; }
    public List<String> getDescription() { return new ArrayList<>(description); }
    public Material getIconMaterial() { return iconMaterial; }
    public String getColorFrom() { return colorFrom; }
    public String getColorTo() { return colorTo; }
    public String getPermission() { return permission; }
    public boolean isDefaultPermission() { return defaultPermission; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public boolean isAnnounceClaim() { return announceClaim; }
    public String getLockedHint() { return lockedHint; }
    public String getDelivery() { return delivery; }
    public KitItem getHelmet() { return helmet; }
    public KitItem getChestplate() { return chestplate; }
    public KitItem getLeggings() { return leggings; }
    public KitItem getBoots() { return boots; }
    public KitItem getOffhand() { return offhand; }
    public List<KitItem> getItems() { return new ArrayList<>(items); }
    public List<KitContainer> getContainers() { return new ArrayList<>(containers); }
    public List<String> getCommands() { return new ArrayList<>(commands); }

    /**
     * Ottiene tutti gli item del kit (equipment + items + containers).
     */
    public List<Object> getAllContents() {
        List<Object> all = new ArrayList<>();
        if (helmet != null) all.add(helmet);
        if (chestplate != null) all.add(chestplate);
        if (leggings != null) all.add(leggings);
        if (boots != null) all.add(boots);
        if (offhand != null) all.add(offhand);
        all.addAll(items);
        all.addAll(containers);
        return all;
    }

    /**
     * Deserializza un Kit da ConfigurationSection.
     */
    public static Kit fromConfig(String id, ConfigurationSection section) {
        if (section == null) return null;

        String displayName = section.getString("display-name", id);
        String tierLabel = section.getString("tier-label", "");
        List<String> description = section.getStringList("description");
        
        String iconName = section.getString("icon", "BARRIER");
        Material iconMaterial = Material.matchMaterial(iconName.toUpperCase());
        if (iconMaterial == null) iconMaterial = Material.BARRIER;

        String colorFrom = section.getString("color-from", "#FFFFFF");
        String colorTo = section.getString("color-to", "#FFFFFF");
        String permission = section.getString("permission", "kit." + id);
        boolean defaultPermission = section.getBoolean("default-permission", false);
        long cooldownSeconds = section.getLong("cooldown", 86400);
        boolean announceClaim = section.getBoolean("announce-claim", false);
        String lockedHint = section.getString("locked-hint", "Bloccato");
        String delivery = section.getString("delivery", "direct");

        // Equipment
        KitItem helmet = null, chestplate = null, leggings = null, boots = null, offhand = null;
        ConfigurationSection equipSection = section.getConfigurationSection("equipment");
        if (equipSection != null) {
            helmet = KitItem.fromConfig(equipSection.getConfigurationSection("helmet"));
            chestplate = KitItem.fromConfig(equipSection.getConfigurationSection("chestplate"));
            leggings = KitItem.fromConfig(equipSection.getConfigurationSection("leggings"));
            boots = KitItem.fromConfig(equipSection.getConfigurationSection("boots"));
            offhand = KitItem.fromConfig(equipSection.getConfigurationSection("offhand"));
        }

        // Items
        List<KitItem> items = new ArrayList<>();
        List<?> itemList = section.getMapList("items");
        if (itemList != null) {
            for (Object obj : itemList) {
                if (obj instanceof ConfigurationSection) {
                    KitItem item = KitItem.fromConfig((ConfigurationSection) obj);
                    if (item != null) items.add(item);
                } else if (obj instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) obj;
                    ConfigurationSection fakeSection = createFakeSection(map);
                    KitItem item = KitItem.fromConfig(fakeSection);
                    if (item != null) items.add(item);
                }
            }
        }

        // Containers
        List<KitContainer> containers = new ArrayList<>();
        List<?> containerList = section.getMapList("containers");
        if (containerList != null) {
            for (Object obj : containerList) {
                if (obj instanceof ConfigurationSection) {
                    KitContainer container = KitContainer.fromConfig((ConfigurationSection) obj);
                    if (container != null) containers.add(container);
                } else if (obj instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) obj;
                    ConfigurationSection fakeSection = createFakeSection(map);
                    KitContainer container = KitContainer.fromConfig(fakeSection);
                    if (container != null) containers.add(container);
                }
            }
        }

        // Commands
        List<String> commands = section.getStringList("commands");

        return new Kit(id, displayName, tierLabel, description, iconMaterial,
                      colorFrom, colorTo, permission, defaultPermission, cooldownSeconds,
                      announceClaim, lockedHint, delivery,
                      helmet, chestplate, leggings, boots, offhand,
                      items, containers, commands);
    }

    /**
     * Crea una ConfigurationSection finta da una Map.
     */
    private static ConfigurationSection createFakeSection(Map<?, ?> map) {
        return new KitContainer.MapConfigurationSection(map);
    }

    /**
     * Serializza un Kit in una mappa per il salvataggio.
     */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("display-name", displayName);
        map.put("tier-label", tierLabel);
        map.put("description", description);
        map.put("icon", iconMaterial.name());
        map.put("color-from", colorFrom);
        map.put("color-to", colorTo);
        map.put("permission", permission);
        map.put("default-permission", defaultPermission);
        map.put("cooldown", cooldownSeconds);
        map.put("announce-claim", announceClaim);
        map.put("locked-hint", lockedHint);
        map.put("delivery", delivery);

        // Equipment
        Map<String, Object> equipMap = new HashMap<>();
        if (helmet != null) equipMap.put("helmet", helmet.serialize());
        if (chestplate != null) equipMap.put("chestplate", chestplate.serialize());
        if (leggings != null) equipMap.put("leggings", leggings.serialize());
        if (boots != null) equipMap.put("boots", boots.serialize());
        if (offhand != null) equipMap.put("offhand", offhand.serialize());
        map.put("equipment", equipMap);

        // Items
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (KitItem item : items) {
            itemsList.add(item.serialize());
        }
        map.put("items", itemsList);

        // Containers
        List<Map<String, Object>> containersList = new ArrayList<>();
        for (KitContainer container : containers) {
            containersList.add(container.serialize());
        }
        map.put("containers", containersList);

        // Commands
        map.put("commands", commands);

        return map;
    }
}
