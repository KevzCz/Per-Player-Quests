package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PerPlayerCollapseLock {
    private PerPlayerCollapseLock() {}

    public static Component reasonForViewer(TeamData teamData, Quest quest) {
        if (!(teamData instanceof IPerPlayerStore store)) return null;
        if (!quest.hasDependencies()) return null;

        List<UUID> members = PerPlayerCollapse.teamMembers(teamData);
        if (members.isEmpty()) return null;

        List<String> missingNames = null;

        for (QuestObject dep : quest.streamDependencies().toList()) {
            if (!(dep instanceof IPerPlayerObject ppo) || !ppo.mqt$isPerPlayer()) continue;
            if (ppo.mqt$getCollapse() != IPerPlayerObject.Collapse.ALL) continue;
            if (PerPlayerCollapse.isCompletedCollapsed(teamData, dep)) continue;

            for (UUID id : members) {
                if (store.mqt$isPerPlayerCompleted(id, dep.id)) continue;
                if (missingNames == null) missingNames = new ArrayList<>();
                String name = resolveName(teamData, id);
                if (!missingNames.contains(name)) missingNames.add(name);
            }
        }

        if (missingNames == null || missingNames.isEmpty()) return null;
        return Component.translatable("perplayerquests.per_player.locked_waiting_on", String.join(", ", missingNames))
                .withStyle(ChatFormatting.RED);
    }

    private static String resolveName(TeamData teamData, UUID id) {
        boolean serverSide = teamData.getFile() != null && teamData.getFile().isServerSide();
        if (serverSide) {
            MinecraftServer server = teamData.getFile() instanceof ServerQuestFile sqf
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
