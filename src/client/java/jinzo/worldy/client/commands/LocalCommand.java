package jinzo.worldy.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class LocalCommand {
    private static int execute(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return 0;
        client.player.networkHandler.sendChatCommand("ch j local");
        return 1;
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("local").executes(LocalCommand::execute);
    }

    // Alias: /lc
    public static LiteralArgumentBuilder<FabricClientCommandSource> registerAlias() {
        return literal("lc").executes(LocalCommand::execute);
    }
}
