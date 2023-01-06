package io.github.mortuusars.monobank.core.inventory;

import io.github.mortuusars.monobank.content.monobank.unlocking.Combination;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CombinationContainer implements Container {

    private List<ItemStack> items;

    public CombinationContainer() {
        this.items = new ArrayList<>(Combination.SIZE);
        for (int i = 0; i < Combination.SIZE; i++) {
            this.items.add(ItemStack.EMPTY);
        }
    }

    @Override
    public int getContainerSize() {
        return Combination.SIZE;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        items.set(slot, ItemStack.EMPTY);
        return getItem(slot);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, 1);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < Combination.SIZE; i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }
}
