package com.ccr4ft3r.geotaggedscreenshots.container;

import com.ccr4ft3r.geotaggedscreenshots.util.FileUtil;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.util.*;

public class WorldScreenshotAlbum {

    private final Map<Integer, GeotaggedScreenshot> screenshotsByHash = new HashMap<>();
    private final UUID worldId;

    public WorldScreenshotAlbum(UUID worldId) {
        this.worldId = worldId;
    }

    public void add(GeotaggedScreenshot geotaggedScreenshot) {
        Vec3 coordinates = geotaggedScreenshot.getMetadata().getCoordinates();
        screenshotsByHash.putIfAbsent(computeKey(coordinates), geotaggedScreenshot);
    }

    private static int computeKey(Vec3 coordinates) {
        return computeKey((int) coordinates.x, (int) coordinates.y, (int) coordinates.z);
    }

    private static int computeKey(int x, int y, int z) {
        return Objects.hash(x, y, z);
    }

    @SuppressWarnings("unused")
    public UUID getWorldId() {
        return worldId;
    }

    public Collection<GeotaggedScreenshot> getScreenshots() {
        return screenshotsByHash.values();
    }

    public GeotaggedScreenshot getScreenshot(int x, int y, int z) {
        return screenshotsByHash.get(computeKey(x, y, z));
    }

    public File findFile(GeotaggedScreenshot screenshot, ImageType imageType) {
        return FileUtil.findFile(screenshot.getMetadata(), imageType);
    }
}