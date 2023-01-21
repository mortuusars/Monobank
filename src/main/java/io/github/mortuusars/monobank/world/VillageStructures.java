package io.github.mortuusars.monobank.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class VillageStructures {

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("minecraft", "empty"));
    private static final ResourceKey<StructureProcessorList> MOSSIFY_10_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("minecraft", "mossify_10_percent"));

    private static final ResourceKey<StructureProcessorList> STREET_PLAINS_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("minecraft", "street_plains"));


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void addVillageStructures(final ServerAboutToStartEvent event) {

        // TODO: config

        Registry<StructureTemplatePool> templatePools = event.getServer().registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).get();
        Registry<StructureProcessorList> processorListsRegistry = event.getServer().registryAccess().registry(Registry.PROCESSOR_LIST_REGISTRY).get();

        Holder<StructureProcessorList> emptyProcessorList = processorListsRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        Holder<StructureProcessorList> mossify10ProcessorList = processorListsRegistry.getHolderOrThrow(MOSSIFY_10_PROCESSOR_LIST_KEY);
        Holder<StructureProcessorList> streetPlainsProcessorList = processorListsRegistry.getHolderOrThrow(STREET_PLAINS_PROCESSOR_LIST_KEY);

        VillageStructures.addStructureToPoolSingle(templatePools, mossify10ProcessorList,
                new ResourceLocation("minecraft:village/plains/houses"),
                Monobank.ID + ":village/houses/plains_vault",  StructureTemplatePool.Projection.RIGID, 50);

        VillageStructures.addStructureToPoolLegacy(templatePools, streetPlainsProcessorList,
                new ResourceLocation("minecraft:village/plains/streets"),
                Monobank.ID + ":village/streets/straight_fix_01", StructureTemplatePool.Projection.TERRAIN_MATCHING, 50);
    }

//    public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
//                                         Registry<StructureProcessorList> processorListRegistry, ResourceLocation poolLocation,
//                                         String structureNbtLocation, int weight) {
//
//        Logger logger = LogUtils.getLogger();
//        logger.info("Adding '{}' structure to pool '{}'. Weight: {}.", structureNbtLocation, poolLocation, weight);
//
//        StructureTemplatePool pool = templatePoolRegistry.get(poolLocation);
//        if (pool == null) {
//            logger.error("Failed to add '{}' to '{}'. Pool not found.", structureNbtLocation, poolLocation);
//            return;
//        }
//
//        Holder<StructureProcessorList> processorHolder = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
//
//        SinglePoolElement piece = SinglePoolElement.single(structureNbtLocation, processorHolder)
//                .apply(StructureTemplatePool.Projection.RIGID);
//
//        //
////        pool.templates.clear();
//
//        for (int i = 0; i < weight; i++) {
//            pool.templates.add(piece);
//        }
//
//        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
//
//        //
////        listOfPieceEntries.clear();
//
//        listOfPieceEntries.add(new Pair<>(piece, weight));
//        pool.rawTemplates = listOfPieceEntries;
//    }

    private static void addStructureToPoolLegacy(Registry<StructureTemplatePool> templatePoolRegistry,
                                                 Holder<StructureProcessorList> processorListHolder,
                                                 ResourceLocation poolRL,
                                                 String nbtPieceRL,
                                                 StructureTemplatePool.Projection projection,
                                                 int weight) {

        Logger logger = LogUtils.getLogger();
        logger.info("Adding '{}' structure to pool '{}'. Weight: {}.", nbtPieceRL, poolRL, weight);

        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) {
            logger.error("Pool '{}' not found.", poolRL);
            return;
        }

        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL, processorListHolder)
                .apply(projection);

        addPieceToPool(piece, pool, weight);
    }

    private static void addStructureToPoolSingle(Registry<StructureTemplatePool> templatePoolRegistry,
                                                 Holder<StructureProcessorList> processorListHolder,
                                                 ResourceLocation poolRL,
                                                 String nbtPieceRL,
                                                 StructureTemplatePool.Projection projection,
                                                 int weight) {

        Logger logger = LogUtils.getLogger();
        logger.info("Adding '{}' structure to pool '{}'. Weight: {}.", nbtPieceRL, poolRL, weight);

        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) {
            logger.error("Pool '{}' not found.", poolRL);
            return;
        }

        SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, processorListHolder)
                .apply(projection);

        addPieceToPool(piece, pool, weight);
    }

    private static void addPieceToPool(SinglePoolElement piece, StructureTemplatePool pool, int weight) {
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }
}

