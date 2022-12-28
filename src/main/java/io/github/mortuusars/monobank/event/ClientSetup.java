package io.github.mortuusars.monobank.event;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.content.monobank.MonobankRenderer;
import io.github.mortuusars.monobank.content.monobank.MonobankScreen;
import io.github.mortuusars.monobank.registry.ModBlockEntityTypes;
import io.github.mortuusars.monobank.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(() ->
        {
            MenuScreens.register(ModMenuTypes.MONOBANK.get(), MonobankScreen::new);
        });
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.MONOBANK.get(), MonobankRenderer::new);
    }

    public static void registerModels(ModelRegistryEvent event) {
        ForgeModelBakery.addSpecialModel(Monobank.resource("block/monobank_door"));
    }

//    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
//        event.registerLayerDefinition(new ModelLayerLocation(Monobank.resource("monobank_door")), () -> LayerDefinition.create());
//    }
}
