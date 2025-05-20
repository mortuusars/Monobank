package io.github.mortuusars.monobank.content.monobank.unlocking;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.rendering.ResizeableItemRenderer;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import io.github.mortuusars.monobank.config.Configuration;
import io.github.mortuusars.monobank.core.inventory.UnlockingSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class UnlockingScreen extends PatchedAbstractContainerScreen<UnlockingMenu> {
    private static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank_unlocking.png");

    public UnlockingScreen(UnlockingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Keyhole casing overlay
        graphics.blit( TEXTURE, getGuiLeft() + 73, getGuiTop() + 28, 176, 0, 72, 30);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (slot instanceof UnlockingSlot unlockingSlot) {
            int x = unlockingSlot.x;
            int y = unlockingSlot.y;

            if (unlockingSlot.hasItem()) {
                int index = menu.slots.indexOf(unlockingSlot);
                if (!menu.combination.matches(index, unlockingSlot.getItem().getItem())) {
                    graphics.fill(x, y, x + 16, y + 16, 100, 0x9FAD422F); // Red overlay
                }
            } else if (Configuration.Server.SHOW_HINT_ICON.get()) {
                ResizeableItemRenderer.renderGuiItem(unlockingSlot.getKeyway(), unlockingSlot.x, unlockingSlot.y, 16, 16, 0, 0xCC00CC, null);
                graphics.fill(x, y, x + 16, y + 16, 100, 0xDD8B8B8B); // Gray overlay
            }

            super.renderSlot(graphics, slot);
        }
        else
            super.renderSlot(graphics, slot);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (hoveredSlot instanceof UnlockingSlot unlockingSlot && !unlockingSlot.hasItem()) {
            graphics.renderTooltip(Minecraft.getInstance().font, unlockingSlot.getKeywayTooltip(), x, y);
        }
        else {
            super.renderTooltip(graphics, x, y);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
