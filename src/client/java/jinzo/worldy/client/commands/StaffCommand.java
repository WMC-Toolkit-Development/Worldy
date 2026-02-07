package jinzo.worldy.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jinzo.worldy.client.Models.StaffState;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class StaffCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("staff")
                .requires(source -> StaffState.get())
                .executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;
                    client.player.networkHandler.sendChatCommand("ch j staff");
                    return 1;
                });
    }
}
