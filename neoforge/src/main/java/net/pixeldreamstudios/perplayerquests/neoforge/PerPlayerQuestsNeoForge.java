package net.pixeldreamstudios.perplayerquests.neoforge;

import dev.architectury.event.events.common.LifecycleEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.pixeldreamstudios.perplayerquests.PerPlayerQuests;
import net.pixeldreamstudios.perplayerquests.commands.PPQCommands;
import net.pixeldreamstudios.perplayerquests.network.PPQNetwork;

@Mod(PerPlayerQuests.MOD_ID)
public final class PerPlayerQuestsNeoForge {
    public PerPlayerQuestsNeoForge(IEventBus modBus) {
        PPQNetwork.registerReceivers();
        LifecycleEvent.SETUP.register(PerPlayerQuests::init);
    }

    @EventBusSubscriber(modid = PerPlayerQuests.MOD_ID)
    public static class Events {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            PPQCommands.register(event.getDispatcher());
        }
    }
}
