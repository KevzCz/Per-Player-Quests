package net.pixeldreamstudios.perplayerquests.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.pixeldreamstudios.perplayerquests.PerPlayerQuests;
import net.pixeldreamstudios.perplayerquests.progression.ServerGlobalClientCache;

import java.util.ArrayList;
import java.util.List;

public record ServerGlobalSyncPacket(List<long[]> progress, long[] started, long[] completed)
        implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(PerPlayerQuests.MOD_ID, "server_global_sync");
    public static final Type<ServerGlobalSyncPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ServerGlobalSyncPacket> STREAM_CODEC =
            StreamCodec.of(ServerGlobalSyncPacket::write, ServerGlobalSyncPacket::read);

    public static void write(FriendlyByteBuf buf, ServerGlobalSyncPacket p) {
        buf.writeVarInt(p.progress().size());
        for (long[] pair : p.progress()) {
            buf.writeLong(pair[0]);
            buf.writeVarLong(pair[1]);
        }
        buf.writeVarInt(p.started().length);
        for (long id : p.started()) buf.writeLong(id);
        buf.writeVarInt(p.completed().length);
        for (long id : p.completed()) buf.writeLong(id);
    }

    public static ServerGlobalSyncPacket read(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<long[]> prog = new ArrayList<>(n);
        for (int i = 0; i < n; i++) prog.add(new long[]{buf.readLong(), buf.readVarLong()});
        long[] started = new long[buf.readVarInt()];
        for (int i = 0; i < started.length; i++) started[i] = buf.readLong();
        long[] completed = new long[buf.readVarInt()];
        for (int i = 0; i < completed.length; i++) completed[i] = buf.readLong();
        return new ServerGlobalSyncPacket(prog, started, completed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerGlobalSyncPacket self, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> ServerGlobalClientCache.apply(self));
    }
}
