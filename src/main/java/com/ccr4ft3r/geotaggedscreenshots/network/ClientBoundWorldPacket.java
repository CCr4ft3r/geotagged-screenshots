package com.ccr4ft3r.geotaggedscreenshots.network;

import net.minecraft.network.FriendlyByteBuf;

public class ClientBoundWorldPacket {

    private final String worldId;

    public ClientBoundWorldPacket(String worldId) {
        this.worldId = worldId;
    }

    public ClientBoundWorldPacket(FriendlyByteBuf packetBuffer) {
        this.worldId = packetBuffer.readUtf();
    }

    public void encodeOnClientSide(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(this.worldId);
    }

    public String getWorldId() {
        return this.worldId;
    }

}