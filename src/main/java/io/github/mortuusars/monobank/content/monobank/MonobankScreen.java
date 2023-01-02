package io.github.mortuusars.monobank.content.monobank;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import io.github.mortuusars.monobank.core.inventory.BigItemHandlerSlot;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Optional;

public class MonobankScreen extends PatchedAbstractContainerScreen<MonobankMenu> {

    private static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank.png");

    private static final Component CTRL_TOOLTIP = TextUtil.translate("gui.monobank.tooltip.ctrl_take_single").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component CTRL_SHIFT_TOOLTIP = TextUtil.translate("gui.monobank.tooltip.ctrl_shift_take_single").withStyle(ChatFormatting.DARK_GRAY);

    public MonobankScreen(MonobankMenu containerMenu, Inventory playerinventory, Component title) {
        super(containerMenu, playerinventory, title);
    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {

        if (pSlotId == MonobankMenu.MONOBANK_SLOT_INDEX && pMouseButton == 0) {

            if (pType == ClickType.PICKUP_ALL)
                return;

            if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                pMouseButton = -102;
                pType = ClickType.QUICK_MOVE;
            }
            else if (Screen.hasControlDown()) {
                pMouseButton = -101;
                pType = ClickType.QUICK_MOVE;
            }
        }

        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int x, int y) {
        if (this.menu.getCarried().isEmpty() && hoveredSlot != null &&
                hoveredSlot instanceof BigItemHandlerSlot bankSlot && bankSlot.hasItem()) {
            renderBankSlotTooltip(bankSlot.getItem(), poseStack, x, y);
        }
        else
            super.renderTooltip(poseStack, x, y);
    }

    @Override
    protected String getCountStringForSlot(Slot slot, ItemStack itemStack, @Nullable String countString) {
        if (slot instanceof BigItemHandlerSlot && !itemStack.isEmpty())
            return TextUtil.shortenNumber(itemStack.getCount());
        return super.getCountStringForSlot(slot, itemStack, countString);
    }

    @Override
    protected void renderSlot(PoseStack poseStack, Slot slot) {

        super.renderSlot(poseStack, slot);

//        if (slot instanceof BigItemHandlerSlot bigSlot/* && bigSlot.getItem().getCount() > 99*/) {
//            RenderSystem.enableDepthTest();
//            ItemStack itemStack = bigSlot.getItem();
//            int count = itemStack.getCount();
//
//            String countString = TextUtil.shortenNumber(count);
//
//            poseStack.pushPose();
//
//            BakedModel model = itemRenderer.getModel(itemStack, null, null, 0);
//
//            Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
//            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
//            RenderSystem.enableBlend();
//            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            PoseStack modelViewStack = RenderSystem.getModelViewStack();
//            modelViewStack.pushPose();
//
//
//            modelViewStack.translate(bigSlot.x, bigSlot.y, itemRenderer.blitOffset + 200);
//            modelViewStack.translate(bigSlot.getWidth() / 2, bigSlot.getHeight() / 2, 0f);
//            modelViewStack.scale(1.0F, -1.0F, 1.0F);
//            modelViewStack.scale(bigSlot.getWidth(), bigSlot.getHeight(), bigSlot.getWidth());
//
//            RenderSystem.applyModelViewMatrix();
//            PoseStack newPoseStack = new PoseStack();
//            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
//            boolean flag = !model.usesBlockLight();
//            if (flag) {
//                Lighting.setupForFlatItems();
//            }
//
//            itemRenderer.render(itemStack, ItemTransforms.TransformType.GUI, false, newPoseStack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);
//            multibuffersource$buffersource.endBatch();
//            RenderSystem.enableDepthTest();
//            if (flag) {
//                Lighting.setupFor3DItems();
//            }
//
//            modelViewStack.popPose();
//            RenderSystem.applyModelViewMatrix();
//
//
////            itemRenderer.render(itemStack, ItemTransforms.TransformType.GUI, false, poseStack, Minecraft.getInstance()
////                            .renderBuffers().bufferSource(),
////                    0xFFFFFF, 0xFFFFFF, );
////            itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.GUI, 0xFFFFFF, 0xFFFFFF,
////                    poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), 0);
//
//            poseStack.popPose();
//
////            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemStack, slot.x, slot.y, slot.x + slot.y * this.imageWidth);
//
////            renderGuiItemDecorationsForBigSlot(this.font, itemStack, slot.x, slot.y, countString);
//        }
//        else
//            super.renderSlot(poseStack, slot);
    }

    protected void renderBankSlotTooltip(ItemStack itemStack, PoseStack poseStack, int x, int y) {
        List<Component> tooltip = super.getTooltipFromItem(itemStack);
        int stackCount = itemStack.getCount();
        if (stackCount > 1000) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            DecimalFormat numberFormatter = new DecimalFormat("###,###,###", symbols);
            String formattedCount = numberFormatter.format(stackCount);

//            tooltip.add(new TextComponent("")); // Separator
            MutableComponent newTitle = tooltip.get(0).copy()
                    .append(new TextComponent(" - ").withStyle(ChatFormatting.GRAY))
                    .append(TextUtil.translate("gui.monobank.count", formattedCount).withStyle(ChatFormatting.GRAY));
            tooltip.set(0, newTitle);
//            tooltip.add(TextUtil.translate("gui.monobank.count", formattedCount).withStyle(ChatFormatting.UNDERLINE));
        }

        tooltip.add(CTRL_TOOLTIP);
        tooltip.add(CTRL_SHIFT_TOOLTIP);

        renderTooltip(poseStack, tooltip, Optional.empty(), x, y, itemStack);
    }
}
