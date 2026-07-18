package net.pixeldreamstudios.perplayerquests.mixin;

import com.mojang.util.UndashedUuid;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.QuestKey;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.progression.IPerPlayerStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(value = TeamData.class, remap = false)
public abstract class PerPlayerTeamDataMixin implements IPerPlayerStore {

    @Unique
    private Object2LongMap<QuestKey> mqt$ppTaskProgress;
    @Unique
    private Object2LongMap<QuestKey> mqt$ppStarted;
    @Unique
    private Object2LongMap<QuestKey> mqt$ppCompleted;
    @Unique
    private Object2LongMap<QuestKey> mqt$ppCompletionCount;
    @Unique
    private Object2LongMap<QuestKey> mqt$ppRepeatableTime;

    @Unique
    private static Object2LongMap<QuestKey> mqt$newMap() {
        Object2LongMap<QuestKey> m = new Object2LongOpenHashMap<>();
        m.defaultReturnValue(0L);
        return m;
    }

    @Unique
    private UUID ppq$activeActor = null;

    @Override
    public void ppq$beginPerPlayer(UUID actor) {
        this.ppq$activeActor = actor;
    }

    @Override
    public void ppq$endPerPlayer() {
        this.ppq$activeActor = null;
    }

    @Override
    public UUID ppq$activeActor() {
        return this.ppq$activeActor;
    }

    @Unique
    private int ppq$forceCollapsedDepsDepth = 0;

    @Override
    public void ppq$beginForceCollapsedDeps() {
        this.ppq$forceCollapsedDepsDepth++;
    }

    @Override
    public void ppq$endForceCollapsedDeps() {
        if (this.ppq$forceCollapsedDepsDepth > 0) this.ppq$forceCollapsedDepsDepth--;
    }

    @Override
    public boolean ppq$forceCollapsedDeps() {
        return this.ppq$forceCollapsedDepsDepth > 0;
    }

    @Unique
    private final ArrayDeque<UUID> ppq$forcedActorStack = new ArrayDeque<>();

    @Override
    public void ppq$beginForceActorDeps(UUID actor) {
        this.ppq$forcedActorStack.push(actor);
    }

    @Override
    public void ppq$endForceActorDeps() {
        if (!this.ppq$forcedActorStack.isEmpty()) this.ppq$forcedActorStack.pop();
    }

    @Override
    public UUID ppq$forcedActor() {
        return this.ppq$forcedActorStack.peek();
    }

    @Unique
    private Object2LongMap<QuestKey> mqt$taskProgressMap() {
        if (mqt$ppTaskProgress == null) mqt$ppTaskProgress = mqt$newMap();
        return mqt$ppTaskProgress;
    }

    @Unique
    private Object2LongMap<QuestKey> mqt$startedMap() {
        if (mqt$ppStarted == null) mqt$ppStarted = mqt$newMap();
        return mqt$ppStarted;
    }

    @Unique
    private Object2LongMap<QuestKey> mqt$completedMap() {
        if (mqt$ppCompleted == null) mqt$ppCompleted = mqt$newMap();
        return mqt$ppCompleted;
    }

    @Unique
    private Object2LongMap<QuestKey> mqt$completionCountMap() {
        if (mqt$ppCompletionCount == null) mqt$ppCompletionCount = mqt$newMap();
        return mqt$ppCompletionCount;
    }

    @Unique
    private Object2LongMap<QuestKey> mqt$repeatableTimeMap() {
        if (mqt$ppRepeatableTime == null) mqt$ppRepeatableTime = mqt$newMap();
        return mqt$ppRepeatableTime;
    }

    @Inject(method = "getOnlineMembers", at = @At("RETURN"), cancellable = true, remap = false)
    private void ppq$scopeOnlineMembersToActor(CallbackInfoReturnable<Collection<ServerPlayer>> cir) {
        UUID actor = this.ppq$activeActor;
        if (actor == null) return;
        for (ServerPlayer p : cir.getReturnValue()) {
            if (p.getUUID().equals(actor)) {
                cir.setReturnValue(List.of(p));
                return;
            }
        }
        cir.setReturnValue(List.of());
    }

    @Override
    public long mqt$getPerPlayerProgress(UUID player, long taskId) {
        return mqt$taskProgressMap().getLong(QuestKey.create(player, taskId));
    }

    @Override
    public void mqt$setPerPlayerProgress(UUID player, long taskId, long progress) {
        QuestKey key = QuestKey.create(player, taskId);
        if (progress <= 0L) {
            mqt$taskProgressMap().removeLong(key);
        } else {
            mqt$taskProgressMap().put(key, progress);
        }
    }

    @Override
    public boolean mqt$isPerPlayerStarted(UUID player, long objId) {
        return mqt$startedMap().containsKey(QuestKey.create(player, objId));
    }

    @Override
    public void mqt$setPerPlayerStarted(UUID player, long objId, boolean started) {
        QuestKey key = QuestKey.create(player, objId);
        if (started) {
            mqt$startedMap().put(key, System.currentTimeMillis());
        } else {
            mqt$startedMap().removeLong(key);
        }
    }

    @Override
    public boolean mqt$isPerPlayerCompleted(UUID player, long objId) {
        return mqt$completedMap().containsKey(QuestKey.create(player, objId));
    }

    @Override
    public void mqt$setPerPlayerCompleted(UUID player, long objId, boolean completed) {
        QuestKey key = QuestKey.create(player, objId);
        if (completed) {
            mqt$completedMap().put(key, System.currentTimeMillis());
        } else {
            mqt$completedMap().removeLong(key);
        }
    }

    @Override
    public int mqt$getPerPlayerCompletionCount(UUID player, long questId) {
        return (int) mqt$completionCountMap().getLong(QuestKey.create(player, questId));
    }

    @Override
    public void mqt$setPerPlayerCompletionCount(UUID player, long questId, int count) {
        QuestKey key = QuestKey.create(player, questId);
        if (count <= 0) {
            mqt$completionCountMap().removeLong(key);
        } else {
            mqt$completionCountMap().put(key, count);
        }
    }

    @Override
    public long mqt$getPerPlayerRepeatableTime(UUID player, long questId) {
        return mqt$repeatableTimeMap().getLong(QuestKey.create(player, questId));
    }

    @Override
    public void mqt$setPerPlayerRepeatableTime(UUID player, long questId, long when) {
        QuestKey key = QuestKey.create(player, questId);
        if (when <= 0L) {
            mqt$repeatableTimeMap().removeLong(key);
        } else {
            mqt$repeatableTimeMap().put(key, when);
        }
    }

    @Override
    public void mqt$clearPerPlayerRepeatState(UUID player, long questId) {
        QuestKey key = QuestKey.create(player, questId);
        mqt$completionCountMap().removeLong(key);
        mqt$repeatableTimeMap().removeLong(key);
    }

    @Override
    public void mqt$clearPerPlayer(UUID player, long objId) {
        QuestKey key = QuestKey.create(player, objId);
        mqt$taskProgressMap().removeLong(key);
        mqt$startedMap().removeLong(key);
        mqt$completedMap().removeLong(key);
        mqt$completionCountMap().removeLong(key);
        mqt$repeatableTimeMap().removeLong(key);
    }

    @Override
    public void mqt$clearAllFor(UUID player) {
        mqt$taskProgressMap().keySet().removeIf(k -> k.uuid().equals(player));
        mqt$startedMap().keySet().removeIf(k -> k.uuid().equals(player));
        mqt$completedMap().keySet().removeIf(k -> k.uuid().equals(player));
        mqt$completionCountMap().keySet().removeIf(k -> k.uuid().equals(player));
        mqt$repeatableTimeMap().keySet().removeIf(k -> k.uuid().equals(player));
    }

    @Override
    public void mqt$clearEverything() {
        mqt$taskProgressMap().clear();
        mqt$startedMap().clear();
        mqt$completedMap().clear();
        mqt$completionCountMap().clear();
        mqt$repeatableTimeMap().clear();
    }

    @Unique
    private void mqt$clearAllPlayersFor(long objId) {
        mqt$taskProgressMap().keySet().removeIf(k -> k.id() == objId);
        mqt$startedMap().keySet().removeIf(k -> k.id() == objId);
        mqt$completedMap().keySet().removeIf(k -> k.id() == objId);
        mqt$completionCountMap().keySet().removeIf(k -> k.id() == objId);
        mqt$repeatableTimeMap().keySet().removeIf(k -> k.id() == objId);
    }

    @Inject(method = "resetProgress", at = @At("HEAD"), remap = false)
    private void mqt$resetProgressPerPlayer(Task task, CallbackInfo ci) {
        mqt$clearAllPlayersFor(task.id);
    }

    @Override
    public void mqt$copyPlayerFrom(IPerPlayerStore other, UUID player, long objId) {
        long prog = other.mqt$getPerPlayerProgress(player, objId);
        if (prog != 0L) mqt$taskProgressMap().put(QuestKey.create(player, objId), prog);
        if (other.mqt$isPerPlayerStarted(player, objId)) mqt$setPerPlayerStarted(player, objId, true);
        if (other.mqt$isPerPlayerCompleted(player, objId)) mqt$setPerPlayerCompleted(player, objId, true);
        int count = other.mqt$getPerPlayerCompletionCount(player, objId);
        if (count > 0) mqt$setPerPlayerCompletionCount(player, objId, count);
        long when = other.mqt$getPerPlayerRepeatableTime(player, objId);
        if (when > 0L) mqt$setPerPlayerRepeatableTime(player, objId, when);
    }

    @Override
    public void mqt$forEachEntry(EntryConsumer consumer) {
        Set<QuestKey> keys = new HashSet<>();
        keys.addAll(mqt$taskProgressMap().keySet());
        keys.addAll(mqt$startedMap().keySet());
        keys.addAll(mqt$completedMap().keySet());
        for (QuestKey k : keys) {
            consumer.accept(
                    k.uuid(), k.id(),
                    mqt$taskProgressMap().getLong(k),
                    mqt$startedMap().containsKey(k),
                    mqt$completedMap().containsKey(k));
        }
    }

    @Override
    public void mqt$forEachRepeatEntry(RepeatEntryConsumer consumer) {
        Set<QuestKey> keys = new HashSet<>();
        keys.addAll(mqt$completionCountMap().keySet());
        keys.addAll(mqt$repeatableTimeMap().keySet());
        for (QuestKey k : keys) {
            consumer.accept(
                    k.uuid(), k.id(),
                    (int) mqt$completionCountMap().getLong(k),
                    mqt$repeatableTimeMap().getLong(k));
        }
    }

    @Override
    public void mqt$copyAllFrom(IPerPlayerStore other) {
        other.mqt$forEachEntry((player, objId, prog, started, completed) -> {
            if (prog != 0L) mqt$taskProgressMap().put(QuestKey.create(player, objId), prog);
            if (started) mqt$setPerPlayerStarted(player, objId, true);
            if (completed) mqt$setPerPlayerCompleted(player, objId, true);
        });
        other.mqt$forEachRepeatEntry((player, questId, count, when) -> {
            if (count > 0) mqt$setPerPlayerCompletionCount(player, questId, count);
            if (when > 0L) mqt$setPerPlayerRepeatableTime(player, questId, when);
        });
    }

    @Inject(method = "writeNetData", at = @At("TAIL"), remap = false)
    private void mqt$writeNetPerPlayer(FriendlyByteBuf buffer, CallbackInfo ci) {
        mqt$writeNetMap(buffer, mqt$taskProgressMap());
        mqt$writeNetMap(buffer, mqt$startedMap());
        mqt$writeNetMap(buffer, mqt$completedMap());
        mqt$writeNetMap(buffer, mqt$completionCountMap());
        mqt$writeNetMap(buffer, mqt$repeatableTimeMap());
    }

    @Inject(method = "readNetData", at = @At("TAIL"), remap = false)
    private void mqt$readNetPerPlayer(FriendlyByteBuf buffer, CallbackInfo ci) {
        mqt$taskProgressMap().clear();
        mqt$startedMap().clear();
        mqt$completedMap().clear();
        mqt$completionCountMap().clear();
        mqt$repeatableTimeMap().clear();
        mqt$readNetMap(buffer, mqt$taskProgressMap());
        mqt$readNetMap(buffer, mqt$startedMap());
        mqt$readNetMap(buffer, mqt$completedMap());
        mqt$readNetMap(buffer, mqt$completionCountMap());
        mqt$readNetMap(buffer, mqt$repeatableTimeMap());
    }

    @Unique
    private static void mqt$writeNetMap(FriendlyByteBuf buffer, Object2LongMap<QuestKey> map) {
        buffer.writeVarInt(map.size());
        for (Object2LongMap.Entry<QuestKey> e : map.object2LongEntrySet()) {
            QuestKey qk = e.getKey();
            buffer.writeUUID(qk.uuid());
            buffer.writeLong(qk.id());
            buffer.writeVarLong(e.getLongValue());
        }
    }

    @Unique
    private static void mqt$readNetMap(FriendlyByteBuf buffer, Object2LongMap<QuestKey> map) {
        int n = buffer.readVarInt();
        for (int i = 0; i < n; i++) {
            UUID uuid = buffer.readUUID();
            long id = buffer.readLong();
            long value = buffer.readVarLong();
            if (value != 0L) {
                map.put(QuestKey.create(uuid, id), value);
            }
        }
    }

    @Inject(method = "serializeNBT", at = @At("RETURN"), remap = false)
    private void mqt$writePerPlayer(CallbackInfoReturnable<SNBTCompoundTag> cir) {
        SNBTCompoundTag root = cir.getReturnValue();
        if (root == null) return;
        SNBTCompoundTag pp = new SNBTCompoundTag();
        mqt$writeMap(pp, mqt$taskProgressMap(), "task_progress");
        mqt$writeMap(pp, mqt$startedMap(), "started");
        mqt$writeMap(pp, mqt$completedMap(), "completed");
        mqt$writeMap(pp, mqt$completionCountMap(), "completion_count");
        mqt$writeMap(pp, mqt$repeatableTimeMap(), "repeatable_time");
        root.put("mqt_per_player", pp);
    }

    @Inject(method = "mergeData", at = @At("TAIL"), remap = false)
    private void mqt$mergePerPlayer(TeamData from, CallbackInfo ci) {
        if (from instanceof IPerPlayerStore other) {
            mqt$copyAllFrom(other);
        }
    }

    @Inject(method = "copyData", at = @At("TAIL"), remap = false)
    private void mqt$copyPerPlayer(TeamData from, CallbackInfo ci) {
        if (from instanceof IPerPlayerStore other) {
            mqt$copyAllFrom(other);
        }
    }

    @Inject(method = "deserializeNBT", at = @At("TAIL"), remap = false)
    private void mqt$readPerPlayer(SNBTCompoundTag nbt, CallbackInfo ci) {
        mqt$taskProgressMap().clear();
        mqt$startedMap().clear();
        mqt$completedMap().clear();
        mqt$completionCountMap().clear();
        mqt$repeatableTimeMap().clear();
        if (nbt == null || !nbt.contains("mqt_per_player")) return;
        CompoundTag pp = nbt.getCompound("mqt_per_player");
        mqt$readMap(pp, mqt$taskProgressMap(), "task_progress");
        mqt$readMap(pp, mqt$startedMap(), "started");
        mqt$readMap(pp, mqt$completedMap(), "completed");
        mqt$readMap(pp, mqt$completionCountMap(), "completion_count");
        mqt$readMap(pp, mqt$repeatableTimeMap(), "repeatable_time");
    }

    @Unique
    private static void mqt$writeMap(SNBTCompoundTag parent, Object2LongMap<QuestKey> map, String key) {
        SNBTCompoundTag byPlayer = new SNBTCompoundTag();
        for (Object2LongMap.Entry<QuestKey> e : map.object2LongEntrySet()) {
            QuestKey qk = e.getKey();
            String uuidStr = UndashedUuid.toString(qk.uuid());
            CompoundTag inner = byPlayer.getCompound(uuidStr);
            inner.putLong(QuestObjectBase.getCodeString(qk.id()), e.getLongValue());
            byPlayer.put(uuidStr, inner);
        }
        parent.put(key, byPlayer);
    }

    @Unique
    private static void mqt$readMap(CompoundTag parent, Object2LongMap<QuestKey> map, String key) {
        if (!parent.contains(key)) return;
        CompoundTag byPlayer = parent.getCompound(key);
        for (String uuidStr : byPlayer.getAllKeys()) {
            UUID uuid;
            try {
                uuid = UndashedUuid.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            CompoundTag inner = byPlayer.getCompound(uuidStr);
            for (String idStr : inner.getAllKeys()) {
                long id = QuestObjectBase.parseCodeString(idStr);
                long value = inner.getLong(idStr);
                if (value != 0L) {
                    map.put(QuestKey.create(uuid, id), value);
                }
            }
        }
    }
}
