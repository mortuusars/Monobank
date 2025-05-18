package io.github.mortuusars.monobank.world.inventory;

import io.github.mortuusars.monobank.util.NumberFormatter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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

    @Override
    public @NotNull Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
        int amountInSlot = getItem().getCount();
        int maxStackSize = getItem().getMaxStackSize();
        // Somewhat stupid way to detect what button was pressed (left or right), it would be hard to do in properly,
        // as this method is called deep in Menu#doClick method.
        // Without this, when monobank slot is clicked, whole stack will be picked up (more than maxStackSize),
        // and it's annoying because it cannot be placed in inventory.
        // This makes picking up items from slot work like in regular inventory, up to 64 max.
        if (maxStackSize < amountInSlot) {
            count = count < amountInSlot
                    ? (maxStackSize + 1) / 2 // Half stack (right click)
                    : maxStackSize; // Full (left click)
        }
        return super.tryRemove(count, decrement, player);
    }
}
