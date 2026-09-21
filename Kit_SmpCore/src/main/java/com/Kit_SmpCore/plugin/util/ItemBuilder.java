package com.Kit_SmpCore.plugin.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builder per la creazione di ItemStack con nome, lore, enchant e PDC.
 */
public class ItemBuilder {

    private final Material material;
    private int amount = 1;
    private String name;
    private List<String> lore;
    private boolean unbreakable = true;
    private final List<ItemFlag> flags = new ArrayList<>();
    private final Plugin plugin;
    private String kitId;
    private Boolean glintOverride;

    public ItemBuilder(Plugin plugin, Material material) {
        this.plugin = plugin;
        this.material = material != null ? material : Material.STONE;
        this.lore = new ArrayList<>();
    }

    /**
     * Imposta la quantità dell'item.
     */
    public ItemBuilder amount(int amount) {
        this.amount = Math.max(1, Math.min(amount, material.getMaxStackSize()));
        return this;
    }

    /**
     * Imposta il nome personalizzato dell'item.
     * Aggiunge automaticamente ChatColor.RESET per evitare italic.
     */
    public ItemBuilder name(String name) {
        if (name != null) {
            this.name = ColorUtil.resetPrefix(ColorUtil.translateColors(name));
        }
        return this;
    }

    /**
     * Aggiunge una linea alla lore.
     */
    public ItemBuilder addLoreLine(String line) {
        if (line != null) {
            this.lore.add(ColorUtil.resetPrefix(ColorUtil.translateColors(line)));
        }
        return this;
    }

    /**
     * Imposta la lore completa.
     */
    public ItemBuilder lore(List<String> lore) {
        if (lore != null) {
            this.lore = new ArrayList<>();
            for (String line : lore) {
                this.lore.add(ColorUtil.resetPrefix(ColorUtil.translateColors(line)));
            }
        }
        return this;
    }

    /**
     * Imposta l'item come indistruttibile (nasconde attributi).
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     * Aggiunge un flag per nascondere attributi.
     */
    public ItemBuilder hideAttribute(Attribute attribute) {
        flags.add(ItemFlag.HIDE_ATTRIBUTES);
        return this;
    }

    /**
     * Aggiunge tutti i flag per nascondere tooltip.
     */
    public ItemBuilder hideAll() {
        flags.add(ItemFlag.HIDE_ENCHANTS);
        flags.add(ItemFlag.HIDE_ATTRIBUTES);
        flags.add(ItemFlag.HIDE_UNBREAKABLE);
        flags.add(ItemFlag.HIDE_DESTROYS);
        flags.add(ItemFlag.HIDE_PLACED_ON);
        flags.add(ItemFlag.HIDE_DYE);
        flags.add(ItemFlag.HIDE_ARMOR_TRIM);
        return this;
    }

    /**
     * Imposta l'override del glint incantato.
     */
    public ItemBuilder glintOverride(boolean glint) {
        this.glintOverride = glint;
        return this;
    }

    /**
     * Aggiunge un enchant all'item.
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (enchantment != null) {
            // Gli enchant verranno applicati dopo nella build
            // Questo è solo un placeholder per API futura
        }
        return this;
    }

    /**
     * Imposta l'ID del kit nel PersistentDataContainer.
     */
    public ItemBuilder kitId(String kitId) {
        this.kitId = kitId;
        return this;
    }

    /**
     * Costruisce l'ItemStack finale.
     * Nota: Gli enchant vanno applicati esternamente tramite ItemMeta.
     */
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        if (name != null) {
            meta.setDisplayName(name);
        }

        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }

        meta.setUnbreakable(unbreakable);

        for (ItemFlag flag : flags) {
            meta.addItemFlags(flag);
        }

        if (glintOverride != null) {
            meta.setEnchantmentGlintOverride(glintOverride);
        }

        // Aggiungi PDC per il kit marker
        if (kitId != null) {
            NamespacedKey key = new NamespacedKey(plugin, "kit_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, kitId);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Applica gli enchant all'ItemStack costruito.
     * Questo metodo va chiamato dopo build() o su un item esistente.
     */
    public static void applyEnchants(ItemStack item, Map<Enchantment, Integer> enchants, Plugin plugin) {
        if (item == null || enchants == null || enchants.isEmpty()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            if (enchant == null) continue;

            // Clampa al livello massimo se non unsafe
            int maxLevel = enchant.getMaxLevel();
            boolean allowUnsafe = false; // Da config
            if (!allowUnsafe && level > maxLevel) {
                level = maxLevel;
            }

            meta.addEnchant(enchant, level, true);
        }

        item.setItemMeta(meta);
    }
}
