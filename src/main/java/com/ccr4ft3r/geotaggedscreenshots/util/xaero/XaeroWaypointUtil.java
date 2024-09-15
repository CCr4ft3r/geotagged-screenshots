package com.ccr4ft3r.geotaggedscreenshots.util.xaero;

import com.ccr4ft3r.geotaggedscreenshots.GeotaggedScreenshots;
import com.ccr4ft3r.geotaggedscreenshots.container.*;
import com.ccr4ft3r.geotaggedscreenshots.mixin.xaero.SupportXaeroMinimapAccessor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.FilenameUtils;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.map.WorldMapSession;
import xaero.map.core.XaeroWorldMapCore;
import xaero.map.gui.GuiMap;
import xaero.map.mods.SupportMods;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.ccr4ft3r.geotaggedscreenshots.ModConstants.*;

public class XaeroWaypointUtil {

    public static void initWaypointSet() {
        if (((SupportXaeroMinimapAccessor) SupportMods.xaeroMinimap).waypointSet() == null
            || ((SupportXaeroMinimapAccessor) SupportMods.xaeroMinimap).waypointWorld() == null) {
            Minecraft mc = Minecraft.getInstance();
            Entity player = Objects.requireNonNull(Minecraft.getInstance().cameraEntity, "No camera entity at client side?");
            WorldMapSession currentSession = XaeroWorldMapCore.currentSession;
            GuiMap guiMap = new GuiMap(null, null, currentSession.getMapProcessor(), player);
            ResourceKey<Level> dimension = Objects.requireNonNull(mc.level, "No level available?").dimension();
            SupportMods.xaeroMinimap.checkWaypoints(mc.allowsMultiplayer(), dimension, "", 1, 1, guiMap);
        }
    }

    public static void addNewScreenshotWaypoint(Vec3 position, File screenshotFile, File thumbnailFile) {
        String name = FilenameUtils.removeExtension(screenshotFile.getName());
        Waypoint waypoint = new Waypoint((int) position.x, (int) position.y, (int) position.z, name, "S", 200);

        SupportXaeroMinimapAccessor minimap = (SupportXaeroMinimapAccessor) SupportMods.xaeroMinimap;
        WaypointSet screenshotWaypointSet = getScreenshotWaypointSet();
        screenshotWaypointSet.getList().add(waypoint);
        try {
            minimap.modMain().getSettings().saveWaypoints(minimap.waypointWorld());
            GeotaggedScreenshot geotaggedScreenshot = new GeotaggedScreenshot(name)
                .setFile(ImageType.ORIGINAL, screenshotFile)
                .setFile(ImageType.THUMBNAIL, thumbnailFile);
            GeotaggedScreenshots.ALBUM.add(geotaggedScreenshot);
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't save waypoints of current world.", e);
        }
    }

    public static WaypointSet getScreenshotWaypointSet() {
        SupportXaeroMinimapAccessor minimap = (SupportXaeroMinimapAccessor) SupportMods.xaeroMinimap;
        if (!minimap.waypointWorld().getSets().containsKey(getWaypointSet()))
            minimap.waypointWorld().addSet(getWaypointSet());
        return minimap.waypointWorld().getSets().get(getWaypointSet());
    }
}