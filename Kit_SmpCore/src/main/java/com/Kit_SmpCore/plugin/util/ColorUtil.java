package com.Kit_SmpCore.plugin.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility per la gestione dei colori e gradienti nei messaggi.
 * Supporta codici & e hex &#RRGGBB.
 */
public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("&([0-9a-fk-orx])", Pattern.CASE_INSENSITIVE);

    /**
     * Applica un gradiente di colore a un testo.
     * @param text Il testo da colorare
     * @param from Colore iniziale (hex #RRGGBB)
     * @param to Colore finale (hex #RRGGBB)
     * @return Testo con gradiente applicato
     */
    public static String gradient(String text, String from, String to) {
        if (text == null || text.isEmpty()) return "";

        Color startColor = parseHex(from);
        Color endColor = parseHex(to);

        if (startColor == null || endColor == null) {
            return translateColors(text);
        }

        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == ' ') {
                result.append(' ');
                continue;
            }

            double ratio = (double) i / Math.max(chars.length - 1, 1);
            int red = (int) (startColor.getRed() + ratio * (endColor.getRed() - startColor.getRed()));
            int green = (int) (startColor.getGreen() + ratio * (endColor.getGreen() - startColor.getGreen()));
            int blue = (int) (startColor.getBlue() + ratio * (endColor.getBlue() - startColor.getBlue()));

            result.append(ChatColor.of(new Color(red, green, blue))).append(c);
        }

        return result.toString();
    }

    /**
     * Traduce i codici colore (& e &#RRGGBB) in ChatColor.
     * @param text Testo con codici colore
     * @return Testo con colori applicati
     */
    public static String translateColors(String text) {
        if (text == null) return "";

        // Prima gestisce gli hex &#RRGGBB
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            try {
                Color color = Color.decode("#" + hex);
                matcher.appendReplacement(buffer, ChatColor.of(color).toString());
            } catch (Exception e) {
                matcher.appendReplacement(buffer, "");
            }
        }
        matcher.appendTail(buffer);
        text = buffer.toString();

        // Poi gestisce i codici & standard
        matcher = AMPERSAND_PATTERN.matcher(text);
        buffer = new StringBuffer();
        while (matcher.find()) {
            String code = matcher.group(1);
            ChatColor chatColor = ChatColor.getByChar(code.charAt(0));
            if (chatColor != null) {
                matcher.appendReplacement(buffer, chatColor.toString());
            } else {
                matcher.appendReplacement(buffer, "&" + code);
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Applica i colori a una lista di stringhe.
     * @param lines Lista di stringhe da colorare
     * @return Nuova lista con colori applicati
     */
    public static List<String> colorList(List<String> lines) {
        if (lines == null) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(translateColors(line));
        }
        return result;
    }

    /**
     * Crea una barra di progresso con caratteri unicode.
     * @param current Valore attuale
     * @param max Valore massimo
     * @param totalBars Numero totale di barre
     * @param filledChar Carattere per le barre piene
     * @param emptyChar Carattere per le barre vuote
     * @return Barra di progresso formattata
     */
    public static String progressBar(double current, double max, int totalBars, String filledChar, String emptyChar) {
        if (max <= 0) return emptyChar.repeat(totalBars);

        int filledBars = (int) Math.round((current / max) * totalBars);
        int emptyBars = totalBars - filledBars;

        return filledChar.repeat(Math.max(0, filledBars)) + emptyChar.repeat(Math.max(0, emptyBars));
    }

    /**
     * Parse di un colore hex (#RRGGBB o RRGGBB).
     * @param hex Stringa hex del colore
     * @return Oggetto Color o null se invalido
     */
    private static Color parseHex(String hex) {
        if (hex == null) return null;
        hex = hex.replace("#", "").trim();
        if (hex.length() != 6) return null;
        try {
            return Color.decode("#" + hex);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Assicura che una stringa inizi con ChatColor.RESET per evitare l'italic.
     * @param text Testo da processare
     * @return Testo con RESET prefix
     */
    public static String resetPrefix(String text) {
        if (text == null || text.isEmpty()) return "";
        if (text.startsWith(ChatColor.RESET.toString())) return text;
        return ChatColor.RESET + text;
    }
}
