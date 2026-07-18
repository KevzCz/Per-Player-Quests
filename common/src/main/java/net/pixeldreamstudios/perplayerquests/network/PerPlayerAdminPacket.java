package net.pixeldreamstudios.perplayerquests.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.PerPlayerQuests;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerAdminHandler;

import java.util.UUID;

public record PerPlayerAdminPacket(Action action, UUID target, long objectId)
        implements CustomPacketPayload {

    public enum Action { RESET, COMPLETE, RELEASE_UNIQUE, RESET_GLOBAL, RESET_ALL }

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(PerPlayerQuests.MOD_ID, "per_player_admin");
    public static final Type<PerPlayerAdminPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, PerPlayerAdminPacket> STREAM_CODEC =
            StreamCodec.of(PerPlayerAdminPacket::write, PerPlayerAdminPacket::read);

    public static void write(FriendlyByteBuf buf, PerPlayerAdminPacket p) {
        buf.writeEnum(p.action());
        buf.writeUUID(p.target());
        buf.writeLong(p.objectId());
    }

    public static PerPlayerAdminPacket read(FriendlyByteBuf buf) {
        return new PerPlayerAdminPacket(buf.readEnum(Action.class), buf.readUUID(), buf.readLong());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PerPlayerAdminPacket self, NetworkManager.PacketContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer sender) {
            ctx.queue(() -> PerPlayerAdminHandler.handle(sender, self));
        }
    }
}
