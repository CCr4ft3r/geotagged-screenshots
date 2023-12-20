package com.ccr4ft3r.geotaggedscreenshots.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ClientBoundWorldPacket {

    private final String worldId;

    private final UUID packetId;

    public ClientBoundWorldPacket(String worldId) {
        this.worldId = worldId;
        this.packetId = UUID.randomUUID();
    }

    public ClientBoundWorldPacket(FriendlyByteBuf packetBuffer) {
        this.worldId = packetBuffer.readUtf();
        this.packetId = packetBuffer.readUUID();
    }

    public void encodeOnClientSide(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(this.worldId);
        packetBuffer.writeUUID(packetId);
    }

    public String getWorldId() {
        return this.worldId;
    }

    public UUID getId() {
        return packetId;
    }
}