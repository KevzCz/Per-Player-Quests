package net.pixeldreamstudios.perplayerquests.progression;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;

import java.util.UUID;

public final class UniqueGate {
    private UniqueGate() {}

    public static boolean isUnique(Quest quest) {
        if (quest == null) return false;
        if ((Object) quest instanceof IUniqueQuest u && u.mqt$isUniqueGlobal()) return true;
        return UniqueInheritance.inherits(quest);
    }

    private static IUniqueQuest asUnique(Quest quest) {
        return (IUniqueQuest) (Object) quest;
    }

    public static boolean isLockedForActor(TeamData teamData, Quest quest) {
        return isLockedFor(quest, PerPlayerActor.resolve(teamData));
    }

    public static boolean isLockedFor(Quest quest, UUID actor) {
        if (!isUnique(quest)) return false;
        boolean inherited = UniqueInheritance.inherits(quest);
        UUID owner = inherited ? UniqueInheritance.owner(quest) : UniqueClaims.owner(quest.id);
        if (owner == null) return inherited;
        return actor == null || !actor.equals(owner);
    }

    public static EventResult onRewardClaim(Reward reward, ServerPlayer player) {
        return isLockedFor(reward.getQuest(), player.getUUID()) ? EventResult.interruptFalse() : EventResult.pass();
    }

    public static boolean onProgressAttempt(TeamData teamData, Quest quest, UUID actor) {
        if (!isUnique(quest) || actor == null) return true;
        if (UniqueInheritance.inherits(quest)) {
            UUID inheritedOwner = UniqueInheritance.owner(quest);
            return inheritedOwner != null && inheritedOwner.equals(actor);
        }
        IUniqueQuest u = asUnique(quest);

        UUID owner = UniqueClaims.owner(quest.id);
        if (owner != null) {
            return owner.equals(actor);
        }
        if (u.mqt$getUniqueLock() == IUniqueQuest.UniqueLock.ON_START) {
            if (UniqueClaims.tryClaim(quest.id, actor)) {
                onClaimed(teamData, quest, actor);
                return true;
            }
            return UniqueClaims.owner(quest.id) != null
                    && UniqueClaims.owner(quest.id).equals(actor);
        }
        return true;
    }

    public static void onCompleted(TeamData teamData, Quest quest, UUID actor) {
        if (!isUnique(quest) || actor == null) return;
        if (UniqueInheritance.inherits(quest)) return;
        IUniqueQuest u = asUnique(quest);
        if (u.mqt$getUniqueLock() == IUniqueQuest.UniqueLock.ON_COMPLETE) {
            if (UniqueClaims.tryClaim(quest.id, actor)) {
                onClaimed(teamData, quest, actor);
            }
        }
    }

    private static void onClaimed(TeamData teamData, Quest quest, UUID actor) {
        if (!(teamData.getFile() instanceof ServerQuestFile sqf)) return;
        MinecraftServer server = sqf.server;
        if (server == null) return;

        UniqueSync.broadcastClaim(server, quest.id, actor);

        if (isUnique(quest) && asUnique(quest).mqt$announceClaim()) {
            ServerPlayer p = server.getPlayerList().getPlayer(actor);
            String who = p != null ? p.getGameProfile().getName() : actor.toString();
            Component msg = Component.translatable(
                    "perplayerquests.unique.claimed_broadcast", who, quest.getTitle())
                    .withStyle(ChatFormatting.GOLD);
            server.getPlayerList().broadcastSystemMessage(msg, false);
        }
    }
}
