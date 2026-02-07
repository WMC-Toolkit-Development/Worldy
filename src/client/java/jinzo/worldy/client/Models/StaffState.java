package jinzo.worldy.client.Models;

public final class StaffState {
    private static volatile boolean isStaff = false;

    public static boolean get() {
        return isStaff;
    }

    public static void set(boolean value) {
        isStaff = value;
    }
}