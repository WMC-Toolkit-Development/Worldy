package jinzo.worldy.client.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import jinzo.worldy.client.models.DeathTracker;
import jinzo.worldy.client.utils.CommandHelper;
import jinzo.worldy.client.utils.WaypointManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WaypointCommand {

    public static void register() {
        // Ensure death tracker is running so /waypoint death has data
        DeathTracker.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("waypoint")
                            .executes((ctx) -> {
                                CommandHelper.sendError("Not enough arguments");
                                return 0;
                            })
                            .then(literal("clear").executes(ctx -> WaypointManager.clearWaypoint()))
                            .then(literal("here").executes(ctx -> WaypointManager.setWaypointHere()))
                            .then(literal("death").executes(ctx -> WaypointManager.setWaypointToDeath()))
                            .then(literal("info").executes(ctx -> WaypointManager.infoWaypoint()))
                            .then(literal("save")
                                    .then(argument("name", StringArgumentType.greedyString())
                                            .executes(WaypointManager::saveWaypoint))
                            )
                            .then(literal("load")
                                    .executes((ctx) -> {
                                        CommandHelper.sendError("Usage: /waypoint load <name>");
                                        return 0;
                                    })
                                    .then(argument("name", StringArgumentType.greedyString())
                                            .suggests(WaypointManager::suggestWaypointNames)
                                            .executes(WaypointManager::loadWaypoint))
                            )
                            .then(literal("delete")
                                    .executes((ctx) -> {
                                        CommandHelper.sendError("Usage: /waypoint delete <name>");
                                        return 0;
                                    })
                                    .then(argument("name", StringArgumentType.greedyString())
                                            .suggests(WaypointManager::suggestWaypointNames)
                                            .executes(WaypointManager::deleteWaypoint))

                            )
                            .then(literal("set")
                                    .executes((ctx) -> {
                                        CommandHelper.sendError("Usage: /waypoint set <x> <y> <z>");
                                        return 0;
                                    })
                                    .then(argument("x", DoubleArgumentType.doubleArg())
                                            .executes((ctx) -> {
                                                CommandHelper.sendError("Usage: /waypoint set <x> <y> <z>");
                                                return 0;
                                            })
                                            .then(argument("y", DoubleArgumentType.doubleArg())
                                                    .executes((ctx) -> {
                                                        CommandHelper.sendError("Usage: /waypoint set <x> <y> <z>");
                                                        return 0;
                                                    })
                                                    .then(argument("z", DoubleArgumentType.doubleArg())
                                                            .executes(WaypointManager::setWaypointFromArgs)
                                                    )
                                            )
                                    )
                            )
            );
        });
    }
}
