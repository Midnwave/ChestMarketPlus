package com.blockforge.chestmarketplus.util;

public final class TimeUtils {

    private TimeUtils() {}

    public static String formatDuration(long seconds) {
        if (seconds < 0) return "Expired";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0 || sb.isEmpty()) sb.append(minutes).append("m");

        return sb.toString().trim();
    }

    public static String formatTimestamp(long unixSeconds) {
        java.time.Instant instant = java.time.Instant.ofEpochSecond(unixSeconds);
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
