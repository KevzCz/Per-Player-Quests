package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PerPlayerRoster {
    private PerPlayerRoster() {}

    public static List<Component> buildLines(TeamData teamData, QuestObjectBase obj) {
        if (!(obj instanceof IPerPlayerObject ppo) || !ppo.mqt$isPerPlayer()) return List.of();
        if (!ppo.mqt$showRosterTooltip()) return List.of();
        if (!(teamData instanceof IPerPlayerStore store)) return List.of();

        List<UUID> members = PerPlayerCollapse.teamMembers(teamData);
        if (members.isEmpty()) return List.of();

        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("perplayerquests.per_player.roster.header")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        for (UUID id : members) {
            boolean completed = store.mqt$isPerPlayerCompleted(id, obj.id);
            boolean started = store.mqt$isPerPlayerStarted(id, obj.id);
            String name = resolveName(teamData, id);

            Component status;
            ChatFormatting color;
            if (completed) {
                status = Component.translatable("perplayerquests.per_player.roster.complete");
                color = ChatFormatting.GREEN;
            } else if (started) {
                status = Component.translatable("perplayerquests.per_player.roster.in_progress");
                color = ChatFormatting.YELLOW;
            } else {
                status = Component.translatable("perplayerquests.per_player.roster.not_started");
                color = ChatFormatting.DARK_GRAY;
            }

            lines.add(Component.literal(" " + name + ": ").withStyle(ChatFormatting.WHITE)
                    .append(status.copy().withStyle(color)));
        }
        return lines;
    }

    private static String resolveName(TeamData teamData, UUID id) {
        boolean serverSide = teamData.getFile() != null && teamData.getFile().isServerSide();
        if (serverSide) {
            MinecraftServer server = teamData.getFile() instanceof dev.ftb.mods.ftbquests.quest.ServerQuestFile sqf
                    ? sqf.server : null;
            if (server != null) {
                var player = server.getPlayerList().getPlayer(id);
                if (player != null) return player.getGameProfile().getName();
                var profile = server.getProfileCache();
                if (profile != null) {
                    var cached = profile.get(id);
                    if (cached.isPresent()) return cached.get().getName();
                }
            }
            return id.toString();
        }
        var known = FTBTeamsAPI.api().getClientManager().getKnownPlayer(id);
        if (known.isPresent()) return known.get().name();
        return id.toString();
    }
}
