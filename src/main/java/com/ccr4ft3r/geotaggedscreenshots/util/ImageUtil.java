package com.ccr4ft3r.geotaggedscreenshots.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class ImageUtil {

    public static void createThumbnail(NativeImage original, File target, int width, int height) {
        NativeImage thumbnail = new NativeImage(width, height, false);
        original.resizeSubRectTo(0, 0, original.getWidth(), original.getHeight(), thumbnail);
        try {
            if (target.getParentFile().exists() || target.getParentFile().mkdirs())
                thumbnail.writeToFile(target);
            else
                throw new IOException("Couldn't create directory " + target.getParentFile());
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't create thumbnail file {} was not found.", target, e);
        }
    }

    public static CompletableFuture<NativeImage> loadImage(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                return NativeImage.read(inputStream);
            } catch (Exception e) {
                LogUtils.getLogger().error("File {} was not found.", file, e);
            }
            return null;
        }, Util.backgroundExecutor());
    }
}