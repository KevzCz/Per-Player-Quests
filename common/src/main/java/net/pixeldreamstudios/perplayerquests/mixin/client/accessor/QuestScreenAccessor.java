package net.pixeldreamstudios.perplayerquests.mixin.client.accessor;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = QuestScreen.class, remap = false)
public interface QuestScreenAccessor {

    @Accessor("file")
    ClientQuestFile ppq$getFile();
}
