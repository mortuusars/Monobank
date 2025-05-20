package io.github.mortuusars.monobank.neoforge.mixin;

import io.github.mortuusars.monobank.neoforge.ItemHandlerCapabilityBlockEntity;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.world.block.monobank.component.Lock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = MonobankBlockEntity.class, remap = false)
public abstract class MonobankBlockEntityMixin extends BlockEntity implements ItemHandlerCapabilityBlockEntity, RandomizableContainer {
    @Unique
    private IItemHandler monobank$itemHandler = new InvWrapper(this) {
        @Override
        public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;

            ItemStack stackInSlot = getInv().getItem(slot);

            int remainingSpace;
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getCount() >= getSlotLimit(slot)) return stack;
                if (!ItemStack.isSameItemSameComponents(stack, stackInSlot)) return stack;
                if (!getInv().canPlaceItem(slot, stack)) return stack;

                remainingSpace = getSlotLimit(slot) - stackInSlot.getCount();

                if (stack.getCount() <= remainingSpace) {
                    if (!simulate) {
                        ItemStack copy = stack.copy();
                        copy.grow(stackInSlot.getCount());
                        getInv().setItem(slot, copy);
                        getInv().setChanged();
                    }

                    return ItemStack.EMPTY;
                } else {
                    // copy the stack to not modify the original one
                    stack = stack.copy();
                    if (!simulate) {
                        ItemStack copy = stack.split(remainingSpace);
                        copy.grow(stackInSlot.getCount());
                        getInv().setItem(slot, copy);
                        getInv().setChanged();
                        return stack;
                    } else {
                        stack.shrink(remainingSpace);
                        return stack;
                    }
                }
            } else {
                if (!getInv().canPlaceItem(slot, stack)) return stack;

                remainingSpace = getSlotLimit(slot);
                if (remainingSpace < stack.getCount()) {
                    // copy the stack to not modify the original one
                    stack = stack.copy();
                    if (!simulate) {
                        getInv().setItem(slot, stack.split(remainingSpace));
                        getInv().setChanged();
                        return stack;
                    } else {
                        stack.shrink(remainingSpace);
                        return stack;
                    }
                } else {
                    if (!simulate) {
                        getInv().setItem(slot, stack);
                        getInv().setChanged();
                    }
                    return ItemStack.EMPTY;
                }
            }
        }
    };

    public MonobankBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Shadow
    public abstract Lock getLock();

    @Override
    public IItemHandler monobank$getItemHandlerCapability() {
        if (getLock().isLocked()) return null;
        return monobank$itemHandler;
    }
}
