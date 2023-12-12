package com.ccr4ft3r.geotaggedscreenshots.container;

import com.ccr4ft3r.geotaggedscreenshots.util.FileUtil;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AlbumCollection {

    public static final AlbumCollection INSTANCE = new AlbumCollection();

    private final Map<UUID, WorldScreenshotAlbum> albumByWorldId = Maps.newConcurrentMap();

    private UUID currentWorldId;

    public AlbumCollection() {
    }

    public UUID getCurrentId() {
        return currentWorldId;
    }

    public WorldScreenshotAlbum getCurrent() {
        return getCurrentId() == null ? null : get(getCurrentId());
    }

    public WorldScreenshotAlbum get(UUID worldId) {
        return albumByWorldId.computeIfAbsent(worldId, WorldScreenshotAlbum::new);
    }

    public void setCurrent(UUID worldId) {
        if (getCurrent() != null)
            getCurrent().getScreenshots().forEach(GeotaggedScreenshot::close);
        this.currentWorldId = worldId;
        albumByWorldId.remove(worldId);
        loadScreenshots(currentWorldId).forEach(screenshot -> getCurrent().add(screenshot));
    }

    public Collection<GeotaggedScreenshot> loadScreenshots(UUID worldId) {
        WorldScreenshotAlbum album = get(worldId);
        if (!album.getScreenshots().isEmpty())
            return album.getScreenshots();

        List<ScreenshotMetadata> thumbnailMetadata = FileUtil.getScreenshotMetadata(worldId, ImageType.THUMBNAIL);
        List<ScreenshotMetadata> screenshotMetadata = FileUtil.getScreenshotMetadata(worldId, ImageType.ORIGINAL);
        thumbnailMetadata.forEach(metadata -> {
            GeotaggedScreenshot screenshot = new GeotaggedScreenshot(album, metadata);
            File screenshotFile = screenshotMetadata.stream().filter(metadata::equals).findFirst().map(ScreenshotMetadata::getFile).orElse(null);
            album.add(screenshot
                .setFile(ImageType.THUMBNAIL, metadata.getFile())
                .setFile(ImageType.ORIGINAL, screenshotFile)
            );
        });
        return album.getScreenshots();
    }
}