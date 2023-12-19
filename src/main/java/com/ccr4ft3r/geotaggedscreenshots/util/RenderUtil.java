package com.ccr4ft3r.geotaggedscreenshots.util;

import com.ccr4ft3r.geotaggedscreenshots.ModConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderUtil {

    public static void renderImage(GuiGraphics guiGraphics, int imageId, int width, int height, int scaledWidth, int scaledHeight, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, imageId);
        RenderSystem.enableBlend();
        blit(guiGraphics, xOffset, yOffset, scaledWidth, scaledHeight, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
    }
    public static void blit(GuiGraphics guiGraphics, int p_283671_, int p_282377_, int p_282058_, int p_281939_, float p_282285_, float p_283199_, int p_282186_, int p_282322_, int p_282481_, int p_281887_) {
        blit(guiGraphics, p_283671_, p_283671_ + p_282058_, p_282377_, p_282377_ + p_281939_, 0, p_282186_, p_282322_, p_282285_, p_283199_, p_282481_, p_281887_);
    }

    static void blit(GuiGraphics guiGraphics, int p_282732_, int p_283541_, int p_281760_, int p_283298_, int p_283429_, int p_282193_, int p_281980_, float p_282660_, float p_281522_, int p_282315_, int p_281436_) {
        innerBlit(guiGraphics, p_282732_, p_283541_, p_281760_, p_283298_, p_283429_, (p_282660_ + 0.0F) / (float)p_282315_, (p_282660_ + (float)p_282193_) / (float)p_282315_, (p_281522_ + 0.0F) / (float)p_281436_, (p_281522_ + (float)p_281980_) / (float)p_281436_);
    }
    private static void innerBlit(GuiGraphics guiGraphics, int p_93114_, int p_93115_, int p_93116_, int p_93117_, int p_93118_, float p_93119_, float p_93120_, float p_93121_, float p_93122_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        bufferbuilder.vertex(matrix4f, (float)p_93114_, (float)p_93117_, (float)p_93118_).uv(p_93119_, p_93122_).endVertex();
        bufferbuilder.vertex(matrix4f, (float)p_93115_, (float)p_93117_, (float)p_93118_).uv(p_93120_, p_93122_).endVertex();
        bufferbuilder.vertex(matrix4f, (float)p_93115_, (float)p_93116_, (float)p_93118_).uv(p_93120_, p_93121_).endVertex();
        bufferbuilder.vertex(matrix4f, (float)p_93114_, (float)p_93116_, (float)p_93118_).uv(p_93119_, p_93121_).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}