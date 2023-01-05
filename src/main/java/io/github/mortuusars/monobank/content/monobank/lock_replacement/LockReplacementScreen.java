package io.github.mortuusars.monobank.content.monobank.lock_replacement;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LockReplacementScreen extends PatchedAbstractContainerScreen<LockReplacementMenu> {
    private static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank_lock_replacement.png");
    public LockReplacementScreen(LockReplacementMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new ImageButton(getGuiLeft() + 128, getGuiTop() + 34,
                18, 18, 176, 0, TEXTURE, this::onConfirmButtonPress));
    }

    private void onConfirmButtonPress(Button button) {
//        getMenu().clickMenuButton()
        this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 0);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
