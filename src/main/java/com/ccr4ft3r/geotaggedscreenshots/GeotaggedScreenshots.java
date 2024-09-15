package com.ccr4ft3r.geotaggedscreenshots;

import com.ccr4ft3r.geotaggedscreenshots.config.ClientConfig;
import com.ccr4ft3r.geotaggedscreenshots.container.GeotaggedScreenshot;
import com.ccr4ft3r.geotaggedscreenshots.container.ImageType;
import com.ccr4ft3r.geotaggedscreenshots.container.WorldScreenshotAlbum;
import com.ccr4ft3r.geotaggedscreenshots.util.xaero.XaeroWaypointUtil;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.map.core.XaeroWorldMapCore;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ccr4ft3r.geotaggedscreenshots.util.ImageUtil.*;
import static com.ccr4ft3r.geotaggedscreenshots.util.xaero.XaeroWaypointUtil.*;

@Mod(ModConstants.MODID)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GeotaggedScreenshots {

    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    public static WorldScreenshotAlbum ALBUM;

    public GeotaggedScreenshots() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG, ModConstants.MODID + "-client.toml");
    }

    public static void load() {
        String newWorldId = XaeroWorldMapCore.currentSession.getMapProcessor().getCurrentWorldId();
        if (ALBUM != null && ALBUM.getId().equals(newWorldId) || newWorldId == null)
            return;
        if (ALBUM != null)
            ALBUM.getScreenshots().forEach(GeotaggedScreenshot::close);

        initWaypointSet();
        ALBUM = new WorldScreenshotAlbum(newWorldId);

        WaypointSet waypointSet = XaeroWaypointUtil.getScreenshotWaypointSet();
        waypointSet.getList().forEach(waypoint -> ALBUM.add(new GeotaggedScreenshot(waypoint.getName())));

        regenerateMissingThumbnails();
    }

    private static void regenerateMissingThumbnails() {
        List<GeotaggedScreenshot> missingThumbnails = ALBUM.getScreenshots().stream()
            .filter(screenshot -> !screenshot.getFile(ImageType.THUMBNAIL).exists()
                && screenshot.getFile(ImageType.ORIGINAL).exists()).toList();
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
            screenshot.setFile(ImageType.THUMBNAIL, createThumbnail(image, screenshot.getFile(ImageType.ORIGINAL)));
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