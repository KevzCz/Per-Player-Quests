package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.progression.IPerPlayerStore;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerActor;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.BiFunction;

@Mixin(value = TeamData.class, remap = false)
public abstract class PerPlayerRepeatMixin {

    @Unique
    private static boolean ppq$perPlayer(Quest quest) {
        return quest != null && (Object) quest instanceof IPerPlayerObject o && o.mqt$isPerPlayer();
    }

    @Inject(method = "getMilliSecondsUntilRepeatable", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$perPlayerRepeatCooldown(Quest quest, CallbackInfoReturnable<Long> cir) {
        if (!ppq$perPlayer(quest)) return;
        TeamData self = (TeamData) (Object) this;
        if (!(self instanceof IPerPlayerStore store)) return;
        if (!quest.canBeRepeated() || quest.getRepeatCooldown() == 0) {
            cir.setReturnValue(0L);
            return;
        }
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        long now = System.currentTimeMillis();
        long when = store.mqt$getPerPlayerRepeatableTime(actor, quest.id);
        cir.setReturnValue(now >= when ? 0L : when - now);
    }

    @Inject(method = "clearRepeatCooldown", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$clearPerPlayerRepeatCooldown(Quest quest, CallbackInfo ci) {
        if (!ppq$perPlayer(quest)) return;
        TeamData self = (TeamData) (Object) this;
        if (!(self instanceof IPerPlayerStore store)) return;
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        store.mqt$setPerPlayerRepeatableTime(actor, quest.id, 0L);
        self.markDirty();
        if (self.getFile() != null && self.getFile().isServerSide()) {
            PerPlayerServer.syncTo(self, actor);
            PerPlayerServer.broadcastTeam(self);
        }
        ci.cancel();
    }

    @Inject(method = "getCompletionCount", at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$perPlayerCompletionCount(Quest quest, CallbackInfoReturnable<Integer> cir) {
        if (!ppq$perPlayer(quest)) return;
        TeamData self = (TeamData) (Object) this;
        if (!(self instanceof IPerPlayerStore store)) return;
        if (!quest.canBeRepeated()) {
            cir.setReturnValue(self.isCompleted(quest) ? 1 : 0);
            return;
        }
        UUID actor = PerPlayerActor.resolve(self);
        if (actor == null) return;
        cir.setReturnValue(store.mqt$getPerPlayerCompletionCount(actor, quest.id));
    }

    @Redirect(method = "markRewardAsClaimed",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2IntMap;merge(JILjava/util/function/BiFunction;)I"),
            remap = false)
    private int ppq$redirectCompletionCountMerge(Long2IntMap map, long questId, int value,
                                                  BiFunction<? super Integer, ? super Integer, ? extends Integer> fn,
                                                  UUID player, dev.ftb.mods.ftbquests.quest.reward.Reward reward, long date) {
        Quest quest = reward.getQuest();
        if (ppq$perPlayer(quest) && (Object) this instanceof IPerPlayerStore store) {
            int updated = store.mqt$getPerPlayerCompletionCount(player, questId) + value;
            store.mqt$setPerPlayerCompletionCount(player, questId, updated);
            TeamData self = (TeamData) (Object) this;
            PerPlayerServer.syncTo(self, player);
            PerPlayerServer.broadcastTeam(self);
            return updated;
        }
        return map.merge(questId, value, fn);
    }

    @Redirect(method = "markRewardAsClaimed",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2LongMap;put(JJ)J"),
            remap = false)
    private long ppq$redirectRepeatableTimePut(Long2LongMap map, long questId, long when,
                                                UUID player, dev.ftb.mods.ftbquests.quest.reward.Reward reward, long date) {
        Quest quest = reward.getQuest();
        if (ppq$perPlayer(quest) && (Object) this instanceof IPerPlayerStore store) {
            store.mqt$setPerPlayerRepeatableTime(player, questId, when);
            TeamData self = (TeamData) (Object) this;
            PerPlayerServer.syncTo(self, player);
            PerPlayerServer.broadcastTeam(self);
            return 0L;
        }
        return map.put(questId, when);
    }

    @Redirect(method = "markRewardAsClaimed",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/Quest;resetProgressIfRepeatable(Ldev/ftb/mods/ftbquests/quest/TeamData;Ljava/util/UUID;)Z"),
            remap = false)
    private boolean ppq$scopedResetProgressIfRepeatable(Quest quest, TeamData data, UUID player) {
        if (!(ppq$perPlayer(quest) && (Object) this instanceof IPerPlayerStore store)) {
            return quest.resetProgressIfRepeatable(data, player);
        }
        store.ppq$beginPerPlayer(player);
        try {
            return quest.resetProgressIfRepeatable(data, player);
        } finally {
            store.ppq$endPerPlayer();
        }
    }
}
