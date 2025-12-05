package jinzo.worldy.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class GlobalCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("global")
                .executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;
                    client.player.networkHandler.sendChatCommand("ch j global");
                    return 1;
                });
    }
}
