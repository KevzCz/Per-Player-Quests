package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = dev.ftb.mods.ftbquests.net.SubmitTaskMessage.class, remap = false)
public abstract class SubmitTaskContextMixin {

    @Inject(method = "lambda$handle$1", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ppq$wrapGateInPlayerContext(SubmitTaskMessage message, ServerPlayer player, TeamData data, CallbackInfo ci) {
        if (data.isLocked()) return;
        if (!(data.getFile() instanceof ServerQuestFile sqf)) return;

        Task task = data.getFile().getTask(message.taskId());
        if (task == null) return;

        ci.cancel();
        sqf.withPlayerContext(player, () -> {
            if (data.canStartTasks(task.getQuest())) {
                task.submitTask(data, player);
            }
        });
    }
}
