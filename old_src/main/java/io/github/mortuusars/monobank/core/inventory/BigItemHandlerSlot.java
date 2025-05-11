package io.github.mortuusars.monobank.core.inventory;

import io.github.mortuusars.monobank.Monobank;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BigItemHandlerSlot extends SlotItemHandler implements IResizeableSlot {
    private MonobankItemStackHandler itemHandler;
//    private int index;

    private int width;
    private int height;

    public BigItemHandlerSlot(MonobankItemStackHandler itemHandler, int index, int x, int y) {
        this(itemHandler, index, x, y, 16, 16);
    }

    public BigItemHandlerSlot(MonobankItemStackHandler itemHandler, int index, int x, int y, int width, int height) {
        super(itemHandler, index, x, y);
        this.itemHandler = itemHandler;
//        this.index = index;
        this.width = width;
        this.height = height;
    }

//    @NotNull
//    @Override
//    public ItemStack getItem() {
//        ItemStack stackInSlot = itemHandler.getStackInSlot(index);
//        return stackInSlot;
//    }

    // Overriden to ignore ItemStack#getMaxCapacity.
    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return Monobank.getSlotCapacity();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
