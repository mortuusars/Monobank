package io.github.mortuusars.monobank.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.network.fabric.FabricS2CPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class MonobankFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricS2CPacketHandler.register();
        ConfigScreenFactoryRegistry.INSTANCE.register(Monobank.ID, ConfigurationScreen::new);
    }
}
