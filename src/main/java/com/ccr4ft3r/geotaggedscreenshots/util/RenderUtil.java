package com.ccr4ft3r.geotaggedscreenshots.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;

public class RenderUtil {

    public static void renderImage(PoseStack matrixStack, int imageId, int width, int height, int scaledWidth, int scaledHeight, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, imageId);
        RenderSystem.enableBlend();
        GuiComponent.blit(matrixStack, xOffset, yOffset, scaledWidth, scaledHeight, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
    }
}