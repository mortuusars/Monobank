package io.github.mortuusars.monobank.registry;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.content.monobank.MonobankBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Monobank.ID);

    public static final RegistryObject<Block> MONOBANK = BLOCKS.register("monobank", MonobankBlock::new);

    public static final void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
