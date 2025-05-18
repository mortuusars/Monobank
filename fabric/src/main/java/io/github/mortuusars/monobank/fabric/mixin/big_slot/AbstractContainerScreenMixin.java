package io.github.mortuusars.monobank.fabric.mixin.big_slot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.monobank.client.gui.rendering.ExtendedSlotRenderer;
import io.github.mortuusars.monobank.world.inventory.BigSlot;
import io.github.mortuusars.monobank.world.inventory.ResizeableSlot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Shadow @Nullable protected Slot hoveredSlot;

    @ModifyArg(method = "renderSlot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"),
                    index = 4)
    private String modifyCountString(@Nullable String original, @Local(argsOnly = true) Slot slot) {
        if (slot instanceof BigSlot bigSlot) {
            return bigSlot.getCountString();
        }
        return original;
    }

    @WrapOperation(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;III)V"))
    private void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset, Operation<Void> original) {
        if (hoveredSlot instanceof ResizeableSlot resizeableSlot) {
            guiGraphics.fillGradient(RenderType.guiOverlay(), x, y, x + resizeableSlot.getWidth(),
                    y + resizeableSlot.getHeight(), 0x80FFFFFF, 0x80FFFFFF, blitOffset);
            return;
        }
        original.call(guiGraphics, x, y, blitOffset);
    }

    @WrapOperation(method = "renderSlot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"))
    private void renderSlot_renderItem(GuiGraphics instance, ItemStack stack, int x, int y, int seed, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        if (slot instanceof ResizeableSlot resizeableSlot) {
            ExtendedSlotRenderer.renderItem(instance, stack, x, y, seed, 0,
                    resizeableSlot.getWidth(), resizeableSlot.getHeight());
            return;
        }
        original.call(instance, stack, x, y, seed);
    }

    @WrapOperation(method = "renderSlot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderFakeItem(Lnet/minecraft/world/item/ItemStack;III)V"))
    private void renderSlot_renderFakeItem(GuiGraphics instance, ItemStack stack, int x, int y, int seed, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        if (slot instanceof ResizeableSlot resizeableSlot) {
            ExtendedSlotRenderer.renderFakeItem(instance, stack, x, y, seed, 0,
                    resizeableSlot.getWidth(), resizeableSlot.getHeight());
            return;
        }
        original.call(instance, stack, x, y, seed);
    }

    @WrapOperation(method = "renderSlot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void renderSlot_renderFakeItem(GuiGraphics instance, Font font, ItemStack stack, int x, int y, String count, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        if (slot instanceof ResizeableSlot resizeableSlot) {
            ExtendedSlotRenderer.renderItemDecorations(instance, font, stack, x, y, count,
                    resizeableSlot.getWidth(), resizeableSlot.getHeight());
            return;
        }
        original.call(instance, font, stack, x, y, count);
    }
}
