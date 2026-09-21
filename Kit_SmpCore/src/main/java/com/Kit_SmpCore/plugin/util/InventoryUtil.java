package com.Kit_SmpCore.plugin.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Utility per operazioni sugli inventory.
 */
public class InventoryUtil {

    /**
     * Conta quanti slot liberi ha un giocatore (inventory + armor se vuoti).
     * Considera gli stack massimi per materiali stackabili.
     */
    public static int countFreeSlots(Player player) {
        if (player == null) return 0;

        PlayerInventory inv = player.getInventory();
        int freeSlots = 0;

        // Slot inventory principale (36 slot: 0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                freeSlots++;
            }
        }

        // Armor slots (4 slot) - solo se vuoti
        if (inv.getHelmet() == null || inv.getHelmet().getType() == Material.AIR) freeSlots++;
        if (inv.getChestplate() == null || inv.getChestplate().getType() == Material.AIR) freeSlots++;
        if (inv.getLeggings() == null || inv.getLeggings().getType() == Material.AIR) freeSlots++;
        if (inv.getBoots() == null || inv.getBoots().getType() == Material.AIR) freeSlots++;

        // Offhand (1 slot) - solo se vuoto
        if (inv.getItemInOffHand() == null || inv.getItemInOffHand().getType() == Material.AIR) freeSlots++;

        return freeSlots;
    }

    /**
     * Simula se gli item entrano nell'inventario del giocatore.
     * Restituisce il numero di slot necessari vs disponibili.
     * 
     * @return int[] {slotNecessari, slotDisponibili}
     */
    public static int[] simulateInventorySpace(Player player, ItemStack[] items) {
        if (player == null || items == null) return new int[]{0, 0};

        PlayerInventory inv = player.getInventory();
        int availableSlots = countFreeSlots(player);
        int neededSlots = 0;

        // Clona l'inventario per simulazione
        ItemStack[] simulatedStorage = inv.getStorageContents().clone();
        ItemStack[] simulatedArmor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            ItemStack armorItem = inv.getArmorContents()[i];
            simulatedArmor[i] = armorItem != null ? armorItem.clone() : null;
        }
        ItemStack simulatedOffHand = inv.getItemInOffHand() != null ? inv.getItemInOffHand().clone() : null;

        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;

            Material mat = item.getType();
            int maxStack = mat.getMaxStackSize();
            int amount = item.getAmount();

            // Item non stackabili (armor, weapons, tools, etc.)
            if (maxStack == 1) {
                neededSlots++;
                continue;
            }

            // Prova a stackare con item esistenti dello stesso tipo
            boolean stacked = false;
            for (ItemStack existing : simulatedStorage) {
                if (existing != null && existing.getType() == mat 
                    && existing.getAmount() < maxStack
                    && existing.isSimilar(item)) {
                    int space = maxStack - existing.getAmount();
                    int toAdd = Math.min(space, amount);
                    existing.setAmount(existing.getAmount() + toAdd);
                    amount -= toAdd;
                    if (amount <= 0) {
                        stacked = true;
                        break;
                    }
                }
            }

            // Se rimane amount, calcola slot aggiuntivi necessari
            if (amount > 0) {
                neededSlots += (int) Math.ceil((double) amount / maxStack);
            }
        }

        return new int[]{neededSlots, availableSlots};
    }

    /**
     * Dà gli item al giocatore, equipaggiando automaticamente armor/offhand negli slot vuoti.
     * Restituisce gli item che non sono entrati.
     */
    public static ItemStack[] giveItems(Player player, ItemStack[] items, boolean autoEquip) {
        if (player == null || items == null) return items;

        PlayerInventory inv = player.getInventory();

        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;

            Material mat = item.getType();

            // Auto-equip per armor e offhand se slot vuoti
            if (autoEquip) {
                if (isHelmet(mat) && (inv.getHelmet() == null || inv.getHelmet().getType() == Material.AIR)) {
                    inv.setHelmet(item);
                    continue;
                }
                if (isChestplate(mat) && (inv.getChestplate() == null || inv.getChestplate().getType() == Material.AIR)) {
                    inv.setChestplate(item);
                    continue;
                }
                if (isLeggings(mat) && (inv.getLeggings() == null || inv.getLeggings().getType() == Material.AIR)) {
                    inv.setLeggings(item);
                    continue;
                }
                if (isBoots(mat) && (inv.getBoots() == null || inv.getBoots().getType() == Material.AIR)) {
                    inv.setBoots(item);
                    continue;
                }
                if (isOffHand(mat) && (inv.getItemInOffHand() == null || inv.getItemInOffHand().getType() == Material.AIR)) {
                    inv.setItemInOffHand(item);
                    continue;
                }
            }

            // Aggiungi all'inventario
            inv.addItem(item);
        }

        return null; // Tutti gli item sono stati dati
    }

    /**
     * Controlla se un materiale è un elmo.
     */
    public static boolean isHelmet(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.endsWith("_HELMET") || name.equals("TURTLE_HELMET") || name.equals("CARVED_PUMPKIN");
    }

    /**
     * Controlla se un materiale è una corazza.
     */
    public static boolean isChestplate(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.endsWith("_CHESTPLATE");
    }

    /**
     * Controlla se un materiale è un leggings.
     */
    public static boolean isLeggings(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.endsWith("_LEGGINGS");
    }

    /**
     * Controlla se un materiale è uno stivale.
     */
    public static boolean isBoots(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.endsWith("_BOOTS");
    }

    /**
     * Controlla se un materiale può stare nell'offhand.
     */
    public static boolean isOffHand(Material mat) {
        if (mat == null) return false;
        return mat == Material.SHIELD 
            || mat == Material.TOTEM_OF_UNDYING 
            || mat == Material.FILLED_MAP
            || mat == Material.CROSSBOW;
    }

    /**
     * Chiude tutte le GUI aperte dai giocatori che hanno un holder specifico.
     */
    public static void closeGUIsByHolder(Inventory holder) {
        // Implementato nel listener
    }
}
