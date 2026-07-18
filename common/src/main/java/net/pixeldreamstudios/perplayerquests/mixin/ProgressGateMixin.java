package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.api.IServerGlobalQuest;
import net.pixeldreamstudios.perplayerquests.progression.IPerPlayerStore;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerActor;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerCollapse;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerServer;
import net.pixeldreamstudios.perplayerquests.progression.ServerGlobalProgress;
import net.pixeldreamstudios.perplayerquests.progression.ServerGlobalServer;
import net.pixeldreamstudios.perplayerquests.progression.UniqueGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Date;
import java.util.UUID;

@Mixin(value = TeamData.class, remap = false)
public abstract class ProgressGateMixin implements IPerPlayerStore {

    @Unique
    private static boolean ppq$globalQuest(Quest quest) {
        return quest != null && (Object) quest instanceof IServerGlobalQuest g && g.ppq$isServerGlobal();
    }

    @Unique
    private static boolean ppq$globalObject(QuestObject object) {
        if (object instanceof Quest q) return ppq$globalQuest(q);
        if (object instanceof Task t) return ppq$globalQuest(t.getQuest());
        return false;
    }

    @Unique
    private static boolean ppq$perPlayer(QuestObject object) {
        if (object instanceof IPerPlayerObject ppo && ppo.mqt$isPerPlayer()) return true;
        if (object instanceof Chapter chapter) {
            var quests = chapter.getQuests();
            if (quests.isEmpty()) return false;
            boolean anyRequired = false;
            for (var q : quests) {
                if (q.isOptional()) continue;
                anyRequired = true;
                if (!((Object) q instanceof IPerPlayerObject qp && qp.mqt$isPerPlayer())) return false;
            }
            return anyRequired;
        }
        return false;
    }

    @Inject(method = "getProgress(Ldev/ftb/mods/ftbquests/quest/task/Task;)J",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$getProgress(Task task, CallbackInfoReturnable<Long> cir) {
        if (ppq$globalQuest(task.getQuest())) {
            cir.setReturnValue(ServerGlobalProgress.getProgress(task.id));
            return;
        }
        if (!ppq$perPlayer(task)) return;
        TeamData self = (TeamData) (Object) this;
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        cir.setReturnValue(mqt$getPerPlayerProgress(actor, task.id));
    }

    @Inject(method = "setProgress", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$setProgress(Task task, long progress, CallbackInfo ci) {
        TeamData self = (TeamData) (Object) this;

        if (ppq$globalQuest(task.getQuest())) {
            if (self.isLocked()) { ci.cancel(); return; }
            if (self.getFile() != null && self.getFile().isServerSide()) {
                ServerGlobalServer.setProgress(self, task, progress);
            }
            ci.cancel();
            return;
        }

        if (!ppq$perPlayer(task)) return;
        if (self.isLocked()) { ci.cancel(); return; }
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;

        if (progress > 0L && self.getFile() != null && self.getFile().isServerSide()) {
            if (!UniqueGate.onProgressAttempt(self, task.getQuest(), actor)) {
                ci.cancel();
                return;
            }
        }

        long max = task.getMaxProgress();
        long clamped = Math.max(0L, Math.min(progress, max));
        long prev = mqt$getPerPlayerProgress(actor, task.id);
        if (prev == clamped && !(clamped == 0L && mqt$isPerPlayerStarted(actor, task.id))) {
            ci.cancel();
            return;
        }

        if (clamped == 0L) {
            mqt$setPerPlayerProgress(actor, task.id, 0L);
            mqt$setPerPlayerStarted(actor, task.id, false);
            mqt$setPerPlayerCompleted(actor, task.id, false);
        } else {
            mqt$setPerPlayerProgress(actor, task.id, clamped);
            if (!mqt$isPerPlayerStarted(actor, task.id)) {
                mqt$setPerPlayerStarted(actor, task.id, true);
            }
        }

        self.clearCachedProgress();
        self.markDirty();
        if (self.getFile() != null && self.getFile().isServerSide()) {
            PerPlayerServer.afterTaskProgress(self, task, actor);
        }
        ci.cancel();
    }

    @Inject(method = "isCompleted", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$isCompleted(QuestObject object, CallbackInfoReturnable<Boolean> cir) {
        if (ppq$globalObject(object)) {
            cir.setReturnValue(ServerGlobalProgress.isCompleted(object.id));
            return;
        }

        TeamData self = (TeamData) (Object) this;
        if (object instanceof Quest quest && UniqueGate.isLockedForActor(self, quest)) {
            cir.setReturnValue(false);
            return;
        }

        if (!ppq$perPlayer(object)) return;
        if (self instanceof IPerPlayerStore store) {
            UUID forcedActor = store.ppq$forcedActor();
            if (forcedActor != null) {
                cir.setReturnValue(mqt$isPerPlayerCompleted(forcedActor, object.id));
                return;
            }
            if (store.ppq$forceCollapsedDeps()) {
                cir.setReturnValue(PerPlayerCollapse.isCompletedCollapsed(self, object));
                return;
            }
        }
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) {
            cir.setReturnValue(PerPlayerCollapse.isCompletedCollapsed(self, object));
            return;
        }
        cir.setReturnValue(mqt$isPerPlayerCompleted(actor, object.id));
    }

    @Inject(method = "isStarted", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$isStarted(QuestObject object, CallbackInfoReturnable<Boolean> cir) {
        if (ppq$globalObject(object)) {
            cir.setReturnValue(ServerGlobalProgress.isStarted(object.id));
            return;
        }
        if (!ppq$perPlayer(object)) return;
        TeamData self = (TeamData) (Object) this;
        if (self instanceof IPerPlayerStore store && store.ppq$forceCollapsedDeps()) {
            cir.setReturnValue(PerPlayerCollapse.isStartedCollapsed(self, object));
            return;
        }
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        cir.setReturnValue(mqt$isPerPlayerStarted(actor, object.id));
    }

    @Unique
    private boolean ppq$perPlayerId(long id) {
        TeamData self = (TeamData) (Object) this;
        Object obj = self.getFile() == null ? null : self.getFile().get(id);
        return obj instanceof QuestObject qo && ppq$perPlayer(qo);
    }

    @Inject(method = "setCompleted", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$setCompleted(long id, Date time, CallbackInfoReturnable<Boolean> cir) {
        if (!ppq$perPlayerId(id)) return;
        TeamData self = (TeamData) (Object) this;
        if (self.isLocked()) { cir.setReturnValue(false); return; }
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        boolean was = mqt$isPerPlayerCompleted(actor, id);
        boolean now = time != null;
        if (was == now) { cir.setReturnValue(false); return; }
        mqt$setPerPlayerCompleted(actor, id, now);
        self.clearCachedProgress();
        self.markDirty();
        if (self.getFile() != null && self.getFile().isServerSide()) {
            PerPlayerServer.syncTo(self, actor);
            PerPlayerServer.broadcastTeam(self);
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "setStarted", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$setStarted(long id, Date time, CallbackInfoReturnable<Boolean> cir) {
        if (!ppq$perPlayerId(id)) return;
        TeamData self = (TeamData) (Object) this;
        if (self.isLocked()) { cir.setReturnValue(false); return; }
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        boolean was = mqt$isPerPlayerStarted(actor, id);
        boolean now = time != null;
        if (was == now) { cir.setReturnValue(false); return; }
        mqt$setPerPlayerStarted(actor, id, now);
        self.clearCachedProgress();
        self.markDirty();
        if (self.getFile() != null && self.getFile().isServerSide()) {
            PerPlayerServer.syncTo(self, actor);
            PerPlayerServer.broadcastTeam(self);
        }
        cir.setReturnValue(true);
    }
}
