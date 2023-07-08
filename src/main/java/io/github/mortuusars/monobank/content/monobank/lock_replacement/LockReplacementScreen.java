package io.github.mortuusars.monobank.content.monobank.lock_replacement;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LockReplacementScreen extends PatchedAbstractContainerScreen<LockReplacementMenu> {
    private static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank_lock_replacement.png");
    private static final Component CONFIRM_TOOLTIP = TextUtil.translate("gui.monobank.lock_replacement.confirm.tooltip");
    public LockReplacementScreen(LockReplacementMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        ImageButton button = new ImageButton(getGuiLeft() + 128, getGuiTop() + 34,
                18, 18, 176, 0, 18, TEXTURE,
                256, 256, this::onConfirmButtonPress, CONFIRM_TOOLTIP);
        button.setTooltip(Tooltip.create(CONFIRM_TOOLTIP));
        this.addRenderableWidget(button);
    }

    private void onConfirmButtonTooltip(Button button, GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.renderTooltip(font, CONFIRM_TOOLTIP, mouseX, mouseY);
    }

    private void onConfirmButtonPress(Button button) {
        assert this.minecraft != null;
        assert this.minecraft.gameMode != null;
        this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
