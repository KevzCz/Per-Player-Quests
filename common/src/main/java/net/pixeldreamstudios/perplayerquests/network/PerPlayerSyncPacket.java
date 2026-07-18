package net.pixeldreamstudios.perplayerquests.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.pixeldreamstudios.perplayerquests.PerPlayerQuests;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerClientSync;

import java.util.ArrayList;
import java.util.List;

public record PerPlayerSyncPacket(List<long[]> taskProgress, long[] started, long[] completed,
                                   List<long[]> repeatState)
        implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(PerPlayerQuests.MOD_ID, "per_player_sync");
    public static final Type<PerPlayerSyncPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, PerPlayerSyncPacket> STREAM_CODEC =
            StreamCodec.of(PerPlayerSyncPacket::write, PerPlayerSyncPacket::read);

    public static void write(FriendlyByteBuf buf, PerPlayerSyncPacket p) {
        buf.writeVarInt(p.taskProgress().size());
        for (long[] pair : p.taskProgress()) {
            buf.writeLong(pair[0]);
            buf.writeVarLong(pair[1]);
        }
        buf.writeVarInt(p.started().length);
        for (long id : p.started()) buf.writeLong(id);
        buf.writeVarInt(p.completed().length);
        for (long id : p.completed()) buf.writeLong(id);
        buf.writeVarInt(p.repeatState().size());
        for (long[] entry : p.repeatState()) {
            buf.writeLong(entry[0]);
            buf.writeVarInt((int) entry[1]);
            buf.writeVarLong(entry[2]);
        }
    }

    public static PerPlayerSyncPacket read(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<long[]> tp = new ArrayList<>(n);
        for (int i = 0; i < n; i++) tp.add(new long[]{buf.readLong(), buf.readVarLong()});
        long[] started = new long[buf.readVarInt()];
        for (int i = 0; i < started.length; i++) started[i] = buf.readLong();
        long[] completed = new long[buf.readVarInt()];
        for (int i = 0; i < completed.length; i++) completed[i] = buf.readLong();
        int rn = buf.readVarInt();
        List<long[]> repeatState = new ArrayList<>(rn);
        for (int i = 0; i < rn; i++) {
            long questId = buf.readLong();
            int count = buf.readVarInt();
            long when = buf.readVarLong();
            repeatState.add(new long[]{questId, count, when});
        }
        return new PerPlayerSyncPacket(tp, started, completed, repeatState);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PerPlayerSyncPacket self, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> PerPlayerClientSync.apply(self));
    }
}
