package jinzo.worldy.client.Models;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Staff {
    private final String displayName;
    private final UUID uuid;
    private final boolean isUnknown;

    public Staff(@NotNull String displayName, @NotNull UUID uuid, boolean isUnknown) {
        this.displayName = displayName;
        this.uuid = uuid;
        this.isUnknown = isUnknown;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public @NotNull UUID getUuid() {
        return uuid;
    }

    public boolean isUnknown() {
        return isUnknown;
    }

    @Override
    public @NotNull String toString() {
        return "Staff{" +
                "displayName='" + displayName + '\'' +
                ", uuid=" + uuid +
                ", isUnknown=" + isUnknown +
                '}';
    }
}
