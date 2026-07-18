package net.pixeldreamstudios.perplayerquests.mixin.client;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.client.gui.ContextMenuBuilder;
import dev.ftb.mods.ftbquests.quest.Chapter;
import net.pixeldreamstudios.perplayerquests.client.PerPlayerAdminMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel$ChapterButton", remap = false)
public abstract class ChapterContextMenuMixin {

    @Shadow(remap = false)
    private Chapter chapter;

    @Redirect(method = "onClicked", at = @At(value = "INVOKE",
            target = "Ldev/ftb/mods/ftbquests/client/gui/ContextMenuBuilder;openContextMenu(Ldev/ftb/mods/ftblibrary/ui/BaseScreen;)V"),
            remap = false)
    private void ppq$addResetChapterEntry(ContextMenuBuilder builder, BaseScreen screen) {
        builder.insertAtBottom(List.<ContextMenuItem>of(
                ContextMenuItem.SEPARATOR,
                PerPlayerAdminMenu.chapterEntry(chapter)
        )).openContextMenu(screen);
    }
}
