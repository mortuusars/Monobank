package io.github.mortuusars.monobank;

import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.world.block.monobank.component.Lock;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;

public class MonobankClient {
    @SuppressWarnings("deprecation")
    public static void init() {
        ItemProperties.register(Monobank.Items.MONOBANK.get(), Monobank.resource("locked"), (stack, level, entity, seed) -> {
            CustomData tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
            if (tag.getUnsafe().contains(MonobankBlockEntity.LOCK_TAG, Tag.TAG_COMPOUND)) {
                CompoundTag lockTag = tag.getUnsafe().getCompound(MonobankBlockEntity.LOCK_TAG);
                return lockTag.getBoolean(Lock.LOCKED_TAG) ? 1 : 0;
            }
            return 0;
        });
    }

    public static class Models {
        public static final ModelResourceLocation MONOBANK_DOOR =
                new ModelResourceLocation(Monobank.resource("monobank_door"), "standalone");
    }
}
