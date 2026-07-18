package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerActor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mixin(value = TeamData.class, remap = false)
public abstract class PerPlayerNotifyMixin {

    @Redirect(
            method = "markTaskCompleted",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;getOnlineMembers()Ljava/util/Collection;"),
            remap = false)
    private Collection<ServerPlayer> mqt$scopeNotifyToActor(TeamData instance, Task task) {
        Collection<ServerPlayer> all = instance.getOnlineMembers();
        if (!(task instanceof IPerPlayerObject ppo) || !ppo.mqt$isPerPlayer()) {
            return all;
        }
        UUID actor = PerPlayerActor.resolve(instance);
        if (actor == null) return all;
        for (ServerPlayer p : all) {
            if (p.getUUID().equals(actor)) return List.of(p);
        }
        return List.of();
    }
}
