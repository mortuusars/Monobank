package io.github.mortuusars.monobank.content.monobank.inventory;

import io.github.mortuusars.monobank.Monobank;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BigItemHandlerSlot extends SlotItemHandler {
    private MonobankItemStackHandler itemHandler;
    private int index;

    public BigItemHandlerSlot(MonobankItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        ItemStack stackInSlot = itemHandler.getStackInSlot(index);
        return stackInSlot;
    }

    // Overriden to ignore ItemStack#getMaxCapacity.
    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return Monobank.getSlotCapacity();
    }
}
