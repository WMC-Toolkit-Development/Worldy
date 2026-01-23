package jinzo.worldy.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jinzo.worldy.client.utils.CommandHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class IcemapCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("icemap")
                .executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    // Create a clickable message using static factory methods
                    Text message = Text.literal("Click here to open the ice highway map.")
                            .setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://nokteholda.github.io/WorldMC-Ice-Highways-Map/")))
                                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Opens the ice highway map in your browser")))
                            );

                    // Send the message to the player
                    CommandHelper.sendMessage(message);

                    return 1;
                });
    }
}
