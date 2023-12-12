package com.ccr4ft3r.geotaggedscreenshots.screens;

import com.ccr4ft3r.geotaggedscreenshots.container.GeotaggedScreenshot;
import com.ccr4ft3r.geotaggedscreenshots.container.ImageType;
import com.ccr4ft3r.geotaggedscreenshots.util.RenderUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ScreenshotView extends Screen {

    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 52;
    private static final int OFFSET = 4;

    private final GeotaggedScreenshot screenshot;

    private final Button closeButton;

    private final String missingMessage;
    private final Screen parent;

    public ScreenshotView(GeotaggedScreenshot screenshot, Vec3 coordinates, Screen parent) {
        super(Component.empty());
        this.screenshot = screenshot;
        this.missingMessage = "Couldn't find screenshot for: " + coordinates;
        this.parent = parent;
        this.closeButton = new Button(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, CommonComponents.GUI_DONE, btn -> onClose());
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        closeButton.x = (width - BUTTON_WIDTH) / 2;
        closeButton.y = height - BUTTON_HEIGHT - OFFSET;
        addRenderableWidget(closeButton);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderDirtBackground(0);

        NativeImage image = screenshot.getOrLoadImage(ImageType.ORIGINAL);
        if (image != null) {
            float imgRatio = (float) image.getWidth() / image.getHeight();
            int height = this.height - OFFSET * 3 - BUTTON_HEIGHT;
            int width = (int) (height * imgRatio);
            if (width > this.width) {
                width = this.width - OFFSET * 3;
                height = (int) (width / imgRatio);
            }
            int xOffset = (this.width - width) / 2;
            int yOffset = (this.height - height) / 2;
            RenderUtil.renderImage(poseStack, screenshot.getId(ImageType.ORIGINAL), image.getWidth(), image.getHeight(), width, height, xOffset, yOffset - OFFSET * 3);
        } else if (screenshot.getImageFuture(ImageType.ORIGINAL).isDone()) {
            int height = Minecraft.getInstance().font.lineHeight;
            GuiComponent.drawCenteredString(poseStack, Minecraft.getInstance().font, missingMessage, this.width / 2, (this.height - height) / 2, 16777215);
        }

        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        screenshot.close(ImageType.ORIGINAL);
        Minecraft.getInstance().setScreen(parent);
    }
}