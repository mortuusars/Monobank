package io.github.mortuusars.monobank.core.inventory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Random;

public class UnlockingSlot extends SlotItemHandler {
    private ItemStack keyway;
    private Component keywayTooltip;

    public UnlockingSlot(IItemHandler itemHandler, int index, int x, int y, ItemStack keyway) {
        super(itemHandler, index, x, y);
        this.keyway = keyway;
        keywayTooltip = createKeywayTooltip(keyway);
    }

    private Component createKeywayTooltip(ItemStack keyway) {
        String title = keyway.getHoverName().getString();
        if (title.length() == 0)
            return Component.literal("");

        String[] split = title.split("\s");
        Random random;

        MutableComponent newTitle = Component.literal("");

        for (int wordIndex = 0; wordIndex < split.length; wordIndex++) {
            String word = split[wordIndex];

            random = new Random(word.hashCode());

            if (word.length() == 0)
                continue;
            for (int charIndex = 0; charIndex < word.length(); charIndex++) {
                MutableComponent character = Component.literal(word.charAt(charIndex) + "");
                if (random.nextDouble() > 0.5f)
                    character = character.withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.GRAY);
                newTitle.append(character);
            }

            if (wordIndex != split.length - 1)
                newTitle.append(" ");
        }

        return newTitle;
    }

    public ItemStack getKeyway () {
        return keyway;
    }

    public Component getKeywayTooltip() {
        return keywayTooltip;
    }
}
