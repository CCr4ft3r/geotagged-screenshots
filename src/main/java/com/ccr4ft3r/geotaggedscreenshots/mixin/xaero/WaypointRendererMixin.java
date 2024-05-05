package com.ccr4ft3r.geotaggedscreenshots.mixin.xaero;

import com.ccr4ft3r.geotaggedscreenshots.container.AlbumCollection;
import com.ccr4ft3r.geotaggedscreenshots.container.GeotaggedScreenshot;
import com.ccr4ft3r.geotaggedscreenshots.container.ImageType;
import com.ccr4ft3r.geotaggedscreenshots.screens.ScreenshotView;
import com.ccr4ft3r.geotaggedscreenshots.util.RenderUtil;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.element.MapElementReader;
import xaero.map.element.MapElementRenderProvider;
import xaero.map.element.MapElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointRenderContext;
import xaero.map.mods.gui.WaypointRenderer;

import java.util.concurrent.CompletableFuture;

@Mixin(xaero.map.mods.gui.WaypointRenderer.class)
public abstract class WaypointRendererMixin extends MapElementRenderer<Waypoint, WaypointRenderContext, WaypointRenderer> {

    protected WaypointRendererMixin(WaypointRenderContext context, MapElementRenderProvider<Waypoint, WaypointRenderContext> provider, MapElementReader<Waypoint, WaypointRenderContext, WaypointRenderer> reader) {
        super(context, provider, reader);
    }

    @Inject(method = "renderElement(ILxaero/map/mods/gui/Waypoint;ZLnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;DDDDFDDLnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;IDFDDZF)Z",
        at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void renderScreenshot(int location, Waypoint w, boolean hovered, Minecraft mc, PoseStack matrixStack, double cameraX, double cameraY, double mouseX, double mouseY, float brightness, double scale, double screenSizeBasedScale, TextureManager textureManager, Font fontRenderer, MultiBufferSource.BufferSource renderTypeBuffers, MultiTextureRenderTypeRendererProvider rendererProvider, int elementIndex, double optionalDepth, float optionalScale, double partialX, double partialY, boolean cave, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        GeotaggedScreenshot geotaggedScreenshot = AlbumCollection.INSTANCE.getCurrent().getScreenshot(w.getX(), w.getY(), w.getZ());
        if (geotaggedScreenshot == null)
            return;

        CompletableFuture<NativeImage> future = geotaggedScreenshot.getImageFuture(ImageType.THUMBNAIL);
        NativeImage image;
        if (future != null && future.isDone() && (image = future.getNow(null)) != null) {
            double imgScale = ((scale / 18) * optionalScale * this.context.worldmapWaypointsScale);
            int width = (int) Math.min(image.getWidth() * 1.5, (int) (image.getWidth() * imgScale));
            int height = (int) Math.min(image.getHeight() * 1.5, (int) (image.getHeight() * imgScale));
            RenderUtil.renderImage(matrixStack, geotaggedScreenshot.getId(ImageType.THUMBNAIL), width, height, width, height, width / -2, height / -2);
            if (!geotagged_screenshots$isHovered(location, w, mouseX, mouseY, scale, width, height, screenSizeBasedScale, context, partialTicks))
                cir.setReturnValue(false);
            else
                geotagged_screenshots$handleInput(w, mc, geotaggedScreenshot);
        } else
            cir.setReturnValue(true);
    }

    @Inject(method = "renderElementPre(ILxaero/map/mods/gui/Waypoint;ZLnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;DDDDFDDLnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;FDDZF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hideShadow(int location, Waypoint w, boolean hovered, Minecraft mc, PoseStack matrixStack, double cameraX, double cameraZ, double mouseX, double mouseZ, float brightness, double scale, double screenSizeBasedScale, TextureManager textureManager, Font fontRenderer, MultiBufferSource.BufferSource renderTypeBuffers, MultiTextureRenderTypeRendererProvider rendererProvider, float optionalScale, double partialX, double partialY, boolean cave, float partialTicks, CallbackInfo ci) {
        GeotaggedScreenshot geotaggedScreenshot = AlbumCollection.INSTANCE.getCurrent().getScreenshot(w.getX(), w.getY(), w.getZ());
        if (geotaggedScreenshot != null)
            ci.cancel();
    }

    @Unique
    private void geotagged_screenshots$handleInput(Waypoint w, Minecraft mc, GeotaggedScreenshot geotaggedScreenshot) {
        if (mc.screen instanceof GuiMapAccessor screen && screen.leftMouseButton().clicked) {
            mc.setScreen(new ScreenshotView(geotaggedScreenshot, new Vec3(w.getX(), w.getY(), w.getZ()), mc.screen));
            screen.setMouseDownPosX(-1);
            screen.setMouseDownPosY(-1);
        }
    }

    @Unique
    private boolean geotagged_screenshots$isHovered(int location, Waypoint waypoint, double mouseX, double mouseY, double scale, int width, int height, double screenSizeBasedScale, WaypointRenderContext context, float partialTicks) {
        double fullScale = reader.getBoxScale(location, waypoint, context);
        if (reader.shouldScaleBoxWithOptionalScale())
            fullScale *= screenSizeBasedScale;

        double left = reader.getInteractionBoxLeft(waypoint, context, partialTicks) * fullScale;
        double right = reader.getInteractionBoxRight(waypoint, context, partialTicks) * fullScale;
        double top = reader.getInteractionBoxTop(waypoint, context, partialTicks) * fullScale;
        double bottom = reader.getInteractionBoxBottom(waypoint, context, partialTicks) * fullScale;
        double labelWidth = (right - left);
        left -= ((double) width / 2 - labelWidth / 2);
        right += ((double) width / 2 - labelWidth / 2);
        top += (bottom - top) - (double) height / 2;
        bottom += (double) height / 2;
        double xOffset = (mouseX - reader.getRenderX(waypoint, context, partialTicks)) * scale;

        if (xOffset < left || xOffset > right)
            return false;

        double yOffset = (mouseY - reader.getRenderZ(waypoint, context, partialTicks)) * scale;
        return yOffset >= top && yOffset <= bottom;
    }
}