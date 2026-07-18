package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.network.PerPlayerAdminPacket;

import java.util.UUID;

public final class PerPlayerAdminHandler {
    private PerPlayerAdminHandler() {}

    public static void handle(ServerPlayer sender, PerPlayerAdminPacket packet) {
        if (!sender.hasPermissions(2)) return;
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) return;

        boolean resetAll = packet.action() == PerPlayerAdminPacket.Action.RESET_ALL;

        QuestObjectBase obj = null;
        if (!resetAll) {
            obj = file.getBase(packet.objectId());
            if (obj == null) return;

            if (packet.action() == PerPlayerAdminPacket.Action.RELEASE_UNIQUE) {
                handleReleaseUnique(sender, obj);
                return;
            }
            if (packet.action() == PerPlayerAdminPacket.Action.RESET_GLOBAL) {
                handleResetGlobal(sender, obj);
                return;
            }
        }

        ServerPlayer target = sender.server.getPlayerList().getPlayer(packet.target());
        UUID targetId = packet.target();

        TeamData data = target != null
                ? file.getTeamData(target).orElse(null)
                : file.getNullableTeamData(mqt$teamOf(targetId));
        if (!(data instanceof IPerPlayerStore store)) {
            sender.sendSystemMessage(Component.literal("Target has no quest data loaded."));
            return;
        }

        if (resetAll) {
            Runnable doReset = () -> file.forAllQuests(quest -> PerPlayerReset.resetQuest(store, data, targetId, quest));
            if (target != null) {
                file.withPlayerContext(target, doReset);
            } else {
                doReset.run();
            }
            data.markDirty();
            sender.sendSystemMessage(Component.literal("Reset all per-player progress for target."));
        } else {
            QuestObjectBase finalObj = obj;
            switch (packet.action()) {
                case RESET -> {
                    Runnable doReset = () -> {
                        if (finalObj instanceof Chapter chapter) {
                            for (Quest quest : chapter.getQuests()) PerPlayerReset.resetQuest(store, data, targetId, quest);
                        } else {
                            resetObject(store, data, targetId, finalObj);
                        }
                    };
                    if (target != null) {
                        file.withPlayerContext(target, doReset);
                    } else {
                        doReset.run();
                    }
                    data.markDirty();
                    sender.sendSystemMessage(Component.literal("Reset per-player progress for target."));
                }
                case COMPLETE -> {
                    if (target != null) {
                        file.withPlayerContext(target, () -> {
                            if (finalObj instanceof Chapter chapter) {
                                for (Quest quest : chapter.getQuests()) PerPlayerReset.completeQuest(data, quest);
                            } else if (finalObj instanceof Task task) {
                                data.setProgress(task, task.getMaxProgress());
                            } else if (finalObj instanceof Quest quest) {
                                PerPlayerReset.completeQuest(data, quest);
                            }
                        });
                        sender.sendSystemMessage(Component.literal("Completed per-player for target."));
                    } else {
                        sender.sendSystemMessage(Component.literal("Target must be online to force-complete."));
                    }
                }
                default -> {
                }
            }
        }

        data.clearCachedProgress();

        if (target != null) {
            PerPlayerServer.syncTo(data, targetId);
        }
        PerPlayerServer.broadcastTeam(data);
    }

    private static void resetObject(IPerPlayerStore store, TeamData data, UUID targetId, QuestObjectBase obj) {
        ProgressChange change = new ProgressChange(obj, targetId);
        change.setReset(true);
        obj.forceProgressRaw(data, change);
        if (obj instanceof Task task) {
            store.mqt$clearPerPlayer(targetId, task.id);
        } else if (obj instanceof Quest quest) {
            PerPlayerReset.resetQuest(store, data, targetId, quest);
        }
    }

    private static void handleReleaseUnique(ServerPlayer sender, QuestObjectBase obj) {
        if (!(obj instanceof Quest quest)) return;
        if (!UniqueGate.isUnique(quest)) {
            sender.sendSystemMessage(Component.literal("Quest is not UNIQUE."));
            return;
        }
        TeamData data = quest.getQuestFile().getTeamData(sender).orElse(null);
        if (data == null) return;
        boolean wasClaimed = UniqueClaims.isClaimed(quest.id);
        PerPlayerReset.releaseUnique(data, quest.id);
        data.clearCachedProgress();
        if (wasClaimed) {
            sender.sendSystemMessage(Component.literal("Released UNIQUE claim on ")
                    .append(quest.getTitle()).append(Component.literal(".")));
        } else {
            sender.sendSystemMessage(Component.literal("Quest was not claimed."));
        }
    }

    private static void handleResetGlobal(ServerPlayer sender, QuestObjectBase obj) {
        if (!(obj instanceof Quest quest) || !ServerGlobalServer.isGlobalQuest(quest)) {
            sender.sendSystemMessage(Component.literal("Quest is not Server-Global."));
            return;
        }
        for (Task t : quest.getTasksAsList()) {
            ServerGlobalProgress.clear(t.id);
        }
        ServerGlobalProgress.clear(quest.id);

        TeamData data = quest.getQuestFile().getTeamData(sender).orElse(null);
        if (data != null) {
            data.clearCachedProgress();
            ServerGlobalSync.broadcast(data);
        }
        sender.sendSystemMessage(Component.literal("Reset Server-Global progress for ")
                .append(quest.getTitle()).append(Component.literal(".")));
    }

    private static UUID mqt$teamOf(UUID player) {
        return FTBTeamsAPI.api().getManager()
                .getTeamForPlayerID(player).map(t -> t.getTeamId()).orElse(player);
    }
}
