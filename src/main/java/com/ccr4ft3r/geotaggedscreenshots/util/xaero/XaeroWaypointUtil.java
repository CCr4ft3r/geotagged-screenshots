package com.ccr4ft3r.geotaggedscreenshots.util.xaero;

import com.ccr4ft3r.geotaggedscreenshots.container.*;
import com.ccr4ft3r.geotaggedscreenshots.mixin.xaero.SupportXaeroMinimapAccessor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.apache.commons.io.FilenameUtils;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.map.core.XaeroWorldMapCore;
import xaero.map.gui.GuiMap;
import xaero.map.mods.SupportMods;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.ccr4ft3r.geotaggedscreenshots.ModConstants.*;

public class XaeroWaypointUtil {

    private static void initWaypointSet() {
        if (((SupportXaeroMinimapAccessor) SupportMods.xaeroMinimap).waypointSet() == null) {
            Minecraft mc = Minecraft.getInstance();
            Entity player = Objects.requireNonNull(mc.getCameraEntity(), "No camera entity?");
            GuiMap guiMap = new GuiMap(null, null, XaeroWorldMapCore.currentSession.getMapProcessor(), player);
            ResourceKey<Level> dimension = Objects.requireNonNull(mc.level, "No level available?").dimension();
            SupportMods.xaeroMinimap.checkWaypoints(mc.allowsMultiplayer(), dimension, "", 1, 1, guiMap);
        }
    }

    public static void addNewScreenshotWaypoint(ScreenshotMetadata metadata, File screenshotFile, File thumbnailFile) {
        initWaypointSet();
        int x = (int) metadata.getCoordinates().x();
        int y = (int) metadata.getCoordinates().y();
        int z = (int) metadata.getCoordinates().z();
        String name = FilenameUtils.removeExtension(screenshotFile.getName());
        Waypoint waypoint = new Waypoint(x, y, z, name, "S", 200);
        SupportXaeroMinimapAccessor minimap = (SupportXaeroMinimapAccessor) SupportMods.xaeroMinimap;
        if (!minimap.waypointWorld().getSets().containsKey(GEOTAGGED_SCREENSHOT_SET))
            minimap.waypointWorld().addSet(GEOTAGGED_SCREENSHOT_SET);
        WaypointSet screenshotWaypointSet = minimap.waypointWorld().getSets().get(GEOTAGGED_SCREENSHOT_SET);
        screenshotWaypointSet.getList().add(waypoint);
        try {
            minimap.modMain().getSettings().saveWaypoints(minimap.waypointWorld());
            WorldScreenshotAlbum album = AlbumCollection.INSTANCE.getCurrent();
            GeotaggedScreenshot geotaggedScreenshot = new GeotaggedScreenshot(album, metadata)
                .setFile(ImageType.ORIGINAL, screenshotFile)
                .setFile(ImageType.THUMBNAIL, thumbnailFile);
            album.add(geotaggedScreenshot);
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't save waypoints of current world.", e);
        }
    }
}