package com.ccr4ft3r.geotaggedscreenshots;

import com.ccr4ft3r.geotaggedscreenshots.config.ClientConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class ModConstants {

    public static final String MODID = "geotagged_screenshots";

    public static final File THUMBNAIL_DIR = new File(FMLPaths.GAMEDIR.get() + "/thumbnails/");

    public static final File SCREENSHOTS_DIR = new File(FMLPaths.GAMEDIR.get() + "/screenshots/");

    public static final File METADATA_DIR = new File(FMLPaths.GAMEDIR.get() + "/screenshots-metadata");

    public static String getWaypointSet() {
        return ClientConfig.CONFIG_DATA.useSeparateSetForGeotaggedScreenshots.get() ?
            ClientConfig.CONFIG_DATA.waypointSetName.get() : "gui.xaero_default";
    }
}