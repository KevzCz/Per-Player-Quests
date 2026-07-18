package net.pixeldreamstudios.perplayerquests.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.pixeldreamstudios.perplayerquests.PerPlayerQuests;
import net.pixeldreamstudios.perplayerquests.progression.UniqueClaimClientCache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record UniqueClaimSyncPacket(List<Entry> entries) implements CustomPacketPayload {

    public record Entry(long questId, UUID owner, String ownerName) {}

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(PerPlayerQuests.MOD_ID, "unique_claim_sync");
    public static final Type<UniqueClaimSyncPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, UniqueClaimSyncPacket> STREAM_CODEC =
            StreamCodec.of(UniqueClaimSyncPacket::write, UniqueClaimSyncPacket::read);

    public static void write(FriendlyByteBuf buf, UniqueClaimSyncPacket p) {
        buf.writeVarInt(p.entries().size());
        for (Entry e : p.entries()) {
            buf.writeLong(e.questId());
            buf.writeUUID(e.owner());
            buf.writeUtf(e.ownerName());
        }
    }

    public static UniqueClaimSyncPacket read(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<Entry> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(new Entry(buf.readLong(), buf.readUUID(), buf.readUtf()));
        }
        return new UniqueClaimSyncPacket(list);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UniqueClaimSyncPacket self, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> UniqueClaimClientCache.apply(self));
    }
}
