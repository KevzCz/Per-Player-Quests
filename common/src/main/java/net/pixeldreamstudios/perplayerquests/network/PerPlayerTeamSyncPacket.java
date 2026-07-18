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
import java.util.UUID;

public record PerPlayerTeamSyncPacket(List<Entry> entries, List<RepeatEntry> repeatEntries)
        implements CustomPacketPayload {

    public record Entry(UUID player, long objId, boolean started, boolean completed) {
    }

    public record RepeatEntry(UUID player, long questId, int completionCount, long repeatableTime) {
    }

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(PerPlayerQuests.MOD_ID, "per_player_team_sync");
    public static final Type<PerPlayerTeamSyncPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, PerPlayerTeamSyncPacket> STREAM_CODEC =
            StreamCodec.of(PerPlayerTeamSyncPacket::write, PerPlayerTeamSyncPacket::read);

    public static void write(FriendlyByteBuf buf, PerPlayerTeamSyncPacket p) {
        buf.writeVarInt(p.entries().size());
        for (Entry e : p.entries()) {
            buf.writeUUID(e.player());
            buf.writeLong(e.objId());
            buf.writeBoolean(e.started());
            buf.writeBoolean(e.completed());
        }
        buf.writeVarInt(p.repeatEntries().size());
        for (RepeatEntry e : p.repeatEntries()) {
            buf.writeUUID(e.player());
            buf.writeLong(e.questId());
            buf.writeVarInt(e.completionCount());
            buf.writeVarLong(e.repeatableTime());
        }
    }

    public static PerPlayerTeamSyncPacket read(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            UUID player = buf.readUUID();
            long objId = buf.readLong();
            boolean started = buf.readBoolean();
            boolean completed = buf.readBoolean();
            entries.add(new Entry(player, objId, started, completed));
        }
        int rn = buf.readVarInt();
        List<RepeatEntry> repeatEntries = new ArrayList<>(rn);
        for (int i = 0; i < rn; i++) {
            UUID player = buf.readUUID();
            long questId = buf.readLong();
            int count = buf.readVarInt();
            long when = buf.readVarLong();
            repeatEntries.add(new RepeatEntry(player, questId, count, when));
        }
        return new PerPlayerTeamSyncPacket(entries, repeatEntries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PerPlayerTeamSyncPacket self, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> PerPlayerClientSync.applyTeam(self));
    }
}
