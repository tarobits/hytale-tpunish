package dev.tarobits.punishments.ui;

import dev.tarobits.punishments.model.punishment.Punishment;

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
