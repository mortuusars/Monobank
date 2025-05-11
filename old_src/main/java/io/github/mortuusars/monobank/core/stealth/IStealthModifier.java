package io.github.mortuusars.monobank.core.stealth;

import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;

public interface IStealthModifier {
    Tuple<Float, Boolean> modify(LivingEntity entity, float previousValue);
}
