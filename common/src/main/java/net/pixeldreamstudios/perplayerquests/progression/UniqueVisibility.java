package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;

public final class UniqueVisibility {
    private UniqueVisibility() {}

    public static boolean isHiddenForViewer(TeamData teamData, long questId) {
        BaseQuestFile file = teamData == null ? null : teamData.getFile();
        if (file != null && file.isServerSide()) {
            return UniqueClaims.isLockedForActor(teamData, questId);
        }
        return ClientUniqueView.isClaimedByOther(questId);
    }
}
