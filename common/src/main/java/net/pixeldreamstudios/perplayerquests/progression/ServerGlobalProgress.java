package net.pixeldreamstudios.perplayerquests.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerGlobalProgress {
    private ServerGlobalProgress() {}

    private static final Map<Long, Long> PROGRESS = new ConcurrentHashMap<>();
    private static final Map<Long, Long> STARTED = new ConcurrentHashMap<>();
    private static final Map<Long, Long> COMPLETED = new ConcurrentHashMap<>();

    public static long getProgress(long taskId) {
        return PROGRESS.getOrDefault(taskId, 0L);
    }

    public static void setProgress(long taskId, long value) {
        if (value <= 0L) PROGRESS.remove(taskId);
        else PROGRESS.put(taskId, value);
    }

    public static boolean isStarted(long objId) {
        return STARTED.containsKey(objId);
    }

    public static void setStarted(long objId, boolean started) {
        if (started) STARTED.put(objId, System.currentTimeMillis());
        else STARTED.remove(objId);
    }

    public static boolean isCompleted(long objId) {
        return COMPLETED.containsKey(objId);
    }

    public static void setCompleted(long objId, boolean completed) {
        if (completed) COMPLETED.put(objId, System.currentTimeMillis());
        else COMPLETED.remove(objId);
    }

    public static void clear(long objId) {
        PROGRESS.remove(objId);
        STARTED.remove(objId);
        COMPLETED.remove(objId);
    }

    public static void clearAll() {
        PROGRESS.clear();
        STARTED.clear();
        COMPLETED.clear();
    }

    public static Map<Long, Long> snapshotProgress() {
        return new HashMap<>(PROGRESS);
    }

    public static CompoundTag save() {
        CompoundTag root = new CompoundTag();
        root.put("progress", writeMap(PROGRESS));
        root.put("started", writeMap(STARTED));
        root.put("completed", writeMap(COMPLETED));
        return root;
    }

    public static void load(CompoundTag root) {
        clearAll();
        if (root == null) return;
        readMap(root.getList("progress", Tag.TAG_COMPOUND), PROGRESS);
        readMap(root.getList("started", Tag.TAG_COMPOUND), STARTED);
        readMap(root.getList("completed", Tag.TAG_COMPOUND), COMPLETED);
    }

    private static ListTag writeMap(Map<Long, Long> map) {
        ListTag list = new ListTag();
        map.forEach((id, v) -> {
            CompoundTag e = new CompoundTag();
            e.putLong("id", id);
            e.putLong("v", v);
            list.add(e);
        });
        return list;
    }

    private static void readMap(ListTag list, Map<Long, Long> map) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            map.put(e.getLong("id"), e.getLong("v"));
        }
    }
}
