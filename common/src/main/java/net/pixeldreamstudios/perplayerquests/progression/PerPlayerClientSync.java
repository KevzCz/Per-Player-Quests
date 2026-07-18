package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.pixeldreamstudios.perplayerquests.network.PerPlayerSyncPacket;
import net.pixeldreamstudios.perplayerquests.network.PerPlayerTeamSyncPacket;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class PerPlayerClientSync {
    private PerPlayerClientSync() {}

    public static void apply(PerPlayerSyncPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        UUID self = mc.player.getUUID();

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null) return;
        TeamData data = file.selfTeamData;
        if (!(data instanceof IPerPlayerStore store)) return;

        store.mqt$clearAllFor(self);
        for (long[] pair : packet.taskProgress()) {
            store.mqt$setPerPlayerProgress(self, pair[0], pair[1]);
        }
        for (long id : packet.started()) {
            store.mqt$setPerPlayerStarted(self, id, true);
        }
        for (long id : packet.completed()) {
            store.mqt$setPerPlayerCompleted(self, id, true);
        }
        for (long[] entry : packet.repeatState()) {
            store.mqt$setPerPlayerCompletionCount(self, entry[0], (int) entry[1]);
            store.mqt$setPerPlayerRepeatableTime(self, entry[0], entry[2]);
        }

        PerPlayerScreenRefresh.refresh();
    }

    public static void applyTeam(PerPlayerTeamSyncPacket packet) {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null) return;
        TeamData data = file.selfTeamData;
        if (!(data instanceof IPerPlayerStore store)) return;

        for (PerPlayerTeamSyncPacket.Entry e : packet.entries()) {
            store.mqt$setPerPlayerStarted(e.player(), e.objId(), e.started());
            store.mqt$setPerPlayerCompleted(e.player(), e.objId(), e.completed());
        }
        for (PerPlayerTeamSyncPacket.RepeatEntry e : packet.repeatEntries()) {
            store.mqt$setPerPlayerCompletionCount(e.player(), e.questId(), e.completionCount());
            store.mqt$setPerPlayerRepeatableTime(e.player(), e.questId(), e.repeatableTime());
        }

        PerPlayerScreenRefresh.refresh();
    }
}
