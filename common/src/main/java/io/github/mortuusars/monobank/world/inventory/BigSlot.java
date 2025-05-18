package io.github.mortuusars.monobank.world.inventory;

import io.github.mortuusars.monobank.util.NumberFormatter;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BigSlot extends Slot implements ResizeableSlot {
    private final int width;
    private final int height;
    private final int capacity;

    public BigSlot(Container container, int index, int x, int y, int width, int height, int capacity) {
        super(container, index, x, y);
        this.width = width;
        this.height = height;
        this.capacity = capacity;
    }

    // Overriden to ignore ItemStack#getMaxCapacity.
    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return capacity;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public @Nullable String getCountString() {
        if (!hasItem() || getItem().getCount() <= 1) return null;
        return NumberFormatter.shortenWithSuffix(getItem().getCount());
    }
}
