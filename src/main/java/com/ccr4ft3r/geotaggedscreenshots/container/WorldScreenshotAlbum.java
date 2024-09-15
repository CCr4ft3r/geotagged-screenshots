package com.ccr4ft3r.geotaggedscreenshots.container;

import java.util.*;

public class WorldScreenshotAlbum {

    private final Map<String, GeotaggedScreenshot> screenshotsByName = new HashMap<>();
    private final String id;

    public WorldScreenshotAlbum(String id) {
        this.id = id;
    }

    public void add(GeotaggedScreenshot geotaggedScreenshot) {
        screenshotsByName.putIfAbsent(geotaggedScreenshot.getName(), geotaggedScreenshot);
    }

    public Collection<GeotaggedScreenshot> getScreenshots() {
        return screenshotsByName.values();
    }

    public GeotaggedScreenshot getScreenshot(String name) {
        return screenshotsByName.get(name);
    }

    public String getId() {
        return id;
    }
}