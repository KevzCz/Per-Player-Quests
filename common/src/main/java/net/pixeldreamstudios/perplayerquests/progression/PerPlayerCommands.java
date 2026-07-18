package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PerPlayerCommands {
    private PerPlayerCommands() {}

    private static long resolveId(BaseQuestFile file, String idOrCode) {
        try {
            return QuestObjectBase.parseHexId(idOrCode.trim()).orElse(0L);
        } catch (Throwable t) {
            return 0L;
        }
    }

    public static int reset(CommandSourceStack source, Collection<ServerPlayer> players, String questObjectId) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) { source.sendFailure(Component.literal("No quest file loaded.")); return 0; }
        long id = resolveId(file, questObjectId);
        QuestObjectBase obj = file.getBase(id);
        if (obj == null) { source.sendFailure(Component.literal("Object not found: " + questObjectId)); return 0; }

        int count = 0;
        Set<TeamData> touchedTeams = new HashSet<>();
        for (ServerPlayer player : players) {
            TeamData data = file.getTeamData(player).orElse(null);
            if (!(data instanceof IPerPlayerStore store)) continue;
            UUID uuid = player.getUUID();
            if (obj instanceof Task task) {
                store.mqt$clearPerPlayer(uuid, task.id);
            } else if (obj instanceof Quest quest) {
                for (Task t : quest.getTasksAsList()) store.mqt$clearPerPlayer(uuid, t.id);
                store.mqt$clearPerPlayer(uuid, quest.id);
            }
            data.markDirty();
            PerPlayerServer.syncTo(data, uuid);
            touchedTeams.add(data);
            count++;
        }
        for (TeamData data : touchedTeams) PerPlayerServer.broadcastTeam(data);
        int fc = count;
        source.sendSuccess(() -> Component.literal("Reset per-player progress for " + fc + " player(s)."), true);
        return count;
    }

    public static int complete(CommandSourceStack source, Collection<ServerPlayer> players, String questObjectId) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) { source.sendFailure(Component.literal("No quest file loaded.")); return 0; }
        long id = resolveId(file, questObjectId);
        QuestObjectBase obj = file.getBase(id);
        if (obj == null) { source.sendFailure(Component.literal("Object not found: " + questObjectId)); return 0; }

        int count = 0;
        Set<TeamData> touchedTeams = new HashSet<>();
        for (ServerPlayer player : players) {
            TeamData data = file.getTeamData(player).orElse(null);
            if (!(data instanceof IPerPlayerStore)) continue;
            file.withPlayerContext(player, () -> {
                if (obj instanceof Task task) {
                    data.setProgress(task, task.getMaxProgress());
                } else if (obj instanceof Quest quest) {
                    for (Task t : quest.getTasksAsList()) data.setProgress(t, t.getMaxProgress());
                }
            });
            PerPlayerServer.syncTo(data, player.getUUID());
            touchedTeams.add(data);
            count++;
        }
        for (TeamData data : touchedTeams) PerPlayerServer.broadcastTeam(data);
        int fc = count;
        source.sendSuccess(() -> Component.literal("Completed per-player for " + fc + " player(s)."), true);
        return count;
    }

    public static int resetChapter(CommandSourceStack source, Collection<ServerPlayer> players, String chapterId) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) { source.sendFailure(Component.literal("No quest file loaded.")); return 0; }
        long id = resolveId(file, chapterId);
        QuestObjectBase obj = file.getBase(id);
        if (!(obj instanceof Chapter chapter)) {
            source.sendFailure(Component.literal("Not a chapter: " + chapterId));
            return 0;
        }

        int count = 0;
        Set<TeamData> touchedTeams = new HashSet<>();
        for (ServerPlayer player : players) {
            TeamData data = file.getTeamData(player).orElse(null);
            if (!(data instanceof IPerPlayerStore store)) continue;
            UUID uuid = player.getUUID();
            file.withPlayerContext(player, () -> {
                for (Quest quest : chapter.getQuests()) PerPlayerReset.resetQuest(store, data, uuid, quest);
            });
            data.markDirty();
            PerPlayerServer.syncTo(data, uuid);
            touchedTeams.add(data);
            count++;
        }
        for (TeamData data : touchedTeams) PerPlayerServer.broadcastTeam(data);
        int fc = count;
        source.sendSuccess(() -> Component.literal("Reset per-player progress in chapter for " + fc + " player(s)."), true);
        return count;
    }

    public static int resetAll(CommandSourceStack source, Collection<ServerPlayer> players) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) { source.sendFailure(Component.literal("No quest file loaded.")); return 0; }

        int count = 0;
        Set<TeamData> touchedTeams = new HashSet<>();
        for (ServerPlayer player : players) {
            TeamData data = file.getTeamData(player).orElse(null);
            if (!(data instanceof IPerPlayerStore store)) continue;
            UUID uuid = player.getUUID();
            file.withPlayerContext(player, () ->
                    file.forAllQuests(quest -> PerPlayerReset.resetQuest(store, data, uuid, quest)));
            data.markDirty();
            PerPlayerServer.syncTo(data, uuid);
            touchedTeams.add(data);
            count++;
        }
        for (TeamData data : touchedTeams) PerPlayerServer.broadcastTeam(data);
        int fc = count;
        source.sendSuccess(() -> Component.literal("Reset all per-player progress for " + fc + " player(s)."), true);
        return count;
    }

    public static int resync(CommandSourceStack source, Collection<ServerPlayer> players) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) { source.sendFailure(Component.literal("No quest file loaded.")); return 0; }
        int count = 0;
        Set<TeamData> touchedTeams = new HashSet<>();
        for (ServerPlayer player : players) {
            TeamData data = file.getTeamData(player).orElse(null);
            if (data == null) continue;
            PerPlayerServer.syncTo(data, player.getUUID());
            UniqueSync.sendTo(player);
            touchedTeams.add(data);
            count++;
        }
        for (TeamData data : touchedTeams) PerPlayerServer.broadcastTeam(data);
        int fc = count;
        source.sendSuccess(() -> Component.literal("Resynced " + fc + " player(s)."), true);
        return count;
    }

    public static List<ServerPlayer> onlineMembers(TeamData data) {
        return List.copyOf(data.getOnlineMembers());
    }

    public static int releaseUnique(CommandSourceStack source, String questObjectId) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) { source.sendFailure(Component.literal("No quest file loaded.")); return 0; }
        long id = resolveId(file, questObjectId);
        QuestObjectBase obj = file.getBase(id);
        if (!(obj instanceof Quest quest) || !UniqueGate.isUnique(quest)) {
            source.sendFailure(Component.literal("Not a UNIQUE quest: " + questObjectId));
            return 0;
        }
        if (!UniqueClaims.isClaimed(quest.id)) {
            source.sendFailure(Component.literal("Quest is not claimed."));
            return 0;
        }
        UniqueClaims.release(quest.id);
        UniqueSync.broadcastAll(source.getServer());
        source.sendSuccess(() -> Component.literal("Released UNIQUE claim on ")
                .append(quest.getTitle()).append(Component.literal(".")), true);
        return 1;
    }

    public static int resetGlobal(CommandSourceStack source, String questObjectId) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) { source.sendFailure(Component.literal("No quest file loaded.")); return 0; }
        long id = resolveId(file, questObjectId);
        QuestObjectBase obj = file.getBase(id);
        if (!(obj instanceof Quest quest) || !ServerGlobalServer.isGlobalQuest(quest)) {
            source.sendFailure(Component.literal("Not a Server-Global quest: " + questObjectId));
            return 0;
        }
        for (Task t : quest.getTasksAsList()) ServerGlobalProgress.clear(t.id);
        ServerGlobalProgress.clear(quest.id);
        ServerGlobalSync.broadcastAll(source.getServer());
        source.sendSuccess(() -> Component.literal("Reset Server-Global progress for ")
                .append(quest.getTitle()).append(Component.literal(".")), true);
        return 1;
    }
}
