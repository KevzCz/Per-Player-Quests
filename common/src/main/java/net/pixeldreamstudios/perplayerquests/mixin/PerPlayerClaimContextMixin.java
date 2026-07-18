package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TeamData.class, remap = false)
public abstract class PerPlayerClaimContextMixin {

    @Inject(method = "claimReward(Lnet/minecraft/server/level/ServerPlayer;Ldev/ftb/mods/ftbquests/quest/reward/Reward;ZJ)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ppq$ensurePlayerContext(ServerPlayer player, Reward reward, boolean notify, long when, CallbackInfo ci) {
        if (ServerQuestFile.INSTANCE == null) return;
        if (player.equals(ServerQuestFile.INSTANCE.getCurrentPlayer())) return;

        ci.cancel();
        TeamData self = (TeamData) (Object) this;
        ServerQuestFile.INSTANCE.withPlayerContext(player, () -> self.claimReward(player, reward, notify, when));
    }
}
