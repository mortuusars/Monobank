package io.github.mortuusars.monobank.client.gui.screen;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.world.inventory.GhostSlot;
import io.github.mortuusars.monobank.world.inventory.menu.LockReplacementMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LockReplacementScreen extends AbstractContainerScreen<LockReplacementMenu> {
    public static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank_lock_replacement.png");
    public static final WidgetSprites CONFIRM_SPRITES = new WidgetSprites(Monobank.resource("confirm_button"),
            Monobank.resource("confirm_button_disabled"),
            Monobank.resource("confirm_button_highlighted"));
    private ImageButton confirmButton;

    public LockReplacementScreen(LockReplacementMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        confirmButton = new ImageButton(leftPos + 128, topPos + 34, 18, 18,
                CONFIRM_SPRITES, this::onConfirmButtonPress, Component.translatable("monobank.gui.monobank.lock_replacement.confirm.tooltip"));
        confirmButton.setTooltip(Tooltip.create(Component.translatable("monobank.gui.monobank.lock_replacement.confirm.tooltip")));
        this.addRenderableWidget(confirmButton);
    }

    protected void onConfirmButtonPress(Button button) {
        if (Minecraft.getInstance().gameMode != null) {
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, LockReplacementMenu.CONFIRM_BUTTON_ID);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        confirmButton.active = getMenu().slots.stream().limit(3).noneMatch(s -> s.getItem().is(Monobank.Tags.Items.LOCK_BLACKLIST));
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        super.renderSlot(guiGraphics, slot);
        if (slot instanceof GhostSlot ghostSlot) {
            if (ghostSlot.getItem().is(Monobank.Tags.Items.LOCK_BLACKLIST)) {
                int x = slot.x;
                int y = slot.y;
                guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, 5, 0x66FF5A4D);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
