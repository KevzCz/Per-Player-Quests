package net.pixeldreamstudios.perplayerquests;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.ftb.mods.ftbquests.events.ClaimRewardEvent;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerLifecycle;
import net.pixeldreamstudios.perplayerquests.progression.ServerGlobalStorage;
import net.pixeldreamstudios.perplayerquests.progression.UniqueClaimStorage;
import net.pixeldreamstudios.perplayerquests.progression.UniqueGate;

public final class PerPlayerQuests {
    public static final String MOD_ID = "perplayerquests";

    public static void init() {
        PlayerEvent.PLAYER_JOIN.register((ServerPlayer player) -> PerPlayerLifecycle.onJoin(player));

        LifecycleEvent.SERVER_STARTED.register(UniqueClaimStorage::load);
        LifecycleEvent.SERVER_STOPPING.register(UniqueClaimStorage::save);
        LifecycleEvent.SERVER_STARTED.register(ServerGlobalStorage::load);
        LifecycleEvent.SERVER_STOPPING.register(ServerGlobalStorage::save);

        ClaimRewardEvent.Pre.EVENT.register(UniqueGate::onRewardClaim);
    }
}
