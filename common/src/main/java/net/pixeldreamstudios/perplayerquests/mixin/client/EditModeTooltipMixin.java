package net.pixeldreamstudios.perplayerquests.mixin.client;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.api.IServerGlobalQuest;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;
import net.pixeldreamstudios.perplayerquests.mixin.client.accessor.QuestScreenAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class EditModeTooltipMixin {

    @Mixin(value = QuestButton.class, remap = false)
    public static abstract class OnQuest {
        @Shadow @Final private QuestScreen questScreen;
        @Shadow @Final private Quest quest;

        @Inject(method = "addMouseOverText", at = @At("TAIL"), remap = false)
        private void ppq$addEditModeLabel(TooltipList list, CallbackInfo ci) {
            if (questScreen == null || !((QuestScreenAccessor) questScreen).ppq$getFile().canEdit()) return;

            boolean perPlayer = (Object) quest instanceof IPerPlayerObject o && o.mqt$isPerPlayer();
            boolean unique = (Object) quest instanceof IUniqueQuest u && u.mqt$isUniqueGlobal();
            boolean global = (Object) quest instanceof IServerGlobalQuest g && g.ppq$isServerGlobal();

            if (perPlayer) {
                list.add(Component.translatable("perplayerquests.edit.per_player_task")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            }
            if (unique) {
                list.add(Component.translatable("perplayerquests.edit.unique_quest")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            }
            if (global) {
                list.add(Component.translatable("perplayerquests.edit.server_global_quest")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            }
        }
    }

    @Mixin(value = TaskButton.class, remap = false)
    public static abstract class OnTask {
        @Shadow @Final private QuestScreen questScreen;
        @Shadow @Final Task task;

        @Inject(method = "addMouseOverText", at = @At("TAIL"), remap = false)
        private void ppq$addEditModeLabel(TooltipList list, CallbackInfo ci) {
            if (questScreen == null || !((QuestScreenAccessor) questScreen).ppq$getFile().canEdit()) return;
            if (!((Object) task instanceof IPerPlayerObject o) || !o.mqt$isPerPlayer()) return;
            list.add(Component.translatable("perplayerquests.edit.per_player_task")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        }
    }
}
