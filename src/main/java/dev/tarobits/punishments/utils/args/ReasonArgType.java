package dev.tarobits.punishments.utils.args;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;

import javax.annotation.Nonnull;

public class ReasonArgType extends SingleArgumentType<String> {
    public ReasonArgType() {
        super(Message.raw("Reason"), "The reason why you are performing this action.", "\"Hacking\"", "\"Doxxing\"");
    }

    @Override
    public String parse(@Nonnull String str, @Nonnull ParseResult parseResult) {
        return str;
    }
}
