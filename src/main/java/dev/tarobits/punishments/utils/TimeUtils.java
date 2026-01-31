package dev.tarobits.punishments.utils;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.logger.HytaleLogger;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class TimeUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static String instantAsDate(Instant inst) {
        return new SimpleDateFormat("d.MM.y").format(Date.from(inst));
    }

    public static String instantAsMinimalDateTime(Instant inst) {
        return new SimpleDateFormat("d.MM HH:mm").format(Date.from(inst));
    }

    public static String instantAsDateTime(Instant inst) {
        return new SimpleDateFormat("d.MM.y HH:mm").format(Date.from(inst));
    }

    public static String instantAsRelativeDuration(Instant inst) {
        return StringUtil.humanizeTime(Duration.between(Instant.now(), inst));
    }

    public static String durationAsAbsoluteDuration(Duration duration) {
        return StringUtil.humanizeTime(duration);
    }

    public static String instantAsRelativeAndAbsoluteDuration(
            Instant inst,
            Duration duration
    ) {
        return TimeUtils.instantAsRelativeDuration(inst)
                .replaceAll("( \\d+m)", "") + " (" + TimeUtils.durationAsAbsoluteDuration(duration)
                .replaceAll("( \\d+m)", "") + ")";
    }
}


