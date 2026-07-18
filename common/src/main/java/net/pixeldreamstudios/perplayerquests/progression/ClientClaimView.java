package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class ClientClaimView {
    private ClientClaimView() {}

    public static boolean isClaimedByOther(Quest quest) {
        UUID owner = resolveOwner(quest);
        if (owner == null) return false;
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null || !owner.equals(mc.player.getUUID());
    }

    public static boolean isClaimed(Quest quest) {
        return resolveOwner(quest) != null;
    }

    public static String ownerName(Quest quest) {
        if (UniqueInheritance.inherits(quest)) {
            Quest source = sourceQuest(quest);
            return source == null ? null : UniqueClaimClientCache.ownerName(source.id);
        }
        return UniqueClaimClientCache.ownerName(quest.id);
    }

    private static UUID resolveOwner(Quest quest) {
        if (UniqueInheritance.inherits(quest)) {
            Quest source = sourceQuest(quest);
            return source == null ? null : UniqueClaimClientCache.owner(source.id);
        }
        return UniqueClaimClientCache.owner(quest.id);
    }

    private static Quest sourceQuest(Quest quest) {
        if (quest == null) return null;
        for (QuestObject dep : quest.streamDependencies().toList()) {
            if (dep instanceof Quest depQuest) {
                if ((Object) depQuest instanceof IUniqueQuest u && u.mqt$isUniqueGlobal()) return depQuest;
                if (UniqueInheritance.inherits(depQuest)) {
                    Quest upstream = sourceQuest(depQuest);
                    if (upstream != null) return upstream;
                }
            }
        }
        return null;
    }
}
