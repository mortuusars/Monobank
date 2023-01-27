package io.github.mortuusars.monobank.data.provider;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Monobank.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        String replacementLock = Registry.Items.REPLACEMENT_LOCK.getId().getPath();
        singleTexture(replacementLock, mcLoc("item/generated"),
                "layer0", modLoc("item/" + replacementLock));

        String monobank = Registry.Blocks.MONOBANK.getId().getPath();
        getBuilder(monobank)
                .parent(getExistingFile(modLoc("item/" + monobank + "_unlocked")))
                .override()
                    .predicate(new ResourceLocation("locked"), 1f)
                    .model(getExistingFile(modLoc("item/" + monobank + "_locked")))
                    .end();
    }
}
