package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.data.AbstractTeam;
import net.pixeldreamstudios.perplayerquests.progression.IPerPlayerStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(targets = "dev.ftb.mods.ftbteams.data.TeamManagerImpl", remap = false)
public abstract class TeamDeletionCleanupMixin {

    @Inject(method = "deleteTeam", at = @At("HEAD"), remap = false)
    private void ppq$clearPerPlayerOnDelete(AbstractTeam team, CallbackInfo ci) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null || team == null) return;
        UUID teamId = team.getId();
        if (teamId == null) return;
        TeamData data = file.getNullableTeamData(teamId);
        if (data instanceof IPerPlayerStore store) {
            store.mqt$clearEverything();
        }
    }
}
