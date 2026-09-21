package com.Kit_SmpCore.plugin.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility per la formattazione del tempo (cooldown, etc.).
 */
public class TimeUtil {

    /**
     * Formatta un tempo in millisecondi in stringa leggibile.
     * Esempio: "1g 4h 32m" o "5m 30s".
     */
    public static String formatTime(long millis) {
        if (millis <= 0) return "0s";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("g ");
            hours %= 24;
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
            minutes %= 60;
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
            seconds %= 60;
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }

    /**
     * Converte secondi in millisecondi.
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }

    /**
     * Converte giorni in millisecondi.
     */
    public static long daysToMillis(long days) {
        return TimeUnit.DAYS.toMillis(days);
    }

    /**
     * Converte ore in millisecondi.
     */
    public static long hoursToMillis(long hours) {
        return TimeUnit.HOURS.toMillis(hours);
    }

    /**
     * Calcola il tempo rimanente in millisecondi dato un timestamp di scadenza.
     * Restituisce 0 se scaduto.
     */
    public static long getRemainingTime(long expiryTimestamp) {
        long now = System.currentTimeMillis();
        long remaining = expiryTimestamp - now;
        return Math.max(0, remaining);
    }

    /**
     * Calcola la percentuale di cooldown completato.
     * @param elapsed Tempo trascorso
     * @param total Tempo totale
     * @return Percentuale 0.0 - 1.0
     */
    public static double getProgressPercent(long elapsed, long total) {
        if (total <= 0) return 1.0;
        return Math.min(1.0, Math.max(0.0, (double) elapsed / total));
    }

    /**
     * Controlla se un timestamp è nel futuro (possibile clock tampering).
     */
    public static boolean isTimestampInFuture(long timestamp, long toleranceMillis) {
        return timestamp > System.currentTimeMillis() + toleranceMillis;
    }
}
