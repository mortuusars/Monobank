package io.github.mortuusars.monobank.content.effect;

import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.config.Configuration;
import io.github.mortuusars.monobank.core.stealth.Stealth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class ThiefEffect extends MobEffect {

    public static final int MAX_AREA_SIZE = 48;

    public ThiefEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static MobEffectInstance createInstance(Duration duration, int amplifier) {
        return new MobEffectInstance(Registry.Effects.THIEF.get(), (int)(duration.getSeconds() * 20), amplifier, false, false);
    }

    public static MobEffectInstance createInstance(Duration duration) {
        return createInstance(duration, 0);
    }

    public static int getBaseDurationSeconds() {
        return Configuration.THIEF_EFFECT_BASE_DURATION.get();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.EMPTY_LIST; // Milk is not strong enough to heal the reputation damage.
    }

    @Override
    public void applyEffectTick(LivingEntity thiefEntity, int amplifier) {
        Level level = thiefEntity.level;
        if (level.isClientSide || level.getGameTime() % 10 != 0 || level.getDifficulty() == Difficulty.PEACEFUL)
            return;

        int baseRadius = 16;
        int searchRadius = Math.min(MAX_AREA_SIZE, baseRadius + baseRadius / 2 * amplifier);
        int aggroRadius = Math.max((int)(searchRadius * Stealth.getValueOf(thiefEntity)), 2);

        AABB maxArea = new AABB(thiefEntity.blockPosition()).inflate(MAX_AREA_SIZE, MAX_AREA_SIZE / 2, MAX_AREA_SIZE);
        List<Entity> entities = level.getEntities(null, maxArea);

        for (Entity entity : entities) {
            if (entity.getType().is(Registry.EntityTags.THIEF_ATTACKERS)
                    && entity instanceof LivingEntity living
                    && entity instanceof NeutralMob neutralMob) {

                float distanceToThief = living.distanceTo(thiefEntity);

                if (neutralMob.canAttack(thiefEntity) && distanceToThief <= aggroRadius && living.hasLineOfSight(thiefEntity))
                    neutralMob.setTarget(thiefEntity); // Attack thief
                else if (neutralMob.getTarget() == thiefEntity) {
                    if ((!neutralMob.canAttack(thiefEntity)) || distanceToThief >= 26f && neutralMob.getLastHurtByMob() != thiefEntity)
                        neutralMob.stopBeingAngry();
                }
            }
        }
    }
}
