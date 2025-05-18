package io.github.mortuusars.monobank.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class GhostSlot extends Slot {
    protected final Predicate<ItemStack> mayPlace;

    public GhostSlot(Container container, int index, int x, int y, Predicate<ItemStack> mayPlace) {
        super(container, index, x, y);
        this.mayPlace = mayPlace;
    }

    public GhostSlot(Container container, int index, int x, int y) {
        this(container, index, x, y, stack -> true);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return mayPlace.test(stack);
    }
}
