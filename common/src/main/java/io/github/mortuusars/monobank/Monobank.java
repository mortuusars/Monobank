package io.github.mortuusars.monobank;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.advancement.trigger.MonobankInventoryChangedTrigger;
import io.github.mortuusars.monobank.advancement.trigger.MonobankLockReplacedTrigger;
import io.github.mortuusars.monobank.advancement.trigger.MonobankUnlockedTrigger;
import io.github.mortuusars.monobank.world.block.monobank.component.Lock;
import io.github.mortuusars.monobank.world.item.ReplacementLockItem;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlock;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.world.inventory.menu.MonobankMenu;
import io.github.mortuusars.monobank.world.inventory.menu.LockReplacementMenu;
import io.github.mortuusars.monobank.world.inventory.menu.CombinationMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Monobank {
    public static final String ID = "monobank";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        Blocks.init();
        BlockEntityTypes.init();
        EntityTypes.init();
        Items.init();
        DataComponents.init();
        MenuTypes.init();
        CriteriaTriggers.init();
        RecipeSerializers.init();
        SoundEvents.init();
        ArgumentTypes.init();
    }

    /**
     * Creates resource location in the mod namespace with the given path.
     */
    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static class Blocks {
        public static final Supplier<Block> MONOBANK = Register.block("monobank",
                () -> new MonobankBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLACK)
                        .strength(8F, 1200F)
                        .noOcclusion() // Without this door and items inside will be black.
                        .sound(SoundType.NETHERITE_BLOCK)));

        static void init() {
        }
    }

    public static class BlockEntityTypes {
        public static final Supplier<BlockEntityType<MonobankBlockEntity>> MONOBANK = Register.blockEntityType("monobank",
                () -> Register.newBlockEntityType(MonobankBlockEntity::new, Blocks.MONOBANK.get()));

        static void init() {
        }
    }

    public static class Items {
        public static final Supplier<BlockItem> MONOBANK = Register.item("monobank",
                () -> new BlockItem(Blocks.MONOBANK.get(), new Item.Properties()
                        .stacksTo(1)
                        .fireResistant()));

        public static final Supplier<Item> REPLACEMENT_LOCK = Register.item("replacement_lock",
                () -> new ReplacementLockItem(new Item.Properties()
                        .stacksTo(16)));

        static void init() {
        }
    }

    public static class DataComponents {
        public static final DataComponentType<Lock> LOCK = Register.dataComponentType("lock", arg -> arg.persistent(Lock.CODEC));

        static void init() {
        }
    }

    public static class EntityTypes {
        static void init() {
        }
    }

    public static class MenuTypes {
        public static final Supplier<MenuType<MonobankMenu>> MONOBANK =
                Register.menuType("monobank", MonobankMenu::fromBuffer);

        public static final Supplier<MenuType<CombinationMenu>> MONOBANK_COMBINATION =
                Register.menuType("lock_picking", CombinationMenu::fromBuffer);

        public static final Supplier<MenuType<LockReplacementMenu>> MONOBANK_LOCK_REPLACEMENT =
                Register.menuType("lock_replacement", LockReplacementMenu::fromBuffer);

        static void init() {
        }
    }

    public static class RecipeSerializers {
        static void init() {
        }
    }

    public static class SoundEvents {
        public static final Supplier<SoundEvent> MONOBANK_OPEN = register("block.monobank.open");
        public static final Supplier<SoundEvent> MONOBANK_CLOSE = register("block.monobank.close");
        public static final Supplier<SoundEvent> MONOBANK_LOCK = register("block.monobank.lock");
        public static final Supplier<SoundEvent> MONOBANK_UNLOCK = register("block.monobank.unlock");
        public static final Supplier<SoundEvent> MONOBANK_CLICK = register("block.monobank.click");

        private static Supplier<SoundEvent> register(String path) {
            Preconditions.checkState(path != null && !path.isEmpty(), "'path' should not be empty.");
            return Register.soundEvent(path, () -> SoundEvent.createVariableRangeEvent(Monobank.resource(path)));
        }

        static void init() {
        }
    }

    public static class Stats {
        private static final Map<ResourceLocation, StatFormatter> STATS = new HashMap<>();

        private static ResourceLocation register(ResourceLocation location, StatFormatter formatter) {
            STATS.put(location, formatter);
            return location;
        }

        public static void register() {
            STATS.forEach((location, formatter) -> {
                Registry.register(BuiltInRegistries.CUSTOM_STAT, location, location);
                net.minecraft.stats.Stats.CUSTOM.get(location, formatter);
            });
        }
    }

    public static class CriteriaTriggers {
        public static Supplier<MonobankInventoryChangedTrigger> MONOBANK_INVENTORY_CHANGED = Register.criterionTrigger("monobank_inventory_changed", MonobankInventoryChangedTrigger::new);
        public static Supplier<MonobankUnlockedTrigger> MONOBANK_UNLOCKED = Register.criterionTrigger("monobank_unlocked", MonobankUnlockedTrigger::new);
        public static Supplier<MonobankLockReplacedTrigger> MONOBANK_LOCK_REPLACED = Register.criterionTrigger("monobank_lock_replaced", MonobankLockReplacedTrigger::new);

        public static void init() {
        }
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> LOCK_BLACKLIST = TagKey.create(Registries.ITEM, resource("lock_blacklist"));
        }

        public static class Blocks {
        }

        public static class EntityTypes {
        }
    }

    public static class LootTables {
        public static final ResourceKey<LootTable> COMBINATION_DEFAULT =
                ResourceKey.create(Registries.LOOT_TABLE, resource("combination/default"));
    }

    public static class ArgumentTypes {
        public static void init() {
        }
    }
}
