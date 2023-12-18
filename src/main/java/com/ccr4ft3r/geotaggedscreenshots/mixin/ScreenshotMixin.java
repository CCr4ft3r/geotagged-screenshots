package com.ccr4ft3r.geotaggedscreenshots.mixin;

import com.ccr4ft3r.geotaggedscreenshots.container.AlbumCollection;
import com.ccr4ft3r.geotaggedscreenshots.container.ScreenshotMetadata;
import com.ccr4ft3r.geotaggedscreenshots.util.FileUtil;
import com.ccr4ft3r.geotaggedscreenshots.util.xaero.XaeroWaypointUtil;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenshotEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static com.ccr4ft3r.geotaggedscreenshots.GeotaggedScreenshots.*;
import static com.ccr4ft3r.geotaggedscreenshots.util.ImageUtil.*;

@Mixin(Screenshot.class)
public class ScreenshotMixin {

    @Unique
    private static final Queue<ScreenshotEvent> geotagged_screenshots$screenshotEvents = new ConcurrentLinkedQueue<>();

    @ModifyArg(method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;_grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"), index = 3)
    private static Consumer<Component> modifyConsumer(Consumer<Component> consumer) {
        return consumer.andThen((component) -> {
            ScreenshotEvent event = geotagged_screenshots$screenshotEvents.poll();
            if (event == null) {
                return;
            }
            File screenshotFile = event.getScreenshotFile();
            if (!screenshotFile.exists())
                return;
            ScreenshotMetadata metadata = new ScreenshotMetadata(screenshotFile, UUID.randomUUID())
                .setWorldId(AlbumCollection.INSTANCE.getCurrentId())
                .setDimensionId(Objects.requireNonNull(Minecraft.getInstance().level, "No level at client side?").dimension().toString())
                .setCoordinates(Objects.requireNonNull(Minecraft.getInstance().player, "No player at client side?").position());
            File thumbnailFile = createThumbnail(event.getImage(), screenshotFile, metadata);
            EXECUTOR.submit(() -> {
                FileUtil.addMetadata(screenshotFile, metadata);
                XaeroWaypointUtil.addNewScreenshotWaypoint(metadata, screenshotFile, thumbnailFile);
            });
        });
    }

    @Inject(method = "_grab", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/event/ScreenshotEvent;getScreenshotFile()Ljava/io/File;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void onScreenshot(File p_92306_, String p_92307_, RenderTarget p_92310_, Consumer<Component> consumer, CallbackInfo ci, NativeImage nativeimage, File file1, File file2, ScreenshotEvent event) {
        geotagged_screenshots$screenshotEvents.add(event);
    }

}