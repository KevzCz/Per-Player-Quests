package net.pixeldreamstudios.perplayerquests.mixin.client;

import dev.ftb.mods.ftblibrary.ui.ContextMenu;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import net.pixeldreamstudios.perplayerquests.client.PerPlayerAdminMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.OtherButtonsPanelBottom$EditSettingsButton", remap = false)
public abstract class SettingsMenuMixin {

    @Redirect(method = "onClicked", at = @At(value = "INVOKE",
            target = "Ldev/ftb/mods/ftbquests/client/gui/quests/QuestScreen;openContextMenu(Ljava/util/List;)Ldev/ftb/mods/ftblibrary/ui/ContextMenu;"),
            remap = false)
    private ContextMenu ppq$addResetAllEntry(QuestScreen screen, List<ContextMenuItem> items) {
        items.add(ContextMenuItem.SEPARATOR);
        items.add(PerPlayerAdminMenu.resetAllEntry());
        return screen.openContextMenu(items);
    }
}
