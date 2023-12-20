package com.ccr4ft3r.geotaggedscreenshots.network;

import com.ccr4ft3r.geotaggedscreenshots.ModConstants;
import com.ccr4ft3r.geotaggedscreenshots.container.AlbumCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1.0.0";
    private static final SimpleChannel SIMPLE_CHANNEL = NetworkRegistry
        .newSimpleChannel(new ResourceLocation(ModConstants.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static UUID lastHandledPacket;

    // Workaround for the issue, that packet listener are being called two times (only happens in 1.20)
    private static boolean shouldHandle(ClientBoundWorldPacket packet) {
        if (packet.getId().equals(lastHandledPacket))
            return false;
        lastHandledPacket = packet.getId();
        return true;
    }

    public static void registerMessages() {
        SIMPLE_CHANNEL.registerMessage(0, ClientBoundWorldPacket.class, ClientBoundWorldPacket::encodeOnClientSide, ClientBoundWorldPacket::new, PacketHandler::handle);
    }

    public static void sendToPlayer(ClientBoundWorldPacket packet, ServerPlayer player) {
        SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private static void handle(ClientBoundWorldPacket packet, Supplier<NetworkEvent.Context> ctx) {
        final NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            if (shouldHandle(packet))
                AlbumCollection.INSTANCE.setCurrent(UUID.fromString(packet.getWorldId()));
        });
    }
}