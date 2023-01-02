package io.github.mortuusars.monobank.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class ModifiersImageButton extends Button {
    protected final ResourceLocation textureLocation;
    protected final int xTextureStart;
    protected final int yTextureStart;
    protected final int yHoverOffset;
    protected int yCtrlOffset;
    protected int yShiftOffset;
    private int yCtrlShiftOffset;
    protected final int textureWidth;
    protected final int textureHeight;
    protected final String id;

    public ModifiersImageButton(int x, int y, int width, int height, int textureU, int textureV, int yHoverOffset,
                                int yCtrlOffset, int yShiftOffset, int yCtrlShiftOffset, ResourceLocation textureLocation,
                                int textureWidth, int textureHeight, OnPress onPress, OnTooltip onTooltip, Component message, String id) {
        super(x, y, width, height, message, onPress, onTooltip);
        this.xTextureStart = textureU;
        this.yTextureStart = textureV;
        this.yHoverOffset = yHoverOffset;
        this.yCtrlOffset = yCtrlOffset;
        this.yShiftOffset = yShiftOffset;
        this.yCtrlShiftOffset = yCtrlShiftOffset;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.textureLocation = textureLocation;
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.textureLocation);
        RenderSystem.enableDepthTest();

        int startYPos = this.yTextureStart;

        if (yCtrlShiftOffset > 0 && (Screen.hasControlDown() && Screen.hasShiftDown()))
            startYPos += yCtrlShiftOffset;
        else if (yCtrlOffset > 0 && Screen.hasControlDown())
            startYPos += yCtrlOffset;
        else if (yShiftOffset > 0 && Screen.hasShiftDown())
            startYPos += yShiftOffset;

        if (this.isHoveredOrFocused())
            startYPos += this.yHoverOffset;

        blit(pPoseStack, this.x, this.y, this.xTextureStart, startYPos, this.width, this.height, this.textureWidth, this.textureHeight);

        if (this.isHovered)
            this.renderToolTip(pPoseStack, pMouseX, pMouseY);
    }

    public static class Builder {
        int x, y, width, height, textureU, textureV, yHoverOffset, yCtrlOffset, yShiftOffset, yCtrlShiftOffset;
        final ResourceLocation textureLocation;
        final OnPress onPress;
        int textureWidth = 256;
        int textureHeight = 256;
        OnTooltip onTooltip = NO_TOOLTIP;
        Component message = TextComponent.EMPTY;
        String id = "";

        public Builder(int x, int y, int width, int height, int textureU, int textureV, ResourceLocation textureLocation, OnPress onPress) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textureU = textureU;
            this.textureV = textureV;
            this.yHoverOffset = height;
            this.yCtrlOffset = -1;
            this.yShiftOffset = -1;
            this.yCtrlShiftOffset = -1;
            this.textureLocation = textureLocation;
            this.onPress = onPress;
        }

        public Builder textureSize(int width, int height) {
            this.textureWidth = width;
            this.textureHeight = height;
            return this;
        }

        public Builder onTooltip(OnTooltip onTooltip) {
            this.onTooltip = onTooltip;
            return this;
        }

        public Builder message(Component message) {
            this.message = message;
            return this;
        }

        public Builder ctrlYOffset(int ctrlOffset) {
            this.yCtrlOffset = ctrlOffset;
            return this;
        }

        public Builder shiftYOffset(int shiftOffset) {
            this.yShiftOffset = shiftOffset;
            return this;
        }

        public Builder ctrlShiftYOffset(int ctrlShiftOffset) {
            this.yCtrlShiftOffset = ctrlShiftOffset;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public ModifiersImageButton build() {
            return new ModifiersImageButton(x, y, width, height, textureU, textureV, yHoverOffset,
                    yCtrlOffset, yShiftOffset, yCtrlShiftOffset,
                    textureLocation, textureWidth, textureHeight, onPress, onTooltip, message, id);
        }
    }
}
