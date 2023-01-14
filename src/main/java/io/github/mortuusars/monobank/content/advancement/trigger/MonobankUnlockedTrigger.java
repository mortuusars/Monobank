package io.github.mortuusars.monobank.content.advancement.trigger;

import com.google.gson.JsonObject;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MonobankUnlockedTrigger extends SimpleCriterionTrigger<MonobankUnlockedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Monobank.resource("monobank_unlocked");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected MonobankUnlockedTrigger.TriggerInstance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext conditionsParser) {
        return new MonobankUnlockedTrigger.TriggerInstance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(EntityPredicate.Composite player) {
            super(MonobankUnlockedTrigger.ID, player);
        }
    }
}
