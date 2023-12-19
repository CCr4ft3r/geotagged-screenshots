package com.ccr4ft3r.geotaggedscreenshots.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class RenderUtil {

    public static void renderImage(GuiGraphics guiGraphics, int imageId, int scaledWidth, int scaledHeight, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, imageId);
        RenderSystem.enableBlend();
        blit(guiGraphics, xOffset, yOffset, scaledWidth, scaledHeight);
        RenderSystem.disableBlend();
    }

    private static void blit(GuiGraphics guiGraphics, int x, int y, int scaledWidth, int scaledHeight) {
        float x2 = x + scaledWidth;
        float y2 = y + scaledHeight;
        float u1 = 0;
        float v1 = 0;
        float u2 = 1f;
        float v2 = 1f;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        bufferBuilder.vertex(matrix4f, x, y, 0).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix4f, x, y2, 0).uv(u1, v2).endVertex();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).uv(u2, v2).endVertex();
        bufferBuilder.vertex(matrix4f, x2, y, 0).uv(u2, v1).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}