package io.github.mortuusars.monobank.client.gui.screen;

import io.github.mortuusars.monobank.Config;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.rendering.ExtendedSlotRenderer;
import io.github.mortuusars.monobank.world.inventory.menu.CombinationMenu;
import io.github.mortuusars.monobank.world.inventory.MatchTemplateSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class CombinationScreen extends AbstractContainerScreen<CombinationMenu> {
    public static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank_unlocking.png");

    public CombinationScreen(CombinationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Keyhole casing overlay
        graphics.blit(TEXTURE, leftPos + 73, topPos + 28, 176, 0, 72, 30);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (hoveredSlot instanceof MatchTemplateSlot matchTemplateSlot && !matchTemplateSlot.hasItem()) {
            graphics.renderTooltip(Minecraft.getInstance().font, matchTemplateSlot.getTemplateTooltip(), x, y);
        } else
            super.renderTooltip(graphics, x, y);
    }

    // -- Slot

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        super.renderSlot(guiGraphics, slot);
        if (slot instanceof MatchTemplateSlot templateSlot) {
            renderTemplateSlotOverlay(guiGraphics, templateSlot);
            renderTemplateSlotIcon(guiGraphics, templateSlot);
        }
    }

    protected void renderTemplateSlotOverlay(GuiGraphics guiGraphics, MatchTemplateSlot slot) {
        if (slot.hasItem() && !slot.containedItemMatches()) {
            int x = slot.x;
            int y = slot.y;
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, 5, 0x66FF5A4D);
        }
    }

    protected void renderTemplateSlotIcon(GuiGraphics guiGraphics, MatchTemplateSlot slot) {
        if (slot.hasItem() || !Config.Server.COMBINATION_SLOT_ICONS.get()) {
            return;
        }

        int x = slot.x;
        int y = slot.y;

        // Ghost (template) item
        ExtendedSlotRenderer.renderFakeItem(guiGraphics, slot.getTemplateItem(), x, y,
                0, 0, 0xCC0055, 16, 16);

        double opacity1 = Config.Server.COMBINATION_SLOT_ICONS_OPACITY.get() / 100.0;
        double opacity2 = opacity1 * opacity1 * opacity1;
        int alpha1 = (int) ((1.0 - opacity1) * 255);
        int alpha2 = (int) ((1.0 - opacity2) * 255);

        int color1 = (alpha1 << 24) | 0x8B8B8B;
        int color2 = (alpha2 << 24) | 0x8B8B8B;

        // Gray checkerboard overlay to hide the item:
        for (int v = 0; v < 4; v++) {
            for (int h = 0; h < 4; h++) {
                int color = (h + v) % 2 == 0 ? color1 : color2;
                guiGraphics.fill(RenderType.guiOverlay(),
                        x + h * 4,
                        y + v * 4,
                        x + h * 4 + 4,
                        y + v * 4 + 4, 5, color);
            }
        }
    }
}
