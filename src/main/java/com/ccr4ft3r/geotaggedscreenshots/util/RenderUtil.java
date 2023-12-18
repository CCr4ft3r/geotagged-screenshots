package com.ccr4ft3r.geotaggedscreenshots.util;

import com.ccr4ft3r.geotaggedscreenshots.ModConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderUtil {

    public static void renderImage(GuiGraphics guiGraphics, int imageId, int width, int height, int scaledWidth, int scaledHeight, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, imageId);
        RenderSystem.enableBlend();
        guiGraphics.blit(new ResourceLocation(ModConstants.MODID, "blahblah"), xOffset, yOffset, scaledWidth, scaledHeight, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
    }
}