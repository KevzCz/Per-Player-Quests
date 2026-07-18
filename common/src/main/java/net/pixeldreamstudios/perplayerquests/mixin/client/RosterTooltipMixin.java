package net.pixeldreamstudios.perplayerquests.mixin.client;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerRoster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RosterTooltipMixin {

    @Mixin(value = QuestButton.class, remap = false)
    public static abstract class OnQuest {
        @Shadow @Final private Quest quest;

        @Inject(method = "addMouseOverText", at = @At("TAIL"), remap = false)
        private void ppq$addRosterTooltip(TooltipList list, CallbackInfo ci) {
            try {
                TeamData data = ClientQuestFile.INSTANCE == null ? null : ClientQuestFile.INSTANCE.selfTeamData;
                if (data == null) return;
                for (Component line : PerPlayerRoster.buildLines(data, quest)) list.add(line);
            } catch (Exception ignored) {
            }
        }
    }

    @Mixin(value = TaskButton.class, remap = false)
    public static abstract class OnTask {
        @Shadow @Final Task task;

        @Inject(method = "addMouseOverText", at = @At("TAIL"), remap = false)
        private void ppq$addRosterTooltip(TooltipList list, CallbackInfo ci) {
            try {
                TeamData data = ClientQuestFile.INSTANCE == null ? null : ClientQuestFile.INSTANCE.selfTeamData;
                if (data == null) return;
                for (Component line : PerPlayerRoster.buildLines(data, task)) list.add(line);
            } catch (Exception ignored) {
            }
        }
    }
}
