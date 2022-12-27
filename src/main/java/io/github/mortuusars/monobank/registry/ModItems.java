package io.github.mortuusars.monobank.registry;

import io.github.mortuusars.monobank.Monobank;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Monobank.ID);

    public static final RegistryObject<BlockItem> MONOBANK = ITEMS.register("monobank",
            () -> new BlockItem(ModBlocks.MONOBANK.get(), new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()
                    .tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
