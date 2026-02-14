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
import net.minecraft.util.math.Vec3d;

import java.net.URI;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MaphereCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("maphere")
                .executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    Vec3d position = client.player.getEntityPos();

                    Text message = Text.translatable("command.worldy.maphere.title")
                            .setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://map.worldmc.org/?worldname=world&mapname=flat&zoom=0&x="+(int)position.x+"&y=64&z="+(int)position.z)))
                                    .withHoverEvent(new HoverEvent.ShowText(Text.translatable("command.worldy.maphere.title")))
                                    .withColor(Formatting.GRAY)
                            );

                    CommandHelper.sendMessage(message);

                    return 1;
                });
    }
}
