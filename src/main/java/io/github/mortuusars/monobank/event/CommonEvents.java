package io.github.mortuusars.monobank.event;

import io.github.mortuusars.monobank.Registry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonEvents {
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        Registry.Advancements.register();
    }

    public static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(Registry.Items.MONOBANK.get());
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Registry.Items.REPLACEMENT_LOCK.get());
        }
    }
}
