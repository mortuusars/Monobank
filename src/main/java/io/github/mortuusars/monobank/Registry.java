package io.github.mortuusars.monobank;

import io.github.mortuusars.monobank.content.monobank.MonobankBlock;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.MonobankMenu;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registry {

    public static class Blocks {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Monobank.ID);

        public static final RegistryObject<Block> MONOBANK = BLOCKS.register("monobank", MonobankBlock::new);
    }

    @SuppressWarnings("unused")
    public static class Items {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Monobank.ID);

        public static final RegistryObject<BlockItem> MONOBANK = ITEMS.register("monobank",
                () -> new BlockItem(Blocks.MONOBANK.get(), new Item.Properties()
                        .stacksTo(1)
                        .fireResistant()
                        .tab(CreativeModeTab.TAB_DECORATIONS)));
    }

    public static class BlockEntityTypes {
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Monobank.ID);

        @SuppressWarnings("DataFlowIssue")
        public static final RegistryObject<BlockEntityType<MonobankBlockEntity>> MONOBANK = BLOCK_ENTITY_TYPES.register("monobank",
                () -> BlockEntityType.Builder.of(MonobankBlockEntity::new, Blocks.MONOBANK.get()).build(null));
    }

    public static class MenuTypes {
        private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Monobank.ID);

        public static final RegistryObject<MenuType<MonobankMenu>> MONOBANK = MENU_TYPES
                .register("monobank", () -> IForgeMenuType.create(MonobankMenu::fromBuffer));
    }

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
    }

    public static void register(IEventBus modEventBus) {
        Blocks.BLOCKS.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        BlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        MenuTypes.MENU_TYPES.register(modEventBus);
        Sounds.SOUNDS.register(modEventBus);
    }
}
