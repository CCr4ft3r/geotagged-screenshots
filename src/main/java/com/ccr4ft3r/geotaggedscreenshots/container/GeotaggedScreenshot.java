package com.ccr4ft3r.geotaggedscreenshots.container;

import com.ccr4ft3r.geotaggedscreenshots.config.ClientConfig;
import com.ccr4ft3r.geotaggedscreenshots.util.ImageUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GeotaggedScreenshot {

    private final String name;

    private final Map<ImageType, File> fileByType = new HashMap<>();
    private final Map<ImageType, CompletableFuture<NativeImage>> futureByType = new HashMap<>();
    private final Map<ImageType, DynamicTexture> textureByType = new HashMap<>();
    private boolean isMissingThumbnail;

    public GeotaggedScreenshot(String name) {
        this.name = name;
    }

    public CompletableFuture<NativeImage> getImageFuture(ImageType type) {
        if (futureByType.get(type) == null && (type == ImageType.ORIGINAL || !isMissingThumbnail))
            loadImage(type);
        return futureByType.get(type);
    }

    private void loadImage(ImageType type) {
        if (!futureByType.containsKey(type)) {
            File file = getFile(type);
            if (file.exists())
                futureByType.put(type, ImageUtil.loadImage(file));
        }
    }

    public GeotaggedScreenshot setFile(ImageType type, File file) {
        fileByType.put(type, file);
        if (type == ImageType.THUMBNAIL)
            isMissingThumbnail = !file.exists();
        return this;
    }

    public File getFile(ImageType type) {
        return fileByType.computeIfAbsent(type, t -> new File(type.getDir(), name + (ClientConfig.CONFIG_DATA.useJpgForScreenshots.get() ? ".jpg" : ".png")));
    }

    public DynamicTexture getTexture(ImageType type) {
        DynamicTexture texture = textureByType.get(type);
        if (texture != null)
            return texture;
        CompletableFuture<NativeImage> future = getImageFuture(type);
        if (future != null && future.isDone()) {
            texture = new DynamicTexture(future.join());
            textureByType.put(type, texture);
            return texture;
        }
        return null;
    }

    public int getId(ImageType type) {
        return getTexture(type) != null ? getTexture(type).getId() : 0;
    }

    public void close() {
        Arrays.stream(ImageType.values()).forEach(this::close);
    }

    public void close(ImageType type) {
        Optional.ofNullable(textureByType.remove(type)).ifPresent(DynamicTexture::close);
        Optional.ofNullable(futureByType.remove(type)).ifPresent(future -> {
            NativeImage image = future.getNow(null);
            if (image != null)
                image.close();
            else future.cancel(true);
        });
    }

    public String getName() {
        return name;
    }
}