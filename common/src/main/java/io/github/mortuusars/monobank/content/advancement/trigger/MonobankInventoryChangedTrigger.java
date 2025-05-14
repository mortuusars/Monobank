package io.github.mortuusars.monobank.content.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MonobankInventoryChangedTrigger extends SimpleCriterionTrigger<MonobankInventoryChangedTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MonobankBlockEntity blockEntity, ItemStack storedStack) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, blockEntity, storedStack));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<ItemPredicate> item,
                                  Optional<LocationPredicate> location) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item),
                        LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location))
                .apply(instance, TriggerInstance::new));

        public boolean matches(ServerPlayer player, MonobankBlockEntity blockEntity, ItemStack storedStack) {
            return itemMatches(blockEntity, storedStack) && locationMatches(player, blockEntity);
        }

        private boolean locationMatches(ServerPlayer player, MonobankBlockEntity blockEntity) {
            if (location.isEmpty()) return true;
            BlockPos pos = blockEntity.getBlockPos();
            return location.get().matches(player.serverLevel(), pos.getX(), pos.getY(), pos.getZ());
        }

        private boolean itemMatches(MonobankBlockEntity blockEntity, ItemStack storedStack) {
            if (item.isEmpty()) return true;
            ItemPredicate predicate = item.get();

            // Handles Monobank Full advancement.
            if (predicate.count().min().isPresent()
                    && predicate.count().min().get() == Integer.MAX_VALUE
                    && storedStack.getCount() >= blockEntity.getCapacity())
                return true;

            return predicate.test(storedStack);
        }
    }
}