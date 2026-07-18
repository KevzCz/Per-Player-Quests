package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.Excludable;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.progression.IPerPlayerStore;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerActor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = TeamData.class, remap = false)
public abstract class PerPlayerExclusivityMixin {

    @Inject(method = "isExcludedByOtherQuestline", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$perActorExclusion(QuestObject qo, CallbackInfoReturnable<Boolean> cir) {
        if (!(qo instanceof Excludable excludable)) return;
        TeamData self = (TeamData) (Object) this;
        if (!(self instanceof IPerPlayerStore store)) return;

        UUID nestedActor = store.ppq$forcedActor();
        if (nestedActor != null) {
            cir.setReturnValue(excludable.isQuestObjectExcluded(self));
            return;
        }

        boolean perActor = (Object) qo instanceof IPerPlayerObject ppo
                && ppo.mqt$getExclusivityScope() == IPerPlayerObject.ExclusivityScope.PER_ACTOR;
        if (!perActor) return;

        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;

        store.ppq$beginForceActorDeps(actor);
        try {
            cir.setReturnValue(excludable.isQuestObjectExcluded(self));
        } finally {
            store.ppq$endForceActorDeps();
        }
    }
}
