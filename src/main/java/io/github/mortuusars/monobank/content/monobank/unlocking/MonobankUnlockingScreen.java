package io.github.mortuusars.monobank.content.monobank.unlocking;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.rendering.ResizeableItemRenderer;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import io.github.mortuusars.monobank.core.inventory.UnlockingSlot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class MonobankUnlockingScreen extends PatchedAbstractContainerScreen<MonobankUnlockingMenu> {

    private static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank_unlocking.png");

    public MonobankUnlockingScreen(MonobankUnlockingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        // Keyhole casing overlay
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, getGuiLeft() + 25, getGuiTop() + 28, 176, 0, 72, 30);

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(PoseStack poseStack, Slot slot) {
        if (slot instanceof UnlockingSlot unlockingSlot/* && !unlockingSlot.hasItem()*/) {
            ResizeableItemRenderer.renderGuiItem(unlockingSlot.getKeyway(), unlockingSlot.x, unlockingSlot.y, 16, 16, -50, 0xCC00CC, null);
            int x = unlockingSlot.x;
            int y = unlockingSlot.y;

            int color = 0x9F8B8B8B; // gray
            if (unlockingSlot.hasItem()) {
                int index = menu.slots.indexOf(unlockingSlot);
                if (!menu.combination.matches(index, unlockingSlot.getItem().getItem()))
                    color = 0x9Fad422f; // red
            }

            fill(poseStack, x, y, x + 16, y + 16, color);
            super.renderSlot(poseStack, slot);
        }
        else
            super.renderSlot(poseStack, slot);
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
