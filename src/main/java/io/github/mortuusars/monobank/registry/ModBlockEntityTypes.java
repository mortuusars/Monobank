package io.github.mortuusars.monobank.registry;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntityTypes {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Monobank.ID);

    public static final RegistryObject<BlockEntityType<MonobankBlockEntity>> MONOBANK = BLOCK_ENTITY_TYPES.register("monobank",
            () -> BlockEntityType.Builder.of(MonobankBlockEntity::new, ModBlocks.MONOBANK.get()).build(null));

    public static final void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
