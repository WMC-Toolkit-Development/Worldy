package jinzo.worldy.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jinzo.worldy.client.Models.StaffState;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class XrayCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("xray")
                .requires(source -> StaffState.get())
                // Default case: no ore specified
                .executes(ctx -> {
                    executeCommand("deepslate_gold_ore");
                    return 1;
                })

                // Specific ore cases
                .then(literal("gold_ore")
                        .executes(ctx -> {
                            executeCommand("gold_ore");
                            return 1;
                        })
                )
                .then(literal("deepslate_gold_ore")
                        .executes(ctx -> {
                            executeCommand("deepslate_gold_ore");
                            return 1;
                        })
                )
                .then(literal("diamond_ore")
                        .executes(ctx -> {
                            executeCommand("diamond_ore");
                            return 1;
                        })
                )
                .then(literal("deepslate_diamond_ore")
                        .executes(ctx -> {
                            executeCommand("deepslate_diamond_ore");
                            return 1;
                        })
                );
    }

    public static void executeCommand(String oreType) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.getNetworkHandler() != null) {
            client.player.networkHandler.sendChatCommand("co l a:-block i:" + oreType + " t:1h");
        } else {
            System.out.println("[Muffin] Player or network handler not available.");
        }
    }
}

