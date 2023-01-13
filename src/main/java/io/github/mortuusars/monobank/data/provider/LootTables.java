package io.github.mortuusars.monobank.data.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class LootTables extends LootTableProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public LootTables(DataGenerator generator) {
        super(generator);
        this.generator = generator;
    }

    @Override
    public void run(HashCache cache) {
        writeTable(cache, Monobank.resource("blocks/monobank"),
                LootTable.lootTable()
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
                        .when(ExplosionCondition.survivesExplosion()))
                .build());

        // Combination:

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

        writeTable(cache, Monobank.resource("combination/village"),
                LootTable.lootTable()
                        .withPool(villageCombinationPool)
                        .withPool(villageCombinationPool)
                        .withPool(villageCombinationPool)
                        .build());

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

        writeTable(cache, Monobank.resource("combination/default"),
                LootTable.lootTable()
                        .withPool(defaultCombinationPool)
                        .withPool(defaultCombinationPool)
                        .withPool(defaultCombinationPool)
                        .build());

        // Contents:

        writeTable(cache, Monobank.resource("monobank/village"),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(Items.EMERALD).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 17))))
                                .add(LootItem.lootTableItem(Items.EMERALD_BLOCK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 15))))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 9))))
                                .add(LootItem.lootTableItem(Items.GOLD_BLOCK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.MAP)
                                        .apply(ExplorationMapFunction.makeExplorationMap()
                                                .setDestination(ConfiguredStructureTags.RUINED_PORTAL)
                                                .setMapDecoration(MapDecoration.Type.RED_X)
                                                .setZoom((byte)1)
                                                .setSkipKnownStructures(false))
                                        .apply(SetNameFunction.setName(TextUtil.translate("filled_map.ruined_portal"))))
                        ).build());
    }

    private void writeTable(HashCache cache, ResourceLocation location, LootTable lootTable) {
        Path outputFolder = this.generator.getOutputFolder();
        Path path = outputFolder.resolve("data/" + location.getNamespace() + "/loot_tables/" + location.getPath() + ".json");
        try {
            DataProvider.save(GSON, cache, net.minecraft.world.level.storage.loot.LootTables.serialize(lootTable), path);
        } catch (IOException e) {
            LOGGER.error("Couldn't write loot lootTable {}", path, e);
        }
    }
}
