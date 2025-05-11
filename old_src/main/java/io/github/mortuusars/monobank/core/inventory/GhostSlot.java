package io.github.mortuusars.monobank.core.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class GhostSlot extends Slot {
    public GhostSlot(Container combinationContainer, int index, int x, int y) {
        super(combinationContainer, index, x, y);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
