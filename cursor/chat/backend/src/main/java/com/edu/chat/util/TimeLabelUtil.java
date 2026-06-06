package com.edu.chat.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TimeLabelUtil {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA).withZone(ZONE);
    private static final DateTimeFormatter FULL_FMT =
            DateTimeFormatter.ofPattern("今天 HH:mm", Locale.CHINA).withZone(ZONE);

    private TimeLabelUtil() {
    }

    public static String relative(Instant instant) {
        if (instant == null) {
            return "";
        }
        Duration d = Duration.between(instant, Instant.now());
        long minutes = d.toMinutes();
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + "分钟前";
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours + "小时前";
        }
        return TIME_FMT.format(instant);
    }

    public static String messageTime(Instant instant) {
        if (instant == null) {
            return "";
        }
        return FULL_FMT.format(instant);
    }
}
