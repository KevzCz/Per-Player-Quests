package net.pixeldreamstudios.perplayerquests.mixin.client;

import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.client.Minecraft;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.api.IServerGlobalQuest;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;
import net.pixeldreamstudios.perplayerquests.client.PerPlayerAdminMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenMenuMixin {

    @Inject(method = "addObjectMenuItems", at = @At("TAIL"), remap = false)
    private void mqt$addPerPlayerMenu(List<ContextMenuItem> contextMenu, Button button, Runnable gui,
                                      QuestObjectBase object, Movable deletionFocus, CallbackInfo ci) {
        if (mqt$isPerPlayer(object)) {
            contextMenu.add(ContextMenuItem.SEPARATOR);
            contextMenu.add(PerPlayerAdminMenu.entry(object));
        }

        boolean isOp = Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.hasPermissions(2);
        if (isOp && object instanceof Quest quest) {
            if ((Object) quest instanceof IUniqueQuest u && u.mqt$isUniqueGlobal()) {
                contextMenu.add(ContextMenuItem.SEPARATOR);
                contextMenu.add(PerPlayerAdminMenu.releaseUniqueEntry(object));
            }
            if ((Object) quest instanceof IServerGlobalQuest g && g.ppq$isServerGlobal()) {
                contextMenu.add(ContextMenuItem.SEPARATOR);
                contextMenu.add(PerPlayerAdminMenu.resetGlobalEntry(object));
            }
        }
    }

    @Unique
    private static boolean mqt$isPerPlayer(QuestObjectBase object) {
        return object instanceof IPerPlayerObject ppo && ppo.mqt$isPerPlayer();
    }
}
