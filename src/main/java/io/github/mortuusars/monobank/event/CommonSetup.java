package io.github.mortuusars.monobank.event;

import io.github.mortuusars.monobank.Registry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonSetup {
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        Registry.Advancements.register();
    }
}
