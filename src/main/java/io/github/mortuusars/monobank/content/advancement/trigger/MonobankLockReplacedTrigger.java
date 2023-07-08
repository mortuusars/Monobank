package io.github.mortuusars.monobank.content.advancement.trigger;

import com.google.gson.JsonObject;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class MonobankLockReplacedTrigger extends SimpleCriterionTrigger<MonobankLockReplacedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Monobank.resource("monobank_lock_replaced");

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected MonobankLockReplacedTrigger.@NotNull TriggerInstance createInstance(@NotNull JsonObject json, @NotNull ContextAwarePredicate player, @NotNull DeserializationContext conditionsParser) {
        return new MonobankLockReplacedTrigger.TriggerInstance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(ContextAwarePredicate player) {
            super(MonobankLockReplacedTrigger.ID, player);
        }
    }
}