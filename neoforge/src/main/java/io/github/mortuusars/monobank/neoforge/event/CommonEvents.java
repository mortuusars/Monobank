package io.github.mortuusars.monobank.neoforge.event;

import io.github.mortuusars.monobank.event.ServerEvents;
import io.github.mortuusars.monobank.network.neoforge.PacketsImpl;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.network.packet.C2SPackets;
import io.github.mortuusars.monobank.network.packet.CommonPackets;
import io.github.mortuusars.monobank.network.packet.Packet;
import io.github.mortuusars.monobank.network.packet.S2CPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CommonEvents {
    @EventBusSubscriber(modid = Monobank.ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SuppressWarnings("unchecked")
        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            // This monstrosity is to avoid having to define packets for forge and fabric separately.
            for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : S2CPackets.getDefinitions()) {
                registrar.playToClient((CustomPacketPayload.Type<Packet>) definition.type(),
                        (StreamCodec<FriendlyByteBuf, Packet>) definition.codec(), PacketsImpl::handle);
            }

            for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : C2SPackets.getDefinitions()) {
                registrar.playToServer((CustomPacketPayload.Type<Packet>) definition.type(),
                        (StreamCodec<FriendlyByteBuf, Packet>) definition.codec(), PacketsImpl::handle);
            }

            for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : CommonPackets.getDefinitions()) {
                registrar.playBidirectional((CustomPacketPayload.Type<Packet>) definition.type(),
                        (StreamCodec<FriendlyByteBuf, Packet>) definition.codec(), PacketsImpl::handle);
            }
        }

        @SubscribeEvent
        public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
                event.accept(Monobank.Items.MONOBANK.get());
            }
            if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
                event.accept(Monobank.Items.REPLACEMENT_LOCK.get());
            }
        }
    }

    @EventBusSubscriber(modid = Monobank.ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameBus {
        @SubscribeEvent
        public static void serverAboutToStart(ServerAboutToStartEvent event) {
            ServerEvents.serverStart(event.getServer());
        }
    }
}
