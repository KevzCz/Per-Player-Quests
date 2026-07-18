package net.pixeldreamstudios.perplayerquests.fabric;

import dev.architectury.event.events.common.LifecycleEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.pixeldreamstudios.perplayerquests.PerPlayerQuests;
import net.pixeldreamstudios.perplayerquests.commands.PPQCommands;
import net.pixeldreamstudios.perplayerquests.network.PPQNetwork;

public final class PerPlayerQuestsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PPQNetwork.registerReceivers();
        LifecycleEvent.SETUP.register(PerPlayerQuests::init);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                PPQCommands.register(dispatcher));
    }
}
