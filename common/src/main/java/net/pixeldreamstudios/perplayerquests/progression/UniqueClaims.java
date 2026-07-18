package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UniqueClaims {
    private UniqueClaims() {}

    private static final Map<Long, UUID> CLAIMS = new ConcurrentHashMap<>();

    public static UUID owner(long questId) {
        return CLAIMS.get(questId);
    }

    public static boolean isClaimed(long questId) {
        return CLAIMS.containsKey(questId);
    }

    public static boolean tryClaim(long questId, UUID player) {
        UUID prev = CLAIMS.putIfAbsent(questId, player);
        return prev == null || prev.equals(player);
    }

    public static void release(long questId) {
        CLAIMS.remove(questId);
    }

    public static boolean isClaimedByActor(TeamData teamData, long questId) {
        UUID actor = PerPlayerActor.resolve(teamData);
        return actor != null && actor.equals(owner(questId));
    }

    public static boolean isLockedForActor(TeamData teamData, long questId) {
        UUID owner = owner(questId);
        if (owner == null) return false;
        UUID actor = PerPlayerActor.resolve(teamData);
        return actor == null || !actor.equals(owner);
    }

    public static CompoundTag save() {
        CompoundTag root = new CompoundTag();
        ListTag claims = new ListTag();
        CLAIMS.forEach((questId, owner) -> {
            CompoundTag e = new CompoundTag();
            e.putLong("quest", questId);
            e.putUUID("owner", owner);
            claims.add(e);
        });
        root.put("claims", claims);
        return root;
    }

    public static void load(CompoundTag root) {
        CLAIMS.clear();
        if (root == null) return;

        ListTag claims = root.getList("claims", Tag.TAG_COMPOUND);
        for (int i = 0; i < claims.size(); i++) {
            CompoundTag e = claims.getCompound(i);
            CLAIMS.put(e.getLong("quest"), e.getUUID("owner"));
        }
    }

    public static void clearAll() {
        CLAIMS.clear();
    }

    public static Map<Long, UUID> snapshot() {
        return new HashMap<>(CLAIMS);
    }
}
