package io.github.mortuusars.monobank.data.provider;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.registry.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Monobank.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        String name = ModBlocks.MONOBANK.get().getRegistryName().getPath();
        withExistingParent(name, modLoc("block/" + name));
    }
}
