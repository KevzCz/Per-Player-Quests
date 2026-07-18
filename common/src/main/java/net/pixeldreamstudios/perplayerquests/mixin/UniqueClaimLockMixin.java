package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerCollapseLock;
import net.pixeldreamstudios.perplayerquests.progression.UniqueClaimLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TeamData.class, remap = false)
public abstract class UniqueClaimLockMixin {

    @Inject(method = "canStartTasks", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$lockClaimedUnique(Quest quest, CallbackInfoReturnable<Boolean> cir) {
        TeamData self = (TeamData) (Object) this;
        if (UniqueClaimLock.isLockedForViewer(self, quest)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getCannotStartReason", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$claimLockReason(Quest quest, CallbackInfoReturnable<Component> cir) {
        TeamData self = (TeamData) (Object) this;
        Component reason = UniqueClaimLock.reasonForViewer(self, quest);
        if (reason != null) {
            cir.setReturnValue(reason);
            return;
        }
        Component collapseReason = PerPlayerCollapseLock.reasonForViewer(self, quest);
        if (collapseReason != null) {
            cir.setReturnValue(collapseReason);
        }
    }
}
