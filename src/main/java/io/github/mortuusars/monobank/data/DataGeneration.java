package io.github.mortuusars.monobank.data;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.data.provider.BlockStates;
import io.github.mortuusars.monobank.data.provider.ItemModels;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Monobank.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        if (event.includeServer()) {
//            BlockTags blockTags = new BlockTags(generator, FarmersDelight.MODID, helper);
//            generator.addProvider(blockTags);
//            generator.addProvider(new ItemTags(generator, blockTags, FarmersDelight.MODID, helper));
//            generator.addProvider(new EntityTags(generator, FarmersDelight.MODID, helper));
//            generator.addProvider(new Recipes(generator));
//            generator.addProvider(new Advancements(generator));
        }
        if (event.includeClient()) {
            BlockStates blockStates = new BlockStates(generator, helper);
            generator.addProvider(blockStates);
            generator.addProvider(new ItemModels(generator, blockStates.models().existingFileHelper));
        }
    }
}