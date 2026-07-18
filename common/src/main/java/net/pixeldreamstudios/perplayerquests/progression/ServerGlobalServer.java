package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.pixeldreamstudios.perplayerquests.api.IServerGlobalQuest;

public final class ServerGlobalServer {
    private ServerGlobalServer() {}

    public static boolean isGlobalQuest(Quest quest) {
        return quest != null && (Object) quest instanceof IServerGlobalQuest g && g.ppq$isServerGlobal();
    }

    public static boolean isGlobalTask(Task task) {
        return task != null && isGlobalQuest(task.getQuest());
    }

    public static void setProgress(TeamData teamData, Task task, long progress) {
        long max = task.getMaxProgress();
        long clamped = Math.max(0L, Math.min(progress, max));
        long prev = ServerGlobalProgress.getProgress(task.id);
        if (prev == clamped) return;

        ServerGlobalProgress.setProgress(task.id, clamped);
        if (clamped > 0L) ServerGlobalProgress.setStarted(task.id, true);
        else {
            ServerGlobalProgress.setStarted(task.id, false);
            ServerGlobalProgress.setCompleted(task.id, false);
        }

        if (clamped >= max) {
            ServerGlobalProgress.setCompleted(task.id, true);
            cascadeQuest(task.getQuest());
        }

        ServerGlobalSync.broadcast(teamData);
    }

    private static void cascadeQuest(Quest quest) {
        for (Task t : quest.getTasksAsList()) {
            if (ServerGlobalProgress.getProgress(t.id) < t.getMaxProgress()) {
                ServerGlobalProgress.setCompleted(quest.id, false);
                return;
            }
        }
        ServerGlobalProgress.setCompleted(quest.id, true);
    }
}
