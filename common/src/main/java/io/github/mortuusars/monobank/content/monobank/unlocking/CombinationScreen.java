package io.github.mortuusars.monobank.content.monobank.unlocking;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import io.github.mortuusars.monobank.world.inventory.MatchTemplateSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class CombinationScreen extends PatchedAbstractContainerScreen<CombinationMenu> {
    public  static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank_unlocking.png");

    public CombinationScreen(CombinationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Keyhole casing overlay
        graphics.blit( TEXTURE, leftPos + 73, topPos + 28, 176, 0, 72, 30);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (slot instanceof MatchTemplateSlot templateSlot/* && !unlockingSlot.hasItem()*/) {
//            Minecraft.getInstance().getItemRenderer().render(templateSlot.getTemplate(), ItemDisplayContext.GUI,
//                    false, graphics.pose(), Minecraft.getInstance().renderBuffers().bufferSource(), 0xCC00CC, OverlayTexture.NO_OVERLAY, );
            int x = templateSlot.x;
            int y = templateSlot.y;

            int color = 0x9F8B8B8B; // gray
            if (templateSlot.hasItem()) {
                int index = menu.slots.indexOf(templateSlot);
                if (!menu.combination.matches(index, templateSlot.getItem().getItem()))
                    color = 0x9Fad422f; // red
            }

            graphics.fill(x, y, x + 16, y + 16, 5, color);
            super.renderSlot(graphics, slot);
        }
        else
            super.renderSlot(graphics, slot);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (hoveredSlot instanceof MatchTemplateSlot matchTemplateSlot && !matchTemplateSlot.hasItem()) {
            graphics.renderTooltip(Minecraft.getInstance().font, matchTemplateSlot.getTemplateTooltip(), x, y);
        }
        else
            super.renderTooltip(graphics, x, y);
    }
}
