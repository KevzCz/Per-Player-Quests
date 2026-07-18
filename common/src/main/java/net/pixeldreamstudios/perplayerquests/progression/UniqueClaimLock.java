package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;

import java.util.UUID;

public final class UniqueClaimLock {
    private UniqueClaimLock() {}

    private static boolean isUnique(Quest quest) {
        if (quest == null) return false;
        if ((Object) quest instanceof IUniqueQuest u && u.mqt$isUniqueGlobal()) return true;
        return UniqueInheritance.inherits(quest);
    }

    public static boolean isLockedForViewer(TeamData teamData, Quest quest) {
        if (!isUnique(quest)) return false;
        BaseQuestFile file = teamData == null ? null : teamData.getFile();
        if (file != null && file.isServerSide()) {
            if (UniqueInheritance.inherits(quest)) {
                UUID owner = UniqueInheritance.owner(quest);
                if (owner == null) return true;
                UUID actor = PerPlayerActor.resolve(teamData);
                return actor == null || !actor.equals(owner);
            }
            return UniqueClaims.isLockedForActor(teamData, quest.id);
        }
        return ClientClaimView.isClaimedByOther(quest);
    }

    public static Component reasonForViewer(TeamData teamData, Quest quest) {
        if (!isLockedForViewer(teamData, quest)) return null;
        String owner = displayName(teamData, quest);
        return Component.translatable("perplayerquests.unique.locked_claimed", owner)
                .withStyle(ChatFormatting.RED);
    }

    private static String displayName(TeamData teamData, Quest quest) {
        BaseQuestFile file = teamData == null ? null : teamData.getFile();
        boolean anonymous = isAnonymous(quest);

        if (file != null && file.isServerSide()) {
            UUID owner = UniqueInheritance.inherits(quest)
                    ? UniqueInheritance.owner(quest)
                    : UniqueClaims.owner(quest.id);
            if (owner == null) return anonymousLabel();
            if (anonymous) return anonymousLabel();
            if (ServerQuestFile.INSTANCE != null) {
                ServerPlayer p = ServerQuestFile.INSTANCE.server.getPlayerList().getPlayer(owner);
                if (p != null) return p.getGameProfile().getName();
            }
            return anonymousLabel();
        }

        String clientName = ClientClaimView.ownerName(quest);
        return clientName == null || clientName.isBlank() ? anonymousLabel() : clientName;
    }

    private static boolean isAnonymous(Quest quest) {
        return isUnique(quest)
                && ((IUniqueQuest) (Object) quest).mqt$getClaimantDisplay() == IUniqueQuest.ClaimantDisplay.ANONYMOUS;
    }

    private static String anonymousLabel() {
        return Component.translatable("perplayerquests.unique.somebody").getString();
    }
}
