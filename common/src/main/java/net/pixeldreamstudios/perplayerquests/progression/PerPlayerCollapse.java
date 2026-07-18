package net.pixeldreamstudios.perplayerquests.progression;

import dev.ftb.mods.ftbquests.quest.Excludable;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PerPlayerCollapse {
    private PerPlayerCollapse() {}

    public static boolean isCompletedCollapsed(TeamData teamData, QuestObject object) {
        return collapsed(teamData, object, IPerPlayerStore::mqt$isPerPlayerCompleted);
    }

    public static boolean isStartedCollapsed(TeamData teamData, QuestObject object) {
        return collapsed(teamData, object, IPerPlayerStore::mqt$isPerPlayerStarted);
    }

    private interface MemberCheck {
        boolean test(IPerPlayerStore store, UUID player, long objId);
    }

    private static boolean collapsed(TeamData teamData, QuestObject object, MemberCheck check) {
        if (!(teamData instanceof IPerPlayerStore store)) return false;
        if (!((Object) object instanceof IPerPlayerObject ppo)) return false;

        IPerPlayerObject.Collapse rule = ppo.mqt$getCollapse();
        boolean serverSide = teamData.getFile() != null && teamData.getFile().isServerSide();

        if (rule == IPerPlayerObject.Collapse.ANY) {
            if (serverSide) {
                Collection<ServerPlayer> members = teamData.getOnlineMembers();
                if (members.isEmpty()) return false;
                for (ServerPlayer p : members) {
                    UUID id = p.getUUID();
                    if (isExcludedFor(store, teamData, object, id)) continue;
                    if (check.test(store, id, object.id)) return true;
                }
                return false;
            }
            List<UUID> members = clientOnlineTeamMembers(teamData.getTeamId());
            if (members.isEmpty()) return false;
            for (UUID id : members) {
                if (isExcludedFor(store, teamData, object, id)) continue;
                if (check.test(store, id, object.id)) return true;
            }
            return false;
        }

        if (serverSide) {
            Set<UUID> members = allTeamMembers(teamData);
            if (members == null || members.isEmpty()) {
                Collection<ServerPlayer> online = teamData.getOnlineMembers();
                if (online.isEmpty()) return false;
                for (ServerPlayer p : online) {
                    UUID id = p.getUUID();
                    if (isExcludedFor(store, teamData, object, id)) continue;
                    if (!check.test(store, id, object.id)) return false;
                }
                return true;
            }
            for (UUID id : members) {
                if (isExcludedFor(store, teamData, object, id)) continue;
                if (!check.test(store, id, object.id)) return false;
            }
            return true;
        }

        List<UUID> clientMembers = clientAllTeamMembers(teamData.getTeamId());
        if (clientMembers.isEmpty()) {
            List<UUID> online = clientOnlineTeamMembers(teamData.getTeamId());
            if (online.isEmpty()) return false;
            for (UUID id : online) {
                if (isExcludedFor(store, teamData, object, id)) continue;
                if (!check.test(store, id, object.id)) return false;
            }
            return true;
        }
        for (UUID id : clientMembers) {
            if (isExcludedFor(store, teamData, object, id)) continue;
            if (!check.test(store, id, object.id)) return false;
        }
        return true;
    }

    private static boolean isExcludedFor(IPerPlayerStore store, TeamData teamData, QuestObject object, UUID member) {
        if (!(object instanceof Excludable)) return false;
        store.ppq$beginForceActorDeps(member);
        try {
            return teamData.isExcludedByOtherQuestline(object);
        } finally {
            store.ppq$endForceActorDeps();
        }
    }

    public static List<UUID> teamMembers(TeamData teamData) {
        boolean serverSide = teamData.getFile() != null && teamData.getFile().isServerSide();
        if (serverSide) {
            Set<UUID> members = allTeamMembers(teamData);
            if (members != null && !members.isEmpty()) return List.copyOf(members);
            List<UUID> online = new ArrayList<>();
            for (ServerPlayer p : teamData.getOnlineMembers()) online.add(p.getUUID());
            return online;
        }
        List<UUID> clientMembers = clientAllTeamMembers(teamData.getTeamId());
        if (!clientMembers.isEmpty()) return clientMembers;
        return clientOnlineTeamMembers(teamData.getTeamId());
    }

    private static Set<UUID> allTeamMembers(TeamData teamData) {
        return FTBTeamsAPI.api().getManager().getTeamByID(teamData.getTeamId())
                .map(dev.ftb.mods.ftbteams.api.Team::getMembers)
                .orElse(null);
    }

    private static List<UUID> clientAllTeamMembers(UUID teamId) {
        List<UUID> out = new ArrayList<>();
        for (KnownClientPlayer p : FTBTeamsAPI.api().getClientManager().knownClientPlayers()) {
            if (p.teamId().equals(teamId)) out.add(p.id());
        }
        return out;
    }

    private static List<UUID> clientOnlineTeamMembers(UUID teamId) {
        List<UUID> out = new ArrayList<>();
        for (KnownClientPlayer p : FTBTeamsAPI.api().getClientManager().knownClientPlayers()) {
            if (p.teamId().equals(teamId) && p.online()) out.add(p.id());
        }
        return out;
    }
}
