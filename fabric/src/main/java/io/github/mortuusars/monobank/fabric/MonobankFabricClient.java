package io.github.mortuusars.monobank.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.MonobankClient;
import io.github.mortuusars.monobank.client.gui.component.CombinationTooltip;
import io.github.mortuusars.monobank.content.monobank.MonobankScreen;
import io.github.mortuusars.monobank.content.monobank.lock_replacement.LockReplacementScreen;
import io.github.mortuusars.monobank.content.monobank.renderer.MonobankRenderer;
import io.github.mortuusars.monobank.content.monobank.unlocking.CombinationScreen;
import io.github.mortuusars.monobank.network.fabric.FabricS2CPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class MonobankFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MonobankClient.init();

        ConfigScreenFactoryRegistry.INSTANCE.register(Monobank.ID, ConfigurationScreen::new);

        MenuScreens.register(Monobank.MenuTypes.MONOBANK.get(), MonobankScreen::new);
        MenuScreens.register(Monobank.MenuTypes.MONOBANK_COMBINATION.get(), CombinationScreen::new);
        MenuScreens.register(Monobank.MenuTypes.MONOBANK_LOCK_REPLACEMENT.get(), LockReplacementScreen::new);

        ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(MonobankClient.Models.MONOBANK_DOOR.id()));

        BlockEntityRenderers.register(Monobank.BlockEntityTypes.MONOBANK.get(), MonobankRenderer::new);

        TooltipComponentCallback.EVENT.register(data -> data instanceof CombinationTooltip combinationTooltip
                ? combinationTooltip : null);

        FabricS2CPacketHandler.register();
    }
}
