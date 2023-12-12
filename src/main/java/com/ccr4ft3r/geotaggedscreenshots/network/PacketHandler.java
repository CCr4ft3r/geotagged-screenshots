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

    public static void registerMessages() {
        SIMPLE_CHANNEL.registerMessage(0, ClientBoundWorldPacket.class, ClientBoundWorldPacket::encodeOnClientSide, ClientBoundWorldPacket::new, PacketHandler::handle);
    }

    public static void sendToPlayer(ClientBoundWorldPacket packet, ServerPlayer player) {
        SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private static void handle(ClientBoundWorldPacket packet, Supplier<NetworkEvent.Context> ctx) {
        final NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            AlbumCollection.INSTANCE.setCurrent(UUID.fromString(packet.getWorldId()));
            context.setPacketHandled(true);
        });
    }
}