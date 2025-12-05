package jinzo.worldy.client.Models;

public class WaypointEntry {
    double x;
    double y;
    double z;
    String world;

    public WaypointEntry(double x, double y, double z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public double x() {return x;}
    public double y() {return y;}
    public double z() {return z;}
    public String world() {return world;}
}
