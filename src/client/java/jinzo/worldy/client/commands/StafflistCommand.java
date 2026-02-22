package jinzo.worldy.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jinzo.worldy.client.models.Staff;
import jinzo.worldy.client.utils.CommandHelper;
import jinzo.worldy.client.utils.StafflistHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class StafflistCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("stafflist")
                .executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    Map<String, List<Staff>> data = StafflistHelper.getCachedStaffData();
                    if (data.isEmpty()) {
                        CommandHelper.sendMessage("command.worldy.data.loading");
                        StafflistHelper.loadStaffListOnJoin(client);
                        return 1;
                    }

                    sendStaffList(client, data);
                    return 1;
                });
    }

    private static void sendStaffList(MinecraftClient client, Map<String, List<Staff>> staffData) {
        if (staffData.isEmpty()) {
            if (client.player != null)
                CommandHelper.sendError("command.worldy.stafflist.no_data");
            return;
        }

        if (client.player != null)
            CommandHelper.sendMessage(Text.translatable("command.worldy.stafflist.title").formatted(Formatting.GOLD, Formatting.BOLD));

        boolean hasUnknownPlayers = false;

        for (Map.Entry<String, List<Staff>> entry : staffData.entrySet()) {
            String role = entry.getKey();
            List<Staff> staffMembers = entry.getValue();

            if (staffMembers.isEmpty()) continue;

            String displayRole = role.substring(0, 1).toUpperCase() + role.substring(1);
            MutableText roleMessage = createRoleMessage(displayRole, staffMembers);
            if (client.player != null) client.player.sendMessage(roleMessage, false);

            if (staffMembers.stream().anyMatch(Staff::isUnknown)) hasUnknownPlayers = true;
        }

        int totalStaff = staffData.values().stream().mapToInt(List::size).sum();
        long onlineCount = staffData.values().stream()
                .flatMap(List::stream)
                .filter(member -> isPlayerOnline(member.getDisplayName(), client))
                .count();

        if (client.player != null) {
            CommandHelper.sendMessage(
                    Text.translatable("command.worldy.stafflist.total_staff").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(totalStaff)).formatted(Formatting.AQUA))
                            .append(Text.literal(" (").formatted(Formatting.GRAY))
                            .append(Text.translatable("command.worldy.stafflist.online_staff", onlineCount).formatted(Formatting.GREEN))
                            .append(Text.literal(")").formatted(Formatting.GRAY))
            );
        }

        if (hasUnknownPlayers) {
            if (client.player != null)
                CommandHelper.sendError("command.worldy.stafflist.usernames_not_found");
        }
    }

    private static boolean isPlayerOnline(String playerName, MinecraftClient client) {
        if (client.getNetworkHandler() == null) return false;
        return client.getNetworkHandler().getPlayerList().stream()
                .anyMatch(entry -> entry.getProfile().name().equals(playerName));
    }

    private static MutableText createRoleMessage(String displayRole, List<Staff> staffMembers) {
        MutableText baseMessage = Text.literal(displayRole + ": ").formatted(Formatting.YELLOW);
        MinecraftClient client = MinecraftClient.getInstance();

        for (int i = 0; i < staffMembers.size(); i++) {
            Staff member = staffMembers.get(i);
            MutableText playerText = createHoverablePlayerText(member, client);

            if (i > 0) baseMessage.append(Text.literal(", ").formatted(Formatting.GRAY));
            baseMessage.append(playerText);
        }
        return baseMessage;
    }

    private static MutableText createHoverablePlayerText(Staff member, MinecraftClient client) {
        boolean isOnline = isPlayerOnline(member.getDisplayName(), client);
        Formatting color;

        if (member.isUnknown()) {
            color = Formatting.RED;
        } else {
            color = isOnline ? Formatting.GREEN : Formatting.GRAY;
        }

        MutableText hoverText = Text.translatable("command.worldy.stafflist.uuid", member.getUuid()).formatted(Formatting.GRAY).append("\n");
        hoverText.append(Text.translatable("command.worldy.stafflist.status").formatted(Formatting.GRAY))
                .append(Text.translatable("command.worldy.stafflist." + (isOnline ? "online" : "offline")).formatted(isOnline ? Formatting.GREEN : Formatting.RED));

        if (member.isUnknown()) {
            hoverText.append(Text.translatable("command.worldy.stafflist.username_not_found").formatted(Formatting.RED));
        }

        String playerName = member.getDisplayName();
        String runCommand = "/res " + playerName;

        return Text.literal(playerName)
                .styled(style -> style
                        .withColor(color)
                        .withHoverEvent(new HoverEvent.ShowText(hoverText))
                        .withClickEvent(new ClickEvent.RunCommand(runCommand))
                );
    }

    private StafflistCommand() {}
}
