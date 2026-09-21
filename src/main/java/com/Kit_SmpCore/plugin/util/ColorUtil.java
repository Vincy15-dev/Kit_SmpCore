package com.Kit_SmpCore.plugin.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility per la gestione dei colori e gradienti nei messaggi.
 * Supporta codici &, hex #RRGGBB e gradienti tra due colori.
 */
public class ColorUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern AMP_PATTERN = Pattern.compile("&([0-9a-fk-orx])", Pattern.CASE_INSENSITIVE);
    
    /**
     * Traduce tutti i codici colore in un stringa (sia & che &#RRGGBB).
     * @param text Il testo da colorare
     * @return Il testo con i codici colore tradotti
     */
    @NotNull
    public static String colorize(@NotNull String text) {
        if (text == null || text.isEmpty()) return "";
        
        // Prima gestiamo gli hex &#RRGGBB
        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        StringBuffer hexResult = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexResult, ChatColor.of("#" + hexMatcher.group(1)).toString());
        }
        hexMatcher.appendTail(hexResult);
        text = hexResult.toString();
        
        // Poi gestiamo i codici &
        Matcher ampMatcher = AMP_PATTERN.matcher(text);
        StringBuffer ampResult = new StringBuffer();
        while (ampMatcher.find()) {
            char c = ampMatcher.group(1).toLowerCase().charAt(0);
            ChatColor chatColor;
            switch (c) {
                case '0': case '1': case '2': case '3': case '4': 
                case '5': case '6': case '7': case '8': case '9':
                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                    chatColor = ChatColor.getByChar(c);
                    break;
                case 'k': chatColor = ChatColor.MAGIC; break;
                case 'l': chatColor = ChatColor.BOLD; break;
                case 'm': chatColor = ChatColor.STRIKETHROUGH; break;
                case 'n': chatColor = ChatColor.UNDERLINE; break;
                case 'o': chatColor = ChatColor.ITALIC; break;
                case 'r': chatColor = ChatColor.RESET; break;
                case 'x': chatColor = ChatColor.BLACK; break; // fallback
                default: chatColor = ChatColor.WHITE; break;
            }
            ampMatcher.appendReplacement(ampResult, chatColor != null ? chatColor.toString() : "");
        }
        ampMatcher.appendTail(ampResult);
        
        return ampResult.toString();
    }
    
    /**
     * Applica un gradiente di colore a un testo.
     * @param text Il testo da colorare
     * @param fromHex Colore iniziale (#RRGGBB)
     * @param toHex Colore finale (#RRGGBB)
     * @return Il testo con gradiente applicato
     */
    @NotNull
    public static String gradient(@NotNull String text, @NotNull String fromHex, @NotNull String toHex) {
        if (text == null || text.isEmpty()) return "";
        
        Color fromColor = parseHex(fromHex);
        Color toColor = parseHex(toHex);
        
        StringBuilder result = new StringBuilder();
        int len = text.length();
        
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            
            // Interpolazione lineare del colore
            float ratio = len > 1 ? (float) i / (len - 1) : 0;
            int red = interpolate(fromColor.getRed(), toColor.getRed(), ratio);
            int green = interpolate(fromColor.getGreen(), toColor.getGreen(), ratio);
            int blue = interpolate(fromColor.getBlue(), toColor.getBlue(), ratio);
            
            result.append(ChatColor.of(String.format("#%02X%02X%02X", red, green, blue)));
            result.append(c);
        }
        
        result.append(ChatColor.RESET);
        return result.toString();
    }
    
    /**
     * Crea una barra di progresso con caratteri unicode.
     * @param current Valore attuale
     * @param max Valore massimo
     * @param totalBars Numero totale di barre (es. 5)
     * @param filledColor Colore per le barre piene
     * @param emptyColor Colore per le barre vuote
     * @return La stringa della barra di progresso
     */
    @NotNull
    public static String progressBar(int current, int max, int totalBars, 
                                     @NotNull String filledColor, @NotNull String emptyColor) {
        if (max <= 0) max = 1;
        float ratio = Math.min((float) current / max, 1.0f);
        int filledBars = Math.round(ratio * totalBars);
        int emptyBars = totalBars - filledBars;
        
        StringBuilder sb = new StringBuilder();
        sb.append(colorize(filledColor));
        for (int i = 0; i < filledBars; i++) {
            sb.append("▰");
        }
        sb.append(colorize(emptyColor));
        for (int i = 0; i < emptyBars; i++) {
            sb.append("▱");
        }
        sb.append(ChatColor.RESET);
        
        return sb.toString();
    }
    
    /**
     * Applica ChatColor.RESET all'inizio di ogni linea per prevenire l'italic.
     * @param lore Lista di stringhe per la lore
     * @return Nuova lista con RESET aggiunto
     */
    @NotNull
    public static List<String> resetLore(@NotNull List<String> lore) {
        List<String> result = new ArrayList<>();
        for (String line : lore) {
            result.add(ChatColor.RESET + colorize(line));
        }
        return result;
    }
    
    /**
     * Parsea un colore hex (#RRGGBB o RRGGBB).
     * @param hex Il colore in formato hex
     * @return Oggetto Color
     */
    @NotNull
    private static Color parseHex(@NotNull String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return new Color(Integer.parseInt(hex, 16));
    }
    
    /**
     * Interpola linearmente tra due valori.
     */
    private static int interpolate(int from, int to, float ratio) {
        return Math.round(from + (to - from) * ratio);
    }
}
