package io.github.mortuusars.monobank.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.monobank.client.gui.rendering.ResizeableItemRenderer;
import io.github.mortuusars.monobank.core.inventory.IResizeableSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ContainerScreenEvent;

import javax.annotation.Nullable;
import java.util.Objects;


@SuppressWarnings({"NullableProblems", "unused"})
public abstract class PatchedAbstractContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public PatchedAbstractContainerScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    /**
     * Updated method to account for IResizeableSlot's.
     */
    @Override
    protected boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return slot instanceof IResizeableSlot resizeableSlot ?
                isHovering(slot.x, slot.y, resizeableSlot.getWidth(), resizeableSlot.getHeight(), mouseX, mouseY) :
                super.isHovering(slot, mouseX, mouseY);
    }

    /**
     * This method is a copy of super-class method, with some minor changes. <br>
     * It is done to allow for rendering overlays for bigger slot sizes than 16.
     * Why it is so hard to allow for custom-sized slots Mojang? Why?
     */
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        this.renderBg(graphics, partialTick, mouseX, mouseY);

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, graphics, mouseX, mouseY));
        RenderSystem.disableDepthTest();

        // Replaced call to super (Screen) with its contents:
        for(Renderable widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(x, y, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.hoveredSlot = null;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for(int k = 0; k < this.menu.slots.size(); ++k) {
            Slot slot = this.menu.slots.get(k);
            if (slot.isActive()) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                this.renderSlot(graphics, slot);
            }

            if (this.isHovering(slot, mouseX, mouseY) && slot.isActive()) {
                this.hoveredSlot = slot;
                int slotX = slot.x;
                int slotY = slot.y;
                // Call to updated method which can render custom-sized slots:
                this.renderSlotHighlight(slot, graphics, slotX, slotY, 100, this.getSlotColor(k));
            }
        }

        this.renderLabels(graphics, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, graphics, mouseX, mouseY));
        ItemStack itemstack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if (!itemstack.isEmpty()) {
            int l1 = 8;
            int i2 = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                itemstack = itemstack.copy();
                itemstack.setCount(Mth.ceil((float)itemstack.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(this.quickCraftingRemainder);
                if (itemstack.isEmpty()) {
                    s = ChatFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(graphics, itemstack, mouseX - x - 8, mouseY - y - i2, s);
        }

        if (!this.snapbackItem.isEmpty()) {
            float f = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int j2 = this.snapbackEnd.x - this.snapbackStartX;
            int k2 = this.snapbackEnd.y - this.snapbackStartY;
            int j1 = this.snapbackStartX + (int)((float)j2 * f);
            int k1 = this.snapbackStartY + (int)((float)k2 * f);
            this.renderFloatingItem(graphics, this.snapbackItem, j1, k1, null);
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    /**
     * This method is a copy of super-class method, with some minor changes. <br>
     * It is done to allow for rendering slot sizes bigger than 16.
     */
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        int x = slot.x;
        int y = slot.y;

        int slotWidth, slotHeight;

        if (slot instanceof IResizeableSlot resizeableSlot) {
            slotWidth = resizeableSlot.getWidth();
            slotHeight = resizeableSlot.getHeight();
        }
        else {
            slotWidth = 16;
            slotHeight = 16;
        }

        ItemStack itemstack = slot.getItem();
        boolean canQuickReplace = false;
        boolean flag1 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack carriedStack = this.menu.getCarried();
        String countString = null;
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
            itemstack = itemstack.copy();
            itemstack.setCount(itemstack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carriedStack.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(slot, carriedStack, true) && this.menu.canDragTo(slot)) {
                itemstack = carriedStack.copy();
                canQuickReplace = true;
                AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemstack);
                int k = Math.min(itemstack.getMaxStackSize(), slot.getMaxStackSize(itemstack));
                if (itemstack.getCount() > k) {
                    countString = ChatFormatting.YELLOW.toString() + k;
                    itemstack.setCount(k);
                }
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }

        if (itemstack.isEmpty() && slot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                graphics.blit(x, y, 0, slotWidth, slotHeight, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (canQuickReplace)
                graphics.fill(x, y, x + slotWidth, y + slotHeight, -2130706433);

            RenderSystem.enableDepthTest();

            assert this.minecraft != null;
            renderSlotItem(graphics, slot, itemstack, this.minecraft.player, x, y);
            countString = getCountStringForSlot(slot, itemstack, countString);
            renderSlotDecorations(graphics, slot, itemstack, this.font, x, y, countString);
        }
    }

    /**
     * Can be used to modify string displaying the count of the stack in slot.
     * @param countString Most of the time is 'null', but will
     *                    have 'yellow [count]' if the stack's count is larger than slot capacity.
     */
    protected String getCountStringForSlot(Slot slot, ItemStack itemStack, @Nullable String countString) {
        return countString;
    }

    /**
     * Expanded method to allow rendering custom-sized slots.
     */
    protected void renderSlotHighlight(Slot slot, GuiGraphics graphics, int x, int y, int blitOffset, int slotColor) {
        if (slot instanceof IResizeableSlot sizeableSlot)
            renderHighlightRectangle(graphics, x, y, sizeableSlot.getWidth(), sizeableSlot.getHeight(), blitOffset, slotColor);
        else
            renderHighlightRectangle(graphics, x, y, 16, 16, blitOffset, slotColor);
    }

    /**
     * Expanded method to allow rendering custom-sized slots.
     */
    protected void renderSlotItem(GuiGraphics graphics, Slot slot, ItemStack slotStack, LivingEntity entity, int x, int y) {
        if (slot instanceof IResizeableSlot resizeableSlot)
            ResizeableItemRenderer.renderGuiItem(slotStack, x, y, resizeableSlot.getWidth(), resizeableSlot.getHeight());
        else {
            assert this.minecraft.player != null;
            graphics.renderItem(this.minecraft.player, slotStack, x, y, slot.x + slot.y * this.imageWidth);
        }
    }

    /**
     * Expanded method to allow rendering custom-sized slots.
     */
    protected void renderSlotDecorations(GuiGraphics graphics, Slot slot, ItemStack slotStack, Font font, int x, int y, String countString) {
        if (slot instanceof IResizeableSlot resizeableSlot)
            ResizeableItemRenderer.renderGuiItemDecorations(graphics, this.font, slotStack,
                    x, y, resizeableSlot.getWidth(), resizeableSlot.getHeight(), countString, slotStack.getCount() > 0);
        else {
            assert Objects.requireNonNull(this.minecraft).player != null;
            graphics.renderItemDecorations(this.font, slotStack, x, y, countString);
        }
    }

    /**
     * Renders overlay rectangle of specified size.
     */
    public static void renderHighlightRectangle(GuiGraphics graphics, int x, int y, int width, int height, int blitOffset, int slotColor) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        graphics.fillGradient(x, y, x + width, y + height, blitOffset + 200, slotColor, slotColor);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }
}
