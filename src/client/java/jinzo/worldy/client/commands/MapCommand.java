package jinzo.worldy.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jinzo.worldy.client.utils.CommandHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.net.URI;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MapCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("map")
                .executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    // Create a clickable message using static factory methods
                    Text message = Text.literal("Click here to open the map.")
                            .setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://map.worldmc.org/")))
                                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Open Dynmap")))
                            );

                    // Send the message to the player
                    CommandHelper.sendMessage(message);

                    return 1;
                })
                .then(literal("here")
                        .executes(ctx -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player == null) return 0;

                            Vec3d position = client.player.getEntityPos();

                            // Create a clickable message using static factory methods
                            Text message = Text.literal("Click here to open the map on your position.")
                                    .setStyle(Style.EMPTY
                                            .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://map.worldmc.org/?worldname=world&mapname=flat&zoom=0&x="+position.x+"&y=64&z="+position.z)))
                                            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Open Dynmap on your position")))
                                    );

                            // Send the message to the player
                            CommandHelper.sendMessage(message);

                            return 1;
                        })
                );
    }
}
