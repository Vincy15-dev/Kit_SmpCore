package com.Kit_SmpCore.plugin.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Builder per creare ItemStack con meta complessi.
 * Supporta: custom name, lore, enchantments, potion types, firework power.
 */
public class ItemBuilder {
    
    private final Material material;
    private int amount = 1;
    private String customName;
    private List<String> lore;
    private boolean glowing = false;
    private PotionType potionType;
    private Integer fireworkPower;
    private final Map<Enchantment, Integer> enchants = new HashMap<>();
    private final List<ItemFlag> flags = new ArrayList<>();
    
    public ItemBuilder(@NotNull Material material) {
        this.material = material;
    }
    
    public ItemBuilder amount(int amount) {
        this.amount = Math.max(1, amount);
        return this;
    }
    
    public ItemBuilder name(String name) {
        this.customName = name;
        return this;
    }
    
    public ItemBuilder lore(List<String> lore) {
        this.lore = lore != null ? new ArrayList<>(lore) : null;
        return this;
    }
    
    public ItemBuilder addLoreLine(String line) {
        if (lore == null) lore = new ArrayList<>();
        lore.add(line);
        return this;
    }
    
    public ItemBuilder glowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }
    
    public ItemBuilder potionType(PotionType type) {
        this.potionType = type;
        return this;
    }
    
    public ItemBuilder fireworkPower(int power) {
        this.fireworkPower = power;
        return this;
    }
    
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.enchants.put(enchantment, level);
        return this;
    }
    
    public ItemBuilder flag(ItemFlag flag) {
        this.flags.add(flag);
        return this;
    }
    
    public ItemBuilder hideFlags() {
        this.flags.add(ItemFlag.HIDE_ENCHANTS);
        this.flags.add(ItemFlag.HIDE_ATTRIBUTES);
        this.flags.add(ItemFlag.HIDE_POTION_EFFECTS);
        this.flags.add(ItemFlag.HIDE_UNBREAKABLE);
        this.flags.add(ItemFlag.HIDE_DYE);
        this.flags.add(ItemFlag.HIDE_ARMOR_TRIM);
        return this;
    }
    
    @NotNull
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta == null) return item;
        
        // Nome personalizzato
        if (customName != null && !customName.isEmpty()) {
            meta.setDisplayName(ColorUtil.colorize(customName));
        }
        
        // Lore
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(ColorUtil.resetLore(lore));
        }
        
        // Enchants
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        
        // Glowing effect (glint override)
        if (glowing && enchants.isEmpty()) {
            try {
                meta.setEnchantmentGlintOverride(true);
            } catch (NoSuchMethodError e) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                flags.add(ItemFlag.HIDE_ENCHANTS);
            }
        }
        
        // Flags
        for (ItemFlag flag : flags) {
            meta.addItemFlags(flag);
        }
        
        // Potion type
        if (meta instanceof PotionMeta && potionType != null) {
            ((PotionMeta) meta).setBasePotionType(potionType);
        }
        
        // Firework power
        if (meta instanceof FireworkMeta && fireworkPower != null) {
            ((FireworkMeta) meta).setPower(fireworkPower);
        }
        
        item.setItemMeta(meta);
        return item;
    }
}
