package io.github.mortuusars.monobank.client.gui.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Contains modified copies of ItemRenderer's methods with the ability to render bigger than 16px.
 */
public class ResizeableItemRenderer {


    public static void renderGuiItem(ItemStack stack, int x, int y, int width, int height, float zOffset, int combinedLight, @Nullable BakedModel bakedModel) {
        if (stack.isEmpty())
            return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        if (bakedModel == null)
            bakedModel = itemRenderer.getModel(stack, null, null, 0);

        Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(x, y, zOffset);
        modelViewStack.translate(width / 2.0f, height / 2.0f, 0.0D);
        modelViewStack.scale(1.0F, -1.0F, 1.0F);
        modelViewStack.scale(width, height, width);
        RenderSystem.applyModelViewMatrix();
        PoseStack newPoseStack = new PoseStack();
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedModel.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        itemRenderer.render(stack, ItemDisplayContext.GUI, false, newPoseStack, multibuffersource$buffersource, combinedLight, OverlayTexture.NO_OVERLAY, bakedModel);
        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderGuiItem(ItemStack stack, int x, int y, int width, int height) {
        renderGuiItem(stack, x, y, width, height, 0xF000F0);
    }

    public static void renderGuiItem(ItemStack stack, int x, int y, int width, int height, int combinedLight) {
        renderGuiItem(stack, x, y, width, height, 100F, combinedLight, null);
    }

    public static void renderGuiItemDecorations(GuiGraphics graphics, Font font, ItemStack slotItemStack, int x, int y, int slotWidth, int slotHeight, String countString, boolean fixDurabilityBarOverlapping) {
        renderGuiItemDecorations(graphics, font, slotItemStack, x, y, slotWidth, slotHeight, 0xFFFFFF, countString, fixDurabilityBarOverlapping);
    }

    public static void renderGuiItemDecorations(GuiGraphics graphics, Font font, ItemStack slotItemStack, int x, int y, int slotWidth, int slotHeight, int fontColor, String countString, boolean fixDurabilityBarOverlapping) {
        if (slotItemStack.isEmpty())
            return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        if (slotItemStack.isBarVisible()) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();

            int leftRightPadding = (int) (2 / 16.0f * slotWidth * 1.2f);
            int bottomPadding = (int) (3 / 16.0f * slotHeight);

            int filledBarWidth = (int) (slotItemStack.getBarWidth() / 16f * slotWidth);
            int barColor = slotItemStack.getBarColor();

            int barStartX = x + leftRightPadding;
            int barStartY = y + slotHeight - bottomPadding;
            int barWidth = slotWidth - leftRightPadding * 2;

            int barThickness = Math.max(2, (int) (2 / 16.0f * slotHeight * 0.9f));
            int filledBarThickness = Math.max(1, (int) (1 / 16.0f * slotHeight * 0.9f));

            graphics.fill(barStartX, barStartY, barStartX + barWidth, barStartY + barThickness, 100, 0xFF000000);
            graphics.fill(barStartX, barStartY, barStartX + filledBarWidth, barStartY + filledBarThickness, 110, barColor | 0xFF << 24);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
        }

        if (slotItemStack.getCount() != 1 || countString != null) {
            String countS = countString == null ? String.valueOf(slotItemStack.getCount()) : countString;

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, 200.0F);
            MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            float startX = x + slotWidth + (1 / 16f * slotWidth) - font.width(countS);
            float startY = y + slotHeight + (2 / 16f * slotHeight) - font.lineHeight;

            // By default, durability bar renders on top of count. This moves it under.
            if (fixDurabilityBarOverlapping)
                graphics.drawString(font, countS, startX, startY, fontColor, true);
            else
                font.drawInBatch(countS, startX, startY, fontColor, true, graphics.pose().last().pose(), multibuffersource$buffersource, Font.DisplayMode.NORMAL, 0, 15728880);

            poseStack.popPose();

            multibuffersource$buffersource.endBatch();
        }

        LocalPlayer localplayer = Minecraft.getInstance().player;
        float cooldownPercent = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(slotItemStack.getItem(), Minecraft.getInstance().getFrameTime());
        if (cooldownPercent > 0.0F) {
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            graphics.fill(x, y + Mth.floor(slotHeight * (1.0F - cooldownPercent)), slotWidth, Mth.ceil(slotHeight * cooldownPercent), 0x7FFFFFFF);
            RenderSystem.enableDepthTest();
        }
    }
}
