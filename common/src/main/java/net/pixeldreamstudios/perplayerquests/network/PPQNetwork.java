package net.pixeldreamstudios.perplayerquests.network;

import dev.architectury.networking.NetworkManager;

public final class PPQNetwork {
    private PPQNetwork() {}

    public static void registerReceivers() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                PerPlayerSyncPacket.TYPE,
                PerPlayerSyncPacket.STREAM_CODEC,
                PerPlayerSyncPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                PerPlayerTeamSyncPacket.TYPE,
                PerPlayerTeamSyncPacket.STREAM_CODEC,
                PerPlayerTeamSyncPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                UniqueClaimSyncPacket.TYPE,
                UniqueClaimSyncPacket.STREAM_CODEC,
                UniqueClaimSyncPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                PerPlayerAdminPacket.TYPE,
                PerPlayerAdminPacket.STREAM_CODEC,
                PerPlayerAdminPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                ServerGlobalSyncPacket.TYPE,
                ServerGlobalSyncPacket.STREAM_CODEC,
                ServerGlobalSyncPacket::handle);
    }
}
