package jinzo.worldy.client.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import jinzo.worldy.client.Models.WaypointEntry;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WaypointManager {
    private static volatile Vec3d target = null;
    private static volatile boolean active = false;

    private static volatile int tickCounter = 0;

    private WaypointManager() {}

    private static final Path WAYPOINTS_PATH = Path.of("config", "worldy_waypoints.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
        CommandHelper.sendMessage(isActive() ?
                String.format("Waypoint currently set to %.2f, %.2f, %.2f.", target.x, target.y, target.z) :
                "No waypoint saved");
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

    public static int saveWaypoint(CommandContext<FabricClientCommandSource> ctx) {
        if (!isActive()) {
            CommandHelper.sendError("No waypoint active");
            return 0;
        }
        String input = ctx.getInput();
        String after = input.trim().substring("waypoint save".length()).trim();
        if (after.isEmpty()) {
            CommandHelper.sendError("Usage: /waypoint save <name>");
            return 0;
        }
        String name = after; // allow spaces in name
        String worldStr = getSimpleWorldName();

        try {
            Map<String, WaypointEntry> waypoints = readWaypoints();

            if (waypoints.containsKey(name.toLowerCase())) {
                CommandHelper.sendError("A waypoint with that name already exists.");
                return 0;
            }

            WaypointEntry entry = new WaypointEntry(target.x, target.y, target.z, worldStr);
            waypoints.put(name.toLowerCase(), entry);
            writeWaypoints(waypoints);
            CommandHelper.sendMessage(String.format("Saved waypoint '%s' at %.2f, %.2f, %.2f (%s).", name, target.x, target.y, target.z, worldStr));
            return 1;
        } catch (Throwable t) {
            CommandHelper.sendError("Failed to save waypoint: " + t.getMessage());
            return 0;
        }
    }

    public static int deleteWaypoint(CommandContext<FabricClientCommandSource> ctx) {
        String input = ctx.getInput();
        String after = input.trim().substring("waypoint delete".length()).trim();
        if (after.isEmpty()) {
            CommandHelper.sendError("Usage: /waypoint delete <name>");
            return 0;
        }
        String name = after;

        try {
            Map<String, WaypointEntry> waypoints = readWaypoints();
            if (!waypoints.containsKey(name.toLowerCase())) {
                CommandHelper.sendError("No waypoint with that name exists.");
                return 0;
            }
            waypoints.remove(name.toLowerCase());
            writeWaypoints(waypoints);
            CommandHelper.sendMessage(String.format("Deleted waypoint '%s'.", name));
            return 1;
        } catch (Throwable t) {
            CommandHelper.sendError("Failed to delete waypoint: " + t.getMessage());
            return 0;
        }
    }

    public static int loadWaypoint(CommandContext<FabricClientCommandSource> ctx) {
        String input = ctx.getInput();
        String after = input.trim().substring("waypoint load".length()).trim();
        if (after.isEmpty()) {
            CommandHelper.sendError("Usage: /waypoint load <name>");
            return 0;
        }

        String name = after;

        try {
            Map<String, WaypointEntry> waypoints = readWaypoints();
            WaypointEntry entry = waypoints.get(name.toLowerCase());
            if (entry == null) {
                CommandHelper.sendError("No waypoint with that name exists.");
                return 0;
            }

            double x = centerOfBlock(entry.x());
            double y = centerOfBlock(entry.y());
            double z = centerOfBlock(entry.z());
            Vec3d pos = new Vec3d(x, y, z);
            setWaypoint(pos);
            CommandHelper.sendMessage(String.format("Loaded waypoint '%s' at %.2f, %.2f, %.2f (%s).", name, x, y, z, entry.world()));
            return 1;
        } catch (Throwable t) {
            CommandHelper.sendError("Failed to load waypoint: " + t.getMessage());
            return 0;
        }
    }

    private static @NotNull Map<String, WaypointEntry> readWaypoints() {
        if (!Files.exists(WAYPOINTS_PATH)) {
            return new HashMap<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(WAYPOINTS_PATH)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return new HashMap<>();
            JsonObject waypointsObj = root.has("waypoints") && root.get("waypoints").isJsonObject()
                    ? root.getAsJsonObject("waypoints")
                    : new JsonObject();

            Map<String, WaypointEntry> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> e : waypointsObj.entrySet()) {
                String key = e.getKey();
                JsonElement val = e.getValue();
                if (!val.isJsonObject()) continue;
                JsonObject obj = val.getAsJsonObject();
                int x = obj.has("x") ? obj.get("x").getAsInt() : 0;
                int y = obj.has("y") ? obj.get("y").getAsInt() : 0;
                int z = obj.has("z") ? obj.get("z").getAsInt() : 0;
                String world = obj.has("world") ? obj.get("world").getAsString() : "overworld";
                map.put(key, new WaypointEntry(x, y, z, world));
            }
            return map;
        } catch (IOException e) {
            System.err.println("Failed to read waypoints: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private static void writeWaypoints(@NotNull Map<String, WaypointEntry> waypoints) throws IOException {
        Files.createDirectories(WAYPOINTS_PATH.getParent());
        JsonObject root = new JsonObject();
        JsonObject waypointsObj = new JsonObject();
        for (Map.Entry<String, WaypointEntry> e : waypoints.entrySet()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", e.getValue().x());
            obj.addProperty("y", e.getValue().y());
            obj.addProperty("z", e.getValue().z());
            obj.addProperty("world", e.getValue().world());
            waypointsObj.add(e.getKey(), obj);
        }
        root.add("waypoints", waypointsObj);

        try (BufferedWriter writer = Files.newBufferedWriter(WAYPOINTS_PATH)) {
            GSON.toJson(root, writer);
        }
    }

    private static String getSimpleWorldName() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return "overworld";
        String key = world.getRegistryKey().getValue().toString().toLowerCase();
        if (key.contains("nether")) return "nether";
        if (key.contains("end")) return "end";
        return "overworld";
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
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return 0;
        }

        String input = ctx.getInput();
        String after = input.trim().substring("waypoint set".length()).trim();
        String[] parts = after.split("\\s+");
        if (parts.length < 3) {
            CommandHelper.sendError("Usage: /waypoint set <x|~> <y|~> <z|~>");
            return 0;
        }

        Vec3d base = new Vec3d(player.getX(), player.getY(), player.getZ());
        Vec3d eye = player.getCameraPosVec(1.0F);

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

    public static CompletableFuture<Suggestions> suggestWaypointNames(CommandContext<FabricClientCommandSource> ctx, SuggestionsBuilder builder) {
        for (String name : getWaypointNames()) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    }

    public static Set<String> getWaypointNames() {
        return readWaypoints().keySet();
    }
}
