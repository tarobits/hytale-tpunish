package dev.tarobits.punishments.utils.ui;

import dev.tarobits.punishments.utils.punishment.Punishment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HistoryStat {
    public Integer numberOfPunishments;
    @Nullable
    public Punishment latestPunishment;

    public HistoryStat(
            @Nonnull Integer numberOfPunishments,
            @Nullable Punishment latestPunishment
    ) {
        this.numberOfPunishments = numberOfPunishments;
        this.latestPunishment = latestPunishment;
    }
}
