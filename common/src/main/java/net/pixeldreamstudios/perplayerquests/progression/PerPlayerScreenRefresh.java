package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class PerPlayerScreenRefresh {
    private PerPlayerScreenRefresh() {}

    static void refresh() {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file != null && file.selfTeamData != null) {
            file.selfTeamData.clearCachedProgress();
        }

        QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
        if (gui != null) {
            gui.refreshChapterPanel();
            gui.refreshQuestPanel();
            gui.refreshViewQuestPanel();
        }
    }
}
