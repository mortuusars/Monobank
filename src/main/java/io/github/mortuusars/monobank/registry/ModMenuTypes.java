package io.github.mortuusars.monobank.registry;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.content.monobank.MonobankMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Monobank.ID);

    public static final RegistryObject<MenuType<MonobankMenu>> MONOBANK = MENU_TYPES
            .register("monobank", () -> IForgeMenuType.create(MonobankMenu::new));

    public static final void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
