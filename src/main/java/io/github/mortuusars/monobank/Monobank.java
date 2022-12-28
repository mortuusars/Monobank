package io.github.mortuusars.monobank;

import io.github.mortuusars.monobank.event.ClientSetup;
import io.github.mortuusars.monobank.registry.ModBlockEntityTypes;
import io.github.mortuusars.monobank.registry.ModBlocks;
import io.github.mortuusars.monobank.registry.ModItems;
import io.github.mortuusars.monobank.registry.ModMenuTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Monobank.ID)
public class Monobank
{
    public static final String ID = "monobank";

    public Monobank()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(ClientSetup::init);

        ModBlocks.register(modEventBus);
        ModBlockEntityTypes.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static final ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }

    public static int getSlotCapacity() {
        // TODO: config max size
        return Integer.MAX_VALUE;
    }
}
