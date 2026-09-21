package com.Kit_SmpCore.plugin.kit;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un container (shulker box) all'interno di un kit.
 * Contiene una lista di KitItem che verranno inseriti nello shulker.
 */
public class KitContainer {
    
    private final Material material;  // Deve essere uno SHULKER_BOX
    private String customName;
    private final List<KitItem> contents = new ArrayList<>();
    
    public KitContainer(@NotNull Material material) {
        if (!material.name().endsWith("_SHULKER_BOX")) {
            throw new IllegalArgumentException("Material must be a shulker box: " + material);
        }
        this.material = material;
    }
    
    @NotNull
    public Material getMaterial() {
        return material;
    }
    
    @NotNull
    public String getCustomName() {
        return customName != null ? customName : "";
    }
    
    public void setCustomName(String customName) {
        this.customName = customName;
    }
    
    @NotNull
    public List<KitItem> getContents() {
        return contents;
    }
    
    public void addContent(@NotNull KitItem item) {
        this.contents.add(item);
    }
    
    /**
     * Controlla se il contenuto è valido (max 27 items).
     */
    public boolean isValidContent() {
        return contents.size() <= 27;
    }
}
