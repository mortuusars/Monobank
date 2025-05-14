package io.github.mortuusars.monobank.neoforge.event;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.MonobankClient;
import io.github.mortuusars.monobank.client.gui.component.CombinationTooltip;
import io.github.mortuusars.monobank.content.monobank.MonobankScreen;
import io.github.mortuusars.monobank.content.monobank.lock_replacement.LockReplacementScreen;
import io.github.mortuusars.monobank.content.monobank.renderer.MonobankRenderer;
import io.github.mortuusars.monobank.content.monobank.unlocking.CombinationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientEvents {
    @EventBusSubscriber(modid = Monobank.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(Monobank.MenuTypes.MONOBANK.get(), MonobankScreen::new);
            event.register(Monobank.MenuTypes.MONOBANK_COMBINATION.get(), CombinationScreen::new);
            event.register(Monobank.MenuTypes.MONOBANK_LOCK_REPLACEMENT.get(), LockReplacementScreen::new);
        }

        @SubscribeEvent
        public static void init(final FMLClientSetupEvent event) {
            event.enqueueWork(MonobankClient::init);
        }

        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(CombinationTooltip.class, combinationTooltip -> combinationTooltip);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(Monobank.BlockEntityTypes.MONOBANK.get(), MonobankRenderer::new);
        }

        @SubscribeEvent
        public static void registerModels(ModelEvent.RegisterAdditional event) {
            event.register(MonobankClient.Models.MONOBANK_DOOR);
        }
    }

    public static class GameBus {

    }
}
