package jinzo.worldy.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class CommandHelper {

    public static void sendError(@NotNull String message) {
        sendMessage(
                Text.literal(message)
                        .setStyle(Style.EMPTY.withColor(Formatting.RED))
        );
    }

    public static void sendWarning(@NotNull String message) {
        sendMessage(
                Text.literal(message)
                        .setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
        );
    }

    public static void sendMessage(@NotNull String message) {
        sendMessage(Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
    }

    public static void sendMessage(@NotNull Text message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        Text prefix = Text.literal("[Worldy]")
                .setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA));

        Text separator = Text.literal(" ");

        player.sendMessage(
                Text.empty()
                        .append(prefix)
                        .append(separator)
                        .append(message),
                false
        );
    }
}
