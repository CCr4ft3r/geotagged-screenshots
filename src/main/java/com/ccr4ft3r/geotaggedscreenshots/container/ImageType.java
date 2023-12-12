package com.ccr4ft3r.geotaggedscreenshots.container;

import com.ccr4ft3r.geotaggedscreenshots.ModConstants;

import java.io.File;

public enum ImageType {
    ORIGINAL(ModConstants.SCREENSHOTS_DIR), THUMBNAIL(ModConstants.THUMBNAIL_DIR);

    private final File dir;

    ImageType(File dir) {
        this.dir = dir;
    }

    public File getDir() {
        return dir;
    }
}