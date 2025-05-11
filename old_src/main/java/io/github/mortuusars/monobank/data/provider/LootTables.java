package io.github.mortuusars.monobank.data.provider;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.data.loot.packs.VanillaChestLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class LootTables {
    public static class BlockLoot extends VanillaBlockLoot {
        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return ForgeRegistries.BLOCKS.getEntries().stream()
                    .filter(e -> e.getKey().location().getNamespace().equals(Monobank.ID))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        }

        @Override
        protected void generate() {
            add(Registry.Blocks.MONOBANK.get(), LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(Registry.Items.MONOBANK.get())
                                            .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                                    .copy("Inventory", "BlockEntityTag.Inventory")
                                                    .copy("Lock", "BlockEntityTag.Lock")
                                                    .copy("Owner", "BlockEntityTag.Owner")
                                                    .copy("LootTable", "BlockEntityTag.LootTable")
                                                    .copy("LootTableSeed", "BlockEntityTag.LootTableSeed")))
                                    .when(ExplosionCondition.survivesExplosion())));
        }
    }

    public static class ChestLoot extends VanillaChestLoot {

        @Override
        public void generate(@NotNull BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            // Combination:

            LootPool.Builder defaultCombinationPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.TRIPWIRE_HOOK))
                    .add(LootItem.lootTableItem(Items.IRON_NUGGET))
                    .add(LootItem.lootTableItem(Items.CHAIN))
                    .add(LootItem.lootTableItem(Items.IRON_INGOT))
                    .add(LootItem.lootTableItem(Items.LEVER))
                    .add(LootItem.lootTableItem(Items.STONE_PRESSURE_PLATE))
                    .add(LootItem.lootTableItem(Items.COMPASS))
                    .add(LootItem.lootTableItem(Items.BOOK))
                    .add(LootItem.lootTableItem(Items.BUCKET));

            consumer.accept(Monobank.resource("combination/default"),
                    LootTable.lootTable()
                            .withPool(defaultCombinationPool)
                            .withPool(defaultCombinationPool)
                            .withPool(defaultCombinationPool));

            LootPool.Builder villageCombinationPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.TRIPWIRE_HOOK))
                    .add(LootItem.lootTableItem(Items.IRON_NUGGET))
                    .add(LootItem.lootTableItem(Items.CHAIN))
                    .add(LootItem.lootTableItem(Items.IRON_INGOT))
                    .add(LootItem.lootTableItem(Items.GLASS_BOTTLE))
                    .add(LootItem.lootTableItem(Items.LEVER))
                    .add(LootItem.lootTableItem(Items.STONE_PRESSURE_PLATE))
                    .add(LootItem.lootTableItem(Items.COMPASS))
                    .add(LootItem.lootTableItem(Items.BOOK))
                    .add(LootItem.lootTableItem(Items.BUCKET));

            consumer.accept(Monobank.resource("combination/village/plains"),
                    LootTable.lootTable()
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool));

            consumer.accept(Monobank.resource("combination/village/taiga"),
                    LootTable.lootTable()
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool));

            consumer.accept(Monobank.resource("combination/village/desert"),
                    LootTable.lootTable()
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool));

            consumer.accept(Monobank.resource("combination/village/snowy"),
                    LootTable.lootTable()
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool));

            consumer.accept(Monobank.resource("combination/village/savanna"),
                    LootTable.lootTable()
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool)
                            .withPool(villageCombinationPool));

            // Contents:

            LootTable.Builder monobankVillageLootBuilder = LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(Items.EMERALD).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 17))))
                            .add(LootItem.lootTableItem(Items.EMERALD_BLOCK).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                            .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 15))))
                            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 9))))
                            .add(LootItem.lootTableItem(Items.GOLD_BLOCK).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                            .add(LootItem.lootTableItem(Items.MAP)
                                    .apply(ExplorationMapFunction.makeExplorationMap()
                                            .setDestination(StructureTags.RUINED_PORTAL)
                                            .setMapDecoration(MapDecoration.Type.RED_X)
                                            .setZoom((byte) 1)
                                            .setSkipKnownStructures(false))
                                    .apply(SetNameFunction.setName(TextUtil.translate("filled_map.ruined_portal")))));

            consumer.accept(Monobank.resource("monobank/village/plains"), monobankVillageLootBuilder);
            consumer.accept(Monobank.resource("monobank/village/taiga"), monobankVillageLootBuilder);
            consumer.accept(Monobank.resource("monobank/village/desert"), monobankVillageLootBuilder);
            consumer.accept(Monobank.resource("monobank/village/snowy"), monobankVillageLootBuilder);
            consumer.accept(Monobank.resource("monobank/village/savanna"), monobankVillageLootBuilder);


        }
    }
}