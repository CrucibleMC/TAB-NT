package me.neznamy.tab.shared.platform.tablist;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

/**
 * TabList for platforms that use packets, which can send all changes in a single packet,
 * massively boosting performance. Overrides single methods and forwards them as bulk methods.
 */
public abstract class BulkUpdateTabList implements TabList {

    @Override
    public void removeEntry(@NonNull UUID entry) {
        removeEntries(Collections.singletonList(entry));
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable IChatBaseComponent displayName) {
        updateDisplayNames(Collections.singletonMap(entry, displayName));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        updateLatencies(Collections.singletonMap(entry, latency));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        updateGameModes(Collections.singletonMap(entry, gameMode));
    }

    @Override
    public void addEntry(@NonNull TabList.Entry entry) {
        addEntries(Collections.singletonList(entry));
    }
}
