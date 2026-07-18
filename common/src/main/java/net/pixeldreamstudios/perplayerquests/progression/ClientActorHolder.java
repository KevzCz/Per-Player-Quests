package net.pixeldreamstudios.perplayerquests.progression;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import java.util.UUID;

@Environment(EnvType.CLIENT)
final class ClientActorHolder {
    private ClientActorHolder() {}

    static UUID localPlayerUuid() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getUUID() : null;
    }
}
