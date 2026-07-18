package net.pixeldreamstudios.perplayerquests.progression;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.network.ServerGlobalSyncPacket;

import java.util.ArrayList;
import java.util.List;

public final class ServerGlobalSync {
    private ServerGlobalSync() {}

    public static void broadcast(TeamData teamData) {
        if (!(teamData.getFile() instanceof ServerQuestFile sqf)) return;
        MinecraftServer server = sqf.server;
        if (server == null) return;
        broadcastAll(server);
    }

    public static void broadcastAll(MinecraftServer server) {
        ServerGlobalSyncPacket packet = build();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            NetworkManager.sendToPlayer(p, packet);
        }
    }

    public static void sendTo(ServerPlayer player) {
        NetworkManager.sendToPlayer(player, build());
    }

    private static ServerGlobalSyncPacket build() {
        List<long[]> progress = new ArrayList<>();
        List<Long> started = new ArrayList<>();
        List<Long> completed = new ArrayList<>();

        BaseQuestFile file = ServerQuestFile.INSTANCE;
        if (file != null) {
            file.forAllQuests(quest -> {
                if (!ServerGlobalServer.isGlobalQuest(quest)) return;
                if (ServerGlobalProgress.isStarted(quest.id)) started.add(quest.id);
                if (ServerGlobalProgress.isCompleted(quest.id)) completed.add(quest.id);
                for (Task task : quest.getTasks()) {
                    long prog = ServerGlobalProgress.getProgress(task.id);
                    if (prog != 0L) progress.add(new long[]{task.id, prog});
                    if (ServerGlobalProgress.isStarted(task.id)) started.add(task.id);
                    if (ServerGlobalProgress.isCompleted(task.id)) completed.add(task.id);
                }
            });
        }

        long[] startedArr = started.stream().mapToLong(Long::longValue).toArray();
        long[] completedArr = completed.stream().mapToLong(Long::longValue).toArray();
        return new ServerGlobalSyncPacket(progress, startedArr, completedArr);
    }
}
