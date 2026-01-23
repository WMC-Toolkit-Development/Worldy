package jinzo.worldy.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "worldy")
public class WorldyConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    public GeneralSettings general = new GeneralSettings();
    @ConfigEntry.Gui.CollapsibleObject
    public WaypointSettings waypoint = new WaypointSettings();
    @ConfigEntry.Gui.CollapsibleObject
    public StaffListSettings staffList = new StaffListSettings();

    public static class GeneralSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean displayLogoutMessages = true;
        @ConfigEntry.Gui.Tooltip
        public boolean autoOpenVoteLinks = false;
    }

    public static class WaypointSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 40)
        public int pathLength = 10;
    }

    public static class StaffListSettings {
        public boolean fetchOnLogin = true;
    }

    @Override
    public void validatePostLoad() {
        if (waypoint.pathLength < 1) waypoint.pathLength = 1;
        if (waypoint.pathLength > 40) waypoint.pathLength = 40;
    }
}
