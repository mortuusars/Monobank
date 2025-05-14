package io.github.mortuusars.monobank.content.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MonobankUnlockedTrigger extends SimpleCriterionTrigger<MonobankUnlockedTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MonobankBlockEntity blockEntity) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, blockEntity));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<LocationPredicate> location) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location))
                .apply(instance, TriggerInstance::new));

        public boolean matches(ServerPlayer player, MonobankBlockEntity blockEntity) {
            if (location.isEmpty()) return true;
            BlockPos pos = blockEntity.getBlockPos();
            return location.get().matches(player.serverLevel(), pos.getX(), pos.getY(), pos.getZ());
        }
    }
}