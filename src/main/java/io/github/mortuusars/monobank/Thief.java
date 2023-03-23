package io.github.mortuusars.monobank;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.config.Configuration;
import io.github.mortuusars.monobank.content.effect.ThiefEffect;
import io.github.mortuusars.monobank.core.stealth.Stealth;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class Thief {
    public static boolean isInProtectedStructureRange(ServerLevel level, BlockPos thiefPos) {
        if (!Configuration.THIEF_ONLY_NEAR_PROTECTED_STRUCTURES.get())
            return true; // Anywhere is protected.

        BlockPos nearestProtectedStructure = level.findNearestMapStructure(Registry.StructureTags.THEFT_PROTECTED, thiefPos, 1, false);
        if (nearestProtectedStructure != null) {
            BlockPos heightmapPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, nearestProtectedStructure);
            double distance = Math.sqrt(thiefPos.distSqr(heightmapPos));
            return distance <= Configuration.THIEF_PROTECTED_STRUCTURE_RANGE.get();
        }

        return false;
    }

    public static boolean shouldDeclareThiefForOpeningLootr(RandomizableContainerBlockEntity containerBE, LivingEntity opener) {
        CompoundTag tag = containerBE.getUpdateTag();
        if (tag.contains("LootrOpeners", Tag.TAG_LIST)) {
            try {
                ListTag lootrOpeners = tag.getList("LootrOpeners", Tag.TAG_INT_ARRAY);

                UUID livingEntityUUID = opener.getUUID();
                for (Tag openerUUID : lootrOpeners) {
                    UUID uuid = NbtUtils.loadUUID(openerUUID);
                    if (uuid.equals(livingEntityUUID))
                        return false;
                }
            }
            catch (Exception e) {
                LogUtils.getLogger().error("Monobank has failed to handle Thief interaction for Lootr container: " + e);
                return false;
            }
        }

        return true;
    }

    public static List<LivingEntity> getWitnesses(LivingEntity thief) {
        if (!Configuration.THIEF_ENABLED.get() || (thief instanceof Player player && (player.isCreative() || player.isSpectator())))
            return Collections.emptyList();

        Level level = thief.level;

        int radius = Math.max((int)(32 * Stealth.getValueOf(thief)), 3);
        AABB crimeScene = new AABB(thief.blockPosition()).inflate(radius, radius * 0.33f, radius);

//        Monobank.LOGGER.debug(radius + " - thief detection radius.");

        Predicate<LivingEntity> isWitness = witness -> {
            float distance = witness.distanceTo(thief);
            return witness.getType().is(Registry.EntityTags.THEFT_HATERS) && (
                    (witness.isSleeping() && distance <= 3) ||
                            (distance <= 7) ||
                            (distance <= 48 && witness.hasLineOfSight(thief)));
        };

        List<LivingEntity> witnesses = level.getEntitiesOfClass(LivingEntity.class, crimeScene)
                .stream()
                .filter(isWitness)
                .toList();

//        Monobank.LOGGER.debug(witnesses.size() + " witnesses of a theft.");

        return witnesses;
    }

    public static void declareThief(LivingEntity offender, List<LivingEntity> witnesses, Offence offence) {
        if (!Configuration.THIEF_ENABLED.get())
            return;

        offender.removeEffect(MobEffects.HERO_OF_THE_VILLAGE); // You either die a hero...

        int durationSeconds = (int)(ThiefEffect.getBaseDurationSeconds() * offence.getModifier());
        int amplifier = offence.getAmplifier();

        MobEffectInstance existingThiefEffect = offender.getEffect(Registry.Effects.THIEF.get());
        if (existingThiefEffect != null) {
            amplifier = Math.max(amplifier, existingThiefEffect.getAmplifier());
            int existingDuration = existingThiefEffect.getDuration() / 20;
            durationSeconds = Math.min(ThiefEffect.getBaseDurationSeconds() * 4,
                    durationSeconds + existingDuration);
        }

        offender.removeEffect(Registry.Effects.THIEF.get());
        MobEffectInstance thiefEffectInstance = ThiefEffect.createInstance(Duration.ofSeconds(durationSeconds), amplifier);
        offender.addEffect(thiefEffectInstance);

        if (offender instanceof Player player) {
            player.displayClientMessage(TextUtil.translate("message.thief.you_were_seen"), true);
        }

        for (LivingEntity witness : witnesses) {
            if (witness instanceof Villager villager && villager.distanceTo(offender) <= 16) {
                if (villager.isSleeping())
                    villager.stopSleeping();

                villager.setUnhappy();
            }
        }
    }

    public enum Offence {
        LIGHT(0, 0.5f),
        MODERATE(1, 1F),
        HEAVY(2, 2F);

        private final int amplifier;
        private final float modifier;

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
