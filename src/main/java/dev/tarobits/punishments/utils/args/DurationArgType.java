package dev.tarobits.punishments.utils.args;

import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import dev.tarobits.punishments.utils.TimeFormat;

public class DurationArgType extends SingleArgumentType<TimeFormat> {
    public DurationArgType() {
        super("Duration", "A duration of time\ny: years\nm: months\nd: days\nh: hours\nmin: minutes", "1h", "5d1h", "7m1d8h", "1y2m30d8h", "4h30min");
    }

    @Override
    public TimeFormat parse(String input, ParseResult result) {
        return TimeFormat.fromDurationString(input);
    }
}
