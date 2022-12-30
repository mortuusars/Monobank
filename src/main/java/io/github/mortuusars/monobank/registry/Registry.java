package io.github.mortuusars.monobank.registry;

import io.github.mortuusars.monobank.Monobank;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registry {

    public static class Sounds {
        private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Monobank.ID);

        public static final RegistryObject<SoundEvent> MONOBANK_LOCK = registerSound("block.monobank.lock");
        public static final RegistryObject<SoundEvent> MONOBANK_UNLOCK = registerSound("block.monobank.unlock");
        public static final RegistryObject<SoundEvent> MONOBANK_OPEN = registerSound("block.monobank.open");
        public static final RegistryObject<SoundEvent> MONOBANK_CLOSE = registerSound("block.monobank.close");
        public static final RegistryObject<SoundEvent> MONOBANK_CLICK = registerSound("block.monobank.click");

        private static RegistryObject<SoundEvent> registerSound(String name) {
            return SOUNDS.register(name, () -> new SoundEvent(Monobank.resource(name)));
        }

        public static void register(IEventBus modEventBus) {
            SOUNDS.register(modEventBus);
        }
    }

    public static void register(IEventBus modEventBus) {
        Sounds.register(modEventBus);
    }
}
