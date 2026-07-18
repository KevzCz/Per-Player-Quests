package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;

public final class PerPlayerLifecycle {
    private PerPlayerLifecycle() {}

    public static void onJoin(ServerPlayer player) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) return;
        TeamData data = file.getTeamData(player).orElse(null);
        if (data != null) {
            PerPlayerServer.syncTo(data, player.getUUID());
            PerPlayerServer.broadcastTeam(data);
        }
        UniqueSync.sendTo(player);
        ServerGlobalSync.sendTo(player);
    }
}
