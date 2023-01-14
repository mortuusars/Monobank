package io.github.mortuusars.monobank.content.advancement.trigger;

import com.google.gson.JsonObject;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MonobankLockReplacedTrigger extends SimpleCriterionTrigger<MonobankLockReplacedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Monobank.resource("monobank_lock_replaced");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected MonobankLockReplacedTrigger.TriggerInstance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext conditionsParser) {
        return new MonobankLockReplacedTrigger.TriggerInstance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(EntityPredicate.Composite player) {
            super(MonobankLockReplacedTrigger.ID, player);
        }
    }
}