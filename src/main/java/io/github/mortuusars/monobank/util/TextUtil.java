package io.github.mortuusars.monobank.util;

import io.github.mortuusars.monobank.Monobank;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TextUtil {
    /**
     * Creates TranslatableComponent from a given key prefixed with the MOD ID.
     */
    public static MutableComponent translate(String key, Object... args) {
        return new TranslatableComponent(Monobank.ID + "." + key, args);
    }
}
