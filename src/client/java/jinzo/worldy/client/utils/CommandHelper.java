package jinzo.worldy.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CommandHelper {

    public static void sendError(@NotNull String message) {
        sendMessage("§c" + message);
    }

    public static void sendWarning(@NotNull String message) {
        sendMessage("§e" + message);
    }

    public static void sendMessage(@NotNull String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        player.sendMessage(Text.literal("§3[Worldy]§7 " + message), false);
    }
}
