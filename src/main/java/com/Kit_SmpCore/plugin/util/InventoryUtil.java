package com.Kit_SmpCore.plugin.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility per operazioni sugli inventory.
 */
public class InventoryUtil {
    
    /**
     * Conta quanti slot liberi ha un player (inventory + armor slots).
     * Considera gli stack massimi per calcolare se nuovi item entrano.
     * @param player Il player
     * @return Numero di slot effettivamente disponibili
     */
    public static int countFreeSlots(@NotNull Player player) {
        Inventory inv = player.getInventory();
        int freeSlots = 0;
        
        // Contiamo slot vuoti nell'inventario principale (36 slot: 0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                freeSlots++;
            }
        }
        
        // Armor slots e offhand non contano come "free" per items generici
        // ma li consideriamo per l'equipaggiamento diretto
        
        return freeSlots;
    }
    
    /**
     * Simula se un insieme di ItemStack entra nell'inventario del player.
     * NON modifica l'inventario reale.
     * @param player Il player
     * @param items Gli item da aggiungere (clonati internamente)
     * @return true se tutti gli item entrano, false altrimenti
     */
    public static boolean canFitItems(@NotNull Player player, @NotNull ItemStack[] items) {
        // Cloniamo l'inventario per la simulazione
        Inventory fakeInv = Bukkit.createInventory(null, 36);
        
        // Copiamo gli item esistenti
        for (int i = 0; i < 36; i++) {
            ItemStack original = player.getInventory().getItem(i);
            if (original != null && original.getType() != Material.AIR) {
                fakeInv.setItem(i, original.clone());
            }
        }
        
        // Proviamo ad aggiungere ogni item
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            ItemStack toAdd = item.clone();
            Map<Integer, ItemStack> remaining = fakeInv.addItem(toAdd);
            
            // Se rimane qualcosa che non entra, ritorniamo false
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calcola quanti slot servono per un array di ItemStack.
     * @param items Gli item da analizzare
     * @return Numero di slot necessari
     */
    public static int countRequiredSlots(@NotNull ItemStack[] items) {
        Map<Material, Integer> materialAmounts = new HashMap<>();
        
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            Material type = item.getType();
            int maxStack = type.getMaxStackSize();
            int amount = item.getAmount();
            
            // Per items non stackabili, ogni item occupa un slot
            if (maxStack <= 1) {
                materialAmounts.merge(type, amount, Integer::sum);
            } else {
                materialAmounts.merge(type, amount, Integer::sum);
            }
        }
        
        int slots = 0;
        for (Map.Entry<Material, Integer> entry : materialAmounts.entrySet()) {
            Material type = entry.getKey();
            int totalAmount = entry.getValue();
            int maxStack = type.getMaxStackSize();
            
            if (maxStack <= 1) {
                slots += totalAmount;
            } else {
                slots += (int) Math.ceil((double) totalAmount / maxStack);
            }
        }
        
        return slots;
    }
    
    /**
     * Trova il primo slot libero nell'inventario.
     * @param inventory L'inventario
     * @return Index del slot libero, o -1 se pieno
     */
    public static int findFirstEmptySlot(@NotNull Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Controlla se un slot è vuoto.
     */
    public static boolean isSlotEmpty(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
    
    /**
     * Da' degli item a un player, dropando a terra quelli che non entrano.
     * @param player Il player
     * @param items Gli item da dare
     * @return true se tutti entrano, false se alcuni sono stati dropati
     */
    public static boolean giveItems(@NotNull Player player, @NotNull ItemStack[] items) {
        boolean allFit = true;
        
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);
            if (!remaining.isEmpty()) {
                allFit = false;
                // Drop quello che non entra
                for (ItemStack drop : remaining.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }
        
        return allFit;
    }
}
