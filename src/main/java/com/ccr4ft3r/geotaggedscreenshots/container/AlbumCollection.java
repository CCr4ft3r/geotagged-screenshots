package com.ccr4ft3r.geotaggedscreenshots.container;

import com.ccr4ft3r.geotaggedscreenshots.config.ClientConfig;
import com.ccr4ft3r.geotaggedscreenshots.util.FileUtil;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ccr4ft3r.geotaggedscreenshots.GeotaggedScreenshots.*;
import static com.ccr4ft3r.geotaggedscreenshots.ModConstants.*;
import static com.ccr4ft3r.geotaggedscreenshots.util.ImageUtil.*;

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
        FileUtil.getScreenshotMetadata(worldId, ImageType.ORIGINAL).forEach(metadata -> {
            GeotaggedScreenshot screenshot = new GeotaggedScreenshot(album, metadata);
            File thumbnailFile = thumbnailMetadata.stream().filter(metadata::equals).findFirst().map(ScreenshotMetadata::getFile).orElse(null);
            album.add(screenshot
                .setFile(ImageType.ORIGINAL, metadata.getFile())
                .setFile(ImageType.THUMBNAIL, Objects.requireNonNullElseGet(thumbnailFile, () ->
                    new File(THUMBNAIL_DIR, metadata.getFile().getName())))
            );
        });

        regenerateMissingThumbnails(album);
        return album.getScreenshots();
    }

    private void regenerateMissingThumbnails(WorldScreenshotAlbum album) {
        List<GeotaggedScreenshot> missingThumbnails = album.getScreenshots().stream()
            .filter(screenshot -> !screenshot.getFile(ImageType.THUMBNAIL).exists()).toList();
        if (missingThumbnails.isEmpty())
            return;

        AtomicInteger completed = new AtomicInteger();
        LogUtils.getLogger().info("Regenerating thumbnails for {} screenshots", missingThumbnails.size());
        displayMessage("started_thumbnail_regeneration", missingThumbnails.size());
        missingThumbnails.forEach(screenshot -> EXECUTOR.submit(() -> {
            CompletableFuture<NativeImage> future = screenshot.getImageFuture(ImageType.ORIGINAL);
            NativeImage image = future.join();
            if (image == null)
                return;
            screenshot.setFile(ImageType.THUMBNAIL, createThumbnail(image, screenshot.getFile(ImageType.ORIGINAL), screenshot.getMetadata()));
            screenshot.close(ImageType.ORIGINAL);
            if (completed.incrementAndGet() == missingThumbnails.size()) {
                displayMessage("completed_thumbnail_regeneration", missingThumbnails.size());
                LogUtils.getLogger().info("Completed regeneration of {} thumbnails", missingThumbnails.size());
            }
        }));
    }

    private static void displayMessage(String id, Object... parameter) {
        if (!ClientConfig.CONFIG_DATA.displayThumbnailRegenerationInChat.get())
            return;
        MutableComponent messageComponent = MutableComponent.create(new TranslatableContents("message.geotagged_screenshots." + id));
        messageComponent = MutableComponent.create(new LiteralContents(MessageFormat.format(messageComponent.getString(), parameter)));
        Objects.requireNonNull(Minecraft.getInstance().player, "Minecraft.getInstance() was called on server side")
            .displayClientMessage(messageComponent, false);
    }
}