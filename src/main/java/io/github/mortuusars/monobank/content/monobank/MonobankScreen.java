package io.github.mortuusars.monobank.content.monobank;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class MonobankScreen extends AbstractContainerScreen<MonobankMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = Monobank.resource("textures/gui/monobank.png");

    private @Nullable ItemStack storedItemStack;
    private int count = 0;

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

//        title = getMenu().blockEntity.getCustomName();

        containerTick();

        addRenderableWidget(new ImageButton(getGuiLeft() + 80, getGuiTop() + 40, 16, 16, 176, 0, BACKGROUND_TEXTURE, pressedButton -> {
            this.onWithdrawButtonPressed();
        }));
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
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);

        // TODO: Shortened count and full count in tooltip
        if (count > 1 && storedItemStack != null) {
            String countString = Integer.toString(count);
            pPoseStack.translate(0.0D, 0.0D, (double)(getMinecraft().getItemRenderer().blitOffset + 200.0F));
            font.drawShadow(pPoseStack, countString, imageWidth / 2 - 8 + 19 - 2 - font.width(countString), 24 + 6 + 3, 0xffffff);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);



//        if (storedItemStack != null) {
//            this.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(storedItemStack, this.getGuiLeft() + imageWidth / 2 - 8, this.getGuiTop() + 24);
//            this.getMinecraft().getItemRenderer().renderGuiItemDecorations(font, storedItemStack, this.getGuiLeft() + imageWidth / 2 - 8, this.getGuiTop() + 24);
//        }
    }
}
