package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;
import net.pixeldreamstudios.perplayerquests.progression.UniqueVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Quest.class, remap = false)
public abstract class UniqueQuestVisibilityMixin {

    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true, remap = false)
    private void mqt$hideClaimedUnique(TeamData data, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (!(self instanceof IUniqueQuest u) || !u.mqt$isUniqueGlobal()) return;
        if (u.mqt$getUniqueView() != IUniqueQuest.UniqueView.HIDDEN_FOR_OTHERS) return;

        long id = ((Quest) (Object) this).id;
        if (UniqueVisibility.isHiddenForViewer(data, id)) {
            cir.setReturnValue(false);
        }
    }
}
