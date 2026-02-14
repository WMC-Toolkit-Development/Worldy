package jinzo.worldy.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jinzo.worldy.client.WorldyClient;
import jinzo.worldy.client.utils.CommandHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class VoteCommand {

    private static final URI VOTE_URI_2 =
            URI.create("https://minecraft-mp.com/server/349046/vote/");

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("vote")
                .executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    String username = client.player.getName().getString();

                    URI voteUri1 = URI.create(
                            "https://minecraftservers.org/vote/678503?username=" + username
                    );

                    List<URI> voteUris = List.of(voteUri1, VOTE_URI_2);

                    if (WorldyClient.getConfig().general.autoOpenVoteLinks) {
                        // Open all vote links
                        client.execute(() -> {
                            for (URI uri : voteUris) {
                                Util.getOperatingSystem().open(uri);
                            }
                        });

                        CommandHelper.sendMessage("command.worldy.vote.title");
                    } else {
                        // Send clickable messages
                        for (URI uri : voteUris) {
                            String host = uri.getHost();
                            String siteName = host != null ? host.split("\\.")[0] : "website";

                            Text message = Text.translatable("command.worldy.vote.click", siteName)
                                    .setStyle(Style.EMPTY
                                            .withClickEvent(new ClickEvent.OpenUrl(uri))
                                            .withHoverEvent(new HoverEvent.ShowText(
                                                    Text.translatable("command.worldy.vote.hover", siteName)
                                            ))
                                            .withColor(Formatting.GRAY)
                                    );

                            CommandHelper.sendMessage(message);
                        }
                    }

                    return 1;
                });
    }
}
