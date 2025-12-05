package jinzo.worldy.client.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import jinzo.worldy.client.Models.DeathTracker;
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
                            .then(literal("clear").executes(ctx -> WaypointManager.clearWaypoint()))
                            .then(literal("here").executes(ctx -> WaypointManager.setWaypointHere()))
                            .then(literal("death").executes(ctx -> WaypointManager.setWaypointToDeath()))
                            .then(literal("info").executes(ctx -> WaypointManager.infoWaypoint()))
                            .then(literal("set")
                                    .then(argument("x", DoubleArgumentType.doubleArg())
                                            .then(argument("y", DoubleArgumentType.doubleArg())
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
