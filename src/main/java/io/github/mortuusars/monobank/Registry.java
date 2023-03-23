package io.github.mortuusars.monobank;

import io.github.mortuusars.monobank.content.advancement.trigger.MonobankInventoryChangedTrigger;
import io.github.mortuusars.monobank.content.advancement.trigger.MonobankLockReplacedTrigger;
import io.github.mortuusars.monobank.content.advancement.trigger.MonobankUnlockedTrigger;
import io.github.mortuusars.monobank.content.effect.ThiefEffect;
import io.github.mortuusars.monobank.content.item.ReplacementLockItem;
import io.github.mortuusars.monobank.content.monobank.MonobankBlock;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.MonobankMenu;
import io.github.mortuusars.monobank.content.monobank.lock_replacement.LockReplacementMenu;
import io.github.mortuusars.monobank.content.monobank.unlocking.UnlockingMenu;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
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

        public static final RegistryObject<Item> REPLACEMENT_LOCK = ITEMS.register("replacement_lock",
                () -> new ReplacementLockItem(new Item.Properties()
                        .stacksTo(16)
                        .tab(CreativeModeTab.TAB_REDSTONE)));
    }

    public static class BlockEntityTypes {
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Monobank.ID);

        @SuppressWarnings("DataFlowIssue")
        public static final RegistryObject<BlockEntityType<MonobankBlockEntity>> MONOBANK = BLOCK_ENTITY_TYPES.register("monobank",
                () -> BlockEntityType.Builder.of(MonobankBlockEntity::new, Blocks.MONOBANK.get()).build(null));
    }

    public static class MenuTypes {
        private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Monobank.ID);

        public static final RegistryObject<MenuType<MonobankMenu>> MONOBANK = MENU_TYPES
                .register("monobank", () -> IForgeMenuType.create(MonobankMenu::fromBuffer));

        public static final RegistryObject<MenuType<UnlockingMenu>> MONOBANK_UNLOCKING = MENU_TYPES
                .register("unlocking", () -> IForgeMenuType.create(UnlockingMenu::fromBuffer));

        public static final RegistryObject<MenuType<LockReplacementMenu>> MONOBANK_LOCK_REPLACEMENT = MENU_TYPES
                .register("lock_replacement", () -> IForgeMenuType.create(LockReplacementMenu::fromBuffer));
    }

    public static class Effects {
        private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Monobank.ID);
        public static final RegistryObject<MobEffect> THIEF = EFFECTS.register("thief",
                () -> new ThiefEffect(MobEffectCategory.HARMFUL, 0x222222));
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

    public static class Advancements {
        public static MonobankInventoryChangedTrigger MONOBANK_INVENTORY_CHANGED = new MonobankInventoryChangedTrigger();
        public static MonobankUnlockedTrigger MONOBANK_UNLOCKED = new MonobankUnlockedTrigger();
        public static MonobankLockReplacedTrigger MONOBANK_LOCK_REPLACED = new MonobankLockReplacedTrigger();

        public static void register() {
            CriteriaTriggers.register(MONOBANK_INVENTORY_CHANGED);
            CriteriaTriggers.register(MONOBANK_UNLOCKED);
            CriteriaTriggers.register(MONOBANK_LOCK_REPLACED);
        }
    }

    public static class BlockTags {
        public static final TagKey<Block> LOOTR_CONTAINERS = net.minecraft.tags.BlockTags.create(new ResourceLocation("lootr:containers"));
    }

    public static class StructureTags {
        public static final TagKey<ConfiguredStructureFeature<?, ?>> THEFT_PROTECTED =
                TagKey.create(net.minecraft.core.Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, Monobank.resource("theft_protected"));
    }

    public static class EntityTags {
        public static final TagKey<EntityType<?>> THEFT_HATERS =
                TagKey.create(net.minecraft.core.Registry.ENTITY_TYPE_REGISTRY, Monobank.resource("theft_haters"));
        public static final TagKey<EntityType<?>> THIEF_ATTACKERS =
                TagKey.create(net.minecraft.core.Registry.ENTITY_TYPE_REGISTRY, Monobank.resource("thief_attackers"));
    }

    public static void register(IEventBus modEventBus) {
        Blocks.BLOCKS.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        BlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        MenuTypes.MENU_TYPES.register(modEventBus);
        Effects.EFFECTS.register(modEventBus);
        Sounds.SOUNDS.register(modEventBus);
    }
}
