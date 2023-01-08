package io.github.mortuusars.monobank.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import io.github.mortuusars.monobank.content.monobank.MonobankScreen;
import io.github.mortuusars.monobank.content.monobank.unlocking.Combination;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class CombinationTooltip implements ClientTooltipComponent, TooltipComponent {
    private ArrayList<ItemStack> items;

    public CombinationTooltip(Combination combination) {
        items = new ArrayList<>();
        for (int i = 0; i < Combination.SIZE; i++) {
            items.add(new ItemStack(combination.getItemInSlot(i)));
        }
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, PoseStack poseStack, ItemRenderer itemRenderer, int blitOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, MonobankScreen.TEXTURE);
        GuiComponent.blit(poseStack, mouseX, mouseY, 176, 12, 72, 30, 256, 256);
        for (int i = 0; i < items.size(); i++) {
            itemRenderer.renderAndDecorateItem(items.get(i), mouseX + 7 + 18 * i + 3 * i, mouseY + 7);
        }
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, MultiBufferSource.BufferSource pBufferSource) {
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public int getWidth(Font font) {
        return 18 * 3 + 2;
    }
}
