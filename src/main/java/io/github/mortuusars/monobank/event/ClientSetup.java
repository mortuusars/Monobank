package io.github.mortuusars.monobank.event;

import io.github.mortuusars.monobank.content.monobank.MonobankScreen;
import io.github.mortuusars.monobank.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(() ->
        {
            MenuScreens.register(ModMenuTypes.MONOBANK.get(), MonobankScreen::new);
        });
    }
}
