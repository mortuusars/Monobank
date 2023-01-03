package io.github.mortuusars.monobank.content.monobank.unlocking;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MonobankUnlockingScreen extends PatchedAbstractContainerScreen<MonobankUnlockingMenu> {

    private static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank_unlocking.png");

    public MonobankUnlockingScreen(MonobankUnlockingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

//        ResizeableItemRenderer.renderGuiItem(getMenu().slots.get(0).getItem(), getGuiLeft() + 40, getGuiTop() + 34, 24, 24, 0xF000F0);
//
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShaderTexture(0, TEXTURE);
//
//        poseStack.pushPose();
//        poseStack.translate(0, 0, 150);
//
//        fill(poseStack, this.leftPos + 40, this.topPos + 30, this.leftPos + 60, this.topPos + 50, 0x55C8C8C8);
//
////        this.blit(poseStack, this.leftPos + 50, this.topPos + 40, 75, 30, 20, 20);
//        poseStack.popPose();

        // Texture overlay
        this.blit(poseStack, getGuiLeft() + 25, getGuiTop() + 28, 176, 0, 72, 30);

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
