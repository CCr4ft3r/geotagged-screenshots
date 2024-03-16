package com.ccr4ft3r.geotaggedscreenshots;

import com.ccr4ft3r.geotaggedscreenshots.config.ClientConfig;
import com.ccr4ft3r.geotaggedscreenshots.network.PacketHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Mod(ModConstants.MODID)
public class GeotaggedScreenshots {

    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    public GeotaggedScreenshots() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG, ModConstants.MODID + "-client.toml");
        PacketHandler.registerMessages();
    }
}