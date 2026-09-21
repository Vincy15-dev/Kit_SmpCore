package com.Kit_SmpCore.plugin.kit;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rappresenta un kit completo con tutti i suoi dati.
 */
public class Kit {
    
    private final String id;
    private String displayName;
    private String tierLabel;
    private List<String> description;
    private Material iconMaterial;
    private String colorFrom;
    private String colorTo;
    private String permission;
    private boolean defaultPermission;
    private long cooldownSeconds;  // -1 = one-time
    private boolean announceClaim;
    private String lockedHint;
    private String deliveryType;
    
    // Equipment
    private KitItem helmet;
    private KitItem chestplate;
    private KitItem leggings;
    private KitItem boots;
    private KitItem offhand;
    
    // Items e containers
    private final List<KitItem> items = new ArrayList<>();
    private final List<KitContainer> containers = new ArrayList<>();
    
    // Comandi console da eseguire al claim
    private final List<String> commands = new ArrayList<>();
    
    // Presets (caricati dal config)
    private static final Map<String, Map<String, Integer>> PRESETS = new HashMap<>();
    
    public Kit(@NotNull String id) {
        this.id = id;
    }
    
    @NotNull
    public String getId() {
        return id;
    }
    
    @NotNull
    public String getDisplayName() {
        return displayName != null ? displayName : id;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    @NotNull
    public String getTierLabel() {
        return tierLabel != null ? tierLabel : "";
    }
    
    public void setTierLabel(String tierLabel) {
        this.tierLabel = tierLabel;
    }
    
    @NotNull
    public List<String> getDescription() {
        return description != null ? description : new ArrayList<>();
    }
    
    public void setDescription(List<String> description) {
        this.description = description;
    }
    
    @NotNull
    public Material getIconMaterial() {
        return iconMaterial != null ? iconMaterial : Material.BARRIER;
    }
    
    public void setIconMaterial(Material iconMaterial) {
        this.iconMaterial = iconMaterial;
    }
    
    @NotNull
    public String getColorFrom() {
        return colorFrom != null ? colorFrom : "#FFFFFF";
    }
    
    public void setColorFrom(String colorFrom) {
        this.colorFrom = colorFrom;
    }
    
    @NotNull
    public String getColorTo() {
        return colorTo != null ? colorTo : "#FFFFFF";
    }
    
    public void setColorTo(String colorTo) {
        this.colorTo = colorTo;
    }
    
    @NotNull
    public String getPermission() {
        return permission != null ? permission : "kit." + id;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    public boolean isDefaultPermission() {
        return defaultPermission;
    }
    
    public void setDefaultPermission(boolean defaultPermission) {
        this.defaultPermission = defaultPermission;
    }
    
    public long getCooldownSeconds() {
        return cooldownSeconds;
    }
    
    public void setCooldownSeconds(long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }
    
    public boolean isOneTime() {
        return cooldownSeconds < 0;
    }
    
    public boolean shouldAnnounceClaim() {
        return announceClaim;
    }
    
    public void setAnnounceClaim(boolean announceClaim) {
        this.announceClaim = announceClaim;
    }
    
    @NotNull
    public String getLockedHint() {
        return lockedHint != null ? lockedHint : "Non hai accesso a questo kit.";
    }
    
    public void setLockedHint(String lockedHint) {
        this.lockedHint = lockedHint;
    }
    
    @NotNull
    public String getDeliveryType() {
        return deliveryType != null ? deliveryType : "direct";
    }
    
    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }
    
    @Nullable
    public KitItem getHelmet() {
        return helmet;
    }
    
    public void setHelmet(KitItem helmet) {
        this.helmet = helmet;
    }
    
    @Nullable
    public KitItem getChestplate() {
        return chestplate;
    }
    
    public void setChestplate(KitItem chestplate) {
        this.chestplate = chestplate;
    }
    
    @Nullable
    public KitItem getLeggings() {
        return leggings;
    }
    
    public void setLeggings(KitItem leggings) {
        this.leggings = leggings;
    }
    
    @Nullable
    public KitItem getBoots() {
        return boots;
    }
    
    public void setBoots(KitItem boots) {
        this.boots = boots;
    }
    
    @Nullable
    public KitItem getOffhand() {
        return offhand;
    }
    
    public void setOffhand(KitItem offhand) {
        this.offhand = offhand;
    }
    
    @NotNull
    public List<KitItem> getItems() {
        return items;
    }
    
    public void addItem(KitItem item) {
        this.items.add(item);
    }
    
    @NotNull
    public List<KitContainer> getContainers() {
        return containers;
    }
    
    public void addContainer(KitContainer container) {
        this.containers.add(container);
    }
    
    @NotNull
    public List<String> getCommands() {
        return commands;
    }
    
    public void addCommand(String command) {
        this.commands.add(command);
    }
    
    /**
     * Registra un preset di enchantments.
     */
    public static void registerPreset(@NotNull String name, @NotNull Map<String, Integer> enchants) {
        PRESETS.put(name, enchants);
    }
    
    /**
     * Ottieni un preset per nome.
     */
    @Nullable
    public static Map<String, Integer> getPreset(@NotNull String name) {
        return PRESETS.get(name);
    }
    
    /**
     * Controlla se un preset esiste.
     */
    public static boolean hasPreset(@NotNull String name) {
        return PRESETS.containsKey(name);
    }
}
