package jinzo.worldy.client.utils;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import me.shedaniel.autoconfig.AutoConfig;
import jinzo.worldy.client.WorldyConfig;
import org.jetbrains.annotations.NotNull;

public class WaypointManager {
    private static volatile Vec3d target = null;
    private static volatile boolean active = false;

    private static volatile int tickCounter = 0;

    private WaypointManager() {}

    public static void setWaypoint(Vec3d t) {
        target = t;
        active = t != null;
        tickCounter = 0;
    }

    public static int clearWaypoint() {
        target = null;
        active = false;
        tickCounter = 0;
        CommandHelper.sendMessage("Waypoint cleared");
        return 1;
    }

    public static int infoWaypoint() {
        CommandHelper.sendMessage(target == null ?
                "No waypoint saved" :
                String.format("Waypoint currently set to %.2f, %.2f, %.2f.", target.x, target.y, target.z));
        return 1;
    }


    public static boolean isActive() {
        return active && target != null;
    }

    public static void spawnPathParticles(int maxBlocks) {
        if (!isActive()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        tickCounter++;
        if (tickCounter % 4 != 0) return;

        Vec3d pos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Vec3d start = pos.add(0, 0.3, 0);
        Vec3d dir = target.subtract(start);
        double distance = dir.length();
        if (distance <= 0.001) return;

        Vec3d unit = dir.normalize();
        ClientWorld world = mc.world;

        int steps = Math.min(maxBlocks, (int) Math.ceil(distance));
        double spacing = 0.8;
        for (int i = 1; i <= steps; i++) {
            double px = start.x + unit.x * i * spacing;
            double py = start.y + unit.y * i * spacing;
            double pz = start.z + unit.z * i * spacing;

            world.addParticleClient(ParticleTypes.CRIT, px, py, pz, 0.0, 0.01, 0.0);
        }

        if (distance <= steps * spacing + 0.5) {
            world.addParticleClient(ParticleTypes.SOUL_FIRE_FLAME,
                    target.x, target.y + 0.1, target.z, 0.0, 0.02, 0.0);
        }
    }

    public static void setLastDeath(Vec3d deathPos) {
        if (deathPos == null) return;
        saveLastDeathToConfig(deathPos);
    }

    public static Vec3d getLastDeath() {
        WorldyConfig cfg = AutoConfig.getConfigHolder(WorldyConfig.class).getConfig();
        return new Vec3d(cfg.waypoint.lastDeathX, cfg.waypoint.lastDeathY, cfg.waypoint.lastDeathZ);
    }

    private static void saveLastDeathToConfig(Vec3d deathPos) {
        try {
            WorldyConfig cfg = AutoConfig.getConfigHolder(WorldyConfig.class).getConfig();
            cfg.waypoint.lastDeathX = (int)deathPos.x;
            cfg.waypoint.lastDeathY = (int)deathPos.y;
            cfg.waypoint.lastDeathZ = (int)deathPos.z;
            AutoConfig.getConfigHolder(WorldyConfig.class).save();
        } catch (Throwable t) {
            System.err.println("Failed to save last death to config: " + t.getMessage());
        }
    }

    public static int setWaypointHere() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return 0;
        }

        Vec3d pos = new Vec3d(player.getX(), player.getY(), player.getZ());
        double x = centerOfBlock(pos.x);
        double y = centerOfBlock(pos.y);
        double z = centerOfBlock(pos.z);

        Vec3d target = new Vec3d(x, y, z);
        WaypointManager.setWaypoint(target);

        CommandHelper.sendMessage(String.format("Waypoint set to your position (%.2f, %.2f, %.2f).", x, y, z));
        return 1;
    }

    public static int setWaypointToDeath() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return 0;
        }

        Vec3d lastDeath = WaypointManager.getLastDeath();

        double x = centerOfBlock(lastDeath.x);
        double y = centerOfBlock(lastDeath.y);
        double z = centerOfBlock(lastDeath.z);

        Vec3d target = new Vec3d(x, y, z);
        WaypointManager.setWaypoint(target);

        CommandHelper.sendMessage(String.format("Waypoint set to last death (%.2f, %.2f, %.2f).", x, y, z));
        return 1;
    }

    public static int setWaypointFromArgs(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return 0;
        }

        String input = ctx.getInput();
        String after = input.trim().substring("waypoint set".length()).trim();
        String[] parts = after.split("\\s+");
        if (parts.length < 3) {
            CommandHelper.sendError("Usage: /waypoint set <x|~> <y|~> <z|~>");
            return 0;
        }

        Vec3d base = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Vec3d eye = mc.player.getCameraPosVec(1.0F);

        double x = parseCoordinate(parts[0], base.x, eye.x);
        double y = parseCoordinate(parts[1], base.y, eye.y);
        double z = parseCoordinate(parts[2], base.z, eye.z);

        x = centerOfBlock(x);
        y = centerOfBlock(y);
        z = centerOfBlock(z);

        Vec3d target = new Vec3d(x, y, z);
        WaypointManager.setWaypoint(target);

        CommandHelper.sendMessage(String.format("Waypoint set to block center (%.2f, %.2f, %.2f). Showing next 10 blocks.", x, y, z));
        return 1;
    }

    public static double centerOfBlock(double coord) {
        return Math.floor(coord) + 0.5;
    }

    public static double parseCoordinate(String token, double relativeFeet, double relativeEye) {
        token = token.trim();
        if (token.startsWith("~")) {
            if (token.length() == 1) {
                return relativeFeet;
            } else {
                String off = token.substring(1);
                try {
                    double val = Double.parseDouble(off);
                    return relativeFeet + val;
                } catch (NumberFormatException e) {
                    return relativeFeet;
                }
            }
        } else {
            try {
                return Double.parseDouble(token);
            } catch (NumberFormatException e) {
                return relativeFeet;
            }
        }
    }
}
