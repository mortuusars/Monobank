package io.github.mortuusars.monobank.content.advancement.trigger;

import com.google.gson.JsonObject;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class MonobankInventoryChangedTrigger extends SimpleCriterionTrigger<MonobankInventoryChangedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Monobank.resource("inventory_changed");

    public void trigger(ServerPlayer player, ItemStack storedStack) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, storedStack));
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext conditionsParser) {
        return new TriggerInstance(player, ItemPredicate.fromJson(json.get("item")));
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite player, ItemPredicate item) {
            super(MonobankInventoryChangedTrigger.ID, player);
            this.item = item;
        }

        /**
         * Creates a trigger instance with required count set to int.maxValue. This is used for 'Fill monobank to full' advancement.
         */
        public static TriggerInstance full() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, /*MinMaxBounds.Ints.atLeast(Integer.MAX_VALUE),*/
                    ItemPredicate.Builder.item()
                            .withCount(MinMaxBounds.Ints.atLeast(Integer.MAX_VALUE))
                            .build());
        }

        @Override
        public JsonObject serializeToJson(SerializationContext pConditions) {
            JsonObject jsonObject = super.serializeToJson(pConditions);
            jsonObject.add("item", item.serializeToJson());
            return jsonObject;
        }

        public boolean matches(ServerPlayer player, ItemStack storedStack) {
            // Handles Monobank Full advancement.
            // 'count' is made public in accesstransformer.cfg.
            if (item.count.getMin() == Integer.MAX_VALUE && storedStack.getCount() >= Monobank.getSlotCapacity())
                return true;

            return item.matches(storedStack);
        }
    }
}
