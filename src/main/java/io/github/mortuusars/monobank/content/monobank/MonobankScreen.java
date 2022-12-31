package io.github.mortuusars.monobank.content.monobank;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.content.monobank.component.ScreenKeyModifier;
import io.github.mortuusars.monobank.content.monobank.inventory.BigItemHandlerSlot;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class MonobankScreen extends AbstractContainerScreen<MonobankMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = Monobank.resource("textures/gui/monobank.png");

    public MonobankScreen(MonobankMenu containerMenu, Inventory playerinventory, Component title) {
        super(containerMenu, playerinventory, title);
    }

    @Override
    protected void containerTick() {
//        Item storedItem = this.getMenu().blockEntity.;
//        CompoundTag storedItemTag = this.getMenu().blockEntity.getStoredItemTag();
//
//        if (getMenu().blockEntity.isEmpty()) {
//            storedItemStack = null;
//            count = 0;
//        }
//        else {
//            storedItemStack = new ItemStack(storedItem, 1);
//            storedItemStack.setTag(storedItemTag);
//            count = this.getMenu().blockEntity.getStoredItemCount();
//        }
    }

    @Override
    protected void init() {
        super.init();

//        addRenderableWidget(new ImageButton(getGuiLeft() + 80, getGuiTop() + 40, 16, 16, 176, 0, BACKGROUND_TEXTURE, pressedButton -> {
//            this.onWithdrawButtonPressed();
//        }));
    }

    private void onWithdrawButtonPressed() {
        int buttonActionID;
        if (Screen.hasShiftDown() && Screen.hasControlDown())
            buttonActionID = ScreenKeyModifier.SHIFT_AND_CTRL.getID();
        else if (Screen.hasShiftDown())
            buttonActionID = ScreenKeyModifier.SHIFT.getID();
        else
            buttonActionID = 0;

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonActionID);
        this.getMenu().clickMenuButton(minecraft.player, buttonActionID);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderSlot(PoseStack pPoseStack, Slot pSlot) {
        if (pSlot instanceof BigItemHandlerSlot bigSlot) {
            RenderSystem.enableDepthTest();
            ItemStack itemStack = bigSlot.getItem();
            int count = itemStack.getCount();

            String countString = TextUtil.formatNumber(count);

            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemStack, pSlot.x, pSlot.y, pSlot.x + pSlot.y * this.imageWidth);
            this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, pSlot.x, pSlot.y, countString);
        }
        else
            super.renderSlot(pPoseStack, pSlot);
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
    }

    @Override
    public List<Component> getTooltipFromItem(ItemStack itemStack) {
        List<Component> tooltipFromItem = super.getTooltipFromItem(itemStack);
        int stackCount = itemStack.getCount();
        if (stackCount > 1000) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            DecimalFormat numberFormatter = new DecimalFormat("###,###,###", symbols);
            String formattedCount = numberFormatter.format(stackCount);

            tooltipFromItem.add(new TextComponent("")); // Separator
            tooltipFromItem.add(TextUtil.translate("gui.count", formattedCount).withStyle(ChatFormatting.UNDERLINE));
        }
        return tooltipFromItem;
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
