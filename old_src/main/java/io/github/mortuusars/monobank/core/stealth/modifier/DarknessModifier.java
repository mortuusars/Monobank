package io.github.mortuusars.monobank.core.stealth.modifier;

import io.github.mortuusars.monobank.core.stealth.IStealthModifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class DarknessModifier implements IStealthModifier {
    @Override
    public Tuple<Float, Boolean> modify(LivingEntity entity, float previousValue) {
        Level level = entity.level();

        int skyBrightness = level.getBrightness(LightLayer.SKY, entity.blockPosition());
        int blockBrightness = level.getBrightness(LightLayer.BLOCK, entity.blockPosition());

        int lightLevel = skyBrightness < 15 ?
                Math.max(blockBrightness, (int) (skyBrightness * ((15 - level.getSkyDarken()) / 15f))) :
                Math.max(blockBrightness, 15 - level.getSkyDarken());

        // 0.25 - 1.0
        float darknessModifier = Mth.map(lightLevel, 0, 15, 0.25f, 1f);

        return new Tuple<>(previousValue * darknessModifier, true);
    }
}
