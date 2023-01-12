package io.github.mortuusars.monobank.core.stealth;

import io.github.mortuusars.monobank.core.stealth.modifier.DarknessModifier;
import io.github.mortuusars.monobank.core.stealth.modifier.InvisibilityModifier;
import io.github.mortuusars.monobank.core.stealth.modifier.SneakingModifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class Stealth {

    private static List<IStealthModifier> modifiers;

    static {
        modifiers = new ArrayList<>();
        modifiers.add(new InvisibilityModifier());
        modifiers.add(new SneakingModifier());
        modifiers.add(new DarknessModifier());
    }

    public static List<IStealthModifier> getModifiers() {
        return modifiers;
    }

    public static void addModifier(IStealthModifier modifier) {
        modifiers.add(modifier);
    }

    public static void addModifier(int index, IStealthModifier modifier) {
        modifiers.add(index, modifier);
    }

    public static float getValueOf(LivingEntity entity) {
        float value = 1f;
        for (IStealthModifier modifier : modifiers) {
            Tuple<Float, Boolean> result = modifier.modify(entity, value);
            value = result.getA();
            if (!result.getB())
                break;
        }
        return value;
    }
}
