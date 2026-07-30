package net.pixeldreamstudios.perplayerquests.fabric.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.pixeldreamstudios.perplayerquests.network.*;

public final class FabricPPQNetwork {
    private FabricPPQNetwork() {}

    public static void init() {
        // Register payload types
        PayloadTypeRegistry.playS2C().register(PerPlayerSyncPacket.TYPE, PerPlayerSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PerPlayerTeamSyncPacket.TYPE, PerPlayerTeamSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(UniqueClaimSyncPacket.TYPE, UniqueClaimSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ServerGlobalSyncPacket.TYPE, ServerGlobalSyncPacket.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(PerPlayerAdminPacket.TYPE, PerPlayerAdminPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PerPlayerAdminPacket.TYPE,
                (payload, fabricContext) -> PerPlayerAdminPacket.handle(payload, wrapServer(fabricContext)));

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerClientReceivers();
        }
    }

    private static NetworkManager.PacketContext wrapServer(ServerPlayNetworking.Context fabricContext) {
        return new NetworkManager.PacketContext() {
            @Override
            public Player getPlayer() {
                return fabricContext.player();
            }

            @Override
            public void queue(Runnable runnable) {
                fabricContext.server().execute(runnable);
            }

            @Override
            public Env getEnvironment() {
                return Env.SERVER;
            }

            @Override
            public RegistryAccess registryAccess() {
                return fabricContext.player().registryAccess();
            }
        };
    }

    @Environment(EnvType.CLIENT)
    private static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(PerPlayerSyncPacket.TYPE,
                (payload, context) -> PerPlayerSyncPacket.handle(payload, wrapClient(context)));

        ClientPlayNetworking.registerGlobalReceiver(PerPlayerTeamSyncPacket.TYPE,
                (payload, context) -> PerPlayerTeamSyncPacket.handle(payload, wrapClient(context)));

        ClientPlayNetworking.registerGlobalReceiver(UniqueClaimSyncPacket.TYPE,
                (payload, context) -> UniqueClaimSyncPacket.handle(payload, wrapClient(context)));

        ClientPlayNetworking.registerGlobalReceiver(ServerGlobalSyncPacket.TYPE,
                (payload, context) -> ServerGlobalSyncPacket.handle(payload, wrapClient(context)));
    }

    @Environment(EnvType.CLIENT)
    private static NetworkManager.PacketContext wrapClient(ClientPlayNetworking.Context context) {
        return new NetworkManager.PacketContext() {
            @Override
            public Player getPlayer() {
                return context.player();
            }

            @Override
            public void queue(Runnable runnable) {
                context.client().execute(runnable);
            }

            @Override
            public Env getEnvironment() {
                return Env.CLIENT;
            }

            @Override
            public RegistryAccess registryAccess() {
                return Minecraft.getInstance().level.registryAccess();
            }
        };
    }
}
