package io.github.mortuusars.monobank.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.Random;

public class TextObfuscator {
    public static MutableComponent obfuscate(String text, double obfuscationChance) {
        if (text.isEmpty()) {
            return Component.literal("");
        }

        obfuscationChance = Mth.clamp(obfuscationChance, 0.0, 1.0);

        String[] split = text.split("\\s+"); // Split by white space
        Random random;

        MutableComponent result = Component.empty();

        for (int wordIndex = 0; wordIndex < split.length; wordIndex++) {
            String word = split[wordIndex];

            if (word.isEmpty()) {
                continue;
            }

            random = new Random(word.hashCode());

            for (int charIndex = 0; charIndex < word.length(); charIndex++) {
                MutableComponent character = Component.literal(word.charAt(charIndex) + "");

                if (random.nextDouble() <= obfuscationChance) {
                    character = character.withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.GRAY);
                }

                result.append(character);
            }

            if (wordIndex != split.length - 1) {
                result.append(" ");
            }
        }

        return result;
    }
}
