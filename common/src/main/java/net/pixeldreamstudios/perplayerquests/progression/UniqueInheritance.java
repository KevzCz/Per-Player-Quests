package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;

import java.util.UUID;

public final class UniqueInheritance {
    private UniqueInheritance() {}

    private static IUniqueQuest asUnique(Quest quest) {
        return quest != null && (Object) quest instanceof IUniqueQuest u ? u : null;
    }

    public static boolean inherits(Quest quest) {
        IUniqueQuest u = asUnique(quest);
        return u != null && u.mqt$getUniqueInherit() == IUniqueQuest.UniqueInherit.FROM_DEPENDENCY;
    }

    private static Quest sourceQuest(Quest quest) {
        if (quest == null) return null;
        for (QuestObject dep : quest.streamDependencies().toList()) {
            if (dep instanceof Quest depQuest) {
                IUniqueQuest u = asUnique(depQuest);
                if (u != null && u.mqt$isUniqueGlobal()) return depQuest;
                if (inherits(depQuest)) {
                    Quest upstream = sourceQuest(depQuest);
                    if (upstream != null) return upstream;
                }
            }
        }
        return null;
    }

    public static UUID owner(Quest quest) {
        if (!inherits(quest)) return null;
        Quest source = sourceQuest(quest);
        return source == null ? null : UniqueClaims.owner(source.id);
    }

    public static boolean isClaimed(Quest quest) {
        return owner(quest) != null;
    }
}
