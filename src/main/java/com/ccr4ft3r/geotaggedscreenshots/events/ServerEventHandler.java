package com.ccr4ft3r.geotaggedscreenshots.events;

import com.ccr4ft3r.geotaggedscreenshots.ModConstants;
import com.ccr4ft3r.geotaggedscreenshots.capabilities.WorldCapability;
import com.ccr4ft3r.geotaggedscreenshots.capabilities.WorldCapabilityProvider;
import com.ccr4ft3r.geotaggedscreenshots.network.ClientBoundWorldPacket;
import com.ccr4ft3r.geotaggedscreenshots.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().getLevel().isClientSide())
            return;
        ServerLevel level = (ServerLevel) event.getEntity().getLevel();
        LazyOptional<WorldCapability> capability = level.getCapability(WorldCapabilityProvider.WORLD_CAP);
        capability.resolve().ifPresent(cap -> {
            ClientBoundWorldPacket packet = new ClientBoundWorldPacket(cap.getWorldId());
            PacketHandler.sendToPlayer(packet, (ServerPlayer) event.getEntity());
        });
    }

    @SubscribeEvent
    public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Level> event) {
        if (event.getObject().isClientSide())
            return;
        if (!event.getObject().getCapability(WorldCapabilityProvider.WORLD_CAP).isPresent())
            event.addCapability(new ResourceLocation(ModConstants.MODID, "world"), new WorldCapabilityProvider());
    }
}