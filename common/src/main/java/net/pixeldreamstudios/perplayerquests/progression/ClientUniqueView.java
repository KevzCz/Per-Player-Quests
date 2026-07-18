package net.pixeldreamstudios.perplayerquests.progression;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class ClientUniqueView {
    private ClientUniqueView() {}

    static boolean isClaimedByOther(long questId) {
        return UniqueClaimClientCache.isClaimedByOther(questId);
    }
}
