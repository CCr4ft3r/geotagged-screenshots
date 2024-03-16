package com.ccr4ft3r.geotaggedscreenshots.mixin;

import com.ccr4ft3r.geotaggedscreenshots.config.ClientConfig;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.ccr4ft3r.geotaggedscreenshots.GeotaggedScreenshots.*;
import static com.ccr4ft3r.geotaggedscreenshots.util.ImageUtil.*;

@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {

    @Unique
    private static final Queue<ScreenshotEvent> geotagged_screenshots$screenshotEvents = new ConcurrentLinkedQueue<>();

    @Unique
    private static final AtomicInteger geotagged_screenshots$screenshotCounter = new AtomicInteger();
    @Unique
    private static final AtomicReference<Boolean> geotagged_screenshots$wasUiHidden = new AtomicReference<>();

    @Shadow
    public static void grab(File p_92296_, @Nullable String p_92297_, RenderTarget p_92300_, Consumer<Component> p_92301_) {
    }

    @Redirect(method = "lambda$_grab$2", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1))
    private static <T> void disableSuccessChatMessage(Consumer<T> consumer, T component) {
        if (ClientConfig.CONFIG_DATA.disableScreenshotChatMessage.get())
            consumer.accept(null);
        else consumer.accept(component);
    }

    @Inject(method = "grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private static void grabHead(File p_92290_, RenderTarget p_92293_, Consumer<Component> p_92294_, CallbackInfo ci) {
        geotagged_screenshots$screenshotCounter.incrementAndGet();
        if (geotagged_screenshots$wasUiHidden.get() == null)
            geotagged_screenshots$wasUiHidden.set(Minecraft.getInstance().options.hideGui);
        Minecraft.getInstance().options.hideGui = ClientConfig.CONFIG_DATA.hideUiAtTakingScreenshots.get();
        SCHEDULER.schedule(() -> {
            grab(p_92290_, null, p_92293_, p_92294_.andThen((component) -> {
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
                ;
                if (geotagged_screenshots$screenshotCounter.decrementAndGet() == 0) {
                    Minecraft.getInstance().options.hideGui = geotagged_screenshots$wasUiHidden.get();
                    geotagged_screenshots$wasUiHidden.set(null);
                }
            }));
        }, 10, TimeUnit.MILLISECONDS);
        ci.cancel();
    }

    @Inject(method = "_grab", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/event/ScreenshotEvent;getScreenshotFile()Ljava/io/File;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void onScreenshot(File p_92306_, String p_92307_, RenderTarget p_92310_, Consumer<Component> consumer, CallbackInfo ci, NativeImage nativeimage, File file1, File file2, ScreenshotEvent event) {
        geotagged_screenshots$screenshotEvents.add(event);
    }
}