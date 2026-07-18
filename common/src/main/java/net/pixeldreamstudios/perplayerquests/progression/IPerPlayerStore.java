package net.pixeldreamstudios.perplayerquests.progression;

import java.util.UUID;

public interface IPerPlayerStore {
    long mqt$getPerPlayerProgress(UUID player, long taskId);

    void mqt$setPerPlayerProgress(UUID player, long taskId, long progress);

    boolean mqt$isPerPlayerStarted(UUID player, long objId);

    void mqt$setPerPlayerStarted(UUID player, long objId, boolean started);

    boolean mqt$isPerPlayerCompleted(UUID player, long objId);

    void mqt$setPerPlayerCompleted(UUID player, long objId, boolean completed);

    void mqt$clearPerPlayer(UUID player, long objId);

    void mqt$clearAllFor(UUID player);

    void mqt$clearEverything();

    void mqt$copyPlayerFrom(IPerPlayerStore other, UUID player, long objId);

    void mqt$copyAllFrom(IPerPlayerStore other);

    void mqt$forEachEntry(EntryConsumer consumer);

    int mqt$getPerPlayerCompletionCount(UUID player, long questId);

    void mqt$setPerPlayerCompletionCount(UUID player, long questId, int count);

    long mqt$getPerPlayerRepeatableTime(UUID player, long questId);

    void mqt$setPerPlayerRepeatableTime(UUID player, long questId, long when);

    void mqt$clearPerPlayerRepeatState(UUID player, long questId);

    void mqt$forEachRepeatEntry(RepeatEntryConsumer consumer);

    void ppq$beginPerPlayer(UUID actor);

    void ppq$endPerPlayer();

    UUID ppq$activeActor();

    void ppq$beginForceCollapsedDeps();

    void ppq$endForceCollapsedDeps();

    boolean ppq$forceCollapsedDeps();

    void ppq$beginForceActorDeps(UUID actor);

    void ppq$endForceActorDeps();

    UUID ppq$forcedActor();

    @FunctionalInterface
    interface EntryConsumer {
        void accept(UUID player, long objId, long taskProgress, boolean started, boolean completed);
    }

    @FunctionalInterface
    interface RepeatEntryConsumer {
        void accept(UUID player, long questId, int completionCount, long repeatableTime);
    }
}
