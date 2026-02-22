package com.blockforge.chestmarketplus.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern MINI_COLOR_PATTERN = Pattern.compile("<(red|green|blue|yellow|gold|gray|white|aqua|dark_red|dark_green|dark_blue|dark_aqua|dark_gray|dark_purple|light_purple|black)>");
    private static final Pattern MINI_FORMAT_PATTERN = Pattern.compile("<(bold|italic|underlined|strikethrough|obfuscated)>");
    private static final Pattern MINI_RESET_PATTERN = Pattern.compile("<reset>");
    private static final Pattern CLOSE_TAG_PATTERN = Pattern.compile("</[^>]+>");

    private MessageUtils() {}

    public static String colorize(String message) {
        if (message == null) return "";

        // handle hex colors: <#aabbcc> -> §x§a§a§b§b§c§c
        Matcher hexMatcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            hexMatcher.appendReplacement(sb, replacement.toString());
        }
        hexMatcher.appendTail(sb);
        message = sb.toString();

        message = message.replace("<red>", "§c")
                .replace("<green>", "§a")
                .replace("<blue>", "§9")
                .replace("<yellow>", "§e")
                .replace("<gold>", "§6")
                .replace("<gray>", "§7")
                .replace("<white>", "§f")
                .replace("<aqua>", "§b")
                .replace("<dark_red>", "§4")
                .replace("<dark_green>", "§2")
                .replace("<dark_blue>", "§1")
                .replace("<dark_aqua>", "§3")
                .replace("<dark_gray>", "§8")
                .replace("<dark_purple>", "§5")
                .replace("<light_purple>", "§d")
                .replace("<black>", "§0");

        message = message.replace("<bold>", "§l")
                .replace("<italic>", "§o")
                .replace("<underlined>", "§n")
                .replace("<strikethrough>", "§m")
                .replace("<obfuscated>", "§k")
                .replace("<reset>", "§r");

        message = CLOSE_TAG_PATTERN.matcher(message).replaceAll("");

        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(colorize(message));
    }

    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(colorize(message));
    }
}
