package io.github.mortuusars.monobank.data.provider;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Monobank.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        String name = Registry.Blocks.MONOBANK.get().getRegistryName().getPath();

        getBuilder(name)
                .parent(getExistingFile(modLoc(name)));

//        withExistingParent(name, modLoc("block/" + name));
    }
}
