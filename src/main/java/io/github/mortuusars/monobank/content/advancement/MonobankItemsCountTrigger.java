package io.github.mortuusars.monobank.content.advancement;

import com.google.gson.JsonObject;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MonobankItemsCountTrigger extends SimpleCriterionTrigger<MonobankItemsCountTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Monobank.resource("monobank_items_count");

    public void trigger(ServerPlayer player, int itemsCount) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(itemsCount));
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext conditionsParser) {
        MinMaxBounds.Ints itemsCount = MinMaxBounds.Ints.fromJson(json.get("count"));
        return new TriggerInstance(player, itemsCount);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private MinMaxBounds.Ints itemsCount;

        public TriggerInstance(EntityPredicate.Composite player, MinMaxBounds.Ints itemsCount) {
            super(MonobankItemsCountTrigger.ID, player);
            this.itemsCount = itemsCount;
        }

        /**
         * Creates a trigger instance with required count set to int.maxValue. This is used for 'Fill monobank to full' advancement.
         */
        public static TriggerInstance full() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(Integer.MAX_VALUE));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext pConditions) {
            JsonObject jsonObject = super.serializeToJson(pConditions);
            jsonObject.add("count", itemsCount.serializeToJson());
            return jsonObject;
        }

        public boolean matches(int count) {
            // Handles Monobank Full advancement.
            Integer atLeast = itemsCount.getMin();
            if (atLeast == Integer.MAX_VALUE && count >= Monobank.getSlotCapacity())
                return true;

            return itemsCount.matches(count);
        }
    }
}
