package io.github.mortuusars.monobank.core.stealth.modifier;

import io.github.mortuusars.monobank.core.stealth.IStealthModifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class InvisibilityModifier implements IStealthModifier {
    @Override
    public Tuple<Float, Boolean> modify(LivingEntity entity, float previousValue) {
        return new Tuple<>(entity.getEffect(MobEffects.INVISIBILITY) != null ?
                previousValue * 0.2f : previousValue, true);
    }
}
