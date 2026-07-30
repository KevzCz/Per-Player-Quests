package net.pixeldreamstudios.perplayerquests.progression;

import net.pixeldreamstudios.perplayerquests.network.NetworkHelper;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.network.PerPlayerSyncPacket;
import net.pixeldreamstudios.perplayerquests.network.PerPlayerTeamSyncPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PerPlayerServer {
    private PerPlayerServer() {}

    public static boolean mqt$isPerPlayer(Object questObject) {
        return questObject instanceof IPerPlayerObject ppo && ppo.mqt$isPerPlayer();
    }

    public static void afterTaskProgress(TeamData teamData, Task task, UUID actor) {
        if (!(teamData instanceof IPerPlayerStore store)) return;

        long progress = store.mqt$getPerPlayerProgress(actor, task.id);
        boolean nowComplete = progress >= task.getMaxProgress();
        boolean wasComplete = store.mqt$isPerPlayerCompleted(actor, task.id);

        if (nowComplete && !wasComplete) {
            boolean questWasComplete = store.mqt$isPerPlayerCompleted(actor, task.getQuest().id);
            if (teamData.areDependenciesComplete(task.getQuest())) {
                store.ppq$beginPerPlayer(actor);
                try {
                    teamData.markTaskCompleted(task);
                } finally {
                    store.ppq$endPerPlayer();
                }
            } else {
                store.mqt$setPerPlayerCompleted(actor, task.id, true);
            }
            boolean questNowComplete = store.mqt$isPerPlayerCompleted(actor, task.getQuest().id);
            if (questNowComplete && !questWasComplete) {
                UniqueGate.onCompleted(teamData, task.getQuest(), actor);
            }
        } else if (!nowComplete && wasComplete) {
            store.mqt$setPerPlayerCompleted(actor, task.id, false);
            store.mqt$setPerPlayerCompleted(actor, task.getQuest().id, false);
        }

        syncTo(teamData, actor);
        broadcastTeam(teamData);
    }

    public static void syncTo(TeamData teamData, UUID actor) {
        if (!(teamData instanceof IPerPlayerStore store)) return;
        ServerPlayer player = onlineMember(teamData, actor);
        if (player == null) return;

        List<long[]> taskProgress = new ArrayList<>();
        List<Long> started = new ArrayList<>();
        List<Long> completed = new ArrayList<>();
        List<long[]> repeatState = new ArrayList<>();

        teamData.getFile().forAllQuests(quest -> {
            if (mqt$isPerPlayer(quest)) {
                if (store.mqt$isPerPlayerCompleted(actor, quest.id)) completed.add(quest.id);
                if (store.mqt$isPerPlayerStarted(actor, quest.id)) started.add(quest.id);
                int count = store.mqt$getPerPlayerCompletionCount(actor, quest.id);
                long when = store.mqt$getPerPlayerRepeatableTime(actor, quest.id);
                if (count != 0 || when != 0L) repeatState.add(new long[]{quest.id, count, when});
            }
            for (Task task : quest.getTasks()) {
                if (task instanceof IPerPlayerObject tppo && tppo.mqt$isPerPlayer()) {
                    long prog = store.mqt$getPerPlayerProgress(actor, task.id);
                    if (prog != 0L) taskProgress.add(new long[]{task.id, prog});
                    if (store.mqt$isPerPlayerStarted(actor, task.id)) started.add(task.id);
                    if (store.mqt$isPerPlayerCompleted(actor, task.id)) completed.add(task.id);
                }
            }
        });

        long[] startedArr = started.stream().mapToLong(Long::longValue).toArray();
        long[] completedArr = completed.stream().mapToLong(Long::longValue).toArray();
        NetworkHelper.sendToPlayer(player, new PerPlayerSyncPacket(taskProgress, startedArr, completedArr, repeatState));
    }

    private static ServerPlayer onlineMember(TeamData teamData, UUID actor) {
        for (ServerPlayer p : teamData.getOnlineMembers()) {
            if (p.getUUID().equals(actor)) return p;
        }
        return null;
    }

    public static void broadcastTeam(TeamData teamData) {
        if (!(teamData instanceof IPerPlayerStore store)) return;
        List<ServerPlayer> recipients = realOnlineMembers(teamData);
        if (recipients.isEmpty()) return;
        List<UUID> members = new ArrayList<>();
        for (ServerPlayer p : recipients) members.add(p.getUUID());

        List<PerPlayerTeamSyncPacket.Entry> entries = new ArrayList<>();
        List<PerPlayerTeamSyncPacket.RepeatEntry> repeatEntries = new ArrayList<>();
        teamData.getFile().forAllQuests(quest -> {
            if (mqt$isPerPlayer(quest)) {
                for (UUID member : members) {
                    entries.add(new PerPlayerTeamSyncPacket.Entry(member, quest.id,
                            store.mqt$isPerPlayerStarted(member, quest.id),
                            store.mqt$isPerPlayerCompleted(member, quest.id)));
                    int count = store.mqt$getPerPlayerCompletionCount(member, quest.id);
                    long when = store.mqt$getPerPlayerRepeatableTime(member, quest.id);
                    if (count != 0 || when != 0L) {
                        repeatEntries.add(new PerPlayerTeamSyncPacket.RepeatEntry(member, quest.id, count, when));
                    }
                }
            }
            for (Task task : quest.getTasks()) {
                if (task instanceof IPerPlayerObject tppo && tppo.mqt$isPerPlayer()) {
                    for (UUID member : members) {
                        entries.add(new PerPlayerTeamSyncPacket.Entry(member, task.id,
                                store.mqt$isPerPlayerStarted(member, task.id),
                                store.mqt$isPerPlayerCompleted(member, task.id)));
                    }
                }
            }
        });
        if (entries.isEmpty() && repeatEntries.isEmpty()) return;

        PerPlayerTeamSyncPacket packet = new PerPlayerTeamSyncPacket(entries, repeatEntries);
        for (ServerPlayer p : recipients) {
            NetworkHelper.sendToPlayer(p, packet);
        }
    }

    private static List<ServerPlayer> realOnlineMembers(TeamData teamData) {
        if (teamData.getFile() == null || !teamData.getFile().isServerSide()) return List.of();
        var teamOpt = FTBTeamsAPI.api().getManager().getTeamByID(teamData.getTeamId());
        if (teamOpt.isEmpty()) return List.of();
        return List.copyOf(teamOpt.get().getOnlineMembers());
    }
}
