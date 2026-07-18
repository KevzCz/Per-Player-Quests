package net.pixeldreamstudios.perplayerquests.progression;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;
import net.pixeldreamstudios.perplayerquests.network.UniqueClaimSyncPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class UniqueSync {
    private UniqueSync() {}

    public static void broadcastAll(MinecraftServer server) {
        if (server == null) return;
        UniqueClaimSyncPacket packet = buildPacket(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            NetworkManager.sendToPlayer(p, packet);
        }
    }

    public static void sendTo(ServerPlayer player) {
        if (player == null || player.server == null) return;
        NetworkManager.sendToPlayer(player, buildPacket(player.server));
    }

    public static void broadcastClaim(MinecraftServer server, long questId, UUID owner) {
        broadcastAll(server);
    }

    private static UniqueClaimSyncPacket buildPacket(MinecraftServer server) {
        Map<Long, UUID> snapshot = UniqueClaims.snapshot();
        List<UniqueClaimSyncPacket.Entry> entries = new ArrayList<>(snapshot.size());
        snapshot.forEach((questId, owner) -> {
            ServerPlayer p = server.getPlayerList().getPlayer(owner);
            String realName = p != null ? p.getGameProfile().getName() : "";

            String name = realName;
            if (ServerQuestFile.INSTANCE != null) {
                QuestObjectBase obj = ServerQuestFile.INSTANCE.getBase(questId);
                if (obj instanceof IUniqueQuest u
                        && u.mqt$getClaimantDisplay() == IUniqueQuest.ClaimantDisplay.ANONYMOUS) {
                    name = "";
                }
            }

            entries.add(new UniqueClaimSyncPacket.Entry(questId, owner, name));
        });
        return new UniqueClaimSyncPacket(entries);
    }
}
