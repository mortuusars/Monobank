package io.github.mortuusars.monobank.data.provider;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Monobank.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        String replacementLock = Registry.Items.REPLACEMENT_LOCK.get().getRegistryName().getPath();
        singleTexture(replacementLock, mcLoc("item/generated"),
                "layer0", modLoc("item/" + replacementLock));

        String monobank = Registry.Blocks.MONOBANK.get().getRegistryName().getPath();
        getBuilder(monobank)
                .parent(getExistingFile(modLoc(monobank)));
    }
}
