package io.github.mortuusars.monobank.fabric.mixin.hopper_insert_fix;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Shadow
    private static boolean canPlaceItemInContainer(Container container, ItemStack stack, int slot, Direction direction) {
        return false;
    }

    @ModifyReturnValue(method = "isFullContainer", at = @At("RETURN"))
    private static boolean isFull(boolean original, @Local(argsOnly = true) Container container) {
        return container instanceof MonobankBlockEntity monobankBlockEntity
                ? monobankBlockEntity.getItem().getCount() >= monobankBlockEntity.getCapacity()
                : original;
    }

    @Inject(method = "tryMoveInItem", at = @At(value = "HEAD"), cancellable = true)
    private static void tryMoveInItem(Container source, Container destination, ItemStack insertedStack, int slot, Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        if (!(destination instanceof MonobankBlockEntity monobankBlockEntity)) return;
        if (!canPlaceItemInContainer(destination, insertedStack, slot, direction)) return;

        ItemStack existingItem = destination.getItem(slot);
        boolean isInserted = false;

        if (existingItem.isEmpty()) {
            destination.setItem(slot, insertedStack);
            insertedStack = ItemStack.EMPTY;
            isInserted = true;
        } else if (insertedStack.getCount() <= monobankBlockEntity.getCapacity() && ItemStack.isSameItemSameComponents(existingItem, insertedStack)) {
            int amount = monobankBlockEntity.getCapacity() - existingItem.getCount();
            int insertedAmount = Math.min(insertedStack.getCount(), amount);
            insertedStack.shrink(insertedAmount);
            existingItem.grow(insertedAmount);
            isInserted = insertedAmount > 0;
        }

        if (isInserted) {
            destination.setChanged();
        }

        cir.setReturnValue(insertedStack);
    }
}
