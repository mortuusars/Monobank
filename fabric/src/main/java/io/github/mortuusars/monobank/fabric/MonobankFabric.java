package io.github.mortuusars.monobank.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import io.github.mortuusars.monobank.Config;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.event.ServerEvents;
import io.github.mortuusars.monobank.network.fabric.FabricC2SPackets;
import io.github.mortuusars.monobank.network.fabric.FabricS2CPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public class MonobankFabric implements ModInitializer {
    // Server field to access when no other objects are available to get it from.
    public static @Nullable MinecraftServer server = null;

    @Override
    public void onInitialize() {
        Monobank.init();

        NeoForgeConfigRegistry.INSTANCE.register(Monobank.ID, ModConfig.Type.SERVER, Config.Server.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(Monobank.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.accept(Monobank.Items.MONOBANK.get());
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(Monobank.Items.REPLACEMENT_LOCK.get());
        });

        ServerLifecycleEvents.SERVER_STARTING.register(ServerEvents::serverStart);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MonobankFabric.server = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            MonobankFabric.server = null;
        });

        FabricC2SPackets.register();
        FabricS2CPackets.register();
    }
}
