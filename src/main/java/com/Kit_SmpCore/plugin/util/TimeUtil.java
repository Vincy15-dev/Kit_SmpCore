package com.Kit_SmpCore.plugin.util;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Utility per la formattazione del tempo e dei cooldown.
 */
public class TimeUtil {
    
    /**
     * Formatta un tempo in millisecondi in una stringa leggibile.
     * @param millis Millisecondi rimanenti
     * @return Stringa formattata (es. "1g 4h 32m")
     */
    @NotNull
    public static String formatCooldown(long millis) {
        if (millis <= 0) return "0s";
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        
        if (days > 0) {
            return days + "g " + hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + " secondi";
        }
    }
    
    /**
     * Calcola la percentuale di completamento di un cooldown.
     * @param elapsed Tempo trascorso in ms
     * @param total Tempo totale in ms
     * @return Percentuale (0-100)
     */
    public static int getCooldownProgress(long elapsed, long total) {
        if (total <= 0) return 100;
        int progress = (int) ((elapsed * 100) / total);
        return Math.min(Math.max(progress, 0), 100);
    }
    
    /**
     * Converte secondi in millisecondi.
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000L;
    }
    
    /**
     * Converte minuti in millisecondi.
     */
    public static long minutesToMillis(long minutes) {
        return minutes * 60L * 1000L;
    }
    
    /**
     * Converte ore in millisecondi.
     */
    public static long hoursToMillis(long hours) {
        return hours * 60L * 60L * 1000L;
    }
    
    /**
     * Converte giorni in millisecondi.
     */
    public static long daysToMillis(long days) {
        return days * 24L * 60L * 60L * 1000L;
    }
}
