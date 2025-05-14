package io.github.mortuusars.monobank;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class MonobankClient {
    public static void init() {
        ItemProperties.register(Monobank.Items.MONOBANK.get(), Monobank.resource("locked"), (stack, level, entity, seed) -> {
            // TODO: implement
//            if (stack.hasTag() && stack.getTag().contains("BlockEntityTag", CompoundTag.TAG_COMPOUND)) {
//                CompoundTag blockEntityTag = stack.getTag().getCompound("BlockEntityTag");
//                if (blockEntityTag.contains(MonobankBlockEntity.LOCK_TAG, CompoundTag.TAG_COMPOUND)) {
//                    CompoundTag lock = blockEntityTag.getCompound(MonobankBlockEntity.LOCK_TAG);
//                    return lock.getBoolean(Lock.LOCKED_TAG) ? 1f : 0f;
//                }
//            }
            return 0f;
        });
    }

    public static class Models {
        public static final ModelResourceLocation MONOBANK_DOOR =
                new ModelResourceLocation(Monobank.resource("monobank_door"), "standalone");
    }
}
