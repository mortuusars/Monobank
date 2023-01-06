package io.github.mortuusars.monobank.content.monobank;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.rendering.ResizeableItemRenderer;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import io.github.mortuusars.monobank.core.inventory.BigItemHandlerSlot;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
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
    protected void init() {
        super.init();
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

        if (getMenu().extraInfo.isOwner)
            this.blit(poseStack, getGuiLeft() + 161, getGuiTop() + 3, 176, 0, 12, 12);

        if (getMenu().extraInfo.hasWarning()) {
            if (getMenu().extraInfo.breakInSucceeded) {
                if (minecraft.level.getGameTime() % 10 > 5) // Blinking fast
                    this.blit(poseStack, getGuiLeft() + 151, getGuiTop() + 38, 188, 0, 10, 10);
            }
            else if (getMenu().extraInfo.breakInAttempted) {
                if (minecraft.level.getGameTime() % 26 > 12) // Blinking slowly
                    this.blit(poseStack, getGuiLeft() + 151, getGuiTop() + 38, 188, 0, 10, 10);
            }
        }
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int x, int y) {
        if (this.menu.getCarried().isEmpty() && hoveredSlot != null &&
                hoveredSlot instanceof BigItemHandlerSlot bankSlot && bankSlot.hasItem()) {
            renderBankSlotTooltip(bankSlot.getItem(), poseStack, x, y);
        }
        else if (isHovering(161, 3, 12, 12, x, y)) { // Owner
            // TODO: render combination and info - TRANSLATION
            renderTooltip(poseStack, new TextComponent("You are the owner"), x, y);
        }
        else if (getMenu().extraInfo.hasWarning() && isHovering(151, 38, 10, 10, x, y)) { // Warning
            if (getMenu().extraInfo.breakInSucceeded)
                renderTooltip(poseStack, new TextComponent("Monobank was opened by someone"), x, y);
            else if (getMenu().extraInfo.breakInAttempted)
                renderTooltip(poseStack, new TextComponent("Someone has unsuccessfully attempted to open this Monobank"), x, y);
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
