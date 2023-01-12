package io.github.mortuusars.monobank.content.effect;

import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.core.stealth.Stealth;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;

public class Thief {

    public static void onEntityInteractEvent(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().level.isClientSide || !(event.getEntity() instanceof LivingEntity livingEntity) || !(event.getTarget() instanceof Villager villager))
            return;

        @Nullable MobEffectInstance thiefEffect = livingEntity.getEffect(Registry.Effects.THIEF.get());
        if (thiefEffect != null) {
            villager.setUnhappy();
            event.setCanceled(true);
        }
    }

    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().level.isClientSide)
            return;

        if (!(event.getEntity() instanceof LivingEntity livingEntity))
            return;

        BlockEntity blockEntityAtPos = livingEntity.level.getBlockEntity(event.getHitVec().getBlockPos());
        if (blockEntityAtPos instanceof MonobankBlockEntity)
            return;

        if (blockEntityAtPos instanceof RandomizableContainerBlockEntity container && container.lootTable != null
                && wasSeenCommittingCrime(event.getPlayer()))
            declareThief(livingEntity, Offence.MODERATE);
    }

    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer().level.isClientSide)
            return;

        BlockEntity blockEntityAtPos = event.getPlayer().level.getBlockEntity(event.getPos());
        if (blockEntityAtPos instanceof RandomizableContainerBlockEntity container && container.lootTable != null
                && wasSeenCommittingCrime(event.getPlayer()))
            declareThief(event.getPlayer(), Offence.HEAVY);
    }

    public static void declareThief(LivingEntity livingEntity, Offence offence) {
        livingEntity.removeEffect(MobEffects.HERO_OF_THE_VILLAGE); // You either die a hero...

        int durationSeconds = (int)(ThiefEffect.getBaseDurationSeconds() * offence.getModifier());
        int amplifier = offence.getAmplifier();

        MobEffectInstance existingThiefEffect = livingEntity.getEffect(Registry.Effects.THIEF.get());
        if (existingThiefEffect != null) {
            amplifier = Math.max(amplifier, existingThiefEffect.getAmplifier());
            int existingDuration = existingThiefEffect.getDuration();
            durationSeconds = Math.min(ThiefEffect.getBaseDurationSeconds() * 10,
                    durationSeconds + livingEntity.level.random.nextInt(existingDuration / 2, existingDuration));
        }

        livingEntity.removeEffect(Registry.Effects.THIEF.get());
        MobEffectInstance thiefEffectInstance = ThiefEffect.createInstance(Duration.ofSeconds(durationSeconds), amplifier);
        livingEntity.addEffect(thiefEffectInstance);

        if (livingEntity instanceof Player player) {
            player.displayClientMessage(TextUtil.translate("message.thief.you_were_seen"), true);
        }
    }

    public static boolean wasSeenCommittingCrime(LivingEntity thief) {
        Level level = thief.level;

        int radius = Math.max((int)(20 * Stealth.getValueOf(thief)), 3);
        AABB crimeScene = new AABB(thief.blockPosition()).inflate(radius, radius * 0.33f, radius);

        List<IronGolem> ironGolemsInArea = level.getEntitiesOfClass(IronGolem.class, crimeScene);

        for (IronGolem ironGolem : ironGolemsInArea) {
            if (ironGolem.distanceTo(thief) <= 3)
                return true;
            else if (ironGolem.hasLineOfSight(thief))
                return true;
        }

        List<Villager> villagersInArea = level.getEntitiesOfClass(Villager.class, crimeScene);

        for (Villager villager : villagersInArea) {
            boolean villagerSawTheft = false;
            if (villager.distanceTo(thief) <= 6)
                villagerSawTheft = villager.isSleeping() && villager.distanceTo(thief) > 3 ? false : true;
            else if (villager.hasLineOfSight(thief))
                villagerSawTheft = true;

            if (villagerSawTheft) {
                villager.setUnhappy();
                if (villager.isSleeping())
                    villager.stopSleeping();
                return true;
            }
        }

        return false;
    }

    public enum Offence {
        LIGHT(0, 0.5f),
        MODERATE(1, 1F),
        HEAVY(2, 2F);

        private int amplifier;
        private float modifier;

        Offence(int amplifier, float modifier) {
            this.amplifier = amplifier;
            this.modifier = modifier;
        }

        public int getAmplifier() {
            return amplifier;
        }

        public float getModifier() {
            return modifier;
        }
    }
}
