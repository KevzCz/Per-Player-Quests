package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;

import java.util.UUID;

public final class PerPlayerReset {
    private PerPlayerReset() {}

    public static void reset(TeamData teamData, long objId) {
        if (!(teamData instanceof IPerPlayerStore store)) return;
        for (ServerPlayer p : teamData.getOnlineMembers()) {
            store.mqt$clearPerPlayer(p.getUUID(), objId);
        }
        for (ServerPlayer p : teamData.getOnlineMembers()) {
            PerPlayerServer.syncTo(teamData, p.getUUID());
        }
    }

    public static void resetQuest(IPerPlayerStore store, TeamData data, UUID targetId, Quest quest) {
        if (!((Object) quest instanceof IPerPlayerObject ppo) || !ppo.mqt$isPerPlayer()) return;
        ProgressChange change = new ProgressChange(quest, targetId);
        change.setReset(true);
        quest.forceProgressRaw(data, change);
        for (Task t : quest.getTasksAsList()) store.mqt$clearPerPlayer(targetId, t.id);
        store.mqt$clearPerPlayer(targetId, quest.id);
    }

    public static void completeQuest(TeamData data, Quest quest) {
        if (!((Object) quest instanceof IPerPlayerObject ppo) || !ppo.mqt$isPerPlayer()) return;
        for (Task t : quest.getTasksAsList()) data.setProgress(t, t.getMaxProgress());
    }

    public static void releaseUnique(TeamData teamData, long questId) {
        if (!UniqueClaims.isClaimed(questId)) return;
        UniqueClaims.release(questId);
        if (teamData.getFile() instanceof ServerQuestFile sqf) {
            MinecraftServer server = sqf.server;
            if (server != null) {
                UniqueSync.broadcastAll(server);
            }
        }
    }
}
