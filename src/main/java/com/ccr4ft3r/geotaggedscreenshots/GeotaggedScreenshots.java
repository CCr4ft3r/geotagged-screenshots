package com.ccr4ft3r.geotaggedscreenshots;

import com.ccr4ft3r.geotaggedscreenshots.network.PacketHandler;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(ModConstants.MODID)
public class GeotaggedScreenshots {

    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public GeotaggedScreenshots() {
        PacketHandler.registerMessages();
    }
}