package com.ccr4ft3r.geotaggedscreenshots;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class ModConstants {

    public static final String MODID = "geotagged_screenshots";

    public static final File THUMBNAIL_DIR = new File(FMLPaths.GAMEDIR.get() + "/thumbnails/");

    public static final File SCREENSHOTS_DIR = new File(FMLPaths.GAMEDIR.get() + "/screenshots/");

    public static final String GEOTAGGED_SCREENSHOT_SET = "Geotagged Screenshots";
}