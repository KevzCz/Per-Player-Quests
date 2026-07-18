package net.pixeldreamstudios.perplayerquests.mixin.client;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;
import net.pixeldreamstudios.perplayerquests.progression.ClientClaimView;
import net.pixeldreamstudios.perplayerquests.progression.UniqueClaimClientCache;
import net.pixeldreamstudios.perplayerquests.progression.UniqueInheritance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestButton.class, remap = false)
public class UniqueTooltipMixin {

    @Shadow @Final private Quest quest;

    @Inject(method = "addMouseOverText", at = @At("TAIL"), remap = false)
    private void ppq$addUniqueClaimTooltip(TooltipList list, CallbackInfo ci) {
        try {
            boolean direct = (Object) quest instanceof IUniqueQuest u && u.mqt$isUniqueGlobal();
            boolean inherited = UniqueInheritance.inherits(quest);
            if (!direct && !inherited) {
                return;
            }
            boolean claimed = direct
                    ? UniqueClaimClientCache.isClaimed(quest.id)
                    : ClientClaimView.isClaimed(quest);
            boolean lockedForMe = ClientClaimView.isClaimedByOther(quest);
            if (!claimed) {
                boolean onStart = ((IUniqueQuest) (Object) quest).mqt$getUniqueLock() == IUniqueQuest.UniqueLock.ON_START;
                String key = onStart
                        ? "perplayerquests.unique.unclaimed_on_start"
                        : "perplayerquests.unique.unclaimed_on_complete";
                list.add(Component.translatable(key)
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
            } else if (!lockedForMe) {
                list.add(Component.translatable("perplayerquests.unique.claimed_by_you")
                        .withStyle(ChatFormatting.GOLD));
            }
        } catch (Exception ignored) {
        }
    }
}
