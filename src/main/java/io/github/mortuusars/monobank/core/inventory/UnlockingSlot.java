package io.github.mortuusars.monobank.core.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class UnlockingSlot extends SlotItemHandler {
    private ItemStack keyway;

    public UnlockingSlot(IItemHandler itemHandler, int index, int x, int y, ItemStack keyway) {
        super(itemHandler, index, x, y);
        this.keyway = keyway;
    }

    public ItemStack getKeyway () {
        return keyway;
    }

    public Component getTooltip() {
//        if (this.hasItem())
            return null;
//        else
//            return keyway.getTooltipLines(null, TooltipFlag.Default.NORMAL);
    }
}
