package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.progression.IPerPlayerStore;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerActor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = TeamData.class, remap = false)
public abstract class PerPlayerDepCacheMixin {

    @Unique
    private static boolean mqt$dependsOnPerPlayer(Quest quest) {
        if ((Object) quest instanceof IPerPlayerObject q && q.mqt$isPerPlayer()) return true;
        for (QuestObject dep : quest.streamDependencies().toList()) {
            if ((Object) dep instanceof IPerPlayerObject dp && dp.mqt$isPerPlayer()) return true;
        }
        return false;
    }

    @Unique
    private static boolean mqt$requiresActorScope(Quest quest) {
        if ((Object) quest instanceof IPerPlayerObject qo
                && qo.mqt$isPerPlayer()
                && qo.mqt$getExclusivityScope() == IPerPlayerObject.ExclusivityScope.PER_ACTOR) {
            return true;
        }
        for (QuestObject dep : quest.streamDependencies().toList()) {
            if ((Object) dep instanceof IPerPlayerObject dp
                    && dp.mqt$isPerPlayer()
                    && dp.mqt$getExclusivityScope() == IPerPlayerObject.ExclusivityScope.PER_ACTOR) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "areDependenciesComplete", at = @At("HEAD"), cancellable = true, remap = false)
    private void mqt$uncachedDepsComplete(Quest quest, CallbackInfoReturnable<Boolean> cir) {
        TeamData self = (TeamData) (Object) this;
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        if (!mqt$dependsOnPerPlayer(quest)) return;
        if (!quest.hasDependencies()) {
            cir.setReturnValue(true);
            return;
        }
        if (self instanceof IPerPlayerStore store) {
            boolean perActor = mqt$requiresActorScope(quest);
            if (perActor) {
                store.ppq$beginForceActorDeps(actor);
                try {
                    cir.setReturnValue(quest.areDependenciesComplete(self));
                } finally {
                    store.ppq$endForceActorDeps();
                }
                return;
            }
            store.ppq$beginForceCollapsedDeps();
            try {
                cir.setReturnValue(quest.areDependenciesComplete(self));
            } finally {
                store.ppq$endForceCollapsedDeps();
            }
        } else {
            cir.setReturnValue(quest.areDependenciesComplete(self));
        }
    }

    @Inject(method = "areDependenciesVisible", at = @At("HEAD"), cancellable = true, remap = false)
    private void mqt$uncachedDepsVisible(Quest quest, CallbackInfoReturnable<Boolean> cir) {
        TeamData self = (TeamData) (Object) this;
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        if (!mqt$dependsOnPerPlayer(quest)) return;
        if (!quest.hasDependencies()) { cir.setReturnValue(true); return; }
        if (self instanceof IPerPlayerStore store) {
            boolean perActor = mqt$requiresActorScope(quest);
            if (perActor) {
                store.ppq$beginForceActorDeps(actor);
                try {
                    cir.setReturnValue(quest.areDependenciesVisible(self));
                } finally {
                    store.ppq$endForceActorDeps();
                }
                return;
            }
            store.ppq$beginForceCollapsedDeps();
            try {
                cir.setReturnValue(quest.areDependenciesVisible(self));
            } finally {
                store.ppq$endForceCollapsedDeps();
            }
        } else {
            cir.setReturnValue(quest.areDependenciesVisible(self));
        }
    }
}
