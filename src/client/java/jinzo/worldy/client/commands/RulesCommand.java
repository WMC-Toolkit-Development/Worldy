package jinzo.worldy.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import jinzo.worldy.client.utils.CommandHelper;
import jinzo.worldy.client.utils.RulesHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class RulesCommand {

    private static int execute(CommandContext<FabricClientCommandSource> ctx) {
        RulesHelper.loadRulesAsync();

        RulesHelper.getCachedRules().ifPresentOrElse(data -> {
            CommandHelper.sendMessage(
                    Text.translatable("command.worldy.rules.open")
                            .styled(style -> style
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create(data.url)))
                                    .withHoverEvent(new HoverEvent.ShowText(
                                            Text.translatable("command.worldy.rules.hover")
                                    ))
                                    .withColor(Formatting.GRAY)
                            )
            );
        }, () -> {
            CommandHelper.sendMessage(
                    Text.translatable("command.worldy.data.loading")
                            .formatted(Formatting.GRAY)
            );
        });

        return 1;
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("rules")
                .executes(RulesCommand::execute)

                // /rules list
                .then(literal("list")
                        .executes(ctx -> {
                            RulesHelper.loadRulesAsync();

                            CommandHelper.sendMessage(
                                    Text.translatable("command.worldy.rules.title")
                                            .formatted(Formatting.GOLD, Formatting.BOLD)
                            );

                            var rules = RulesHelper.getAllRules();
                            if (rules.isEmpty()) {
                                CommandHelper.sendMessage(
                                        Text.translatable("command.worldy.rules.loading")
                                                .formatted(Formatting.GRAY)
                                );
                                return 1;
                            }

                            rules.forEach(rule ->
                                    CommandHelper.sendMessage(
                                            Text.literal(rule.id + " - " + rule.title)
                                                    .formatted(Formatting.GRAY)
                                    )
                            );

                            return 1;
                        })
                )

                // /rules info <id>
                .then(literal("info")
                        .then(argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    RulesHelper.loadRulesAsync();

                                    RulesHelper.findRuleById(id).ifPresentOrElse(rule -> {
                                        CommandHelper.sendMessage(
                                                Text.literal(rule.id + " - " + rule.title)
                                                        .formatted(Formatting.GOLD, Formatting.BOLD)
                                        );
                                        CommandHelper.sendMessage(
                                                Text.literal(rule.description)
                                                        .formatted(Formatting.GRAY)
                                        );
                                    }, () -> {
                                        CommandHelper.sendMessage(
                                                Text.translatable("command.worldy.rules.not_found")
                                                        .formatted(Formatting.RED)
                                        );
                                    });

                                    return 1;
                                })
                        )
                );
    }
}
