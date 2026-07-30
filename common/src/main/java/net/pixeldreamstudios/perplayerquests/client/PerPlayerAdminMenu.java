package net.pixeldreamstudios.perplayerquests.client;

import net.pixeldreamstudios.perplayerquests.network.NetworkHelper;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.perplayerquests.network.PerPlayerAdminPacket;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class PerPlayerAdminMenu {
    private PerPlayerAdminMenu() {}

    public static ContextMenuItem entry(QuestObjectBase object) {
        return new ContextMenuItem(
                Component.translatable("perplayerquests.per_player.menu"),
                Icons.PLAYER,
                b -> new PerPlayerPickerScreen(object).openGui());
    }

    public static ContextMenuItem chapterEntry(QuestObjectBase chapter) {
        return new ContextMenuItem(
                Component.translatable("perplayerquests.per_player.menu.reset_chapter"),
                Icons.PLAYER,
                b -> new PerPlayerPickerScreen(chapter).openGui());
    }

    public static ContextMenuItem resetAllEntry() {
        return new ContextMenuItem(
                Component.translatable("perplayerquests.per_player.menu.reset_all"),
                Icons.PLAYER,
                b -> PerPlayerPickerScreen.forWholeFile().openGui());
    }

    public static ContextMenuItem releaseUniqueEntry(QuestObjectBase object) {
        return new ContextMenuItem(
                Component.translatable("perplayerquests.unique.menu.release"),
                Icons.CANCEL,
                b -> NetworkHelper.sendToServer(new PerPlayerAdminPacket(
                        PerPlayerAdminPacket.Action.RELEASE_UNIQUE, new UUID(0L, 0L), object.id)));
    }

    public static ContextMenuItem resetGlobalEntry(QuestObjectBase object) {
        return new ContextMenuItem(
                Component.translatable("perplayerquests.server_global.menu.reset"),
                Icons.REFRESH,
                b -> NetworkHelper.sendToServer(new PerPlayerAdminPacket(
                        PerPlayerAdminPacket.Action.RESET_GLOBAL, new UUID(0L, 0L), object.id)));
    }
}
