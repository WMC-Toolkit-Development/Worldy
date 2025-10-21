package jinzo.worldy.client.Models;

import java.util.UUID;

public final class Staff {
    private final String displayName;
    private final UUID uuid;
    private final boolean isUnknown;

    public Staff(String displayName, UUID uuid, boolean isUnknown) {
        this.displayName = displayName;
        this.uuid = uuid;
        this.isUnknown = isUnknown;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isUnknown() {
        return isUnknown;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "displayName='" + displayName + '\'' +
                ", uuid=" + uuid +
                ", isUnknown=" + isUnknown +
                '}';
    }
}
