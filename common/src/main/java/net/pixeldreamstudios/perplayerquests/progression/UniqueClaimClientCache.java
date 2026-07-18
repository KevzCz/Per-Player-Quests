package net.pixeldreamstudios.perplayerquests.progression;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.pixeldreamstudios.perplayerquests.network.UniqueClaimSyncPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class UniqueClaimClientCache {
    private UniqueClaimClientCache() {}

    private static final Map<Long, UUID> OWNER = new HashMap<>();
    private static final Map<Long, String> OWNER_NAME = new HashMap<>();

    public static void apply(UniqueClaimSyncPacket packet) {
        OWNER.clear();
        OWNER_NAME.clear();
        for (UniqueClaimSyncPacket.Entry e : packet.entries()) {
            OWNER.put(e.questId(), e.owner());
            OWNER_NAME.put(e.questId(), e.ownerName());
        }
    }

    public static UUID owner(long questId) {
        return OWNER.get(questId);
    }

    public static String ownerName(long questId) {
        return OWNER_NAME.get(questId);
    }

    public static boolean isClaimed(long questId) {
        return OWNER.containsKey(questId);
    }

    public static boolean isClaimedByOther(long questId) {
        UUID owner = OWNER.get(questId);
        if (owner == null) return false;
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null || !owner.equals(mc.player.getUUID());
    }
}
