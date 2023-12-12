package com.ccr4ft3r.geotaggedscreenshots.container;

import com.ccr4ft3r.geotaggedscreenshots.util.ImageUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GeotaggedScreenshot {

    private final WorldScreenshotAlbum parent;
    private final ScreenshotMetadata metadata;

    private final Map<ImageType, File> fileByType = new HashMap<>();
    private final Map<ImageType, CompletableFuture<NativeImage>> imageByType = new HashMap<>();
    private final Map<ImageType, DynamicTexture> textureByType = new HashMap<>();

    public GeotaggedScreenshot(WorldScreenshotAlbum parent, ScreenshotMetadata metadata) {
        this.parent = parent;
        this.metadata = metadata;
    }

    public CompletableFuture<NativeImage> getImageFuture(ImageType type) {
        return imageByType.get(type);
    }

    public NativeImage getOrLoadImage(ImageType type) {
        if (!imageByType.containsKey(type)) {
            File file = fileByType.get(type);
            if (file != null && file.exists()) {
                imageByType.put(type, ImageUtil.loadImage(file));
            } else {
                file = parent.load(this, type);
                fileByType.put(type, file);
                imageByType.put(type, ImageUtil.loadImage(file));
            }
        }
        return imageByType.get(type).getNow(null);
    }

    public GeotaggedScreenshot setFile(ImageType type, File file) {
        fileByType.put(type, file);
        return this;
    }

    public DynamicTexture getTexture(ImageType type) {
        DynamicTexture texture = textureByType.get(type);
        if (texture != null)
            return texture;
        CompletableFuture<NativeImage> image = imageByType.get(type);
        if (image == null)
            getOrLoadImage(type);
        else if (image.isDone()) {
            texture = new DynamicTexture(image.join());
            textureByType.put(type, texture);
            return texture;
        }
        return null;
    }

    public int getId(ImageType type) {
        return getTexture(type) != null ? getTexture(type).getId() : 0;
    }

    public ScreenshotMetadata getMetadata() {
        return metadata;
    }

    public void close() {
        Arrays.stream(ImageType.values()).forEach(this::close);
    }

    public void close(ImageType type) {
        Optional.ofNullable(textureByType.remove(type)).ifPresent(DynamicTexture::close);
        Optional.ofNullable(imageByType.remove(type)).ifPresent(future -> {
            NativeImage image = future.getNow(null);
            if (image != null)
                image.close();
            else future.cancel(true);
        });
    }

    @Override
    public int hashCode() {
        Vec3 coordinates = metadata.getCoordinates();
        return Objects.hash((int) coordinates.x, (int) coordinates.y, (int) coordinates.z);
    }
}