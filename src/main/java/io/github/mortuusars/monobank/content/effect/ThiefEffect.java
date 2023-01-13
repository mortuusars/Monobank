package io.github.mortuusars.monobank.content.effect;

import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.core.stealth.Stealth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

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
        //TODO: config duration
        return 360;
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
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        Level level = livingEntity.level;
        if (level.isClientSide || level.getGameTime() % 10 != 0 ||
                (livingEntity instanceof Player player && (player.isCreative() || player.isSpectator())))
            return;

        if (level.getDifficulty() == Difficulty.PEACEFUL)
            return;

        int baseRadius = 16;
        int searchRadius = Math.min(MAX_AREA_SIZE, baseRadius + baseRadius / 2 * amplifier);
        int aggroRadius = Math.max((int)(searchRadius * Stealth.getValueOf(livingEntity)), 2);

        AABB maxArea = new AABB(livingEntity.blockPosition()).inflate(MAX_AREA_SIZE, MAX_AREA_SIZE / 2, MAX_AREA_SIZE);
        List<IronGolem> golemsInArea = level.getEntitiesOfClass(IronGolem.class, maxArea);

        for (IronGolem golem : golemsInArea) {
            float distanceToThief = golem.distanceTo(livingEntity);

            if (distanceToThief <= aggroRadius && golem.hasLineOfSight(livingEntity))
                golem.setTarget(livingEntity); // Attack thief
            else if (distanceToThief >= 26f && golem.getTarget() == livingEntity && golem.getLastHurtByMob() != livingEntity)
                golem.stopBeingAngry();
        }



        // No spawning for now:


        // Spawn additional golems:

//        if (golemsInArea.size() >= 2 || level.getRandom().nextInt(50) != 0)
//            return;
//
//        List<Villager> villagersInArea = level.getEntitiesOfClass(Villager.class, area.inflate( -15));
//        BlockPos playerPos = livingEntity.blockPosition();
//
//        Collections.sort(villagersInArea, (villager1, villager2) -> playerPos.distToCenterSqr(
//                villager1.position()) >= playerPos.distToCenterSqr(villager2.position()) ? 1 : -1);
//
//        if (villagersInArea.size() < 3)
//            return;
//
//        Villager furthestVillager = villagersInArea.get(villagersInArea.size() - 1);
//        Villager closestVillager = villagersInArea.get(villagersInArea.size() - 1);
//        IronGolem spawnedGolem = furthestVillager.trySpawnGolem(((ServerLevel) level));
//        if (spawnedGolem != null) {
//            closestVillager.setUnhappy();
//            furthestVillager.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100));
//            if (Monobank.IN_DEBUG)
//                LogUtils.getLogger().debug(furthestVillager + " has spawned a golem '" + spawnedGolem + "' because '" + livingEntity + "' is thief.");
//        }
    }
}
