package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class PerPlayerActor {
    private PerPlayerActor() {}

    public static UUID resolve(TeamData teamData) {
        BaseQuestFile file = teamData.getFile();
        if (file != null && file.isServerSide()) {
            ServerPlayer current = ServerQuestFile.INSTANCE == null ? null
                    : ServerQuestFile.INSTANCE.getCurrentPlayer();
            return current == null ? null : current.getUUID();
        }
        return ClientActorHolder.localPlayerUuid();
    }
}
