package io.github.mortuusars.monobank.core.stealth.modifier;

import io.github.mortuusars.monobank.core.stealth.IStealthModifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SneakingModifier implements IStealthModifier {
    @Override
    public Tuple<Float, Boolean> modify(LivingEntity entity, float previousValue) {
        return entity instanceof Player player && player.isSecondaryUseActive() ?
                new Tuple<>(previousValue * 0.6f, true) : new Tuple<>(previousValue, true);
    }
}
