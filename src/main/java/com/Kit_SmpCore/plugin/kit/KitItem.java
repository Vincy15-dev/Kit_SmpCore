package com.Kit_SmpCore.plugin.kit;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rappresenta un item singolo all'interno di un kit.
 * Può essere un item normale, una pozione, un fuoco d'artificio o un item con enchant custom.
 */
public class KitItem {
    
    private final Material material;
    private int amount;
    private String customName;
    private List<String> lore;
    private String presetName;
    private final Map<Enchantment, Integer> enchants = new HashMap<>();
    private PotionType potionType;
    private Integer fireworkPower;
    private TrimPattern trimPattern;
    private TrimMaterial trimMaterial;
    private boolean mark = false;  // Se applicare il PDC marker
    
    public KitItem(@NotNull Material material) {
        this.material = material;
        this.amount = 1;
    }
    
    @NotNull
    public Material getMaterial() {
        return material;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = Math.max(1, amount);
    }
    
    @Nullable
    public String getCustomName() {
        return customName;
    }
    
    public void setCustomName(@Nullable String customName) {
        this.customName = customName;
    }
    
    @Nullable
    public List<String> getLore() {
        return lore;
    }
    
    public void setLore(@Nullable List<String> lore) {
        this.lore = lore;
    }
    
    @Nullable
    public String getPresetName() {
        return presetName;
    }
    
    public void setPresetName(@Nullable String presetName) {
        this.presetName = presetName;
    }
    
    @NotNull
    public Map<Enchantment, Integer> getEnchants() {
        return enchants;
    }
    
    public void addEnchant(@NotNull Enchantment enchantment, int level) {
        this.enchants.put(enchantment, level);
    }
    
    @Nullable
    public PotionType getPotionType() {
        return potionType;
    }
    
    public void setPotionType(@Nullable PotionType potionType) {
        this.potionType = potionType;
    }
    
    @Nullable
    public Integer getFireworkPower() {
        return fireworkPower;
    }
    
    public void setFireworkPower(@Nullable Integer fireworkPower) {
        this.fireworkPower = fireworkPower;
    }
    
    @Nullable
    public TrimPattern getTrimPattern() {
        return trimPattern;
    }
    
    public void setTrimPattern(@Nullable TrimPattern trimPattern) {
        this.trimPattern = trimPattern;
    }
    
    @Nullable
    public TrimMaterial getTrimMaterial() {
        return trimMaterial;
    }
    
    public void setTrimMaterial(@Nullable TrimMaterial trimMaterial) {
        this.trimMaterial = trimMaterial;
    }
    
    public boolean shouldMark() {
        return mark;
    }
    
    public void setMark(boolean mark) {
        this.mark = mark;
    }
    
    /**
     * Controlla se l'item è stackabile.
     */
    public boolean isStackable() {
        return material.getMaxStackSize() > 1;
    }
}
