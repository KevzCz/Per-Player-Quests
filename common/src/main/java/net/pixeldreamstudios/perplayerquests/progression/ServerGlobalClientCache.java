package net.pixeldreamstudios.perplayerquests.progression;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.pixeldreamstudios.perplayerquests.network.ServerGlobalSyncPacket;

@Environment(EnvType.CLIENT)
public final class ServerGlobalClientCache {
    private ServerGlobalClientCache() {}

    public static void apply(ServerGlobalSyncPacket packet) {
        ServerGlobalProgress.clearAll();
        for (long[] pair : packet.progress()) {
            ServerGlobalProgress.setProgress(pair[0], pair[1]);
        }
        for (long id : packet.started()) {
            ServerGlobalProgress.setStarted(id, true);
        }
        for (long id : packet.completed()) {
            ServerGlobalProgress.setCompleted(id, true);
        }
        ServerGlobalScreenRefresh.refresh();
    }
}
