package io.github.mortuusars.monobank.event;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.MonobankRenderer;
import io.github.mortuusars.monobank.content.monobank.MonobankScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(Registry.MenuTypes.MONOBANK.get(), MonobankScreen::new));
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registry.BlockEntityTypes.MONOBANK.get(), MonobankRenderer::new);
    }

    public static void registerModels(ModelRegistryEvent ignoredEvent) {
        ForgeModelBakery.addSpecialModel(Monobank.resource("block/monobank_door"));
    }
}
